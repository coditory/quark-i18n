package com.coditory.quark.i18n

import spock.lang.Specification

import static com.coditory.quark.i18n.I18nMessagePackFactory.emptyMessagePack
import static com.coditory.quark.i18n.Locales.EN
import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL

class MissingMessageSpec extends Specification {
    def "should throw error for missing message"() {
        given:
            I18nMessagePack messages = emptyMessagePack()
        when:
            messages.getMessage(EN_US, "home.xxx")
        then:
            I18nMessagesException e = thrown(I18nMessagesException)
            e.message == "Missing message en-US:home.xxx"
    }

    def "should throw error for missing message with indexed arguments"() {
        given:
            I18nMessagePack messages = emptyMessagePack()
        when:
            messages.getMessage(EN_US, "home.xxx", 123, "xyz")
        then:
            I18nMessagesException e = thrown(I18nMessagesException)
            e.message == "Missing message en-US:home.xxx(123, xyz)"
    }

    def "should throw error for missing message with named arguments"() {
        given:
            I18nMessagePack messages = emptyMessagePack()
        when:
            messages.getMessage(EN_US, "home.xxx", [someNumber: 123, someText: "xyz"])
        then:
            I18nMessagesException e = thrown(I18nMessagesException)
            e.message == "Missing message en-US:home.xxx(someNumber=123, someText=xyz)"
    }

    def "should throw error for missing message and no default locale"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(PL, "home.hello", "Witamy")
                    .addMessage(EN, "home.bye", "Bye")
                    .buildLocalized(PL)
        when:
            messages.getMessage("home.bye")
        then:
            I18nMessagesException e = thrown(I18nMessagesException)
            e.message == "Missing message pl:home.bye"
    }

    def "should return missing message path when using printing unresolved message handler"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .usePathOnMissingMessage()
                    .buildLocalized(EN_US)
        when:
            String result = messages.getMessage("home.bye")
        then:
            result == "home.bye"
    }
}
