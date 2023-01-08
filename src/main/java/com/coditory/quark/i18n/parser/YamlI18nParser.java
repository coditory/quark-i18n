package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class YamlI18nParser implements I18nParser {
    @Override
    @NotNull
    public Map<I18nKey, String> parse(@NotNull String content, @Nullable Locale locale) {
        requireNonNull(content);
        Map<String, Object> entries = parseYaml(content);
        return I18nParsers.parseEntries(entries, locale);
    }

    private Map<String, Object> parseYaml(@NotNull String content) {
        try {
            Yaml yaml = new Yaml(); // Yaml is not thread safe!
            Map<String, Object> entries = yaml.load(content);
            return entries == null || entries.isEmpty() ? Map.of() : entries;
        } catch (Throwable e) {
            throw new I18nParseException("Could not parse YAML", e);
        }
    }
}