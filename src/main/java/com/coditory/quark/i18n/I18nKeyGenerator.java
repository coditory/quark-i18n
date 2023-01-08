package com.coditory.quark.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class I18nKeyGenerator {
    private final List<Locale> defaultLocales;
    private final List<I18nPath> globalPrefixes;
    private final LocaleResolver localeResolver;

    public I18nKeyGenerator(Locale defaultLocale, List<I18nPath> globalPrefixes, LocaleResolver localeResolver) {
        expectNonNull(localeResolver, "localeResolver");
        expectNonNull(globalPrefixes, "globalPrefixes");
        this.defaultLocales = defaultLocale != null
                ? localeResolver.getLocaleHierarchy(defaultLocale)
                : List.of();
        this.globalPrefixes = List.copyOf(globalPrefixes);
        this.localeResolver = localeResolver;
    }

    List<I18nKey> keys(I18nKey key) {
        expectNonNull(key, "key");
        return keys(key, List.of());
    }

    List<I18nKey> keys(I18nKey key, I18nPath prefix) {
        expectNonNull(key, "key");
        return prefix == null || prefix.isRoot()
                ? keys(key)
                : keys(key, List.of(prefix));
    }

    List<I18nKey> keys(I18nKey key, List<I18nPath> prefixes) {
        expectNonNull(key, "key");
        expectNonNull(prefixes, "prefixes");
        List<Locale> locales = localeResolver.getLocaleHierarchy(key.locale());
        I18nPath path = key.path();
        List<I18nKey> keys = new ArrayList<>(6 * (1 + prefixes.size() + globalPrefixes.size()));
        // locales x prefix + path
        for (I18nPath prefix : prefixes) {
            for (Locale loc : locales) {
                keys.add(I18nKey.of(loc, prefix.child(path)));
            }
        }
        // locales x path
        for (Locale loc : locales) {
            keys.add(I18nKey.of(loc, path));
        }
        // locales x globalPrefixes
        for (I18nPath prefix : globalPrefixes) {
            for (Locale loc : locales) {
                keys.add(I18nKey.of(loc, prefix.child(path)));
            }
        }
        // defaultLocales x prefix + path
        for (I18nPath prefix : prefixes) {
            for (Locale loc : defaultLocales) {
                keys.add(I18nKey.of(loc, prefix.child(path)));
            }
        }
        // defaultLocales x path
        for (Locale loc : defaultLocales) {
            keys.add(I18nKey.of(loc, path));
        }
        // defaultLocales x globalPrefixes
        for (I18nPath prefix : globalPrefixes) {
            for (Locale loc : defaultLocales) {
                keys.add(I18nKey.of(loc, prefix.child(path)));
            }
        }
        return keys;
    }
}
