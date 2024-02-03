package com.coditory.quark.i18n.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

final class ClassPath {
    private static final Logger logger = LoggerFactory.getLogger(ClassPath.class.getName());
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private static final String PATH_SEPARATOR_SYS_PROP = File.pathSeparator;
    private static final String JAVA_CLASS_PATH_SYS_PROP = System.getProperty("java.class.path");
    private final Set<ResourceInfo> resources;

    private ClassPath(Set<ResourceInfo> resources) {
        this.resources = resources;
    }

    static ClassPath from(ClassLoader classloader) {
        requireNonNull(classloader);
        Set<LocationInfo> locations = locationsFrom(classloader);
        Set<Path> scanned = new LinkedHashSet<>();
        for (LocationInfo location : locations) {
            scanned.add(location.path());
        }
        Set<ResourceInfo> resources = new LinkedHashSet<>();
        for (LocationInfo location : locations) {
            resources.addAll(location.scanResources(scanned));
        }
        return new ClassPath(resources);
    }

    Set<ResourceInfo> getResources(String packageName) {
        requireNonNull(packageName);
        if (packageName.isBlank()) {
            return resources;
        }
        String path = packageName.replace(".", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String filePrefix = path;
        return resources.stream()
                .filter(r -> r.getResourceName().startsWith(filePrefix))
                .collect(toSet());
    }

    public static class ResourceInfo {
        private final String resourceName;
        final ClassLoader loader;

        static ResourceInfo of(String resourceName, ClassLoader loader) {
            return resourceName.endsWith(CLASS_FILE_NAME_EXTENSION)
                    ? new ClassInfo(resourceName, loader)
                    : new ResourceInfo(resourceName, loader);
        }

        ResourceInfo(String resourceName, ClassLoader loader) {
            this.resourceName = requireNonNull(resourceName);
            this.loader = requireNonNull(loader);
        }

        public final URL url() {
            URL url = loader.getResource(resourceName);
            if (url == null) {
                throw new NoSuchElementException(resourceName);
            }
            return url;
        }

        public final String getResourceName() {
            return resourceName;
        }

        @Override
        public int hashCode() {
            return resourceName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ResourceInfo that) {
                return resourceName.equals(that.resourceName)
                        && loader == that.loader;
            }
            return false;
        }

        @Override
        public String toString() {
            return resourceName;
        }
    }

    public static final class ClassInfo extends ResourceInfo {
        private final String className;

        ClassInfo(String resourceName, ClassLoader loader) {
            super(resourceName, loader);
            this.className = ClassPath.getClassName(resourceName);
        }

        @Override
        public String toString() {
            return className;
        }
    }

    static Set<LocationInfo> locationsFrom(ClassLoader classloader) {
        Set<LocationInfo> locations = new LinkedHashSet<>();
        for (Map.Entry<Path, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
            locations.add(new LocationInfo(entry.getKey(), entry.getValue()));
        }
        return unmodifiableSet(locations);
    }

    static final class LocationInfo {
        final Path home;
        private final ClassLoader classloader;

        LocationInfo(Path home, ClassLoader classloader) {
            this.home = requireNonNull(home);
            this.classloader = requireNonNull(classloader);
        }

        Path path() {
            return home;
        }

        Set<ResourceInfo> scanResources(Set<Path> scannedFiles) {
            Set<ResourceInfo> resources = new LinkedHashSet<>();
            scannedFiles.add(home);
            scan(home, scannedFiles, resources);
            return unmodifiableSet(resources);
        }

        private void scan(Path path, Set<Path> scannedUris, Set<ResourceInfo> result) {
            try {
                if (!Files.exists(path)) {
                    return;
                }
            } catch (SecurityException e) {
                logger.warn("Cannot access " + path + ": " + e);
                return;
            }
            if (Files.isDirectory(path)) {
                scanDirectory(path, result);
            } else {
                scanJar(path, scannedUris, result);
            }
        }

        private void scanJar(Path path, Set<Path> scannedUris, Set<ResourceInfo> result) {
            File file = path.toFile();
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                // Not a jar file
                return;
            }
            try {
                for (File child : getClassPathFromManifest(file, jarFile.getManifest())) {
                    // We only scan each file once independent of the classloader that file might be
                    // associated with.
                    if (scannedUris.add(child.toPath())) {
                        scan(child.toPath(), scannedUris, result);
                    }
                }
                scanJarFile(jarFile, result);
            } catch (IOException e) {
                // Missing manifest
            } finally {
                try {
                    jarFile.close();
                } catch (IOException ignored) { // similar to try-with-resources, but don't fail scanning
                }
            }
        }

