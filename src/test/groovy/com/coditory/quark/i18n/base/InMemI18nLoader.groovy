package com.coditory.quark.i18n.base

import com.coditory.quark.i18n.I18nKey
import com.coditory.quark.i18n.I18nPath
import com.coditory.quark.i18n.loader.I18nLoader
import com.coditory.quark.i18n.loader.I18nMessageBundle

class InMemI18nLoader implements I18nLoader {
    static final InMemI18nLoader of(Map<I18nKey, String> entries, I18nPath prefix) {
        List<I18nMessageBundle> templates = [new I18nMessageBundle(entries, prefix)]
        return new InMemI18nLoader(templates)
    }

    private final List<I18nMessageBundle> result
    private int executed = 0

    InMemI18nLoader(List<I18nMessageBundle> result) {
        this.result = result
    }

    @Override
    List<I18nMessageBundle> load() {
        return result
    }

    int getExecuted() {
        return executed
    }
}
