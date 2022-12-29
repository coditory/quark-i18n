package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

import static com.coditory.quark.i18n.I18nMessages.EMPTY_ARGS;
import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public interface I18nMessagePack {
    static I18nMessagePackBuilder builder() {
        return new I18nMessagePackBuilder();
    }

    @NotNull
    I18nMessages localize(@NotNull Locale locale);

    @NotNull
    default I18nMessages localize(@NotNull String localeTag) {
        Locale locale = Locales.parseLocale(localeTag);
        return localize(locale);
    }

    @NotNull
    I18nMessagePack withQueryPrefix(@NotNull String prefix);

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
        return getMessageOrNull(locale, key, EMPTY_ARGS);
    }

    @NotNull
    String format(@NotNull Locale locale, @NotNull String template, Object... args);

    @NotNull
    String format(@NotNull Locale locale, @NotNull String expression, @NotNull Map<String, Object> args);
}
