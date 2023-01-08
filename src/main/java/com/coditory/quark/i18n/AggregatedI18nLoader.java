package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nLoader;
import com.coditory.quark.i18n.loader.I18nMessageBundle;
import com.coditory.quark.i18n.loader.WatchableI18nLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

final class AggregatedI18nLoader implements WatchableI18nLoader {
    private final List<I18nLoader> loaders = new ArrayList<>();
    private final Map<I18nKey, String> currentEntries = new LinkedHashMap<>();
    private final ConcurrentHashMap<I18nLoader, List<I18nMessageBundle>> cachedResults = new ConcurrentHashMap<>();
    private final Set<I18nLoaderChangeListener> listeners = new LinkedHashSet<>();
    private boolean watching = false;

    public synchronized void addLoader(I18nLoader loader) {
        expectNonNull(loader, "loader");
        appendCurrentEntries();
        loaders.add(loader);
    }

    public synchronized void addMessage(I18nKey key, String value) {
        expectNonNull(key, "key");
        expectNonNull(value, "value");
        currentEntries.put(key, value);
    }

    public synchronized void addMessages(Map<I18nKey, String> messages) {
        expectNonNull(messages, "messages");
        currentEntries.putAll(messages);
    }

    public synchronized AggregatedI18nLoader copy() {
        appendCurrentEntries();
        AggregatedI18nLoader copy = new AggregatedI18nLoader();
        copy.loaders.addAll(loaders);
        return copy;
    }

    @Override
    public synchronized void addChangeListener(I18nLoaderChangeListener listener) {
        expectNonNull(listener, "listener");
        listeners.add(listener);
    }

    @Override
    public synchronized void startWatching() {
        if (watching) {
            throw new IllegalStateException("Loader is already watching for changes");
        }
        watching = true;
        for (I18nLoader loader : loaders) {
            if (loader instanceof WatchableI18nLoader watchableLoader) {
                watchableLoader.startWatching();
                watchableLoader.addChangeListener(bundles -> onBundlesChange(loader, bundles));
            }
        }
    }

    private synchronized void onBundlesChange(I18nLoader loader, List<I18nMessageBundle> bundles) {
        cachedResults.put(loader, bundles);
        List<I18nMessageBundle> result = loaders.stream()
                .map(l -> cachedResults.getOrDefault(l, List.of()))
                .reduce(new ArrayList<>(), (m, e) -> {
                    m.addAll(e);
                    return m;
                });
        for (I18nLoaderChangeListener listener : listeners) {
            listener.onChange(result);
        }
    }

    @Override
    public synchronized void stopWatching() {
        if (!watching) {
            return;
        }
        for (I18nLoader loader : loaders) {
            if (loader instanceof WatchableI18nLoader watchableLoader) {
                watchableLoader.stopWatching();
            }
        }
    }

    @Override
    @NotNull
    public synchronized List<I18nMessageBundle> load() {
        appendCurrentEntries();
        return loaders.stream()
                .map(this::load)
                .reduce(new ArrayList<>(), (result, e) -> {
                    result.addAll(e);
                    return result;
                });
    }

    private List<I18nMessageBundle> load(I18nLoader loader) {
        List<I18nMessageBundle> bundles = loader.load();
        cachedResults.put(loader, bundles);
        return bundles;
    }

    private void appendCurrentEntries() {
        if (!currentEntries.isEmpty()) {
            Map<I18nKey, String> copy = new LinkedHashMap<>(currentEntries);
            I18nMessageBundle templates = new I18nMessageBundle(copy);
            List<I18nMessageBundle> result = List.of(templates);
            I18nLoader loader = () -> result;
            loaders.add(loader);
            cachedResults.put(loader, result);
            currentEntries.clear();
        }
    }
}