        private void scanJarFile(JarFile file, Set<ResourceInfo> result) {
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                    continue;
                }
                result.add(ResourceInfo.of(entry.getName(), classloader));
            }
        }

        private void scanDirectory(Path path, Set<ResourceInfo> result) {
            Set<Path> currentPath = new HashSet<>();
            currentPath.add(path.normalize());
            scanDirectory(path, "", currentPath, result);
        }

        private void scanDirectory(
                Path path,
                String packagePrefix,
                Set<Path> currentPath,
                Set<ResourceInfo> builder
        ) {
            listDirOrEmpty(path).forEach(p -> {
                String name = p.getFileName().toString();
                if (Files.isDirectory(p)) {
                    Path deref = p.normalize();
                    if (currentPath.add(deref)) {
                        scanDirectory(deref, packagePrefix + name + "/", currentPath, builder);
                        currentPath.remove(deref);
                    }
                } else {
                    String resourceName = packagePrefix + name;
                    if (!resourceName.equals(JarFile.MANIFEST_NAME)) {
                        builder.add(ResourceInfo.of(resourceName, classloader));
                    }
                }
            });
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LocationInfo that) {
                return home.equals(that.home) && classloader.equals(that.classloader);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return home.hashCode();
        }

        @Override
        public String toString() {
            return home.toString();
        }
    }

    static Set<File> getClassPathFromManifest(File jarFile, Manifest manifest) {
        if (manifest == null) {
            return Set.of();
        }
        Set<File> result = new LinkedHashSet<>();
        String classpathAttribute = manifest
                .getMainAttributes()
                .getValue(Attributes.Name.CLASS_PATH.toString());
        if (classpathAttribute != null) {
            for (String path : classpathAttribute.split(" ")) {
                if (path.isBlank()) {
                    continue;
                }
                URL url;
                try {
                    url = getClassPathEntry(jarFile, path);
                } catch (MalformedURLException e) {
                    // Ignore bad entry
                    logger.warn("Invalid Class-Path entry: " + path);
                    continue;
                }
                if (url.getProtocol().equals("file")) {
                    result.add(toFile(url));
                }
            }
        }
        return unmodifiableSet(result);
    }

    static Map<Path, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<Path, ClassLoader> entries = new LinkedHashMap<>();
        // Search parent first, since it's the order ClassLoader#loadClass() uses.
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
        }
        for (URL url : getClassLoaderUrls(classloader)) {
            String protocol = url.getProtocol();
            if ("file".equals(protocol) || "jimfs".equals(protocol)) {
                try {
                    Path path = Path.of(url.toURI());
                    if (!entries.containsKey(path)) {
                        entries.put(path, classloader);
                    }
                } catch (URISyntaxException e) {
                    logger.warn("Could not map classpath URL to path: " + url);
                }
            }
        }
        return unmodifiableMap(entries);
    }

    private static List<URL> getClassLoaderUrls(ClassLoader classloader) {
        if (classloader instanceof URLClassLoader) {
            return Arrays.asList(((URLClassLoader) classloader).getURLs());
        }
        if (classloader.equals(ClassLoader.getSystemClassLoader())) {
            return parseJavaClassPath();
        }
        return List.of();
    }

    private static List<URL> parseJavaClassPath() {
        List<URL> urls = new ArrayList<>();
        for (String entry : JAVA_CLASS_PATH_SYS_PROP.split(PATH_SEPARATOR_SYS_PROP)) {
            try {
                try {
                    urls.add(new File(entry).toURI().toURL());
                } catch (SecurityException e) {
                    // File.toURI checks to see if the file is a directory
                    URI uri = new URI("file", null, new File(entry).getAbsolutePath(), null);
                    urls.add(uri.toURL());
                }
            } catch (MalformedURLException|URISyntaxException e) {
                logger.warn("Malformed classpath entry: " + entry, e);
            }
        }
        return unmodifiableList(urls);
    }

    private static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
        return jarFile.toURI().resolve(path).toURL();
    }

    private static String getClassName(String filename) {
        int classNameEnd = filename.length() - CLASS_FILE_NAME_EXTENSION.length();
        return filename.substring(0, classNameEnd).replace('/', '.');
    }

    private static File toFile(URL url) {
        try {
            return new File(url.toURI()); // Accepts escaped characters like %20.
        } catch (URISyntaxException e) { // URL.toURI() doesn't escape chars.
            return new File(url.getPath()); // Accepts non-escaped chars like space.
        }
    }

    private static List<Path> listDirOrEmpty(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.toList();
        } catch (IOException e) {
            logger.warn("Could not list directory: " + dir);
            return List.of();
        }
    }
}
