package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class ImmutableI18nMessagePack implements I18nMessagePack {
    private final Map<I18nKey, MessageTemplate> templates;
    private final MessageTemplateParser parser;
    private final I18nMissingMessageHandler unresolvedMessageHandler;
    private final I18nPath queryPrefix;
    private final I18nKeyGenerator keyGenerator;

    ImmutableI18nMessagePack(
            Map<I18nKey, MessageTemplate> templates,
            MessageTemplateParser parser,
            I18nMissingMessageHandler unresolvedMessageHandler,
            I18nKeyGenerator keyGenerator
    ) {
        this(templates, parser, unresolvedMessageHandler, keyGenerator, null);
    }

    private ImmutableI18nMessagePack(
            Map<I18nKey, MessageTemplate> templates,
            MessageTemplateParser parser,
            I18nMissingMessageHandler unresolvedMessageHandler,
            I18nKeyGenerator keyGenerator,
            I18nPath queryPrefix
    ) {
        expectNonNull(templates, "templates");
        this.templates = Map.copyOf(templates);
        this.parser = expectNonNull(parser, "parser");
        this.unresolvedMessageHandler = expectNonNull(unresolvedMessageHandler, "unresolvedMessageHandler");
        this.keyGenerator = expectNonNull(keyGenerator, "keyGenerator");
        this.queryPrefix = queryPrefix;
    }

    @NotNull
    @Override
    public I18nMessages localize(@NotNull Locale locale) {
        expectNonNull(locale, "locale");
        return new I18nMessages(this, locale);
    }

    @NotNull
    @Override
    public String getMessage(@NotNull I18nKey key, Object... args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        String result = getMessageOrNull(key, args);
        return result == null
                ? unresolvedMessageHandler.onUnresolvedMessage(key, args)
                : result;
    }

    @NotNull
    @Override
    public String getMessage(@NotNull I18nKey key, @NotNull Map<String, Object> args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        String result = getMessageOrNull(key, args);
        return result == null
                ? unresolvedMessageHandler.onUnresolvedMessageWithNamedArguments(key, args)
                : result;
    }

    @Override
    @Nullable
    public String getMessageOrNull(@NotNull I18nKey key, Object... args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        return getTemplate(key)
                .map(message -> message.resolve(key.locale(), args))
                .orElse(null);
    }

    @Override
    @Nullable
    public String getMessageOrNull(@NotNull I18nKey key, @NotNull Map<String, Object> args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        return getTemplate(key)
                .map(message -> message.resolve(key.locale(), args))
                .orElse(null);
    }

    private Optional<MessageTemplateWithKey> getTemplate(I18nKey key) {
        return keyGenerator.keys(key, queryPrefix).stream()
                .filter(templates::containsKey)
                .map(matched -> new MessageTemplateWithKey(matched, templates.get(matched)))
                .findFirst();
    }

    @NotNull
    @Override
    public String format(@NotNull Locale locale, @NotNull String template, Object... args) {
        expectNonNull(locale, "locale");
        expectNonNull(template, "template");
        expectNonNull(args, "args");
        try {
            return parser.parseTemplate(locale, template)
                    .resolve(locale, args);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not format message " + template
                    + "\" with indexed arguments " + Arrays.toString(args) + " and locale: " + locale);
        }
    }

    @NotNull
    @Override
    public String format(@NotNull Locale locale, @NotNull String template, @NotNull Map<String, Object> args) {
        expectNonNull(locale, "locale");
        expectNonNull(template, "template");
        expectNonNull(args, "args");
        try {
            return parser.parseTemplate(locale, template)
                    .resolve(locale, args);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not format message "
                    + template
                    + "\" with named arguments " + args + " and locale: " + locale);
        }
    }

    @Override
    @NotNull
    public I18nMessagePack prefixQueries(@NotNull I18nPath prefix) {
        expectNonNull(prefix, "prefix");
        return new ImmutableI18nMessagePack(templates, parser, unresolvedMessageHandler, keyGenerator, prefix);
    }

    private record MessageTemplateWithKey(I18nKey key, MessageTemplate template) {
        String resolve(Locale locale, @NotNull Map<String, Object> args) {
            try {
                return template.resolve(locale, args);
            } catch (Throwable e) {
                throw new IllegalArgumentException("Could not resolve message "
                        + key.toShortString() + "=\"" + template.getValue()
                        + "\" with named arguments " + args + " and locale: " + locale, e);
            }
        }

        String resolve(Locale locale, @NotNull Object[] args) {
            try {
                return template.resolve(locale, args);
            } catch (Throwable e) {
                throw new IllegalArgumentException("Could not resolve message "
                        + key.toShortString() + "=\"" + template.getValue()
                        + "\" with indexed arguments " + Arrays.toString(args) + " and locale: " + locale, e);
            }
        }
    }
}
