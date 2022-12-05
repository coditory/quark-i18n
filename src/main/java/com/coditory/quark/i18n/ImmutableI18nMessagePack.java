package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.stream.Collectors.toMap;

final class ImmutableI18nMessagePack implements I18nMessagePack {
    private final Map<I18nKey, MessageTemplate> templates;
    private final MessageTemplateFormatter templateFormatter;
    private final I18nUnresolvedMessageHandler unresolvedMessageHandler;
    private final I18nKeyGenerator keyGenerator;
    private final List<I18nPath> prefixes;

    ImmutableI18nMessagePack(
            Map<I18nKey, MessageTemplate> templates,
            MessageTemplateFormatter templateFormatter,
            I18nUnresolvedMessageHandler unresolvedMessageHandler,
            I18nKeyGenerator keyGenerator,
            List<I18nPath> prefixes
    ) {
        expectNonNull(templates, "templates");
        expectNonNull(templateFormatter, "templateFormatter");
        expectNonNull(unresolvedMessageHandler, "unresolvedMessageHandler");
        expectNonNull(keyGenerator, "keyGenerator");
        expectNonNull(prefixes, "prefixes");
        this.templates = Map.copyOf(templates);
        this.templateFormatter = templateFormatter;
        this.unresolvedMessageHandler = unresolvedMessageHandler;
        this.keyGenerator = keyGenerator;
        this.prefixes = List.copyOf(prefixes);
    }

    @NotNull
    @Override
    public I18nMessages localize(@NotNull Locale locale) {
        expectNonNull(locale, "locale");
        return new I18nMessages(this, locale);
    }

    @NotNull
    @Override
    public String getMessage(@NotNull Locale locale, @NotNull String key, Object... args) {
        expectNonNull(locale, "locale");
        expectNonBlank(key, "key");
        expectNonNull(args, "args");
        I18nKey messageKey = I18nKey.of(locale, key);
        return keyGenerator.keys(prefixes, messageKey)
                .stream()
                .map(templates::get)
                .filter(Objects::nonNull)
                .map(message -> message.format(args))
                .findFirst()
                .orElseGet(() -> unresolvedMessageHandler.onUnresolvedMessage(messageKey, args));
    }

    @NotNull
    @Override
    public String format(@NotNull Locale locale, @NotNull String template, Object... args) {
        expectNonNull(locale, "locale");
        expectNonNull(template, "template");
        expectNonNull(args, "args");
        return templateFormatter.format(locale, template, args);
    }

    @Override
    @NotNull
    public I18nMessagePack addPrefix(@NotNull String prefix) {
        I18nPath path = I18nPath.of(prefix);
        List<I18nPath> prefixes = new ArrayList<>(this.prefixes);
        prefixes.add(path);
        return new ImmutableI18nMessagePack(templates, templateFormatter, unresolvedMessageHandler, keyGenerator, prefixes);
    }
}
