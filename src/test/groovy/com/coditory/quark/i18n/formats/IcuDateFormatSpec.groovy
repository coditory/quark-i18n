package com.coditory.quark.i18n.formats

import com.coditory.quark.i18n.I18nMessagePack
import com.coditory.quark.i18n.I18nMessagePackFactory
import com.coditory.quark.i18n.I18nSystemDefaults
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL

class IcuDateFormatSpec extends Specification {
    static final I18nMessagePack messages = I18nMessagePackFactory.emptyMessagePack()
    static final Instant instant = Instant.parse("2007-12-03T10:15:30.00Z")
    static final Date date = Date.from(instant)
    static final ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("GMT"))
    static final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    static final LocalDate localDate = LocalDate.ofInstant(instant, ZoneOffset.UTC)

    void setup() {
        I18nSystemDefaults.setupGmtAndEnUsAsDefaults()
    }

    @Unroll
    def "should format message with date format: #format"() {
        expect:
            i18nFormat(EN_US, format, value) == expectedEn
            i18nFormat(PL_PL, format, value) == expectedPl
        where:
            format                                   | value   | expectedEn                    | expectedPl
            "{0}"                                    | instant | "12/3/07, 10:15 AM"           | "3.12.2007, 10:15"
            "{0, date}"                              | instant | "Dec 3, 2007"                 | "3 gru 2007"
            "{0, date, short}"                       | instant | "12/3/07"                     | "3.12.2007"
            "{0, date, medium}"                      | instant | "Dec 3, 2007"                 | "3 gru 2007"
            "{0, date, long}"                        | instant | "December 3, 2007"            | "3 grudnia 2007"
            "{0, date, full}"                        | instant | "Monday, December 3, 2007"    | "poniedziałek, 3 grudnia 2007"
            "{0, date,yyyy-MM-dd'T'HH:mm:ss.SSS zz}" | instant | "2007-12-03T10:15:30.000 GMT" | "2007-12-03T10:15:30.000 GMT"
            "{0, date, ::dMMMM}"                     | instant | "December 3"                  | "3 grudnia"
    }

    @Unroll
    def "should format date objects of type: #value"() {
        expect:
            i18nFormat(EN_US, "{0}", value) == expected
        where:
            value         | expected
            instant       | "12/3/07, 10:15 AM"
            date          | "12/3/07, 10:15 AM"
            zonedDateTime | "12/3/07, 10:15 AM"
            localDateTime | "12/3/07, 10:15 AM"
            localDate     | "12/3/07, 12:00 AM"
    }

    @Unroll
    def "should skip registering default java time transformers"() {
        given:
            I18nMessagePack messages = I18nMessagePack.builder()
                    .disableJava8ArgumentTransformers()
                    .build()
        expect:
            messages.format(EN_US, "{0}", value) == expected
        where:
            value         | expected
            instant       | "2007-12-03T10:15:30Z"
            date          | "12/3/07, 10:15 AM"
            zonedDateTime | "2007-12-03T10:15:30Z[GMT]"
            localDateTime | "2007-12-03T10:15:30"
            localDate     | "2007-12-03"
    }

    private String i18nFormat(Locale locale, String template, Object... args) {
        return messages.format(locale, template, args)
    }
}
