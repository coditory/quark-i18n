package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nKey;
import com.coditory.quark.i18n.I18nPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

final class PropertiesI18nParser implements I18nParser {
    @Override
    @NotNull
    public Map<I18nKey, String> parse(@NotNull String content, @Nullable Locale locale) {
        requireNonNull(content);
        Map<String, Object> entries = parseProperties(content);
        return I18nParsers.parseEntries(entries, locale);
    }

    private Map<String, Object> parseProperties(String content) {
        try {
            Properties properties = new Properties();
            InputStream input = new ByteArrayInputStream(content.getBytes());
            properties.load(input);
            if (properties.isEmpty()) {
                return Map.of();
            }
            return properties.entrySet().stream()
                    .map(entry -> Map.entry(Objects.toString(entry.getKey()), entry.getValue()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Throwable e) {
            throw new I18nParseException("Could not parse properties", e);
        }
    }
}
