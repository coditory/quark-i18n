package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public record I18nKey(Locale locale, I18nPath path) {
    public I18nKey(@NotNull Locale locale, @NotNull I18nPath path) {
        this.locale = expectNonNull(locale, "locale");
        this.path = expectNonNull(path, "path");
    }

    @NotNull
    public static I18nKey of(@NotNull Locale locale, @NotNull String path) {
        expectNonNull(locale, "locale");
        expectNonBlank(path, "path");
        return new I18nKey(locale, I18nPath.of(path));
    }

    @NotNull
    public static I18nKey of(@NotNull Locale locale, String... path) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        return new I18nKey(locale, I18nPath.of(path));
    }

    @NotNull
    public static I18nKey of(@NotNull Locale locale, @NotNull I18nPath path) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        return new I18nKey(locale, path);
    }

    @NotNull
    public I18nKey withLocale(@NotNull Locale locale) {
        expectNonNull(locale, "locale");
        return Objects.equals(this.locale, locale)
                ? this
                : new I18nKey(locale, path);
    }

    @NotNull
    public I18nKey withPath(@NotNull String path) {
        expectNonNull(path, "path");
        return withPath(I18nPath.of(path));
    }

    @NotNull
    public I18nKey withPath(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return Objects.equals(this.path, path)
                ? this
                : new I18nKey(locale, path);
    }

    @NotNull
    public I18nKey prefixPath(@NotNull String path) {
        expectNonNull(path, "path");
        return prefixPath(I18nPath.of(path));
    }

    @NotNull
    public I18nKey prefixPath(@NotNull I18nPath path) {
        expectNonNull(path, "path");
        return new I18nKey(locale, path.child(this.path));
    }

    @NotNull
    public I18nKey child(@NotNull String segment) {
        expectNonNull(segment, "segment");
        return new I18nKey(locale, path.child(segment));
    }

    public String pathValue() {
        return path.getValue();
    }

    public String toShortString() {
        String localeString = locale.toString().replaceAll("_", "-");
        return localeString + ':' + path;
    }

    @Override
    public String toString() {
        return "I18nKey{" + toShortString() + '}';
    }
}
