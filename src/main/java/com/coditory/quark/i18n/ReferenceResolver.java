package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nMessageBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.Collections.unmodifiableMap;

final class ReferenceResolver {
    private final Map<I18nKey, I18nMessageBundle> bundles;
    private final Map<I18nKey, String> templates;
    private final I18nKeyGenerator keyGenerator;
    private final boolean resolveReferences;

    ReferenceResolver(List<I18nMessageBundle> bundles, I18nKeyGenerator keyGenerator, boolean resolveReferences) {
        expectNonNull(bundles, "bundles");
        expectNonNull(keyGenerator, "keyGenerator");
        Map<I18nKey, String> templates = new HashMap<>();
        Map<I18nKey, I18nMessageBundle> bundlesByKey = new HashMap<>();
        for (I18nMessageBundle templateEntry : bundles) {
            templates.putAll(templateEntry.templates());
            templateEntry.templates().keySet()
                    .forEach(key -> bundlesByKey.put(key, templateEntry));
        }
        this.keyGenerator = expectNonNull(keyGenerator, "keyGenerator");
        this.templates = unmodifiableMap(templates);
        this.bundles = unmodifiableMap(bundlesByKey);
        this.resolveReferences = resolveReferences;
    }

    String resolveReferences(I18nKey key, String template) {
        expectNonNull(key, "key");
        expectNonNull(template, "template");
        return resolveReferences(key.locale(), key.path(), template, 0);
    }

    String resolveReferences(Locale locale, String template) {
        expectNonNull(locale, "locale");
        expectNonNull(template, "template");
        return resolveReferences(locale, null, template, 0);
    }

    private String resolveReferences(Locale locale, I18nPath path, String template, int iteration) {
        if (!resolveReferences) {
            return template;
        }
        if (!template.contains("$")) {
            return template;
        }
        if (iteration + 1 > 10) {
            throw new IllegalArgumentException("Detected potential cyclic reference");
        }
        StringBuilder result = new StringBuilder();
        StringBuilder reference = new StringBuilder();
        boolean modified = false;
        boolean dollar = false;
        boolean brace = false;
        boolean escaped = false;
        for (PrimitiveIterator.OfInt it = template.codePoints().iterator(); it.hasNext(); ) {
            int c = it.next();
            if (c == '\\') {
                escaped = true;
            } else if (escaped) {
                if (c != '$') {
                    result.append('\\');
                } else {
                    modified = true;
                }
                result.append(c);
                escaped = false;
            } else if (c == '$') {
                modified = true;
                dollar = true;
            } else if (c == '{' && brace) {
                throw new IllegalArgumentException("Duplicated '{'");
            } else if (c == '{' && dollar && !reference.isEmpty()) {
                throw new IllegalArgumentException("Unexpected '{'");
            } else if (c == '{' && dollar) {
                brace = true;
            } else if ((c == '}' && brace) || (dollar && !brace && !isReferenceChar(c)) || (!it.hasNext() && dollar && !brace)) {
                if (!it.hasNext() && isReferenceChar(c)) {
                    reference.appendCodePoint(c);
                }
                I18nPath referencePath = I18nPath.of(reference.toString());
                String referenceTemplate = resolveReference(locale, path, referencePath);
                result.append(referenceTemplate);
                if (c != '}' && it.hasNext()) {
                    result.appendCodePoint(c);
                }
                reference = new StringBuilder();
                dollar = false;
                brace = false;
            } else if (dollar) {
                if (isReferenceChar(c)) {
                    reference.appendCodePoint(c);
                }
            } else {
                result.appendCodePoint(c);
            }
        }
        if (dollar || brace) {
            throw new IllegalArgumentException("Invalid reference");
        }
        if (escaped) {
            result.append('\\');
        }
        return modified
                ? resolveReferences(locale, path, result.toString(), iteration + 1)
                : template;
    }

    private boolean isReferenceChar(int codePoint) {
        return codePoint == '-' || codePoint == '_' || codePoint == '.' || Character.isLetterOrDigit(codePoint);
    }

    private String resolveReference(Locale locale, I18nPath sourcePath, I18nPath referencePath) {
        I18nKey referenceKey = I18nKey.of(locale, referencePath);
        I18nMessageBundle bundle = sourcePath != null
                ? bundles.get(I18nKey.of(locale, sourcePath))
                : null;
        List<I18nKey> keys = bundle != null && bundle.prefix() != null
                ? keyGenerator.keys(referenceKey, bundle.prefix())
                : keyGenerator.keys(referenceKey);
        return keys.stream()
                .map(templates::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new I18nMessagesException("Missing reference: " + referencePath));
    }
}
