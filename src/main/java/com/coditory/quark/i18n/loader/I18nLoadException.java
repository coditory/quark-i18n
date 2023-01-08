package com.coditory.quark.i18n.loader;

import com.coditory.quark.i18n.I18nMessagesException;

public final class I18nLoadException extends I18nMessagesException {
    public I18nLoadException(String message) {
        super(message);
    }

    public I18nLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
