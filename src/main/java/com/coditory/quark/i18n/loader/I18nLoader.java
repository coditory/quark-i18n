package com.coditory.quark.i18n.loader;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface I18nLoader {
    @NotNull
    List<I18nMessageBundle> load();
}
