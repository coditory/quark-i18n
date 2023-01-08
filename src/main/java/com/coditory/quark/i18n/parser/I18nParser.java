package com.coditory.quark.i18n.parser;

import com.coditory.quark.i18n.I18nKey;
import com.coditory.quark.i18n.I18nPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

@FunctionalInterface
public interface I18nParser {
    @NotNull
    Map<I18nKey, String> parse(@NotNull String content, @Nullable Locale locale);
}
