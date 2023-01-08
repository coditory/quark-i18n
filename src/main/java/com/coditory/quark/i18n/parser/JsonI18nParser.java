package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class JsonI18nParser implements I18nParser {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    @NotNull
    public Map<I18nKey, String> parse(@NotNull String content, @Nullable Locale locale) {
        requireNonNull(content);
        Map<String, Object> entries = parseJson(content);
        return I18nParsers.parseEntries(entries, locale);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(@NotNull String content) {
        try {
            Map<String, Object> entries = gson.fromJson(content, Map.class);
            return entries == null || entries.isEmpty() ? Map.of() : entries;
        } catch (Throwable e) {
            throw new I18nParseException("Could not parse JSON", e);
        }
    }
}