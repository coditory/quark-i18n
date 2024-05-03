package com.coditory.quark.i18n.loader

import com.coditory.quark.i18n.I18nPath
import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.i18n.Locales.EN
import static com.coditory.quark.i18n.Locales.EN_GB
import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL
import static com.coditory.quark.i18n.loader.I18nPathPattern.I18nPathGroups
import static com.coditory.quark.i18n.loader.I18nPathPattern.of

class I18nPathPatternSpec extends Specification {
    @Unroll
    def "should match file path #input with pattern: #pattern"() {
        expect:
            matches(pattern, input)
        where:
            pattern                              | input
            // absolute
            "/abc/**/*.json"                     | "/abc/xxx.json"
            "/abc/**/*.json"                     | "/abc/def/xxx.json"
            "/abc/**/*.json"                     | "/abc/def/ghi/xxx.json"
            "/abc/*.json"                        | "/abc/def.json"
            // relative
            "abc/**/*.json"                      | "abc/xxx.json"
            "abc/**/*.json"                      | "abc/def/xxx.json"
            "abc/**/*.json"                      | "abc/def/ghi/xxx.json"
            "abc/*.json"                         | "abc/def.json"
            // no prefix
            "**/*.json"                          | "xxx.json"
            "**/*.json"                          | "abc/def/xxx.json"
            "**/*.json"                          | "/abc/xxx.json"
            // one star
            "/abc/*.json"                        | "/abc/.json"
            "/abc/x*.json"                       | "/abc/x.json"
            "/abc/x*.json"                       | "/abc/xyz.json"
            "/abc/x*z.json"                      | "/abc/xz.json"
            "/abc/x*z.json"                      | "/abc/xyyyz.json"
            "/abc/x*.json"                       | "/abc/x.json"
            "/abc/*.json"                        | "/abc/x.json.json"
            "/abc/*.*.json"                      | "/abc/x.xml.json"
            // capturing groups
            "/abc/{prefixes}/i18n.json"          | "/abc/i18n.json"
            "/abc/{prefixes}/*.json"             | "/abc/def/xxx.json"
            "{prefixes}/*.json"                  | "/abc/def/xxx.json"
            "/abc/**/i18n_{locale}.json"         | "/abc/def/i18n_pl-PL.json"
            "/abc/**/i18n_{lang}-{country}.json" | "/abc/def/i18n_pl-PL.json"
            "/abc/**/{locale}/{prefix}.json"     | "/abc/def/pl-PL/homepage.json"
    }

    @Unroll
    def "should not match file path #input with pattern: #pattern"() {
        expect:
            !matches(pattern, input)
        where:
            pattern                              | input
            "/abc/**/*.json"                     | "/abc.json"
            "/abc/**/*.json"                     | "/abc/def/xxx.json2"
            "/abc/**/*.json"                     | "abc/def/xxx.json2"
            "abc/**/*.json"                      | "/abc/xxx.json"
            "abc/*.json"                         | "abc/def/xxx.json"
            "abc/*.json"                         | "abc/xxx.json2"
            "/abc/**/x*.json"                    | "/abc/def/yyy.json"
            "/abc/**/x*z.json"                   | "/abc/def/xyzy.json"
            "/abc/**/x*.json"                    | "/abc/def/yx.json"
            // capturing groups
            "/abc/{prefixes}/*.json"             | "/abc/def/xxx.json2"
            "/abc/**/i18n_{locale}.json"         | "/abc/def/i18n_pl-pl.json"
            "/abc/**/i18n_{locale}.json"         | "/abc/def/i18n_pl_PL.json"
            "/abc/**/i18n_{lang}-{country}.json" | "/abc/def/i18n_plll-PL.json"
            "/abc/**/i18n_{lang}-{country}.json" | "/abc/def/i18n_pl-PLL.json"
            "/abc/**/{locale}/{prefix}.json"     | "/abc/def/pl--PL/homepage.json"
    }

