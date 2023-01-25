package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static Currencies.parseCurrency
import static Currencies.parseCurrencyOrDefault
import static Currencies.parseCurrencyOrEmpty
import static Currencies.parseCurrencyOrNull

class CurrenciesSpec extends Specification {
    @Unroll
    def "should parse Currency value: #value"() {
        expect:
            parseCurrencyOrNull(value) == expected
        where:
            value || expected
            "PLN" || Currencies.PLN
            "pln" || Currencies.PLN
            "EUR" || Currencies.EUR
            "XYZ" || null
    }

    @Unroll
    def "should parse optional Currency"() {
        expect:
            parseCurrencyOrEmpty(value) == expected

        where:
            value || expected
            "PLN" || Optional.of(Currencies.PLN)
            "XYZ" || Optional.empty()
    }

    @Unroll
    def "should parse Currency or return default value"() {
        expect:
            parseCurrencyOrDefault(value, defaultValue) == expected
        where:
            value | defaultValue   || expected
            "XYZ" | Currencies.PLN || Currencies.PLN
            "GBP" | Currencies.PLN || Currencies.GBP
    }

    def "should parse Currency or throw error"() {
        when:
            parseCurrency("GBP") == Currencies.GBP
        then:
            noExceptionThrown()

        when:
            parseCurrency("XYZ")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Could not parse Currency: 'XYZ'"
    }

    @Unroll
    def "should format amount by currency: #currency"() {
        expect:
            Currencies.formatByCurrency(1234.5, currency) == expectedFloat
            Currencies.formatByCurrency(BigDecimal.valueOf(1234.5), currency) == expectedFloat
            Currencies.formatByCurrency(1234, currency) == expectedLong
            Currencies.formatByCurrency(-1234, currency) == expectedNegative
        where:
            currency       || expectedFloat | expectedLong  | expectedNegative
            Currencies.PLN || "1 234,50 zł" | "1 234,00 zł" | "-1 234,00 zł"
            Currencies.EUR || "1.234,50 €"  | "1.234,00 €"  | "-1.234,00 €"
            Currencies.USD || "\$1,234.50"  | "\$1,234.00"  | "-\$1,234.00"
    }
}