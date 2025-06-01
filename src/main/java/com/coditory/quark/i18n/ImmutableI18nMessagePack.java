package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class ImmutableI18nMessagePack implements I18nMessagePack {
    private final Map<I18nKey, MessageTemplate> templates;
    private final MessageTemplateParser parser;
    private final I18nMissingMessageHandler unresolvedMessageHandler;
    private final I18nKeyGenerator keyGenerator;

    ImmutableI18nMessagePack(
            Map<I18nKey, MessageTemplate> templates,
            MessageTemplateParser parser,
            I18nMissingMessageHandler unresolvedMessageHandler,
            I18nKeyGenerator keyGenerator
    ) {
        expectNonNull(templates, "templates");
        this.templates = Map.copyOf(templates);
        this.parser = expectNonNull(parser, "parser");
        this.unresolvedMessageHandler = expectNonNull(unresolvedMessageHandler, "unresolvedMessageHandler");
        this.keyGenerator = expectNonNull(keyGenerator, "keyGenerator");
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
                ? unresolvedMessageHandler.onUnresolvedMessage(key, keyGenerator.keys(key), args)
                : result;
    }

    @NotNull
    @Override
    public String getMessage(@NotNull I18nKey key, @NotNull Map<String, Object> args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        String result = getMessageOrNull(key, args);
        return result == null
                ? unresolvedMessageHandler.onUnresolvedMessageWithNamedArguments(key, keyGenerator.keys(key), args)
                : result;
    }

    @Override
    @Nullable
    public String getMessageOrNull(@NotNull I18nKey key, Object... args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        return getOptionalTemplate(key)
                .map(message -> message.resolve(args))
                .orElse(null);
    }

    @Override
    @Nullable
    public String getMessageOrNull(@NotNull I18nKey key, @NotNull Map<String, Object> args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        return getOptionalTemplate(key)
                .map(message -> message.resolve(args))
                .orElse(null);
    }

    @Override
    @NotNull
    public I18nMessage getTemplate(@NotNull I18nKey key) {
        return getOptionalTemplate(key).orElse(null);
    }

    @Override
    @Nullable
    public I18nMessage getTemplateOrNull(@NotNull I18nKey key) {
        return getOptionalTemplate(key).orElse(null);
    }

    private Optional<I18nMessage> getOptionalTemplate(I18nKey key) {
        return keyGenerator.keys(key).stream()
                .filter(templates::containsKey)
                .map(matched -> new I18nMessage(key.locale(), matched, templates.get(matched)))
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
                    + "\" with indexed arguments " + Arrays.toString(args) + " and locale: " + locale, e);
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
                    + "\" with named arguments " + args + " and locale: " + locale, e);
        }
    }

    @Override
    public @NotNull I18nMessagePack prefixQueries(@NotNull I18nPath prefix) {
        expectNonNull(prefix, "prefix");
        I18nKeyGenerator updated = keyGenerator.prefixQueries(prefix);
        return new ImmutableI18nMessagePack(templates, parser, unresolvedMessageHandler, updated);
    }

    @Override
    @NotNull
    public I18nMessagePack withQueryPrefixes(@NotNull List<I18nPath> prefixes) {
        expectNonNull(prefixes, "prefixes");
        I18nKeyGenerator updated = keyGenerator.withPrefixes(prefixes);
        return new ImmutableI18nMessagePack(templates, parser, unresolvedMessageHandler, updated);
    }
}
