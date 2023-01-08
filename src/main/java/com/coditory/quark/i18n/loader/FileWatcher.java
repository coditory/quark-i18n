package com.coditory.quark.i18n.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Objects.requireNonNull;

public final class FileWatcher implements Runnable {
    private static final int MAX_DIRS_TO_WATCH = 1_000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<Path, WatchKey> watchedDirKeys = new HashMap<>();
    private final Set<Path> watchedFiles = new HashSet<>();
    private final List<FileChangeListener> listeners;
    private final FileSystem fileSystem;
    private final Predicate<Path> filter;
    private final List<Path> baseDirectories;
    private volatile WatchService initializedWatchService;

    FileWatcher(FileSystem fileSystem, List<FileChangeListener> listeners, List<String> baseDirectories, Predicate<Path> filter) {
        this.filter = requireNonNull(filter);
        this.fileSystem = requireNonNull(fileSystem);
        this.listeners = List.copyOf(listeners);
        this.baseDirectories = baseDirectories.stream()
                .map(fileSystem::getPath)
                .distinct()
                .toList();
    }

    void initializeWatchService() {
        getWatchService();
    }

    private synchronized WatchService getWatchService() {
        if (this.initializedWatchService != null) {
            return this.initializedWatchService;
        }
        WatchService watchService;
        try {
            watchService = fileSystem.newWatchService();
            this.initializedWatchService = watchService;
        } catch (IOException e) {
            throw new I18nLoadException("Could not create watch service for fileSystem", e);
        }
        baseDirectories
                .forEach(path -> watchDir(watchService, path));
        return watchService;
    }

