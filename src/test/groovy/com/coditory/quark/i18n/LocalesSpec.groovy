package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static Locales.parseLocale
import static Locales.parseLocaleOrDefault
import static Locales.parseLocaleOrEmpty
import static Locales.parseLocaleOrNull

class LocalesSpec extends Specification {
    @Unroll
    def "should parse Locale value: #value"() {
        expect:
            parseLocaleOrNull(value) == expected
        where:
            value   || expected
            "pl"    || Locales.PL
            "pl-PL" || Locales.PL_PL
            "pl-pl" || Locales.PL_PL
            "PL-PL" || Locales.PL_PL
            "xx"    || null
            "xx-XX" || null
    }

    @Unroll
    def "should parse optional Locale"() {
        expect:
            parseLocaleOrEmpty(value) == expected

        where:
            value || expected
            "pl"  || Optional.of(Locales.PL)
            "xx"  || Optional.empty()
    }

    @Unroll
    def "should parse Locale or return default value"() {
        expect:
            parseLocaleOrDefault(value, defaultValue) == expected
        where:
            value | defaultValue || expected
            "xx"  | Locales.PL   || Locales.PL
            "en"  | Locales.PL   || Locales.EN
    }

    def "should parse Locale or throw error"() {
        when:
            parseLocale("en") == Locales.EN
        then:
            noExceptionThrown()

        when:
            parseLocale("xx")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Locale not available: 'xx'"
    }
}