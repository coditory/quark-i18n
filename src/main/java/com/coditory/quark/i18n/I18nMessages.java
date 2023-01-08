package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public final class I18nMessages {
    static final Object[] EMPTY_ARGS = new Object[0];
    private final I18nMessagePack messagePack;
    private final Locale locale;

    I18nMessages(I18nMessagePack messagePack, Locale locale) {
        this.messagePack = expectNonNull(messagePack, "messagePack");
        this.locale = expectNonNull(locale, "locale");
    }

    @NotNull
    public String getMessage(@NotNull I18nPath path, Object... args) {
        expectNonNull(path, "path");
        expectNonNull(args, "args");
        return messagePack.getMessage(locale, path, args);
    }

    @NotNull
    public String getMessage(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return messagePack.getMessage(locale, path);
    }

    @NotNull
    public String getMessage(@NotNull String key, Object... args) {
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        return messagePack.getMessage(locale, key, args);
    }

    @NotNull
    public String getMessage(@NotNull String key, @NotNull Map<String, Object> args) {
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        return messagePack.getMessage(locale, key, args);
    }

    @NotNull
    public String getMessage(@NotNull String key) {
        expectNonNull(key, "key");
        return getMessage(key, EMPTY_ARGS);
    }

    @NotNull
    public I18nMessages addPrefix(@NotNull String prefix) {
        return messagePack.prefixQueries(prefix).localize(locale);
    }

    @NotNull
    public String format(@NotNull String template, Object... args) {
        expectNonNull(template, "template");
        expectNonNull(args, "args");
        return messagePack.format(locale, template, args);
    }

    @NotNull
    public Locale getLocale() {
        return locale;
    }
}
