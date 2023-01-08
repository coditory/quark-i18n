package com.coditory.quark.i18n.loader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

final class ResourceScanner {
    static List<Resource> scanFiles(FileSystem fs, I18nPathPattern pathPattern) {
        requireNonNull(fs);
        requireNonNull(pathPattern);
        Path basePath = fs.getPath(pathPattern.getBaseDirectory());
        try {
            return scanFiles(basePath, pathPattern.getPattern().asMatchPredicate());
        } catch (IOException e) {
            throw new RuntimeException("Could not scan files", e);
        }
    }

    private static List<Resource> scanFiles(Path basePath, Predicate<String> filter) throws IOException {
        Queue<Path> queue = new LinkedList<>();
        queue.add(basePath);
        List<Resource> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            Path path = queue.poll();
            if (!Files.exists(path)) {
                break;
            }
            if (Files.isDirectory(path)) {
                List<Path> children = Files.list(path).toList();
                queue.addAll(children);
            } else if (filter.test(path.toString())) {
                URL url = path.toUri().toURL();
                Resource resource = new Resource(path.toString(), url);
                result.add(resource);
            }
        }
        return result;
    }

    static List<Resource> scanClassPath(ClassLoader classLoader, I18nPathPattern pathPattern) {
        String basePackage = pathPattern.getBaseDirectory().replace("/", ".");
        if (basePackage.startsWith(".")) {
            basePackage = basePackage.substring(1);
        }
        if (basePackage.endsWith(".")) {
            basePackage = basePackage.substring(0, basePackage.length() - 1);
        }
        try {
            return scanClassPath(classLoader, basePackage, pathPattern.getPattern().asMatchPredicate());
        } catch (IOException e) {
            throw new RuntimeException("Could not scan classpath", e);
        }
    }

    private static List<Resource> scanClassPath(ClassLoader classLoader, String packageName, Predicate<String> filter)
            throws IOException {
        return ClassPath.from(classLoader)
                .getResources(packageName)
                .stream()
                .filter(r -> filter.test(r.getResourceName()))
                .map(r -> new Resource(r.getResourceName(), r.url()))
                .toList();
    }
}