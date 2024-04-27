package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nMessageBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public final class Reloadable18nMessagePack implements I18nMessagePack {
    private final Function<List<I18nMessageBundle>, I18nMessagePack> i18nMessagePackCreator;
    private final AggregatedI18nLoader loader;
    private volatile I18nMessagePack i18nMessagePack;

    Reloadable18nMessagePack(AggregatedI18nLoader loader, Function<List<I18nMessageBundle>, I18nMessagePack> i18nMessagePackCreator) {
        expectNonNull(i18nMessagePackCreator, "i18nMessagePackCreator");
        expectNonNull(loader, "loader");
        this.i18nMessagePackCreator = i18nMessagePackCreator;
        this.loader = loader;
        reload();
    }

    public void reload() {
        reload(loader.load());
    }

    private void reload(List<I18nMessageBundle> bundles) {
        this.i18nMessagePack = i18nMessagePackCreator.apply(bundles);
    }

    public synchronized void startWatching() {
        loader.startWatching(this::reload);
    }

    public synchronized void stopWatching() {
        loader.stopWatching();
    }

    @Override
    public @NotNull I18nMessages localize(@NotNull Locale locale) {
        return i18nMessagePack.localize(locale);
    }

    @Override
    public @NotNull I18nMessagePack prefixQueries(@NotNull List<I18nPath> prefixes) {
        return i18nMessagePack.prefixQueries(prefixes);
    }

    @Override
    public @NotNull String getMessage(@NotNull I18nKey key, Object... args) {
        return i18nMessagePack.getMessage(key, args);
    }

    @Override
    public @NotNull String getMessage(@NotNull I18nKey key, Map<String, Object> args) {
        return i18nMessagePack.getMessage(key, args);
    }

    @Override
    public @Nullable String getMessageOrNull(@NotNull I18nKey key, Object... args) {
        return i18nMessagePack.getMessageOrNull(key, args);
    }

    @Override
    @Nullable
    public String getMessageOrNull(@NotNull I18nKey key, Map<String, Object> args) {
        return i18nMessagePack.getMessageOrNull(key, args);
    }

    @Override
    @NotNull
    public String getMessage(@NotNull Locale locale, @NotNull String key, Object... args) {
        return i18nMessagePack.getMessage(locale, key, args);
    }

    @Override
    @NotNull
    public String format(@NotNull Locale locale, @NotNull String template, Object... args) {
        return i18nMessagePack.format(locale, template, args);
    }

    @Override
    @NotNull
    public String format(@NotNull Locale locale, @NotNull String template, @NotNull Map<String, Object> args) {
        return i18nMessagePack.format(locale, template, args);
    }

    @Override
    public void close() {
        loader.stopWatching();
    }
}
