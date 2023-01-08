package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nMessageBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class TemplatesBundlePrefixes {
    static List<I18nMessageBundle> prefix(List<I18nMessageBundle> bundles) {
        expectNonNull(bundles, "bundles");
        return bundles.stream()
                .map(TemplatesBundlePrefixes::prefix)
                .toList();
    }

    static private I18nMessageBundle prefix(I18nMessageBundle bundle) {
        I18nPath prefix = bundle.prefix();
        if (prefix == null || prefix.isRoot()) {
            return bundle;
        }
        Map<I18nKey, String> mapped = new HashMap<>();
        for (Map.Entry<I18nKey, String> entry : bundle.templates().entrySet()) {
            I18nKey prefixed = entry.getKey().prefixPath(prefix);
            mapped.put(prefixed, entry.getValue());
        }
        return new I18nMessageBundle(mapped, prefix);
    }
}
