package com.coditory.quark.i18n.parser

import com.coditory.quark.i18n.I18nKey
import com.coditory.quark.i18n.I18nPath
import com.coditory.quark.i18n.Locales
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN
import static com.coditory.quark.i18n.Locales.PL

class PropertiesI18nParserSpec extends Specification {
    @Unroll
    def "should parse properties i18n messages"() {
        when:
            Map<I18nKey, String> parsed = I18nParsers.parseProperties("""
            title.en=Homepage
            title.pl=Strona domowa
            """.stripIndent().trim())
        then:
            parsed == [
                    (I18nKey.of(EN, I18nPath.of("title"))): "Homepage",
                    (I18nKey.of(PL, I18nPath.of("title"))): "Strona domowa"
            ]
    }

    @Unroll
    def "should parse properties i18n messages with predefined locale and prefix"() {
        when:
            Map<I18nKey, String> parsed = I18nParsers.parseProperties("""
            title=Homepage
            user.name=User Name
            """.stripIndent().trim(), Locales.EN)
        then:
            parsed == [
                    (I18nKey.of(EN, I18nPath.of("title")))    : "Homepage",
                    (I18nKey.of(EN, I18nPath.of("user.name"))): "User Name"
            ]
    }
}
