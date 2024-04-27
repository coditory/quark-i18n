package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL
import static com.coditory.quark.i18n.Locales.PL_PL

class MessageResolutionSpec extends Specification {
    def "should return message with two indexed arguments"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(PL, "hello", "Witaj {0} {1}")
                    .buildLocalized(PL)
        when:
            String result = messages.getMessage("hello", "Jan", "Kowalski")
        then:
            result == "Witaj Jan Kowalski"
    }

    def "should return message with two named arguments"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(PL, "hello", "Witaj {firstName} {lastName}")
                    .buildLocalized(PL)
        when:
            String result = messages.getMessage("hello", [firstName: "Jan", lastName: "Kowalski"])
        then:
            result == "Witaj Jan Kowalski"
    }

    @Unroll
    def "should prefix queries (#locale, #path)"() {
        given:
            I18nMessagePack messagePack = I18nMessagePack.builder()
                    .addMessage(EN_US, "a", "en-US:a")
                    .addMessage(EN_US, "x.y.a", "en-US:x.y.a")
                    .addMessage(EN_US, "b", "en-US:b")
                    .addMessage(PL, "c", "pl:c")
                    .addMessage(PL, "x.y.c", "pl:x.y.c")
                    .setDefaultLocale(PL_PL)
                    .build()
                    .prefixQueries("x.y", "")
        expect:
            messagePack.getMessage(EN_US, "a") == "en-US:x.y.a"
            messagePack.getMessage(EN_US, "b") == "en-US:b"
            messagePack.getMessage(EN_US, "c") == "pl:x.y.c"
    }
}
