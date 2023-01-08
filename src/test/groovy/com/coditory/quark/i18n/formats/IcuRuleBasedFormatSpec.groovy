package com.coditory.quark.i18n.formats

import com.coditory.quark.i18n.I18nMessagePack
import com.coditory.quark.i18n.I18nMessagePackFactory
import com.coditory.quark.i18n.I18nSystemDefaults
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL

class IcuRuleBasedFormatSpec extends Specification {
    static final I18nMessagePack messages = I18nMessagePackFactory.emptyMessagePack()

    void setup() {
        I18nSystemDefaults.setupGmtAndEnUsAsDefaults()
    }

    @Unroll
    def "should format message with date format: #format"() {
        expect:
            i18nFormat(EN_US, format, value) == expectedEn
            i18nFormat(PL_PL, format, value) == expectedPl
        where:
            format          | value | expectedEn          | expectedPl
            "{0, spellout}" | 12.5  | "twelve point five" | "dwanaście przecinek pięć"
            "{0, ordinal}"  | 1     | "1st"               | "1."
            "{0, ordinal}"  | 12    | "12th"              | "12."
            "{0, duration}" | 12    | "12 sec."           | "12"
            "{0, duration}" | 1212  | "20:12"             | "1 212"
    }

    private String i18nFormat(Locale locale, String template, Object... args) {
        return messages.format(locale, template, args)
    }
}
