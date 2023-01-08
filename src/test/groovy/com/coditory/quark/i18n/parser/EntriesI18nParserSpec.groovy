package com.coditory.quark.i18n.parser

import com.coditory.quark.i18n.I18nKey
import com.coditory.quark.i18n.I18nPath
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.DE_DE
import static com.coditory.quark.i18n.Locales.EN_GB
import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL
import static com.coditory.quark.i18n.parser.I18nParsers.parseEntries

class EntriesI18nParserSpec extends Specification {
    @Unroll
    def "should parse locale from last path segment #input"() {
        when:
            Map<I18nKey, String> parsed = parseEntries([(input): "value"])
        then:
            parsed == [(expected): "value"]
        where:
            input       || expected
            "a.b.c.pl"  || I18nKey.of(PL, I18nPath.of("a.b.c"))
            "a.b.en-US" || I18nKey.of(EN_US, I18nPath.of("a.b"))
            "a.en-GB"   || I18nKey.of(EN_GB, I18nPath.of("a"))
    }

    @Unroll
    def "should parse locale from the first underscored segment #input"() {
        when:
            Map<I18nKey, String> parsed = parseEntries([(input): "value"])
        then:
            parsed == [(expected): "value"]
        where:
            input             || expected
            "a.b._pl.c"       || I18nKey.of(PL, I18nPath.of("a.b.c"))
            "a.b._pl.c.pl"    || I18nKey.of(PL, I18nPath.of("a.b.c.pl"))
            "a.b._pl.c._en.d" || I18nKey.of(PL, I18nPath.of("a.b.c.d"))
            "a.b._en-US.c"    || I18nKey.of(EN_US, I18nPath.of("a.b.c"))
            "a._en-GB.b"      || I18nKey.of(EN_GB, I18nPath.of("a.b"))
    }

    @Unroll
    def "should use locale passed as param for #input"() {
        when:
            Map<I18nKey, String> parsed = parseEntries([(input): "value"], DE_DE)
        then:
            parsed == [(expected): "value"]
        where:
            input       || expected
            "a.b.c"     || I18nKey.of(DE_DE, I18nPath.of("a.b.c"))
            "a.b._pl.c" || I18nKey.of(DE_DE, I18nPath.of("a.b._pl.c"))
            "a.b.c.pl"  || I18nKey.of(DE_DE, I18nPath.of("a.b.c.pl"))
    }

    @Unroll
    def "should throw exception when parsing entry key #input"() {
        when:
            parseEntries([(input): "value"])
        then:
            thrown(RuntimeException)
        where:
            input << ["_pl", "en", "xxx", "", null]
    }
}
