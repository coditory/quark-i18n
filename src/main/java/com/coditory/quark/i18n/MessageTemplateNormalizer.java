package com.coditory.quark.i18n;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class MessageTemplateNormalizer {
    private final boolean normalizeWhiteSpaces;

    MessageTemplateNormalizer(boolean normalizeWhiteSpaces) {
        this.normalizeWhiteSpaces = normalizeWhiteSpaces;
    }

    String normalize(String template) {
        expectNonNull(template, "template");
        if (!normalizeWhiteSpaces) {
            return template;
        }
        return template.trim()
                .replaceAll("\\s+", " ");
    }
}
