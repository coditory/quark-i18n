package com.coditory.quark.i18n

import spock.lang.Specification

import static com.coditory.quark.i18n.Locales.EN

class TypeBasedArgFormattingSpec extends Specification {
    def "should transform message argument by type"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "{0}")
                    .addArgumentTransformer(Foo, { ">>${it.value}<<" })
                    .buildLocalized(EN)
        when:
            String result = messages.getMessage("msg", new Foo("abc"))
        then:
            result == ">>abc<<"
    }

    def "should transform argument transitively"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "{0} {1}")
                    .addArgumentTransformer(Foo, { new Bar(it.value) })
                    .addArgumentTransformer(Bar, { ">>${it.value}<<" })
                    .buildLocalized(EN)
        when:
            String result = messages.getMessage("msg", new Foo("abc"), new Bar("def"))
        then:
            result == ">>abc<< >>def<<"
    }

    def "should format transformed argument"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "{0,number,00000.00000}")
                    .addArgumentTransformer(Foo, { Double.parseDouble(it.value) })
                    .buildLocalized(EN)
        when:
            String result = messages.getMessage("msg", new Foo("123.456"))
        then:
            result == "00123.45600"
    }

    def "should propagate error on message transformation"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "{0}")
                    .addArgumentTransformer(Foo, { throw new RuntimeException("Simulated") })
                    .buildLocalized(EN)
        when:
            messages.getMessage("msg", new Foo("123.456"))
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.getMessage().startsWith("Could not resolve message en:msg=\"{0}\"")
            e.getCause().getMessage() == "Could not transform argument: 0=Foo(123.456)"
            e.getCause().getCause().getMessage() == "Simulated"
    }

    def "should not transform unused argument"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "{0}")
                    .addArgumentTransformer(Foo, { throw new RuntimeException("should not be executed") })
                    .buildLocalized(EN)
        when:
            String message = messages.getMessage("msg", "used", new Foo("unused"))
        then:
            message == "used"
    }

    def "should throw error on cyclic transformation"() {
        given:
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN, "msg", "{0}")
                    .addArgumentTransformer(Foo, { new Bar(it.value) })
                    .addArgumentTransformer(Bar, { new Foo(it.value) })
                    .buildLocalized(EN)
        when:
            messages.getMessage("msg", new Foo("unused"))
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.getMessage().startsWith("Could not resolve message en:msg=\"{0}\"")
            e.getCause().getMessage().startsWith("Could not transform argument: 0=Foo(unused)")
            e.getCause().getCause().getMessage() == "Too many argument transformations"
    }

    class Foo {
        final String value

        Foo(String value) {
            this.value = value
        }

        @Override
        String toString() {
            return "Foo($value)"
        }
    }

    class Bar {
        final String value

        Bar(String value) {
            this.value = value
        }

        @Override
        String toString() {
            return "Bar($value)"
        }
    }
}
