package com.coditory.quark.i18n

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.ArgumentIndexExtractor.extractArgumentIndexes

class ArgumentIndexExtractorSpec extends Specification {
    @Unroll
    def "should extract argument indexes from: #template"() {
        when:
            List<Integer> indexes = extractArgumentIndexes(template).toList()
            indexes.sort()
        then:
            indexes == expected
        where:
            template             || expected
            "{0}"                || [0]
            "{0,number,integer}" || [0]
            "{ 0 , number }"     || [0]
            "{ 10 , number }"    || [10]
            "{ 0 } {10}"         || [0, 10]
            "'{abc '{0}}"        || [0]
            "'{abc {'{0}} }"     || [0]
    }

    @Unroll
    def "should extract no argument indexes from: #template"() {
        when:
            List<Integer> indexes = extractArgumentIndexes(template).toList()
        then:
            indexes == []
        where:
            template << [
                    "",
                    "0",
                    "{named}",
                    "{0named}",
                    "{0 named}",
                    "'{0}",
                    "'{abc {0}}",
                    "'{abc {} { {0}} {}}"
            ]
    }
}
