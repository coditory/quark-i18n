package com.coditory.quark.i18n

import ch.qos.logback.classic.Logger
import com.coditory.quark.i18n.base.CapturingAppender
import org.slf4j.LoggerFactory
import spock.lang.Specification

import static com.coditory.quark.i18n.Locales.DE_DE
import static com.coditory.quark.i18n.Locales.EN
import static com.coditory.quark.i18n.Locales.EN_GB
import static com.coditory.quark.i18n.Locales.EN_US
import static com.coditory.quark.i18n.Locales.PL_PL

class MissingMessageDetectionSpec extends Specification {
    CapturingAppender appender = new CapturingAppender()

    void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(appender)
    }

    void cleanup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.detachAppender(appender)
    }

    def "should detect missing message"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN_US, "hello", "Hello")
                    .addMessage(PL_PL, "hello", "Cześć")
                    .addMessage(DE_DE, "bye", "Tschüss")
                    .logMissingMessages()
                    .build()
        then:
            missingMessageReport() == """
            Missing Messages
            ================
               Path: bye
            Missing: en_US, pl_PL
            Sources: de_DE
            
               Path: hello
            Missing: de_DE
            Sources: en_US, pl_PL
            
            Total: 2""".stripIndent().trim()
    }

    def "should detect missing message from the same language and different countries"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN_US, "hello", "hello")
                    .addMessage(EN_GB, "bye", "bye")
                    .addMessage(EN, "other", "other")
                    .logMissingMessages()
                    .build()
        then:
            missingMessageReport() == """
            Missing Messages
            ================
               Path: bye
            Missing: en, en_US
            Sources: en_GB
             
               Path: hello
            Missing: en, en_GB
            Sources: en_US
             
            Total: 2""".stripIndent().trim()
    }

    def "should print report and throw error on missing messages"() {
        given:
            I18nMissingMessagesDetector detector = I18nMissingMessagesDetector.builder()
                    .throwErrorOnMissingMessages()
                    .logMissingMessages()
                    .build()
        when:
            I18nMessagePack.builder()
                    .addMessage(EN_US, "hello", "Hello US")
                    .addMessage(PL_PL, "bye", "Narka")
                    .detectMissingMessages(detector)
                    .build()
        then:
            IllegalStateException e = thrown(IllegalStateException)
            e.getMessage() == "Detected missing messages: 2"

        and:
            missingMessageReport() == """
            Missing Messages
            ================
               Path: bye
            Missing: en_US
            Sources: pl_PL
            
               Path: hello
            Missing: pl_PL
            Sources: en_US
            
            Total: 2""".stripIndent().trim()
    }

    def "should detect missing message in lang-only locale when all country based locales are valid"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN, "bye", "Bye")
                    .addMessage(EN_US, "hello", "Hello US")
                    .addMessage(EN_GB, "hello", "Hello GB")
                    .addMessage(PL_PL, "hello", "Cześć")
                    .logMissingMessages()
                    .build()
        then:
            missingMessageReport() == """
            Missing Messages
            ================
               Path: bye
            Missing: pl_PL
            Sources: en

               Path: hello
            Missing: en
            Sources: en_GB, en_US, pl_PL
            
            Total: 2""".stripIndent().trim()
    }

    def "should detect no missing message"() {
        when:
            I18nMessagePack.builder()
                    .addMessage(EN_US, "hello", "Hello US")
                    .addMessage(EN_GB, "hello", "Hello GB")
                    .addMessage(PL_PL, "hello", "Cześć")
                    .logMissingMessages()
                    .build()
        then:
            missingMessageReport() == ""
    }

    def "should skip validating messages by path prefix: #skipPath"() {
        given:
            I18nMissingMessagesDetector detector = I18nMissingMessagesDetector.builder()
                    .skipPath(skipPath)
                    .logMissingMessages()
                    .build()
        when:
            I18nMessagePack.builder()
                    .addMessage(EN_US, "a.b.c.d", "MISSING")
                    .addMessage(EN_US, "x", "X")
                    .addMessage(EN_GB, "x", "X")
                    .addMessage(PL_PL, "x", "X")
                    .detectMissingMessages(detector)
                    .build()
        then:
            missingMessageReport() == ""

        where:
            skipPath << [
                    "a.b.c.d",
                    "a.b.c.*",
                    "a.b.**",
                    "a.**",
                    "a.**.d",
                    "**.d",
                    "*.*.*.*"
            ]
    }

    String missingMessageReport() {
        List<String> logs = appender.getLogsByMessagePrefix("\nMissing Messages")
        if (logs.isEmpty()) {
            return ""
        }
        return logs.get(0)
                .replaceAll("\\[WARN\\]", "")
                .trim()
    }
}
