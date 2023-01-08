package com.coditory.quark.i18n.formats

import com.coditory.quark.i18n.I18nMessagePack
import com.coditory.quark.i18n.I18nMessages
import spock.lang.Specification

import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL

class IcuSelectFormatSpec extends Specification {
    def "should format a simple message with select format"() {
        given:
            String key = "sent-you-a-message"
            I18nMessages messages = I18nMessagePack.builder()
                    .addMessage(EN_US, key, """
                    {0, select,
                        male {He}
                        female {She}
                        other {They}
                    } sent you a message
                    """.stripIndent().trim())
                    .addMessage(PL_PL, key, """
                    {0, select,
                        male {On wysłał}
                        female {Ona wysłała}
                        other {Oni wysłali}
                    } ci wiadomość
                    """.stripIndent().trim())
                    .build()
                    .localize(locale)
        when:
            String result = messages.getMessage(key, gender)
        then:
            result == expected
        where:
            locale | gender   || expected
            EN_US  | "male"   || "He sent you a message"
            EN_US  | "female" || "She sent you a message"
            EN_US  | "other"  || "They sent you a message"
            PL_PL  | "male"   || "On wysłał ci wiadomość"
            PL_PL  | "female" || "Ona wysłała ci wiadomość"
            PL_PL  | "other"  || "Oni wysłali ci wiadomość"
    }
}
