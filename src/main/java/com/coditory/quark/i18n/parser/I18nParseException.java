package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nMessagesException;

public final class I18nParseException extends I18nMessagesException {
    I18nParseException(String message) {
        super(message);
    }

    I18nParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
