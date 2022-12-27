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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

public class I18nFileLoader implements WatchableI18nLoader {
    private final List<I18nLoaderChangeListener> listeners = new ArrayList<>();
    private final Set<I18nPathPattern> pathPatterns;
    private final ClassLoader classLoader;
    private final I18nParser parser;
    private final Map<String, I18nParser> parsersByExtension;
    private final I18nPath staticPrefix;
    private final FileSystem fileSystem;
    private final Map<String, CachedResource> cachedResources = new LinkedHashMap<>();
    private final Map<String, Map<I18nKey, String>> cachedEntries = new LinkedHashMap<>();
    private Thread watchThread;

    I18nFileLoader(
            Set<I18nPathPattern> pathPatterns,
            FileSystem fileSystem,
            ClassLoader classLoader,
            I18nParser fileParser,
            Map<String, I18nParser> parsersByExtension,
            I18nPath staticPrefix
    ) {
        this.classLoader = classLoader;
        this.staticPrefix = requireNonNull(staticPrefix);
        this.fileSystem = requireNonNull(fileSystem);
        this.pathPatterns = Set.copyOf(pathPatterns);
        this.parsersByExtension = Map.copyOf(parsersByExtension);
        this.parser = fileParser;
    }

    @NotNull
    @Override
    public synchronized Map<I18nKey, String> load() {
        Map<I18nKey, String> result = new LinkedHashMap<>();
        for (I18nPathPattern pathPattern : pathPatterns) {
            List<Resource> resources = scanFiles(pathPattern);
            for (Resource resource : resources) {
                Map<I18nKey, String> parsed = load(resource, pathPattern);
                result.putAll(parsed);
            }
        }
        return unmodifiableMap(result);
    }

    private Map<I18nKey, String> load(Resource resource, I18nPathPattern pathPattern) {
        I18nPathGroups matchedGroups = pathPattern.matchGroups(resource.name());
        return load(resource, matchedGroups);
    }

    private Map<I18nKey, String> load(Resource resource, I18nPathGroups matchedGroups) {
        Map<I18nKey, String> parsed = parseFile(resource, matchedGroups);
        String urlString = resource.url().toString();
        cachedEntries.put(urlString, parsed);
        cachedResources.put(urlString, new CachedResource(resource, matchedGroups));
        return unmodifiableMap(parsed);
    }

    private List<Resource> scanFiles(I18nPathPattern pathPattern) {
        return classLoader != null
                ? ResourceScanner.scanClassPath(classLoader, pathPattern)
                : ResourceScanner.scanFiles(fileSystem, pathPattern);
    }

    private Map<I18nKey, String> parseFile(Resource resource, I18nPathGroups matchedGroups) {
        String extension = getExtension(resource.name());
        I18nParser parser = parsersByExtension.getOrDefault(extension, this.parser);
        if (parser == null) {
            throw new I18nLoadException("No file parser defined for: " + resource.name());
        }
        String content = readFile(resource);
        Locale locale = matchedGroups.locale();
        I18nPath prefix = matchedGroups.path() == null
                ? staticPrefix
                : staticPrefix.child(matchedGroups.path());
        try {
            return parser.parse(content, prefix, locale);
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
            throw new IllegalStateException("Loader is already watching for changes");
        }
        if (cachedEntries.isEmpty()) {
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
            case DELETE -> cachedEntries.remove(urlString);
            case MODIFY -> {
                cachedEntries.remove(urlString);
                loadToCache(resource);
            }
            case CREATE -> loadToCache(resource);
        }
        Map<I18nKey, String> entries = cachedEntries.values().stream()
                .reduce(new LinkedHashMap<>(), (acc, map) -> {
                    acc.putAll(map);
                    return acc;
                });
        for (I18nLoaderChangeListener listener : listeners) {
            listener.onChange(entries);
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

    private record CachedResource(Resource resource, I18nPathGroups matchedGroups) {
    }
}