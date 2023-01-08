package com.coditory.quark.i18n

import spock.lang.Specification

import static com.coditory.quark.i18n.Locales.EN

class ReferenceResolutionSpec extends Specification {
    def "should resolve message reference"() {
        when:
            String message = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$company.name was established on 1988")
                    .addMessage(EN, "company.name", "ACME")
                    .build()
                    .getMessage(EN, "msg")
        then:
            message == "ACME was established on 1988"
    }

    def "should skip reference resolution when requested"() {
        when:
            String message = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$company.name was established on 1988")
                    .addMessage(EN, "company.name", "ACME")
                    .disableReferenceResolution()
                    .build()
                    .getMessage(EN, "msg")
        then:
            message == "\$company.name was established on 1988"
    }

    def "should resolve a message with a transitive reference"() {
        given:
            I18nMessagePack messagePack = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$company.name1 was established on 1988")
                    .addMessage(EN, "company.name1", ">\${company.name2}<")
                    .addMessage(EN, "company.name2", ">\${company.name3}<")
                    .addMessage(EN, "company.name3", "ACME")
                    .build()
        when:
            String message = messagePack.getMessage(EN, "msg")
        then:
            message == ">>ACME<< was established on 1988"
    }

    def "should resolve a message with a reference in braces and without them"() {
        given:
            I18nMessagePack messagePack = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "a: \${a}, b: \${ b }, c: \$c")
                    .addMessage(EN, "a", "A")
                    .addMessage(EN, "b", "B")
                    .addMessage(EN, "c", "C")
                    .build()
        when:
            String message = messagePack.getMessage(EN, "msg")
        then:
            message == "a: A, b: B, c: C"
    }

    def "should throw error on unresolvable reference"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$company.name was established on 1988")
                    .build()
        then:
            I18nMessagesException e = thrown(I18nMessagesException)
            e.getMessage() == "Missing reference: company.name"
    }

    def "should throw error on cyclic reference"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$company.name1 was established on 1988")
                    .addMessage(EN, "company.name1", "\${company.name2}")
                    .addMessage(EN, "company.name2", "\${company.name3}")
                    .addMessage(EN, "company.name3", "\${company.name1}")
                    .build()
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.getMessage() == "Detected potential cyclic reference"
    }

    def "should throw error on a reference to itself"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN, "msg", "\$msg")
                    .build()
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.getMessage() == "Detected potential cyclic reference"
    }

    def "should throw error on missing reference even when using printing unresolved message handler"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN, "msg", ">> \${missing.value} <<")
                    .setMissingMessageHandler(I18nMissingMessageHandler.pathPrintingHandler())
                    .build()
        then:
            I18nMessagesException e = thrown(I18nMessagesException)
            e.getMessage() == "Missing reference: missing.value"
    }
}
