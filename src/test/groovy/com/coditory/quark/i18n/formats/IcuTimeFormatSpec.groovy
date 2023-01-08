package com.coditory.quark.i18n.formats

import com.coditory.quark.i18n.I18nMessagePack
import com.coditory.quark.i18n.I18nMessagePackFactory
import com.coditory.quark.i18n.I18nSystemDefaults
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL

class IcuTimeFormatSpec extends Specification {
    static final I18nMessagePack messages = I18nMessagePackFactory.emptyMessagePack()
    static final Instant instant = Instant.parse("2007-12-03T10:15:30.00Z")

    void setup() {
        I18nSystemDefaults.setupGmtAndEnUsAsDefaults()
    }

    @Unroll
    def "should format message with date format: #format"() {
        expect:
            i18nFormat(EN_US, format, value) == expectedEn
            i18nFormat(PL_PL, format, value) == expectedPl
        where:
            format                                   | value   | expectedEn                        | expectedPl
            "{0}"                                    | instant | "12/3/07, 10:15 AM"               | "3.12.2007, 10:15"
            "{0, time}"                              | instant | "10:15:30 AM"                     | "10:15:30"
            "{0, time, short}"                       | instant | "10:15 AM"                        | "10:15"
            "{0, time, medium}"                      | instant | "10:15:30 AM"                     | "10:15:30"
            "{0, time, long}"                        | instant | "10:15:30 AM GMT"                 | "10:15:30 GMT"
            "{0, time, full}"                        | instant | "10:15:30 AM Greenwich Mean Time" | "10:15:30 czas uniwersalny"
            "{0, time,yyyy-MM-dd'T'HH:mm:ss.SSS zz}" | instant | "2007-12-03T10:15:30.000 GMT"     | "2007-12-03T10:15:30.000 GMT"
    }

    private String i18nFormat(Locale locale, String template, Object... args) {
        return messages.format(locale, template, args)
    }
}
