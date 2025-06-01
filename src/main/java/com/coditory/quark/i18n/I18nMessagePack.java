package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coditory.quark.i18n.I18nMessages.EMPTY_ARGS;
import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public interface I18nMessagePack extends Closeable {
    static I18nMessagePackBuilder builder() {
        return new I18nMessagePackBuilder();
    }

    @NotNull
    I18nMessage getTemplate(@NotNull I18nKey key);

    @Nullable
    I18nMessage getTemplateOrNull(@NotNull I18nKey key);

    @NotNull
    default I18nMessage getTemplate(@NotNull Locale locale, @NotNull I18nPath path) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getTemplate(messageKey);
    }

    @Nullable
    default I18nMessage getTemplateOrNull(@NotNull Locale locale, @NotNull I18nPath path) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getTemplateOrNull(messageKey);
    }

    @NotNull
    String getMessage(@NotNull I18nKey key, Object... args);

    @NotNull
    String getMessage(@NotNull I18nKey key, Map<String, Object> args);

    @Nullable
    String getMessageOrNull(@NotNull I18nKey key, Object... args);

    @Nullable
    String getMessageOrNull(@NotNull I18nKey key, Map<String, Object> args);

    @NotNull
    default String getMessage(@NotNull I18nKey key) {
        expectNonNull(key, "key");
        return getMessage(key, EMPTY_ARGS);
    }

    @NotNull
    default String getMessage(@NotNull Locale locale, @NotNull String path, Object... args) {
        expectNonNull(locale, "locale");
        expectNonBlank(path, "path");
        expectNonNull(args, "args");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getMessage(messageKey, args);
    }

    @NotNull
    default String getMessage(@NotNull Locale locale, @NotNull String path, @NotNull Map<String, Object> args) {
        expectNonNull(locale, "locale");
        expectNonBlank(path, "path");
        expectNonNull(args, "args");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getMessage(messageKey, args);
    }

    @NotNull
    default String getMessage(@NotNull Locale locale, @NotNull I18nPath path, Object... args) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        expectNonNull(args, "args");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getMessage(messageKey, args);
    }

    @NotNull
    default String getMessage(@NotNull Locale locale, @NotNull String path) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        return getMessage(locale, path, EMPTY_ARGS);
    }

    @Nullable
    default String getMessageOrNull(@NotNull I18nKey key) {
        expectNonNull(key, "key");
        return getMessageOrNull(key, EMPTY_ARGS);
    }

    @Nullable
    default String getMessageOrNull(@NotNull Locale locale, @NotNull String path, Object... args) {
        expectNonNull(locale, "locale");
        expectNonBlank(path, "path");
        expectNonNull(args, "args");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getMessageOrNull(messageKey, args);
    }

    @Nullable
    default String getMessageOrNull(@NotNull Locale locale, @NotNull I18nPath path, Object... args) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        expectNonNull(args, "args");
        I18nKey messageKey = I18nKey.of(locale, path);
        return getMessageOrNull(messageKey, args);
    }

    @Nullable
    default String getMessageOrNull(@NotNull Locale locale, @NotNull String key) {
        expectNonNull(locale, "locale");
        expectNonNull(key, "key");
        return getMessageOrNull(locale, key, EMPTY_ARGS);
    }

    @NotNull
    String format(@NotNull Locale locale, @NotNull String template, Object... args);

    @NotNull
    String format(@NotNull Locale locale, @NotNull String template, @NotNull Map<String, Object> args);

    @NotNull
    I18nMessages localize(@NotNull Locale locale);

    @NotNull
    default I18nMessages localize(@NotNull String locale) {
        expectNonNull(locale, "locale");
        return localize(Locales.parseLocale(locale));
    }

    @NotNull
    default I18nMessagePack prefixQueries(@NotNull String prefix) {
        expectNonNull(prefix, "prefix");
        return prefixQueries(I18nPath.of(prefix));
    }

    @NotNull
    I18nMessagePack prefixQueries(@NotNull I18nPath prefix);

    @NotNull
    I18nMessagePack withQueryPrefixes(@NotNull List<I18nPath> prefixes);

    @NotNull
    default I18nMessagePack withQueryPrefixes(@NotNull I18nPath... prefixes) {
        expectNonNull(prefixes, "prefixes");
        return withQueryPrefixes(List.of(prefixes));
    }

    @NotNull
    default I18nMessagePack withQueryPrefixes(@NotNull String... prefixes) {
        expectNonNull(prefixes, "prefixes");
        List<I18nPath> paths = Arrays.stream(prefixes).map(I18nPath::of).toList();
        return withQueryPrefixes(paths);
    }

    @Override
    default void close() {
        // implementation optional
    }
}
