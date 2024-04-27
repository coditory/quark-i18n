package com.coditory.quark.i18n

import com.coditory.quark.i18n.base.InMemI18nLoader
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN
import static com.coditory.quark.i18n.Locales.EN_GB
import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL
import static com.coditory.quark.i18n.Locales.PL_PL

class ReferenceHierarchySpec extends Specification {
    def "should resolve reference using source message locale (not user locale)"() {
        given:
            I18nMessagePack messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$ref")
                    .addMessage(EN, "ref", "en:ref")
                    .addMessage(EN_US, "ref", "en-US:ref")
                    .addMessage(PL_PL, "ref", "pl-PL:ref")
                    .setDefaultLocale(PL_PL)
                    .build()
        when:
            String message = messages.getMessage(EN_US, "msg")
        then:
            message == "en:ref"
    }

    def "should resolve references using fallback locales (#locale, #reference)"() {
        when:
            String message = I18nMessagePack.builder()
                    .addMessage(locale, "msg", "\${$reference}")
            // EN_US
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
                    .getMessage(locale, "msg")
        then:
            message == expected
        where:
            locale | reference || expected
            EN_US  | "a"       || "en-US:a"
            EN_US  | "b"       || "en:b"
            EN_US  | "c"       || "pl-PL:c"
            EN_US  | "d"       || "pl:d"
            EN     | "a"       || "en:a"
    }

    @Unroll
    def "should resolve references using fallback key prefix (#locale, #reference)"() {
        when:
            String message = I18nMessagePack.builder()
                    .addMessage(locale, "msg", "\${$reference}")
            // EN_US
                    .addMessage(EN_US, "a", "en-US:a")
                    .addMessage(EN_US, "fallback.b", "en-US:fallback.b")
                    .addMessage(EN_US, "fallback.c", "en-US:fallback.c")
            // EN
                    .addMessage(EN, "a", "en:a")
                    .addMessage(EN, "b", "en:b")
                    .addMessage(EN, "fallback.c", "en:fallback.c")
                    .addMessage(EN, "fallback.d", "en:fallback.d")
            // PL - default's parent
                    .addMessage(PL, "a", "pl:a")
                    .addMessage(PL, "b", "pl:b")
                    .addMessage(PL, "c", "pl:c")
                    .addMessage(PL, "d", "pl:d")
                    .addMessage(PL, "e", "pl:e")
                    .addMessage(PL, "fallback.f", "pl:fallback.f")
            // common settings
                    .setDefaultLocale(PL_PL)
                    .prefixReferenceQueries("", "fallback")
                    .build()
                    .getMessage(locale, "msg")
        then:
            message == expected

        where:
            locale | reference || expected
            EN_US  | "a"       || "en-US:a"
            EN_US  | "b"       || "en:b"
            EN_US  | "c"       || "en-US:fallback.c"
            EN_US  | "d"       || "en:fallback.d"
            EN_US  | "e"       || "pl:e"
            EN_US  | "f"       || "pl:fallback.f"
    }

    @Unroll
    def "should prefix references from a loaded bundle (#locale, #reference)"() {
        when:
            String message = I18nMessagePack.builder()
                    .addLoader(InMemI18nLoader.of([
                            (I18nKey.of(locale, "msg")): "\$" + reference,
                    ], I18nPath.of("common")))
            // EN_US
                    .addMessage(EN_US, "common.a", "en-US:common.a")
                    .addMessage(EN_US, "b", "en-US:b")
                    .addMessage(EN_US, "c", "en-US:c")
            // EN
                    .addMessage(EN, "common.a", "en:common.a")
                    .addMessage(EN, "common.b", "en:common.b")
                    .addMessage(EN, "c", "en:c")
                    .addMessage(EN, "d", "en:d")
            // PL - default's parent
                    .addMessage(PL, "common.a", "pl:common.a")
                    .addMessage(PL, "common.b", "pl:common.b")
                    .addMessage(PL, "common.c", "pl:common.c")
                    .addMessage(PL, "common.d", "pl:common.d")
                    .addMessage(PL, "common.e", "pl:common.e")
                    .addMessage(PL, "f", "pl:f")
            // settings
                    .setDefaultLocale(PL_PL)
                    .build()
                    .getMessage(locale, "common.msg")
        then:
            message == expected

        where:
            locale | reference || expected
            EN_US  | "a"       || "en-US:common.a"
            EN_US  | "b"       || "en:common.b"
            EN_US  | "c"       || "en-US:c"
            EN_US  | "d"       || "en:d"
            EN_US  | "e"       || "pl:common.e"
            EN_US  | "f"       || "pl:f"
    }
}
