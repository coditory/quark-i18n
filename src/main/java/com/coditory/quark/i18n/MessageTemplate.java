package com.coditory.quark.i18n;

import com.ibm.icu.text.MessageFormat;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.coditory.quark.i18n.ArgumentIndexExtractor.extractArgumentIndexes;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class MessageTemplate {
    static MessageTemplate parse(String template, ArgumentResolver argumentResolver) {
        expectNonNull(template, "template");
        expectNonNull(argumentResolver, "argumentResolver");
        try {
            MessageFormat messageFormat = new MessageFormat(template);
            return new MessageTemplate(template, messageFormat, argumentResolver);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse message template: " + template, e);
        }
    }

    private final ConcurrentHashMap<Locale, MessageFormat> formats = new ConcurrentHashMap<>();
    private final ArgumentResolver argumentResolver;
    private final String template;
    private final MessageFormat messageFormat;
    private final boolean dynamic;
    private final Set<String> usedArgumentNames;
    private final Set<Integer> usedArgumentIndexes;

    private MessageTemplate(String template, MessageFormat messageFormat, ArgumentResolver argumentResolver) {
        this.template = expectNonNull(template, "template");
        this.messageFormat = expectNonNull(messageFormat, "messageFormat");
        this.argumentResolver = expectNonNull(argumentResolver, "argumentResolver");
        this.usedArgumentNames = messageFormat.usesNamedArguments()
                ? Set.copyOf(messageFormat.getArgumentNames())
                : Set.of();
        this.usedArgumentIndexes = messageFormat.usesNamedArguments()
                ? Set.of()
                : extractArgumentIndexes(template);
        this.dynamic = !usedArgumentNames.isEmpty() || !usedArgumentIndexes.isEmpty();
    }

    public String resolve(Locale locale, Object[] args) {
        expectNonNull(locale, "locale");
        expectNonNull(args, "args");
        MessageFormat messageFormat = getMessageFormat(locale);
        Object[] resolvedArgs = argumentResolver.resolveArguments(args, usedArgumentIndexes);
        return messageFormat.format(resolvedArgs);
    }

    public String resolve(Locale locale, Map<String, Object> args) {
        expectNonNull(locale, "locale");
        expectNonNull(args, "args");
        MessageFormat messageFormat = getMessageFormat(locale);
        Map<String, Object> resolvedArgs = argumentResolver.resolveArguments(args, usedArgumentNames);
        return messageFormat.format(resolvedArgs);
    }

    private MessageFormat getMessageFormat(Locale locale) {
        return dynamic
                ? formats.computeIfAbsent(locale, this::createMessageFormat)
                : messageFormat;
    }

    private MessageFormat createMessageFormat(Locale locale) {
        MessageFormat copy = (MessageFormat) this.messageFormat.clone();
        copy.setLocale(locale);
        return copy;
    }

    public String getValue() {
        return template;
    }

    @Override
    public String toString() {
        return "MessageTemplate{" + template + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageTemplate that = (MessageTemplate) o;
        return template.equals(that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template);
    }
}