    @Unroll
    def "should throw validation exception for pattern: #pattern"() {
        when:
            matches(pattern, "input")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message.startsWith(message)
        where:
            pattern                      || message
            "/ab**c"                     || "Invalid dir placeholder (**)"
            "/ab{prefixes}c"             || "Prefixes group (\"{prefixes}\") should be followed by a '/'"
            "/abc/"                      || "Expected path to not end with /"
            "/abc\\xxx.json"             || "Expected unix file separators"
            "[a-z].json"                 || "Invalid character '['"
            "{a-z}.json"                 || "Found undefined group name in: {a-z}.json"
            "a+.json"                    || "Invalid character '+'"
            "{}.json"                    || "Found undefined group name in: {}.json"
            "{locale}}.json"             || "Found unmatched brace in path: {locale}}.json"
            "{locale}-{locale}.json"     || "Expected at most one {locale}"
            "{lang}-{lang}.json"         || "Expected at most one {lang}"
            "{country}-{country}.json"   || "Expected at most one {country}"
            "{prefix}-{prefix}.json"     || "Expected at most one {prefix}"
            "{prefixes}-{prefixes}.json" || "Expected at most one {prefixes}"
            "{locale}-{lang}.json"       || "Expected either {locale} or {lang} with optional {country}"
            "/***/x.json"                || "Too many stars"
            "{country}/abc.json"         || "Missing {lang} to match {country}"
    }

    @Unroll
    def "should extract path pattern groups: #pattern"() {
        when:
            I18nPathGroups matched = matchGroups(pattern, input)
        then:
            matched.locale() == locale
            matched.path() == path
        where:
            pattern                         | input                          || locale | path
            "**/i18n-{prefix}-{locale}.yml" | "/abc/i18n-homepage-pl-PL.yml" || PL_PL  | I18nPath.of("homepage")
            "{prefixes}/i18n-{locale}.yml"  | "/abc/i18n-pl-PL.yml"          || PL_PL  | I18nPath.of("abc")
    }

    @Unroll
    def "should extract key locale from path pattern: #pattern"() {
        when:
            I18nPathGroups matched = matchGroups(pattern, input)
        then:
            matched.locale() == locale
        where:
            pattern                     | input            || locale
            "i18n_{locale}.yml"         | "i18n_en.yml"    || EN
            "i18n_{locale}.yml"         | "i18n_en-US.yml" || EN_US
            "{locale}/i18n.yml"         | "en-US/i18n.yml" || EN_US
            "i18n_{lang}-{country}.yml" | "i18n_en-US.yml" || EN_US
            "{lang}/{country}/i18n.yml" | "en/GB/i18n.yml" || EN_GB
            "{lang}/*/i18n.yml"         | "en/GB/i18n.yml" || EN
    }

    @Unroll
    def "should extract key prefix from path pattern: #pattern"() {
        when:
            I18nPathGroups matched = matchGroups(pattern, input)
        then:
            matched.path() == (path == null ? null : I18nPath.of(path))
        where:
            pattern                                 | input                            || path
            "**/i18n-{prefix}.yml"                  | "/abc/i18n-homepage.yml"         || "homepage"
            "/i18n/{prefix}.yml"                    | "/i18n/glossary.yml"             || "glossary"
            "com/i18n/{prefixes}/i18n.yml"          | "com/i18n/i18n.yml"              || null
            "com/i18n/{prefixes}/i18n.yml"          | "com/i18n/abc/def/i18n.yml"      || "abc.def"
            "com/i18n/{prefixes}/i18n-{prefix}.yml" | "com/i18n/abc/def/i18n-base.yml" || "abc.def.base"
            "com/i18n/{prefixes}/xxx/i18n.yml"      | "com/i18n/abc/def/xxx/i18n.yml"  || "abc.def"
            "com/i18n/{prefix}/xxx/i18n.yml"        | "com/i18n/base/xxx/i18n.yml"     || "base"
            "com/{prefix}/xxx/{prefixes}/i18n.yml"  | "com/abc/xxx/def/ghi/i18n.yml"   || "abc.def.ghi"
            "com/{prefix}/{prefixes}/i18n.yml"      | "com/abc/def/ghi/i18n.yml"       || "abc.def.ghi"
    }

    @Unroll
    def "should extract base directory from path pattern: #pattern"() {
        when:
            I18nPathPattern pathPattern = I18nPathPattern.of(pattern)
        then:
            pathPattern.getBaseDirectory() == baseDir
        where:
            pattern                          || baseDir
            "**/i18n-{prefix}.yml"           || ""
            "/i18n/{prefix}.yml"             || "/i18n"
            "com/i18n/{prefixes}/*/i18n.yml" || "com/i18n"
            "com/i18n/*/{prefixes}/i18n.yml" || "com/i18n"
            "/com/i18n/**/i18n.yml"          || "/com/i18n"
            "/com/i18n/*/i18n.yml"           || "/com/i18n"
            "/com/i18n/abc-*/i18n.yml"       || "/com/i18n"
    }

    private I18nPathGroups matchGroups(String pattern, String path) {
        return of(pattern).matchGroups(path)
    }

    private boolean matches(String pattern, String path) {
        return of(pattern).matches(path)
    }
}
