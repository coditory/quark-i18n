package com.coditory.quark.i18n.loader;

import com.coditory.quark.i18n.I18nKey;
import com.coditory.quark.i18n.I18nPath;
import com.coditory.quark.i18n.loader.FileWatcher.FileChangedEvent;
import com.coditory.quark.i18n.loader.I18nPathPattern.I18nPathGroups;
import com.coditory.quark.i18n.parser.I18nParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class I18nFileSystemLoader implements WatchableI18nLoader {
    public static I18nFileLoaderBuilder builder() {
        return builder(FileSystems.getDefault());
    }

    public static I18nFileLoaderBuilder builder(FileSystem fileSystem) {
        return new I18nFileLoaderBuilder()
                .scanFileSystem(fileSystem);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<I18nLoaderChangeListener> listeners = new ArrayList<>();
    private final Set<I18nPathPattern> pathPatterns;
    private final I18nParser parser;
    private final Map<String, I18nParser> parsersByExtension;
    private final I18nPath staticPrefix;
    private final FileSystem fileSystem;
    private final Map<String, CachedResource> cachedResources = new LinkedHashMap<>();
    private final Map<String, I18nMessageBundle> cachedBundles = new LinkedHashMap<>();
    private Thread watchThread;

    I18nFileSystemLoader(
            Set<I18nPathPattern> pathPatterns,
            FileSystem fileSystem,
            I18nParser fileParser,
            Map<String, I18nParser> parsersByExtension,
            I18nPath staticPrefix
    ) {
        this.staticPrefix = requireNonNull(staticPrefix);
        this.fileSystem = requireNonNull(fileSystem);
        this.pathPatterns = Set.copyOf(pathPatterns);
        this.parsersByExtension = Map.copyOf(parsersByExtension);
        this.parser = fileParser;
    }

    @NotNull
    @Override
    public synchronized List<I18nMessageBundle> load() {
        List<I18nMessageBundle> result = new ArrayList<>();
        for (I18nPathPattern pathPattern : pathPatterns) {
            List<Resource> resources = scanFiles(pathPattern);
            for (Resource resource : resources) {
                I18nMessageBundle templates = load(pathPattern, resource);
                result.add(templates);
                logger.debug("Loaded message bundle: {}", resource.url());
            }
        }
        return unmodifiableList(result);
    }

    private I18nMessageBundle load(I18nPathPattern pathPattern, Resource resource) {
        I18nPathGroups matchedGroups = pathPattern.matchGroups(resource.name());
        return load(resource, matchedGroups);
    }

    private I18nMessageBundle load(Resource resource, I18nPathGroups matchedGroups) {
        Locale locale = matchedGroups.locale();
        I18nPath prefix = matchedGroups.path() != null
                ? staticPrefix.child(matchedGroups.path())
                : I18nPath.root();
        Map<I18nKey, String> parsed = parseFile(locale, resource);
        String urlString = resource.url().toString();
        I18nMessageBundle result = new I18nMessageBundle(parsed, prefix);
        cachedBundles.put(urlString, result);
        cachedResources.put(urlString, new CachedResource(resource, matchedGroups));
        return result;
    }

    private List<Resource> scanFiles(I18nPathPattern pathPattern) {
        return ResourceScanner.scanFiles(fileSystem, pathPattern);
    }

    private Map<I18nKey, String> parseFile(Locale locale, Resource resource) {
        String extension = getExtension(resource.name());
        I18nParser parser = parsersByExtension.getOrDefault(extension, this.parser);
        if (parser == null) {
            throw new I18nLoadException("No file parser defined for: " + resource.name());
        }
        String content = readFile(resource);
        if (content.isBlank()) {
            return Map.of();
        }
        try {
            return parser.parse(content, locale);
        } catch (Throwable e) {
            throw new I18nLoadException("Could not parse file: " + resource.name(), e);
        }
    }

    private String readFile(Resource resource) {
        try {
            StringBuilder resultStringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.url().openStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
            }
            return resultStringBuilder.toString();
        } catch (Throwable e) {
            throw new I18nLoadException("Could not read classpath resource: " + resource.name(), e);
        }
    }

    private String getExtension(String resourceName) {
        int idx = resourceName.lastIndexOf('.');
        return idx == 0 || idx == resourceName.length() - 1
                ? null
                : resourceName.substring(idx + 1);
    }

    @Override
    public synchronized void startWatching() {
        if (watchThread != null) {
            throw new I18nLoadException("Loader is already watching for changes");
        }
        if (cachedBundles.isEmpty()) {
            load();
        }
        watchThread = FileWatcher.builder()
                .addListener(this::onFileChange)
                .fileSystem(fileSystem)
                .addPathPatterns(pathPatterns)
                .startWatchingThread();
    }

    @Override
    public synchronized void stopWatching() {
        if (watchThread == null) {
            return;
        }
        watchThread.interrupt();
        try {
            watchThread.join();
        } catch (InterruptedException e) {
            throw new I18nLoadException("Interrupted join with watching thread", e);
        }
    }

    @Override
    public synchronized void addChangeListener(I18nLoaderChangeListener listener) {
        listeners.add(listener);
    }

    private synchronized void onFileChange(FileChangedEvent event) {
        Path path = event.path();
        URL url = pathToUrl(path);
        String urlString = url.toString();
        Resource resource = new Resource(path.toString(), url);
        switch (event.changeType()) {
            case DELETE -> {
                I18nMessageBundle prev = cachedBundles.remove(urlString);
                if (prev != null) {
                    logger.info("Removed messages from file: {}", relativize(path));
                }
            }
            case MODIFY -> {
                I18nMessageBundle prev = cachedBundles.remove(urlString);
                loadToCache(resource);
                if (prev != null) {
                    logger.info("Reloaded messages from file: {}", relativize(path));
                }
            }
            case CREATE -> {
                loadToCache(resource);
                logger.info("Loaded messages from file: {}", relativize(path));
            }
        }
        List<I18nMessageBundle> bundles = cachedBundles.values()
                .stream()
                .toList();
        for (I18nLoaderChangeListener listener : listeners) {
            listener.onChange(bundles);
        }
    }

    private void loadToCache(Resource resource) {
        String urlString = resource.url().toString();
        if (cachedResources.containsKey(urlString)) {
            CachedResource cachedResource = cachedResources.get(urlString);
            load(resource, cachedResource.matchedGroups());
            return;
        }
        pathPatterns.stream()
                .filter(path -> path.matches(resource.name()))
                .findFirst()
                .map(pattern -> new CachedResource(resource, pattern.matchGroups(resource.name())))
                .ifPresent(cachedResource -> load(resource, cachedResource.matchedGroups()));
    }

    private URL pathToUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new I18nLoadException("Could not convert path to URL. Path: " + path, e);
        }
    }

    private Path relativize(Path path) {
        Path base = fileSystem.getPath("").toAbsolutePath();
        return path.startsWith(base)
                ? base.relativize(path)
                : path;
    }

    private record CachedResource(Resource resource, I18nPathGroups matchedGroups) {
    }
}
