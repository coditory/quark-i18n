package com.coditory.quark.i18n.formatter;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface I18nFormatter {
    @NotNull
    String format(@NotNull Object value);
}
