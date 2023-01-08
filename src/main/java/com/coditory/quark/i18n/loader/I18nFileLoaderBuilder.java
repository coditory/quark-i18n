package com.coditory.quark.i18n.loader;

import com.coditory.quark.i18n.I18nPath;
import com.coditory.quark.i18n.parser.I18nParser;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.coditory.quark.i18n.parser.I18nParsers.I18N_PARSERS_BY_EXT;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

public final class I18nFileLoaderBuilder {
    private final List<I18nPathPattern> pathPatterns = new ArrayList<>();
    private final Map<String, I18nParser> fileParsersByExtension = new HashMap<>(I18N_PARSERS_BY_EXT);
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private FileSystem fileSystem = FileSystems.getDefault();
    private I18nPath staticPrefix = I18nPath.root();
    private I18nParser fileParser;

    I18nFileLoaderBuilder() {
        // package protected constructor
    }

    public I18nFileLoaderBuilder scanPathPattern(String filePattern) {
        requireNonNull(filePattern);
        I18nPathPattern template = I18nPathPattern.of(filePattern);
        this.pathPatterns.add(template);
        return this;
    }

    public I18nFileLoaderBuilder scanPathPatterns(String... others) {
        requireNonNull(others);
        Arrays.stream(others).forEach(this::scanPathPattern);
        return this;
    }

    I18nFileLoaderBuilder scanFileSystem() {
        return scanFileSystem(FileSystems.getDefault());
    }

    I18nFileLoaderBuilder scanFileSystem(FileSystem fileSystem) {
        this.fileSystem = requireNonNull(fileSystem);
        this.classLoader = null;
        return this;
    }

    I18nFileLoaderBuilder scanClassPath() {
        return scanClassPath(Thread.currentThread().getContextClassLoader());
    }

    I18nFileLoaderBuilder scanClassPath(ClassLoader classLoader) {
        this.classLoader = requireNonNull(classLoader);
        this.fileSystem = null;
        return this;
    }

    public I18nFileLoaderBuilder fileParser(String extension, I18nParser fileParser) {
        requireNonNull(extension);
        requireNonNull(fileParser);
        this.fileParsersByExtension.put(extension, fileParser);
        return this;
    }

    public I18nFileLoaderBuilder fileParser(I18nParser fileParser) {
        requireNonNull(fileParser);
        this.fileParser = fileParser;
        return this;
    }

    public I18nFileLoaderBuilder staticKeyPrefix(String prefix) {
        requireNonNull(prefix);
        I18nPath path = I18nPath.of(prefix);
        return staticKeyPrefix(path);
    }

    public I18nFileLoaderBuilder staticKeyPrefix(I18nPath prefix) {
        requireNonNull(prefix);
        this.staticPrefix = prefix;
        return this;
    }

    public I18nLoader build() {
        if (classLoader != null && fileSystem != null) {
            throw new IllegalStateException("Expected either classPath or fileSystem to be defined");
        }
        if (classLoader != null) {
            return buildClassPathLoader();
        }
        if (fileSystem != null) {
            return buildFileSystemLoader();
        }
        throw new IllegalStateException("Expected classPath or fileSystem to be defined");
    }

    public I18nLoader buildClassPathLoader() {
        // class path patterns with and without leading '/' should not differ
        Set<I18nPathPattern> absolute = pathPatterns.stream()
                .map(pattern -> {
                    String source = pattern.getSource();
                    return source.startsWith("/")
                            ? I18nPathPattern.of(source.substring(1))
                            : pattern;
                }).collect(toCollection(LinkedHashSet::new));
        return new I18nClassPathLoader(
                absolute,
                classLoader,
                fileParser,
                fileParsersByExtension,
                staticPrefix
        );
    }

    public I18nLoader buildFileSystemLoader() {
        Set<I18nPathPattern> absolute = pathPatterns.stream()
                .map(pattern -> pattern.withAbsoluteBaseDirectory(fileSystem))
                .collect(toCollection(LinkedHashSet::new));
        return new I18nFileSystemLoader(
                absolute,
                fileSystem,
                fileParser,
                fileParsersByExtension,
                staticPrefix
        );
    }
}
