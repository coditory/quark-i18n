package com.coditory.quark.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.coditory.quark.i18n.Locales.isSubLocale;
import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.stream.Collectors.toSet;

final public class I18nMissingMessagesDetector {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Set<String> pathsToSkip;
    private final boolean logMissingMessages;
    private final Function<List<MissingMessages>, RuntimeException> errorCreator;

    private I18nMissingMessagesDetector(boolean logMissingMessages, Function<List<MissingMessages>, RuntimeException> errorCreator, Set<String> pathsToSkip) {
        this.logMissingMessages = logMissingMessages;
        this.errorCreator = errorCreator;
        this.pathsToSkip = Set.copyOf(pathsToSkip);
    }

    void detect(Set<I18nKey> keys) {
        List<MissingMessages> missingMessages = findMissingMessages(keys);
        if (missingMessages.isEmpty()) {
            return;
        }
        if (logMissingMessages) {
            String report = toReport(missingMessages);
            logger.warn(report);
        }
        if (errorCreator != null) {
            throw errorCreator.apply(missingMessages);
        }
    }

    private String toReport(List<MissingMessages> missingMessages) {
        StringBuilder report = new StringBuilder();
        report.append("\nMissing Messages");
        report.append("\n================");
        for (MissingMessages missing : missingMessages) {
            report.append("\n   Path: ").append(missing.path.getValue());
            report.append("\nMissing: ").append(toString(missing.missingLanguages, 10));
            report.append("\nSources: ").append(toString(missing.sourceLocales, 3));
            report.append("\n");
        }
        report.append("\nTotal: ").append(missingMessages.size());
        return report.toString();
    }

    private List<MissingMessages> findMissingMessages(Set<I18nKey> keys) {
        List<Pattern> skipPatterns = pathsToSkip.stream()
                .map(s -> s.replaceAll("\\.", "\\\\.").replaceAll("\\*\\*", ".+").replaceAll("\\*", "[^.]+"))
                .map(Pattern::compile)
                .toList();
        Map<I18nPath, Set<Locale>> sourceLocalesByPath = new HashMap<>();
        Set<Locale> allLocales = new HashSet<>();
        Set<I18nPath> skippedPaths = new HashSet<>();
        for (I18nKey key : keys) {
            boolean skipped = skippedPaths.contains(key.path())
                    || skipPatterns.stream().anyMatch(s -> s.matcher(key.pathValue()).matches());
            if (skipped) {
                skippedPaths.add(key.path());
                continue;
            }
            allLocales.add(key.locale());
            sourceLocalesByPath.compute(key.path(), (path, value) -> {
                Set<Locale> locales = value == null ? new HashSet<>() : value;
                locales.add(key.locale());
                return locales;
            });
        }
        List<MissingMessages> result = new ArrayList<>();
        for (I18nPath path : sourceLocalesByPath.keySet()) {
            Set<Locale> missingLocales = new HashSet<>(allLocales);
            Set<Locale> sourceLocales = sourceLocalesByPath.get(path);
            for (Locale sourceLocale : sourceLocales) {
                missingLocales = missingLocales.stream()
                        .filter(missing -> !isSubLocale(sourceLocale, missing))
                        .collect(toSet());
            }
            if (!missingLocales.isEmpty()) {
                result.add(new MissingMessages(path, sourceLocales, missingLocales));
            }
        }
        result.sort(Comparator.comparing(it -> it.path.getValue()));
        return result;
    }

    private String toString(Set<?> set, int limit) {
        if (set.isEmpty()) {
            return "";
        }
        List<String> items = set.stream()
                .map(Object::toString)
                .sorted()
                .limit(limit)
                .toList();
        String result = items.toString();
        result = result.substring(1, result.length() - 1);
        if (set.size() > items.size()) {
            result += "... (total: " + set.size() + ")";
        }
        return result;
    }

    public static I18nMissingMessagesDetectorBuilder builder() {
        return new I18nMissingMessagesDetectorBuilder();
    }

    public static final class I18nMissingMessagesDetectorBuilder {
        private final Set<String> pathsToSkip = new HashSet<>();
        private boolean logMissingMessages = false;
        private Function<List<MissingMessages>, RuntimeException> errorCreator;

        public I18nMissingMessagesDetectorBuilder logMissingMessages() {
            this.logMissingMessages = true;
            return this;
        }

        public I18nMissingMessagesDetectorBuilder throwErrorOnMissingMessages() {
            return throwErrorOnMissingMessages((missing) -> new IllegalStateException("Detected missing messages: " + missing.size()));
        }

        public I18nMissingMessagesDetectorBuilder throwErrorOnMissingMessages(Function<List<MissingMessages>, RuntimeException> errorCreator) {
            expectNonNull(errorCreator, "errorCreator");
            this.errorCreator = errorCreator;
            return this;
        }

        public I18nMissingMessagesDetectorBuilder skipPath(String pathPattern) {
            expectNonBlank(pathPattern, "pathPattern");
            pathsToSkip.add(pathPattern);
            return this;
        }

        public I18nMissingMessagesDetectorBuilder skipPaths(String pathPattern, String... others) {
            skipPath(pathPattern);
            for (String path : others) {
                skipPath(path);
            }
            return this;
        }

        public I18nMissingMessagesDetectorBuilder skipPaths(Collection<String> pathPatterns) {
            expectNonNull(pathPatterns, "pathPatterns");
            pathPatterns.forEach(this::skipPath);
            return this;
        }

        public I18nMissingMessagesDetector build() {
            return new I18nMissingMessagesDetector(logMissingMessages, errorCreator, pathsToSkip);
        }
    }

    private record MissingMessages(I18nPath path, Set<Locale> sourceLocales, Set<Locale> missingLanguages) {
    }
}
