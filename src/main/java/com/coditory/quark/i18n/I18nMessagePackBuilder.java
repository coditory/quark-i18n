package com.coditory.quark.i18n;

import com.coditory.quark.i18n.loader.I18nClassPathLoader;
import com.coditory.quark.i18n.loader.I18nFileSystemLoader;
import com.coditory.quark.i18n.loader.I18nLoader;
import com.coditory.quark.i18n.loader.I18nMessageBundle;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.coditory.quark.i18n.I18nArgTransformers.javaTimeI18nArgTransformers;
import static com.coditory.quark.i18n.Preconditions.expectNonBlank;
import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public final class I18nMessagePackBuilder {
    private final AggregatedI18nLoader loader = new AggregatedI18nLoader();
    private final List<I18nPath> referenceFallbackPaths = new ArrayList<>();
    private final List<I18nPath> messageFallbackPaths = new ArrayList<>();
    private final List<I18nArgTransformer<?>> argTransformers = new ArrayList<>();
    private I18nMissingMessageHandler missingMessageHandler = I18nMissingMessageHandler.errorThrowingHandler();
    private Locale defaultLocale;
    private boolean transformJava8TimeTypes = true;
    private boolean normalizeWhitespaces = false;
    private boolean resolveReferences = true;
    private I18nMissingMessagesDetector missingMessagesDetector;

    I18nMessagePackBuilder() {
        // package protected constructor
    }

    private I18nMessagePackBuilder copy() {
        I18nMessagePackBuilder builder = new I18nMessagePackBuilder();
        builder.loader.addLoader(loader.copy());
        builder.referenceFallbackPaths.addAll(referenceFallbackPaths);
        builder.messageFallbackPaths.addAll(messageFallbackPaths);
        builder.argTransformers.addAll(argTransformers);
        builder.missingMessageHandler = missingMessageHandler;
        builder.defaultLocale = defaultLocale;
        builder.transformJava8TimeTypes = transformJava8TimeTypes;
        builder.normalizeWhitespaces = normalizeWhitespaces;
        builder.resolveReferences = resolveReferences;
        builder.missingMessagesDetector = missingMessagesDetector;
        return builder;
    }

    @NotNull
    public I18nMessagePackBuilder scanFileSystem(@NotNull String firstPattern, String... others) {
        expectNonBlank(firstPattern, "firstPattern");
        return scanFileSystem(FileSystems.getDefault(), firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanFileSystem(@NotNull FileSystem fileSystem, @NotNull String firstPattern, String... others) {
        expectNonBlank(firstPattern, "firstPattern");
        expectNonNull(fileSystem, "fileSystem");
        return scanFileSystemWithPrefix(I18nPath.root(), fileSystem, firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanFileSystemWithPrefix(@NotNull String prefix, @NotNull String firstPattern, String... others) {
        expectNonBlank(prefix, "prefix");
        expectNonBlank(firstPattern, "firstPattern");
        return scanFileSystemWithPrefix(prefix, FileSystems.getDefault(), firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanFileSystemWithPrefix(@NotNull String prefix, @NotNull FileSystem fileSystem, @NotNull String firstPattern, String... others) {
        expectNonBlank(prefix, "prefix");
        expectNonBlank(firstPattern, "firstPattern");
        expectNonNull(fileSystem, "fileSystem");
        return scanFileSystemWithPrefix(I18nPath.of(prefix), fileSystem, firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanFileSystemWithPrefix(@NotNull I18nPath prefix, @NotNull FileSystem fileSystem, @NotNull String firstPattern, String... others) {
        expectNonNull(prefix, "prefix");
        expectNonBlank(firstPattern, "firstPattern");
        expectNonNull(fileSystem, "fileSystem");
        I18nLoader loader = I18nFileSystemLoader
                .builder(fileSystem)
                .scanPathPattern(firstPattern)
                .scanPathPatterns(others)
                .staticKeyPrefix(prefix)
                .build();
        this.loader.addLoader(loader);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder scanClassPath(@NotNull String firstPattern, String... others) {
        expectNonBlank(firstPattern, "firstPattern");
        return scanClassPath(Thread.currentThread().getContextClassLoader(), firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanClassPath(@NotNull ClassLoader classLoader, @NotNull String firstPattern, String... others) {
        expectNonBlank(firstPattern, "firstPattern");
        expectNonNull(classLoader, "classLoader");
        return scanClassPathWithPrefix(I18nPath.root(), classLoader, firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanClassPathWithPrefix(@NotNull String prefix, @NotNull String firstPattern, String... others) {
        expectNonBlank(prefix, "prefix");
        expectNonBlank(firstPattern, "firstPattern");
        return scanClassPathWithPrefix(prefix, Thread.currentThread().getContextClassLoader(), firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanClassPathWithPrefix(@NotNull String prefix, @NotNull ClassLoader classLoader, @NotNull String firstPattern, String... others) {
        expectNonBlank(prefix, "prefix");
        expectNonBlank(firstPattern, "firstPattern");
        expectNonNull(classLoader, "classLoader");
        return scanClassPathWithPrefix(I18nPath.of(prefix), classLoader, firstPattern, others);
    }

    @NotNull
    public I18nMessagePackBuilder scanClassPathWithPrefix(@NotNull I18nPath prefix, @NotNull ClassLoader classLoader, @NotNull String firstPattern, String... others) {
        expectNonNull(prefix, "prefix");
        expectNonBlank(firstPattern, "firstPattern");
        expectNonNull(classLoader, "classLoader");
        I18nLoader loader = I18nClassPathLoader
                .builder(classLoader)
                .scanPathPattern(firstPattern)
                .scanPathPatterns(others)
                .staticKeyPrefix(prefix)
                .build();
        this.loader.addLoader(loader);
        return this;
    }

    @NotNull
    public <T> I18nMessagePackBuilder addArgumentTransformer(Class<T> type, Function<T, Object> transform) {
        expectNonNull(type, "type");
        expectNonNull(transform, "transform");
        this.argTransformers.add(I18nArgTransformer.of(type, transform));
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder disableJava8ArgumentTransformers() {
        this.transformJava8TimeTypes = false;
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder normalizeWhitespaces() {
        this.normalizeWhitespaces = true;
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder disableReferenceResolution() {
        this.resolveReferences = false;
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder usePathOnMissingMessage() {
        this.missingMessageHandler = I18nMissingMessageHandler.pathPrintingHandler();
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder validateNoMissingMessages() {
        return detectMissingMessages(I18nMissingMessagesDetector.builder()
                .throwErrorOnMissingMessages()
                .build());
    }

    @NotNull
    public I18nMessagePackBuilder logMissingMessages() {
        return detectMissingMessages(I18nMissingMessagesDetector.builder()
                .logMissingMessages()
                .build());
    }

    @NotNull
    public I18nMessagePackBuilder detectMissingMessages(I18nMissingMessagesDetector missingMessagesDetector) {
        expectNonNull(missingMessagesDetector, "missingMessagesDetector");
        if (this.missingMessagesDetector != null) {
            throw new IllegalArgumentException("Missing messages detector was already defined");
        }
        this.missingMessagesDetector = missingMessagesDetector;
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addLoader(@NotNull I18nLoader loader) {
        expectNonNull(loader, "loader");
        this.loader.addLoader(loader);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addMessage(@NotNull Locale locale, @NotNull I18nPath path, @NotNull String template) {
        expectNonNull(locale, "locale");
        expectNonNull(path, "path");
        expectNonBlank(template, "template");
        return addMessage(I18nKey.of(locale, path), template);
    }

    @NotNull
    public I18nMessagePackBuilder addMessage(@NotNull Locale locale, @NotNull String path, @NotNull String template) {
        expectNonNull(locale, "locale");
        expectNonBlank(path, "path");
        expectNonBlank(template, "template");
        return addMessage(I18nKey.of(locale, path), template);
    }

    @NotNull
    public I18nMessagePackBuilder addMessage(@NotNull I18nKey key, @NotNull String template) {
        expectNonNull(key, "key");
        expectNonBlank(template, "template");
        this.loader.addMessage(key, template);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addMessages(@NotNull Map<I18nKey, String> messages) {
        expectNonNull(messages, "messages");
        this.loader.addMessages(messages);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addMessages(@NotNull I18nMessageBundle messageBundle) {
        expectNonNull(messageBundle, "messageBundle");
        this.loader.addLoader(() -> List.of(messageBundle));
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder setMissingMessageHandler(@NotNull I18nMissingMessageHandler missingMessageHandler) {
        expectNonNull(missingMessageHandler, "missingMessageHandler");
        this.missingMessageHandler = missingMessageHandler;
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder setDefaultLocale(@NotNull Locale defaultLocale) {
        expectNonNull(defaultLocale, "defaultLocale");
        this.defaultLocale = defaultLocale;
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addReferenceFallbackKeyPrefixes(@NotNull List<String> keyPrefixes) {
        expectNonNull(keyPrefixes, "keyPrefixes");
        keyPrefixes.forEach(this::addReferenceFallbackKeyPrefix);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addReferenceFallbackKeyPrefixes(@NotNull String... keyPrefixes) {
        expectNonNull(keyPrefixes, "keyPrefixes");
        Arrays.stream(keyPrefixes).forEach(this::addReferenceFallbackKeyPrefix);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addReferenceFallbackKeyPrefix(@NotNull String keyPrefix) {
        expectNonBlank(keyPrefix, "keyPrefix");
        I18nPath i18nPath = I18nPath.of(keyPrefix);
        this.referenceFallbackPaths.add(i18nPath);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addMessageFallbackKeyPrefixes(@NotNull List<String> keyPrefixes) {
        expectNonNull(keyPrefixes, "keyPrefixes");
        keyPrefixes.forEach(this::addMessageFallbackKeyPrefix);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addMessageFallbackKeyPrefixes(@NotNull String... keyPrefixes) {
        expectNonNull(keyPrefixes, "keyPrefixes");
        Arrays.stream(keyPrefixes).forEach(this::addMessageFallbackKeyPrefix);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addMessageFallbackKeyPrefix(@NotNull String keyPrefix) {
        expectNonBlank(keyPrefix, "keyPrefix");
        I18nPath i18nPath = I18nPath.of(keyPrefix);
        this.messageFallbackPaths.add(i18nPath);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addFallbackKeyPrefixes(@NotNull List<String> keyPrefixes) {
        expectNonNull(keyPrefixes, "keyPrefixes");
        addMessageFallbackKeyPrefixes(keyPrefixes);
        addReferenceFallbackKeyPrefixes(keyPrefixes);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addFallbackKeyPrefixes(@NotNull String... keyPrefixes) {
        expectNonNull(keyPrefixes, "keyPrefixes");
        addMessageFallbackKeyPrefixes(keyPrefixes);
        addReferenceFallbackKeyPrefixes(keyPrefixes);
        return this;
    }

    @NotNull
    public I18nMessagePackBuilder addFallbackKeyPrefix(@NotNull String keyPrefix) {
        expectNonBlank(keyPrefix, "keyPrefix");
        addMessageFallbackKeyPrefix(keyPrefix);
        addReferenceFallbackKeyPrefix(keyPrefix);
        return this;
    }

    @NotNull
    public I18nMessagePack build() {
        List<I18nMessageBundle> bundles = loader.load();
        return build(bundles);
    }

    @NotNull
    public I18nMessages buildLocalized(Locale locale) {
        expectNonNull(locale, "locale");
        I18nMessagePack pack = build();
        return pack.localize(locale);
    }

    @NotNull
    public Reloadable18nMessagePack buildReloadable() {
        I18nMessagePackBuilder copy = this.copy();
        AggregatedI18nLoader loader = copy.loader;
        return new Reloadable18nMessagePack(loader, copy::build);
    }

    @NotNull
    public Reloadable18nMessagePack buildAndWatchForChanges() {
        Reloadable18nMessagePack messagePack = buildReloadable();
        messagePack.startWatching();
        return messagePack;
    }

    private I18nMessagePack build(List<I18nMessageBundle> bundles) {
        bundles = TemplatesBundlePrefixes.prefix(bundles);
        detectMissingMessages(bundles);
        LocaleResolver localeResolver = LocaleResolver.of(defaultLocale, bundles);
        I18nKeyGenerator messageKeyGenerator = new I18nKeyGenerator(defaultLocale, messageFallbackPaths, localeResolver);
        MessageTemplateParser parser = buildMessageTemplateParser(bundles, localeResolver);
        Map<I18nKey, MessageTemplate> templates = parser.parseTemplates(bundles);
        return new ImmutableI18nMessagePack(templates, parser, missingMessageHandler, messageKeyGenerator);
    }

    private MessageTemplateParser buildMessageTemplateParser(List<I18nMessageBundle> bundles, LocaleResolver localeResolver) {
        I18nKeyGenerator referenceKeyGenerator = new I18nKeyGenerator(defaultLocale, referenceFallbackPaths, localeResolver);
        ReferenceResolver referenceResolver = new ReferenceResolver(bundles, referenceKeyGenerator, resolveReferences);
        ArgumentResolver argumentResolver = buildArgumentResolver();
        MessageTemplateNormalizer messageTemplateNormalizer = new MessageTemplateNormalizer(normalizeWhitespaces);
        return new MessageTemplateParser(referenceResolver, argumentResolver, messageTemplateNormalizer);
    }

    private ArgumentResolver buildArgumentResolver() {
        List<I18nArgTransformer<?>> result = new ArrayList<>();
        if (transformJava8TimeTypes) {
            result.addAll(javaTimeI18nArgTransformers());
        }
        result.addAll(argTransformers);
        return ArgumentResolver.of(result);
    }

    private void detectMissingMessages(List<I18nMessageBundle> bundles) {
        if (missingMessagesDetector == null) {
            return;
        }
        Set<I18nKey> keys = new HashSet<>();
        for (I18nMessageBundle bundle : bundles) {
            keys.addAll(bundle.templates().keySet());
        }
        missingMessagesDetector.detect(keys);
        // don't detect messages multiple times in devmode
        missingMessagesDetector = null;
    }
}
