package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nMessageBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.stream.Collectors.toSet;

final class LocaleResolver {
    static LocaleResolver of(Locale defaultLocale, List<I18nMessageBundle> templates) {
        expectNonNull(templates, "templates");
        Set<Locale> availableLocales = templates.stream()
                .flatMap(t -> t.templates().keySet().stream())
                .map(I18nKey::locale)
                .collect(toSet());
        return new LocaleResolver(defaultLocale, availableLocales);
    }

    private final LruCache<Locale, Locale> cache;
    private final LruCache<Locale, List<Locale>> hierarchyCache;
    private final Locale defaultLocale;
    private final Set<Locale> availableLocales;

    private LocaleResolver(Locale defaultLocale, Set<Locale> availableLocales) {
        this.defaultLocale = defaultLocale;
        this.availableLocales = Set.copyOf(availableLocales);
        this.cache = new LruCache<>(availableLocales.size() * 10 + 1);
        this.hierarchyCache = new LruCache<>(availableLocales.size() * 10 + 1);
    }

    I18nKey resolveQueryLocale(I18nKey key) {
        expectNonNull(key, "key");
        Locale locale = resolveQueryLocale(key.locale());
        return key.withLocale(locale);
    }

    Locale resolveQueryLocale(Locale locale) {
        expectNonNull(locale, "locale");
        return isValid(locale)
                ? locale
                : cache.computeIfAbsent(locale, this::resolveOnCacheMiss);
    }

    List<Locale> getLocaleHierarchy(Locale locale) {
        expectNonNull(locale, "locale");
        return hierarchyCache.computeIfAbsent(locale, this::resolveHierarchyOnCacheMiss);
    }

    private Locale resolveOnCacheMiss(Locale locale) {
        return getLocaleHierarchy(locale).stream()
                .filter(this::isValid)
                .findFirst()
                .orElse(defaultLocale);
    }

    private List<Locale> resolveHierarchyOnCacheMiss(Locale locale) {
        List<Locale> result = new ArrayList<>();
        while (locale != null) {
            result.add(locale);
            locale = Locales.generalize(locale);
        }
        return Collections.unmodifiableList(result);
    }

    private boolean isValid(Locale locale) {
        return availableLocales.contains(locale) || Objects.equals(defaultLocale, locale);
    }
}
