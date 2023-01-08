package com.coditory.quark.i18n.loader;

import com.coditory.quark.i18n.I18nPath;
import com.coditory.quark.i18n.Locales;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

final class I18nPathPattern {
    private static final Set<Integer> WHITELIST_CHARS = (
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789" +
                    "/-_.*{}"
    )
            .chars()
            .boxed()
            .collect(toSet());
    private static final String PREFIXES_GROUP_MARKER = "{prefixes}";
    private static final String PREFIX_GROUP_MARKER = "{prefix}";
    private static final String LOCALE_GROUP_MARKER = "{locale}";
    private static final String LANG_GROUP_MARKER = "{lang}";
    private static final String COUNTRY_GROUP_MARKER = "{country}";
    private static final List<String> MARKERS = List.of(PREFIXES_GROUP_MARKER, PREFIX_GROUP_MARKER, LOCALE_GROUP_MARKER, LANG_GROUP_MARKER, COUNTRY_GROUP_MARKER);

    public static I18nPathPattern of(String pathPattern) {
        return of(pathPattern, null);
    }

    static I18nPathPattern of(String pathPattern, String baseDirectory) {
        validate(pathPattern);
        String patternString = pathPattern
                .replace(".", "\\.")
                .replace("*", "#")
                .replace("##/", "([A-Za-z0-9-_.]+/)*")
                .replace("^##", "([A-Za-z0-9-_.]+/)*")
                .replace("##", "[A-Za-z0-9-_./]*")
                .replace("#", "[A-Za-z0-9-_.]*")
                .replace(PREFIXES_GROUP_MARKER + "/", "(?<prefixes>([A-Za-z0-9-_.]+/)*)")
                .replace(PREFIX_GROUP_MARKER, "(?<prefix>[a-zA-Z0-9-_.]+)")
                .replace(LANG_GROUP_MARKER, "(?<lang>[a-z]{2})")
                .replace(COUNTRY_GROUP_MARKER, "(?<country>[A-Z]{2})")
                .replace(LOCALE_GROUP_MARKER, "(?<lang>[a-z]{2})(-(?<country>[A-Z]{2}))?");
        if (pathPattern.startsWith("**") || pathPattern.startsWith(PREFIXES_GROUP_MARKER)) {
            patternString = "/?" + patternString;
        }
        Pattern pattern = Pattern.compile(patternString);
        String directory = baseDirectory == null ? extractBaseDir(pathPattern) : baseDirectory;
        return new I18nPathPattern(pathPattern, pattern, directory);
    }

    private static String extractBaseDir(String pathPattern) {
        int bracketIndex = pathPattern.indexOf("{");
        int starIndex = pathPattern.indexOf("*");
        int dirEnd = bracketIndex >= 0 && (bracketIndex < starIndex || starIndex < 0)
                ? bracketIndex
                : starIndex;
        if (dirEnd < 0) {
            dirEnd = pathPattern.length() - 1;
        }
        while (dirEnd > 0 && pathPattern.charAt(dirEnd) != '/') dirEnd--;
        return pathPattern.substring(0, dirEnd);
    }

    boolean hasLanguageGroup() {
        return activeGroups.containsKey(LANG_GROUP_MARKER) || activeGroups.containsKey(LOCALE_GROUP_MARKER);
    }

    private final String source;
    private final Pattern pattern;
    private final String baseDirectory;
    private final Map<String, Integer> activeGroups;

