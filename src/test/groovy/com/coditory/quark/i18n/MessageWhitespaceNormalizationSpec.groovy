package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN

class MessageWhitespaceNormalizationSpec extends Specification {
    def "should replace multiple whitespaces with a single space"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", " \n\tsome   text\n with\t\tspaces  ")
                    .normalizeWhitespaces()
                    .buildLocalized(EN)
        when:
            String result = messages.getMessage("msg")
        then:
            result == "some text with spaces"
    }

    @Unroll
    def "should skip whitespace normalization by default"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", " \n\tsome   text\n with\t\tspaces  ")
                    .buildLocalized(EN)
        when:
            String result = messages.getMessage("msg")
        then:
            result == " \n\tsome   text\n with\t\tspaces  "
    }
}
