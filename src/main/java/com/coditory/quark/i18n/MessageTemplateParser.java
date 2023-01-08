package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nMessageBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class MessageTemplateParser {
    private final ReferenceResolver referenceResolver;
    private final ArgumentResolver argumentResolver;
    private final MessageTemplateNormalizer messageTemplateNormalizer;

    public MessageTemplateParser(ReferenceResolver referenceResolver, ArgumentResolver argumentResolver, MessageTemplateNormalizer messageTemplateNormalizer) {
        this.referenceResolver = expectNonNull(referenceResolver, "referenceResolver");
        this.argumentResolver = expectNonNull(argumentResolver, "argumentResolver");
        this.messageTemplateNormalizer = expectNonNull(messageTemplateNormalizer, "messageTemplateNormalizer");
    }

    Map<I18nKey, MessageTemplate> parseTemplates(List<I18nMessageBundle> bundles) {
        expectNonNull(bundles, "bundles");
        Map<I18nKey, MessageTemplate> result = new HashMap<>();
        for (I18nMessageBundle bundle : bundles) {
            for (Map.Entry<I18nKey, String> entry : bundle.templates().entrySet()) {
                I18nKey key = entry.getKey();
                String value = entry.getValue();
                MessageTemplate template = parseTemplate(key, value);
                result.put(key, template);
            }
        }
        return result;
    }

    MessageTemplate parseTemplate(I18nKey key, String template) {
        expectNonNull(key, "key");
        expectNonNull(template, "template");
        template = messageTemplateNormalizer.normalize(template);
        template = referenceResolver.resolveReferences(key, template);
        return MessageTemplate.parse(template, argumentResolver);
    }

    MessageTemplate parseTemplate(Locale locale, String template) {
        expectNonNull(locale, "locale");
        expectNonNull(template, "template");
        template = messageTemplateNormalizer.normalize(template);
        template = referenceResolver.resolveReferences(locale, template);
        return MessageTemplate.parse(template, argumentResolver);
    }
}