    private I18nPathPattern(
            String source,
            Pattern pattern,
            String baseDirectory
    ) {
        this.source = source;
        this.pattern = pattern;
        this.baseDirectory = baseDirectory;
        this.activeGroups = MARKERS.stream()
                .map(marker -> Map.entry(marker, source.indexOf(marker)))
                .filter(e -> e.getValue() >= 0)
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public String getSource() {
        return source;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public I18nPathPattern withAbsoluteBaseDirectory(FileSystem fileSystem) {
        if (baseDirectory.startsWith("/")) {
            return this;
        }
        String absoluteBaseDir = fileSystem.getPath(baseDirectory).toAbsolutePath().toString();
        String sourceWithoutBaseDir = source.substring(baseDirectory.length());
        String newSource = normalizePath(absoluteBaseDir + "/" + sourceWithoutBaseDir);
        return I18nPathPattern.of(newSource);
    }

    public boolean matches(String path) {
        String normalized = normalizePath(path);
        return pattern.asMatchPredicate().test(normalized);
    }

    public I18nPathGroups matchGroups(String path) {
        if (activeGroups.isEmpty()) {
            return new I18nPathGroups(null, null);
        }
        String normalized = normalizePath(path);
        Matcher matcher = pattern.matcher(normalized);
        if (!matcher.matches()) {
            return new I18nPathGroups(null, null);
        }
        Locale locale = extractLocale(matcher);
        I18nPath i18nPath = extractPath(matcher);
        return new I18nPathGroups(locale, i18nPath);
    }

    private String normalizePath(String path) {
        String winFix = File.separatorChar == '\\'
                ? path.replace("\\", "/")
                : path;
        return winFix.replaceAll("//", "/");
    }

    private Locale extractLocale(Matcher matcher) {
        if (!activeGroups.containsKey(LANG_GROUP_MARKER) && !activeGroups.containsKey(LOCALE_GROUP_MARKER)) {
            return null;
        }
        String tag = matcher.group("lang");
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        if (activeGroups.containsKey(COUNTRY_GROUP_MARKER) || activeGroups.containsKey(LOCALE_GROUP_MARKER)) {
            String countryMatch = matcher.group("country");
            if (countryMatch != null && !countryMatch.isEmpty()) {
                tag = tag + "-" + countryMatch;
            }
        }
        return Locales.parseLocale(tag);
    }

    private I18nPath extractPath(Matcher matcher) {
        String prefix = null;
        if (activeGroups.containsKey(PREFIXES_GROUP_MARKER)) {
            String prefixesMatch = matcher.group("prefixes");
            if (prefixesMatch != null && !prefixesMatch.equals("/")) {
                prefix = prefixesMatch.substring(0, prefixesMatch.length() - 1);
            }
        }
        if (activeGroups.containsKey(PREFIX_GROUP_MARKER)) {
            String prefixMatch = matcher.group("prefix");
            if (prefixMatch != null && !prefixMatch.isEmpty()) {
                if (prefix == null) {
                    prefix = prefixMatch;
                } else if (activeGroups.get(PREFIX_GROUP_MARKER) < activeGroups.get(PREFIXES_GROUP_MARKER)) {
                    prefix = prefixMatch + "/" + prefix;
                } else {
                    prefix = prefix + "/" + prefixMatch;
                }
            }
        }
        return prefix != null
                ? I18nPath.of(prefix.split("/"))
                : null;
    }

    private static void validate(String pathPattern) {
        if (pathPattern == null || pathPattern.isBlank()) {
            throw new IllegalArgumentException("Expected non-blank path");
        }
        if (pathPattern.endsWith("/")) {
            throw new IllegalArgumentException("Expected path to not end with /. Path: " + pathPattern);
        }
        if (pathPattern.contains("\\")) {
            throw new IllegalArgumentException("Expected unix file separators. Path: " + pathPattern);
        }
        if (count(pathPattern, PREFIX_GROUP_MARKER) > 1) {
            throw new IllegalArgumentException("Expected at most one " + PREFIX_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (count(pathPattern, PREFIXES_GROUP_MARKER) > 1) {
            throw new IllegalArgumentException("Expected at most one " + PREFIXES_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (count(pathPattern, LOCALE_GROUP_MARKER) > 1) {
            throw new IllegalArgumentException("Expected at most one " + LOCALE_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (count(pathPattern, LANG_GROUP_MARKER) > 1) {
            throw new IllegalArgumentException("Expected at most one " + LANG_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (count(pathPattern, COUNTRY_GROUP_MARKER) > 1) {
            throw new IllegalArgumentException("Expected at most one " + COUNTRY_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (count(pathPattern, LOCALE_GROUP_MARKER) > 0 && (count(pathPattern, LANG_GROUP_MARKER) + count(pathPattern, COUNTRY_GROUP_MARKER) > 0)) {
            throw new IllegalArgumentException("Expected either " + LOCALE_GROUP_MARKER + " or " + LANG_GROUP_MARKER + " with optional " + COUNTRY_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (count(pathPattern, COUNTRY_GROUP_MARKER) > count(pathPattern, LANG_GROUP_MARKER)) {
            throw new IllegalArgumentException("Missing " + LANG_GROUP_MARKER + " to match " + COUNTRY_GROUP_MARKER + " in path: " + pathPattern);
        }
        if (pathPattern.contains("***")) {
            throw new IllegalArgumentException("Too many stars in path: " + pathPattern);
        }
        if (!pathPattern.equals("**")) {
            int correctPlaceholders = count(pathPattern, "/**/");
            if (pathPattern.endsWith("**")) {
                correctPlaceholders++;
            }
            if (pathPattern.startsWith("**")) {
                correctPlaceholders++;
            }
            if (correctPlaceholders != count(pathPattern, "**")) {
                throw new IllegalArgumentException("Invalid dir placeholder (**). Path: " + pathPattern);
            }
        }
        if (pathPattern.contains("./")) {
            throw new IllegalArgumentException("Unexpected './'. Path: " + pathPattern);
        }
        if (pathPattern.contains("../")) {
            throw new IllegalArgumentException("Unexpected '../'. Path: " + pathPattern);
        }
        if (count(pathPattern, "{") != count(pathPattern, "}")) {
            throw new IllegalArgumentException("Found unmatched brace in path: " + pathPattern);
        }
        if (count(pathPattern, PREFIXES_GROUP_MARKER) != count(pathPattern, PREFIXES_GROUP_MARKER + "/")) {
            throw new IllegalArgumentException("Prefixes group (\"" + PREFIXES_GROUP_MARKER + "\") should be followed by a '/'. Path: " + pathPattern);
        }
        int markers = MARKERS.stream()
                .map(m -> count(pathPattern, m))
                .reduce(0, Integer::sum);
        if (count(pathPattern, "{") != markers) {
            throw new IllegalArgumentException("Found undefined group name in: " + pathPattern);
        }
        for (int c : pathPattern.toCharArray()) {
            if (!WHITELIST_CHARS.contains(c)) {
                throw new IllegalArgumentException("Invalid character '" + (char) c + "' in path: " + pathPattern);
            }
        }
    }

    private static int count(String text, String substring) {
        int result = 0;
        int index = text.indexOf(substring);
        while (index >= 0) {
            index = text.indexOf(substring, index + substring.length());
            result++;
        }
        return result;
    }

    @Override
    public String toString() {
        return "I18nPathPattern{" + source + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I18nPathPattern that = (I18nPathPattern) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    public record I18nPathGroups(Locale locale, I18nPath path) {
    }
}
