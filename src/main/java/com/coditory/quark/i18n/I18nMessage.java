package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class I18nMessage {
    private final I18nKey key;
    private final MessageTemplate template;
    private final Locale locale;

    I18nMessage(Locale locale, I18nKey key, MessageTemplate template) {
        this.key = key;
        this.locale = locale;
        this.template = template;
    }

    public String resolve(@NotNull Map<String, Object> args) {
        try {
            return template.resolve(locale, args);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not resolve message "
                    + key.toShortString() + "=\"" + template.getValue()
                    + "\" with named arguments " + args + " and locale: " + locale, e);
        }
    }

    public String resolve(@NotNull Object[] args) {
        try {
            return template.resolve(locale, args);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not resolve message "
                    + key.toShortString() + "=\"" + template.getValue()
                    + "\" with indexed arguments " + Arrays.toString(args) + " and locale: " + locale, e);
        }
    }

    @Override
    public String toString() {
        return "I18nMessage{" + key.toShortString() + "(" + locale + "): " + template + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I18nMessage that = (I18nMessage) o;
        return locale.equals(that.locale) && key.equals(that.key) && template.equals(that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, key, template);
    }
}