    @SuppressWarnings("unchecked")
    public synchronized void run() {
        WatchService watchService = getWatchService();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                if (key.watchable() instanceof Path keyPath) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context() instanceof Path eventPath) {
                            Path path = keyPath.resolve(eventPath);
                            logger.debug("Watch event: " + event.kind() + ": " + path);
                            onChange(path, (WatchEvent.Kind<Path>) event.kind(), watchService);
                        }
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            logger.debug("Stopping watch service because of interruption");
        } finally {
            stopWatching(watchService);
        }
    }

    private boolean isWatchable(Path dir) {
        return Files.isDirectory(dir)
                && (isUnderOrEqualBaseDir(dir) || isAboveBaseDir(dir));
    }

    private boolean isAboveBaseDir(Path dir) {
        return baseDirectories.stream()
                .anyMatch(baseDir -> !baseDir.equals(dir) && baseDir.startsWith(dir));
    }

    private boolean isUnderOrEqualBaseDir(Path dir) {
        return baseDirectories.stream()
                .anyMatch(dir::startsWith);
    }

    private void stopWatching(WatchService watchService) {
        try {
            watchService.close();
        } catch (IOException e) {
            logger.warn("Could not stop watch service");
        }
    }

    private void onChange(Path path, WatchEvent.Kind<Path> eventKind, WatchService watchService) {
        if (eventKind.equals(ENTRY_CREATE)) {
            if (watchedFiles.contains(path)) {
                onModify(path);
            } else {
                onCreate(path, watchService);
            }
        } else if (eventKind.equals(ENTRY_DELETE)) {
            onDelete(path);
        } else if (eventKind.equals(ENTRY_MODIFY) && watchedFiles.contains(path)) {
            onModify(path);
        }
    }

    private void onCreate(Path path, WatchService watchService) {
        List<Path> createdFiles = List.of();
        if (Files.isDirectory(path)) {
            Set<Path> alreadyWatched = new LinkedHashSet<>(watchedFiles);
            watchDir(watchService, path);
            createdFiles = watchedFiles.stream()
                    .filter(file -> !alreadyWatched.contains(file))
                    .toList();
        } else if (filter.test(path)) {
            createdFiles = List.of(path);
            watchedFiles.add(path);
        }
        createdFiles.forEach(file -> {
            logger.debug("File created: {}", file);
            notifyListeners(FileChangeType.CREATE, file);
        });
    }

    private void onModify(Path path) {
        logger.debug("File modified: {}", path);
        notifyListeners(FileChangeType.MODIFY, path);
    }

    private void onDelete(Path path) {
        List<Path> deletedFiles;
        if (watchedFiles.contains(path)) {
            deletedFiles = List.of(path);
        } else {
            List<Path> dirsToRemove = watchedDirKeys.keySet().stream()
                    .filter(dir -> dir.startsWith(path))
                    .toList();
            if (!dirsToRemove.isEmpty()) {
                dirsToRemove.forEach(dir -> {
                    logger.debug("Directory deleted: {}", dir);
                    watchedDirKeys.remove(dir).cancel();
                });
            }
            deletedFiles = watchedFiles.stream()
                    .filter(file -> file.startsWith(path))
                    .toList();
        }
        deletedFiles.forEach(file -> {
            logger.debug("File deleted: {}", file);
            watchedFiles.remove(file);
            notifyListeners(FileChangeType.DELETE, file);
        });
    }

    private void notifyListeners(FileChangeType changeType, Path path) {
        FileChangedEvent event = new FileChangedEvent(changeType, path);
        for (FileChangeListener listener : listeners) {
            try {
                listener.onFileChange(event);
            } catch (RuntimeException e) {
                logger.warn("File change event listener exception", e);
            }
        }
    }

    private void watchDir(WatchService watchService, Path path) {
        if (watchedDirKeys.containsKey(path) || !isWatchable(path)) {
            return;
        }
        if (watchedDirKeys.size() > 10_000) {
            throw new I18nLoadException("Sanity check for too many directories too watch: " + MAX_DIRS_TO_WATCH);
        }
        if (!Files.isDirectory(path) && !Files.isRegularFile(path)) {
            throw new I18nLoadException("Expected file or directory to exist: " + path);
        }
        try {
            WatchKey watchKey = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watchedDirKeys.put(path, watchKey);
            if (isUnderOrEqualBaseDir(path)) {
                logger.debug("Watching dir recursively: " + path);
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        requireNonNull(dir);
                        requireNonNull(attrs);
                        if (!dir.equals(path)) {
                            watchDir(watchService, dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                        requireNonNull(path);
                        requireNonNull(attrs);
                        if (filter.test(path)) {
                            watchedFiles.add(path);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                logger.debug("Watching dir: " + path);
            }
        } catch (IOException e) {
            throw new I18nLoadException("Could not register watcher for file path: " + path, e);
        }
    }

    public static FileWatcherBuilder builder() {
        return new FileWatcherBuilder();
    }

    public static class FileWatcherBuilder {
        private final AtomicLong THREAD_COUNT = new AtomicLong(0);
        private final List<FileChangeListener> listeners = new ArrayList<>();
        private final List<I18nPathPattern> pathPatterns = new ArrayList<>();
        private FileSystem fileSystem = FileSystems.getDefault();

        public FileWatcherBuilder addListener(FileChangeListener listener) {
            requireNonNull(listener);
            listeners.add(listener);
            return this;
        }

        public FileWatcherBuilder addPathPatterns(Collection<I18nPathPattern> patterns) {
            requireNonNull(patterns);
            patterns.forEach(this::addPathPattern);
            return this;
        }

        public FileWatcherBuilder addPathPattern(String pattern) {
            requireNonNull(pattern);
            I18nPathPattern pathPattern = I18nPathPattern.of(pattern);
            pathPatterns.add(pathPattern);
            return this;
        }

        public FileWatcherBuilder addPathPattern(I18nPathPattern pathPattern) {
            requireNonNull(pathPattern);
            pathPatterns.add(pathPattern);
            return this;
        }

        public FileWatcherBuilder fileSystem(FileSystem fileSystem) {
            requireNonNull(fileSystem);
            this.fileSystem = fileSystem;
            return this;
        }

        public FileWatcher build() {
            List<I18nPathPattern> absolutePatterns = pathPatterns.stream()
                    .map(p -> p.withAbsoluteBaseDirectory(fileSystem))
                    .toList();
            List<String> baseDirectories = absolutePatterns.stream()
                    .map(I18nPathPattern::getBaseDirectory)
                    .distinct()
                    .toList();
            Predicate<String> filter = absolutePatterns.stream()
                    .map(p -> p.getPattern().asMatchPredicate())
                    .reduce((p) -> false, Predicate::or);
            Predicate<Path> pathFilter = p -> filter.test(p.toString());
            return new FileWatcher(fileSystem, listeners, baseDirectories, pathFilter);
        }

        public Thread startWatchingThread() {
            FileWatcher watcher = build();
            watcher.initializeWatchService();
            Thread thread = new Thread(watcher, "i18n-file-watcher-" + THREAD_COUNT.getAndIncrement());
            thread.start();
            return thread;
        }
    }

    @FunctionalInterface
    public interface FileChangeListener {
        void onFileChange(FileChangedEvent event);
    }

    public enum FileChangeType {
        CREATE, MODIFY, DELETE
    }

    public record FileChangedEvent(FileChangeType changeType, Path path) {
    }
}