package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.ZoneIds.parseZoneId
import static com.coditory.quark.i18n.ZoneIds.parseZoneIdOrDefault
import static com.coditory.quark.i18n.ZoneIds.parseZoneIdOrEmpty
import static com.coditory.quark.i18n.ZoneIds.parseZoneIdOrNull

class ZoneIdsSpec extends Specification {
    @Unroll
    def "should parse ZoneId value: #value"() {
        expect:
            parseZoneIdOrNull(value) == expected
        where:
            value                 || expected
            "America/Los_Angeles" || ZoneIds.PST
            "Europe/Warsaw"       || ZoneIds.Europe.Warsaw
            "UTC"                 || ZoneIds.UTC
            "Neverland/TreeHouse" || null
            "Xyz"                 || null
    }

    @Unroll
    def "should parse optional ZoneId"() {
        expect:
            parseZoneIdOrEmpty(value) == expected

        where:
            value                 || expected
            "Europe/Warsaw"       || Optional.of(ZoneIds.Europe.Warsaw)
            "Neverland/TreeHouse" || Optional.empty()
    }

    @Unroll
    def "should parse ZoneId or return default value"() {
        expect:
            parseZoneIdOrDefault(value, defaultValue) == expected
        where:
            value                 | defaultValue || expected
            "Neverland/TreeHouse" | ZoneIds.PST  || ZoneIds.PST
            "America/Los_Angeles" | ZoneIds.ACT  || ZoneIds.PST
    }

    def "should parse ZoneId or throw error"() {
        when:
            parseZoneId("America/Los_Angeles") == ZoneIds.America.LosAngeles
        then:
            noExceptionThrown()

        when:
            parseZoneId("Neverland/TreeHouse")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Could not parse ZoneId: 'Neverland/TreeHouse'"
    }
}