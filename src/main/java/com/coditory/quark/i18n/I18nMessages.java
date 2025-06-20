package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
    public I18nMessage getTemplate(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return messagePack.getTemplate(locale, path);
    }

    @NotNull
    public I18nMessage getTemplate(@NotNull String path) {
        expectNonNull(path, "path");
        return getTemplate(I18nPath.of(path));
    }

    @Nullable
    public I18nMessage getTemplateOrNull(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return messagePack.getTemplateOrNull(locale, path);
    }

    @Nullable
    public I18nMessage getTemplateOrNull(@NotNull String path) {
        expectNonNull(path, "path");
        return getTemplateOrNull(I18nPath.of(path));
    }

    @NotNull
    public String getMessage(@NotNull I18nPath path, Object... args) {
        expectNonNull(path, "path");
        expectNonNull(args, "args");
        return messagePack.getMessage(locale, path, args);
    }

    @Nullable
    public String getMessageOrNull(@NotNull I18nPath path, Object... args) {
        expectNonNull(path, "path");
        expectNonNull(args, "args");
        return messagePack.getMessageOrNull(locale, path, args);
    }

    @NotNull
    public String getMessage(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return messagePack.getMessage(locale, path);
    }

    @Nullable
    public String getMessageOrNull(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return messagePack.getMessageOrNull(locale, path);
    }

    @NotNull
    public String getMessage(@NotNull String key, Object... args) {
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        return messagePack.getMessage(locale, key, args);
    }

    @Nullable
    public String getMessageOrNull(@NotNull String key, Object... args) {
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        return messagePack.getMessageOrNull(locale, key, args);
    }

    @NotNull
    public String getMessage(@NotNull String key, @NotNull Map<String, Object> args) {
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        return messagePack.getMessage(locale, key, args);
    }

    @Nullable
    public String getMessageOrNull(@NotNull String key, @NotNull Map<String, Object> args) {
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        return messagePack.getMessageOrNull(locale, key, args);
    }

    @NotNull
    public String getMessage(@NotNull String key) {
        expectNonNull(key, "key");
        return getMessage(key, EMPTY_ARGS);
    }

    @Nullable
    public String getMessageOrNull(@NotNull String key) {
        expectNonBlank(key, "key");
        return messagePack.getMessageOrNull(locale, key);
    }

    @NotNull
    public I18nMessages prefixQueries(@NotNull String prefix) {
        expectNonNull(prefix, "prefix");
        return prefixQueries(I18nPath.of(prefix));
    }

    @NotNull
    public I18nMessages prefixQueries(@NotNull I18nPath prefix) {
        expectNonNull(prefix, "prefix");
        return messagePack.prefixQueries(prefix).localize(locale);
    }

    @NotNull
    public I18nMessages withQueryPrefixes(@NotNull List<I18nPath> prefixes) {
        expectNonNull(prefixes, "prefixes");
        return messagePack.withQueryPrefixes(prefixes).localize(locale);
    }

    @NotNull
    public I18nMessages withQueryPrefixes(@NotNull I18nPath... prefixes) {
        expectNonNull(prefixes, "prefixes");
        return messagePack.withQueryPrefixes(prefixes).localize(locale);
    }

    @NotNull
    public I18nMessages withQueryPrefixes(@NotNull String... prefixes) {
        expectNonNull(prefixes, "prefixes");
        return messagePack.withQueryPrefixes(prefixes).localize(locale);
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
