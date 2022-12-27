package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nKey;
import com.coditory.quark.i18n.I18nPath;
import com.coditory.quark.i18n.Locales;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EntriesI18nParser {
    @SuppressWarnings("unchecked")
    @NotNull
    public Map<I18nKey, String> parseEntries(
            @NotNull Map<String, Object> values,
            @Nullable I18nPath prefix,
            @Nullable Locale locale
    ) {
        if (prefix == null) {
            prefix = I18nPath.root();
        }
        Map<I18nKey, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            I18nPath path = prefix.child(entry.getKey());
            if (entry.getValue() instanceof Map<?, ?>) {
                Map<String, Object> children = (Map<String, Object>) entry.getValue();
                result.putAll(parseEntries(children, path, locale));
            } else if (entry.getValue() instanceof Collection<?>) {
                throw new I18nParseException("Unexpected collection on path: " + path);
            } else {
                I18nKey key = resolveKey(path, locale);
                String value = entry.getValue().toString();
                result.put(key, value);
            }
        }
        return result;
    }

    private I18nKey resolveKey(I18nPath path, Locale locale) {
        if (path.isRoot()) {
            throw new I18nParseException("Expected non-empty path");
        }
        if (locale != null) {
            return I18nKey.of(locale, path);
        }
        if (path.getSegments().size() <= 1) {
            throw new I18nParseException("Expected at least two segments in path: " + path);
        }
        return path.getValue().contains("._")
                ? getKeyWithUnderscoredLocale(path)
                : getKeyWithLocaleInLastSegment(path);
    }

    private I18nKey getKeyWithUnderscoredLocale(I18nPath path) {
        String localeSegment = path.getSegments().stream()
                .filter(s -> s.startsWith("_"))
                .findFirst()
                .get();
        Locale keyLocale = Locales.parseLocale(localeSegment.substring(1));
        List<String> segments = path.getSegments().stream()
                .filter(s -> !s.startsWith("_"))
                .toList();
        I18nPath filteredPath = I18nPath.of(segments);
        return I18nKey.of(keyLocale, filteredPath);
    }

    private I18nKey getKeyWithLocaleInLastSegment(I18nPath path) {
        String lastSegment = path.getLastSegment();
        Locale keyLocale = Locales.parseLocale(lastSegment);
        return I18nKey.of(keyLocale, path.parentOrRoot());
    }
}