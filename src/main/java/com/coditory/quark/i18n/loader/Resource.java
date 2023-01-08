package com.coditory.quark.i18n.loader;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

import static java.util.Objects.requireNonNull;

record Resource(@NotNull String name, @NotNull URL url) {
    Resource(String name, URL url) {
        this.name = requireNonNull(name);
        this.url = requireNonNull(url);
    }
}
