package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

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

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(@NotNull String content) {
        try {
            LoadSettings settings = LoadSettings.builder().build();
            Load yaml = new Load(settings);
            return (Map<String, Object>) yaml.loadFromString(content);
        } catch (Throwable e) {
            throw new I18nParseException("Could not parse YAML", e);
        }
    }
}