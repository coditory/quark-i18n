package com.coditory.quark.i18n.loader;

import com.coditory.quark.i18n.I18nKey;
import com.coditory.quark.i18n.I18nPath;
import com.coditory.quark.i18n.loader.I18nPathPattern.I18nPathGroups;
import com.coditory.quark.i18n.parser.I18nParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public final class I18nClassPathLoader implements I18nLoader {
    public static I18nFileLoaderBuilder builder() {
        return builder(Thread.currentThread().getContextClassLoader());
    }

    public static I18nFileLoaderBuilder builder(ClassLoader classLoader) {
        return new I18nFileLoaderBuilder()
                .scanClassPath(classLoader);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Set<I18nPathPattern> pathPatterns;
    private final ClassLoader classLoader;
    private final I18nParser parser;
    private final Map<String, I18nParser> parsersByExtension;
    private final I18nPath staticPrefix;

    I18nClassPathLoader(
            Set<I18nPathPattern> pathPatterns,
            ClassLoader classLoader,
            I18nParser fileParser,
            Map<String, I18nParser> parsersByExtension,
            I18nPath staticPrefix
    ) {
        this.classLoader = requireNonNull(classLoader);
        this.staticPrefix = requireNonNull(staticPrefix);
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
        return new I18nMessageBundle(parsed, prefix);
    }

    private List<Resource> scanFiles(I18nPathPattern pathPattern) {
        return ResourceScanner.scanClassPath(classLoader, pathPattern);
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
}
