package com.coditory.quark.i18n

import com.coditory.quark.i18n.base.UsesInMemFs
import com.coditory.quark.i18n.loader.I18nFileSystemLoader
import com.coditory.quark.i18n.loader.I18nLoader
import spock.lang.Specification

import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL

class LoadI18nFromFileSystemSpec extends Specification implements UsesInMemFs {
    def "should load i18n files"() {
        given:
            writeInMemFsFile("i18n/homepage.yml", """
            homepage:
              title:
                en: Homepage
                pl: Strona domowa
            """)
            writeInMemFsFile("i18n/user.yml", """
            user:
              title:
                en: User
                pl: Użytkownik
            """)

        when:
            I18nMessagePack i18nMessagePack = scanInMemFs("i18n/*")

        then:
            I18nMessages messagesPl = i18nMessagePack.localize(PL_PL)
            messagesPl.getMessage("user.title") == "Użytkownik"
            messagesPl.getMessage("homepage.title") == "Strona domowa"

        and:
            I18nMessages messagesEn = i18nMessagePack.localize(EN_US)
            messagesEn.getMessage("user.title") == "User"
            messagesEn.getMessage("homepage.title") == "Homepage"
    }

    def "should load i18n files using file prefix"() {
        given:
            writeInMemFsFile("com/acme/homepage/i18n.yml", """
            title:
              en: Homepage
              pl: Strona domowa
            """)
            writeInMemFsFile("com/acme/user/i18n.yml", """
            title:
              en: User
              pl: Użytkownik
            """)

        when:
            I18nMessagePack i18nMessagePack = scanInMemFs("com/acme/{prefix}/i18n.yml")

        then:
            I18nMessages messagesPl = i18nMessagePack.localize(PL_PL)
            messagesPl.getMessage("user.title") == "Użytkownik"
            messagesPl.getMessage("homepage.title") == "Strona domowa"

        and:
            I18nMessages messagesEn = i18nMessagePack.localize(EN_US)
            messagesEn.getMessage("user.title") == "User"
            messagesEn.getMessage("homepage.title") == "Homepage"
    }

    def "should load a single i18n file with a static key prefix"() {
        given:
            writeInMemFsFile("abc/i18n.yml", """
            title:
              en: Homepage
              pl: Strona domowa
            """)

        when:
            I18nLoader loader = I18nFileSystemLoader.builder(inMemFs)
                    .scanPathPattern("abc/i18n.yml")
                    .staticKeyPrefix("homepage")
                    .build()
            I18nMessagePack i18nMessagePack = I18nMessagePack.builder()
                    .addLoader(loader)
                    .build()

        then:
            I18nMessages messagesPl = i18nMessagePack.localize(PL_PL)
            messagesPl.getMessage("homepage.title") == "Strona domowa"

        and:
            I18nMessages messagesEn = i18nMessagePack.localize(EN_US)
            messagesEn.getMessage("homepage.title") == "Homepage"
    }

    def "should load i18n files using locale from file path"() {
        given:
            writeInMemFsFile("com/acme/homepage/i18n-pl.yml", "homepage.title: Strona domowa")
            writeInMemFsFile("com/acme/homepage/i18n-en.yml", "homepage.title: Homepage")
            writeInMemFsFile("com/acme/user/i18n-pl.yml", "user.title: Użytkownik")
            writeInMemFsFile("com/acme/user/i18n-en.yml", "user.title: User")

        when:
            I18nMessagePack i18nMessagePack = scanInMemFs("com/acme/**/i18n-{locale}.yml")

        then:
            I18nMessages messagesPl = i18nMessagePack.localize(PL_PL)
            messagesPl.getMessage("user.title") == "Użytkownik"
            messagesPl.getMessage("homepage.title") == "Strona domowa"

        and:
            I18nMessages messagesEn = i18nMessagePack.localize(EN_US)
            messagesEn.getMessage("user.title") == "User"
            messagesEn.getMessage("homepage.title") == "Homepage"
    }

    def "should load i18n files using locale and prefix from files path"() {
        given:
            writeInMemFsFile("com/acme/homepage/i18n-pl.yml", "title: Strona domowa")
            writeInMemFsFile("com/acme/homepage/i18n-en.yml", "title: Homepage")
            writeInMemFsFile("com/acme/user/i18n-pl.yml", "title: Użytkownik")
            writeInMemFsFile("com/acme/user/i18n-en.yml", "title: User")

        when:
            I18nMessagePack i18nMessagePack = scanInMemFs("com/acme/{prefix}/i18n-{locale}.yml")

        then:
            I18nMessages messagesPl = i18nMessagePack.localize(PL_PL)
            messagesPl.getMessage("user.title") == "Użytkownik"
            messagesPl.getMessage("homepage.title") == "Strona domowa"

        and:
            I18nMessages messagesEn = i18nMessagePack.localize(EN_US)
            messagesEn.getMessage("user.title") == "User"
            messagesEn.getMessage("homepage.title") == "Homepage"
    }
}
