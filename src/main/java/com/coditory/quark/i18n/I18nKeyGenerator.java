package com.coditory.quark.i18n;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class I18nKeyGenerator {
    private final List<Locale> defaultLocales;
    private final List<I18nPath> defaultPrefixes;
    private final LocaleResolver localeResolver;

    public I18nKeyGenerator(Locale defaultLocale, List<I18nPath> prefixes, LocaleResolver localeResolver) {
        this(defaultLocale != null ? localeResolver.getLocaleHierarchy(defaultLocale) : List.of(), prefixes, localeResolver);
    }

    private I18nKeyGenerator(List<Locale> defaultLocales, List<I18nPath> prefixes, LocaleResolver localeResolver) {
        expectNonNull(defaultLocales, "defaultLocales");
        expectNonNull(localeResolver, "localeResolver");
        expectNonNull(prefixes, "prefixes");
        this.defaultLocales = defaultLocales;
        this.defaultPrefixes = prefixes.isEmpty() ? List.of(I18nPath.root()) : List.copyOf(prefixes);
        this.localeResolver = localeResolver;
    }

    I18nKeyGenerator prefixQueries(I18nPath prefix) {
        expectNonNull(prefix, "prefix");
        List<I18nPath> resolved = defaultPrefixes.stream()
                .map((p) -> p.child(prefix))
                .toList();
        return new I18nKeyGenerator(defaultLocales, resolved, localeResolver);
    }

    I18nKeyGenerator withPrefixes(List<I18nPath> prefixes) {
        expectNonNull(prefixes, "prefixes");
        return new I18nKeyGenerator(defaultLocales, prefixes, localeResolver);
    }

    List<I18nKey> keys(I18nKey key) {
        expectNonNull(key, "key");
        return keys(key, List.of());
    }

    List<I18nKey> keys(I18nKey key, I18nPath prefix) {
        expectNonNull(key, "key");
        return keys(key, List.of(prefix));
    }

    List<I18nKey> keys(I18nKey key, List<I18nPath> prefixes) {
        expectNonNull(key, "key");
        expectNonNull(prefixes, "prefixes");
        List<Locale> locales = localeResolver.getLocaleHierarchy(key.locale());
        Set<I18nKey> used = new HashSet<>();
        List<I18nKey> keys = new ArrayList<>(6 * (1 + prefixes.size() + this.defaultPrefixes.size()));
        I18nPath path = key.path();
        // locales x (prefix + path)
        for (I18nPath prefix : prefixes) {
            I18nPath prefixed = prefix.child(path);
            for (Locale loc : locales) {
                I18nKey k = I18nKey.of(loc, prefixed);
                used.add(k);
                keys.add(k);
            }
        }
        // locales * (defaultPrefixes + path)
        for (I18nPath prefix : this.defaultPrefixes) {
            I18nPath prefixed = prefix.child(path);
            for (Locale loc : locales) {
                I18nKey k = I18nKey.of(loc, prefixed);
                if (!used.contains(k)) {
                    used.add(k);
                    keys.add(k);
                }
            }
        }
        // defaultLocales x (prefix + path)
        for (I18nPath prefix : prefixes) {
            I18nPath prefixed = prefix.child(path);
            for (Locale loc : defaultLocales) {
                I18nKey k = I18nKey.of(loc, prefixed);
                if (!used.contains(k)) {
                    used.add(k);
                    keys.add(k);
                }
            }
        }
        // defaultLocales * (defaultPrefixes + path)
        for (I18nPath prefix : this.defaultPrefixes) {
            I18nPath prefixed = prefix.child(path);
            for (Locale loc : defaultLocales) {
                I18nKey k = I18nKey.of(loc, prefixed);
                if (!used.contains(k)) {
                    used.add(k);
                    keys.add(k);
                }
            }
        }
        return keys;
    }
}
