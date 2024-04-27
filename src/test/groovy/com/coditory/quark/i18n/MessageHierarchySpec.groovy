package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN
import static com.coditory.quark.i18n.Locales.EN_GB
import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL
import static com.coditory.quark.i18n.Locales.PL_PL

class MessageHierarchySpec extends Specification {
    @Unroll
    def "should resolve message using fallback locales (#locale, #path)"() {
        given:
            I18nMessagePack messagePack = I18nMessagePack.builder()
                    .addMessage(EN_US, "a", "en-US:a")
            // EN
                    .addMessage(EN, "a", "en:a")
                    .addMessage(EN, "b", "en:b")
            // PL_PL - default
                    .setDefaultLocale(PL_PL)
                    .addMessage(PL_PL, "a", "pl-PL:a")
                    .addMessage(PL_PL, "b", "pl-PL:b")
                    .addMessage(PL_PL, "c", "pl-PL:c")
            // PL - default's parent
                    .addMessage(PL, "a", "pl:a")
                    .addMessage(PL, "b", "pl:b")
                    .addMessage(PL, "c", "pl:c")
                    .addMessage(PL, "d", "pl:d")
            // EN_GB - control group
                    .addMessage(EN_GB, "a", "en-GB:a")
                    .addMessage(EN_GB, "b", "en-GB:b")
                    .addMessage(EN_GB, "c", "en-GB:c")
                    .build()
        when:
            String result = messagePack.getMessage(locale, path)
        then:
            result == expected
        where:
            locale                         | path || expected
            EN_US                          | "a"  || "en-US:a"
            EN_US                          | "b"  || "en:b"
            EN_US                          | "c"  || "pl-PL:c"
            EN_US                          | "d"  || "pl:d"
            EN                             | "a"  || "en:a"
            Locale.forLanguageTag("en-XX") | "a"  || "en:a"
    }

    @Unroll
    def "should resolve message using fallback prefix (#locale, #path)"() {
        given:
            I18nMessagePack messagePack = I18nMessagePack.builder()
            // EN_US
                    .addMessage(EN_US, "a", "en-US:a")
                    .addMessage(EN_US, "fallback.a", "en-US:fallback.a")
                    .addMessage(EN_US, "fallback.b", "en-US:fallback.b")
                    .addMessage(EN_US, "fallback.c", "en-US:fallback.c")
            // EN
                    .addMessage(EN, "a", "en:a")
                    .addMessage(EN, "b", "en:b")
                    .addMessage(EN, "fallback.a", "en:fallback.a")
                    .addMessage(EN, "fallback.b", "en:fallback.b")
                    .addMessage(EN, "fallback.c", "en:fallback.c")
                    .addMessage(EN, "fallback.d", "en:fallback.d")
            // PL - default's parent
                    .addMessage(PL, "a", "pl:a")
                    .addMessage(PL, "b", "pl:b")
                    .addMessage(PL, "c", "pl:c")
                    .addMessage(PL, "d", "pl:d")
                    .addMessage(PL, "e", "pl:e")
                    .addMessage(PL, "fallback.a", "pl:fallback.a")
                    .addMessage(PL, "fallback.b", "pl:fallback.b")
                    .addMessage(PL, "fallback.c", "pl:fallback.c")
                    .addMessage(PL, "fallback.d", "pl:fallback.d")
                    .addMessage(PL, "fallback.e", "pl:fallback.e")
                    .addMessage(PL, "fallback.f", "pl:fallback.f")
            // EN_GB - control group
                    .addMessage(EN_GB, "a", "en-GB:a")
                    .addMessage(EN_GB, "b", "en-GB:b")
                    .addMessage(EN_GB, "c", "en-GB:c")
            // common settings
                    .prefixQueries("", "fallback")
                    .setDefaultLocale(PL_PL)
                    .build()
        when:
            String result = messagePack.getMessage(locale, path)
        then:
            result == expected
        where:
            locale                         | path || expected
            EN_US                          | "a"  || "en-US:a"
            EN_US                          | "b"  || "en:b"
            EN_US                          | "c"  || "en-US:fallback.c"
            EN_US                          | "d"  || "en:fallback.d"
            EN_US                          | "e"  || "pl:e"
            EN_US                          | "f"  || "pl:fallback.f"
            EN                             | "a"  || "en:a"
            Locale.forLanguageTag("en-XX") | "a"  || "en:a"
    }
}
