package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public final class Locales {
    private static final Set<Locale> AVAILABLE_LOCALES = Set.of(Locale.getAvailableLocales());

    /**
     * English
     */
    public static Locale EN = Locale.forLanguageTag("en");

    /**
     * English (United Kingdom)
     */
    public static Locale EN_GB = Locale.forLanguageTag("en-GB");

    /**
     * English (United States)
     */
    public static Locale EN_US = Locale.forLanguageTag("en-US");

    /**
     * French
     */
    public static Locale FR = Locale.forLanguageTag("fr");

    /**
     * French (France)
     */
    public static Locale FR_FR = Locale.forLanguageTag("fr-FR");

    /**
     * Japanese
     */
    public static Locale JA = Locale.forLanguageTag("ja");

    /**
     * Japanese (Japan)
     */
    public static Locale JA_JP = Locale.forLanguageTag("ja-JP");

    /**
     * German
     */
    public static Locale DE = Locale.forLanguageTag("de");

    /**
     * German (Germany)
     */
    public static Locale DE_DE = Locale.forLanguageTag("de-DE");

    /**
     * Chinese
     */
    public static Locale ZH = Locale.forLanguageTag("zh");

    /**
     * Chinese (China)
     */
    public static Locale ZH_CN = Locale.forLanguageTag("zh-CN");

    /**
     * Polish
     */
    public static Locale PL = Locale.forLanguageTag("pl");

    /**
     * Polish (Poland)
     */
    public static Locale PL_PL = Locale.forLanguageTag("pl-PL");

    private Locales() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static boolean isAvailable(@NotNull Locale locale) {
        expectNonNull(locale, "locale");
        return AVAILABLE_LOCALES.contains(locale);
    }

    public static boolean isMoreSpecific(@NotNull Locale locale, @NotNull Locale other) {
        expectNonNull(locale, "locale");
        expectNonNull(other, "other");
        if (!locale.getLanguage().equals(other.getLanguage())) {
            return false;
        }
        String country = locale.getCountry();
        if (!country.isEmpty() && !country.equals(other.getLanguage())) {
            return false;
        }
        String variant = locale.getVariant();
        return variant.isEmpty() || variant.equals(other.getLanguage());
    }

    @Nullable
    public static Locale generalize(@NotNull Locale locale) {
        expectNonNull(locale, "locale");
        if (!locale.getVariant().isEmpty()) {
            return new Locale(locale.getLanguage(), locale.getCountry());
        }
        if (!locale.getCountry().isEmpty()) {
            return new Locale(locale.getLanguage());
        }
        return null;
    }

    @NotNull
    public static Locale parseLocale(@NotNull String value) {
        expectNonNull(value, "value");
        Locale locale;
        try {
            locale = Locale.forLanguageTag(value.replace("_", "-").trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse Locale: '" + value + "'");
        }
        if (!isAvailable(locale)) {
            throw new IllegalArgumentException("Locale not available: '" + value + "'");
        }
        return locale;
    }

    @Nullable
    public static Locale parseLocaleOrNull(@NotNull String value) {
        try {
            return parseLocale(value);
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public static Locale parseLocaleOrDefault(@NotNull String value, @NotNull Locale defaultLocale) {
        Locale result = parseLocaleOrNull(value);
        return result == null ? defaultLocale : result;
    }

    @NotNull
    public static Optional<Locale> parseLocaleOrEmpty(@NotNull String value) {
        return Optional.ofNullable(parseLocaleOrNull(value));
    }

    public static boolean isSubLocale(@NotNull Locale parent, @NotNull Locale child) {
        expectNonNull(parent, "parent");
        expectNonNull(child, "child");
        if (!Objects.equals(parent.getLanguage(), child.getLanguage())) {
            return false;
        }
        if (!isNullOrEmpty(parent.getCountry()) && !Objects.equals(parent.getCountry(), child.getCountry())) {
            return false;
        }
        return isNullOrEmpty(parent.getVariant()) || Objects.equals(parent.getVariant(), child.getVariant());
    }

    private static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }
}
