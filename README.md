# Quark i18n

[![Build](https://github.com/coditory/quark-i18n/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-i18n/actions/workflows/build.yml)
[![Coverage](https://codecov.io/gh/coditory/quark-i18n/branch/master/graph/badge.svg?token=SPRRPIHQJ4)](https://codecov.io/gh/coditory/quark-i18n)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-i18n/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-i18n)

**🚧 This library as under heavy development until release of version `1.x.x` 🚧**

> Updated approch to i18n and java [ResourceBundles](https://www.baeldung.com/java-resourcebundle).
> Uses [icu4j](https://github.com/unicode-org/icu) for standardized message formatting and solves multiple pain points.

* [Installation](#installation)
* [Basic usage](#basic-usage)
* [Message formatting](#message-formatting)
* [Message loading](#message-loading)
  * [Manual messages creation](#manual-messages-creation)
  * [Loading messages from classpath or file system](#loading-messages-from-classpath-or-file-system)
  * [Single message file with multiple locales](#single-message-file-with-multiple-locales)
  * [Watching for file changes](#watching-for-file-changes)
* [Message resolution](#message-resolution)
  * [Message fallbacks](#message-fallbacks)
  * [Prefixed queries](#prefixed-queries)
  * [Localized queries](#localized-queries)
* [Message references](#message-references)
  * [Reference resolution order](#reference-resolution-order)
  * [References in a prefixed file](#references-in-a-prefixed-file)
* [Type based formatters](#type-based-formatters)
* [Missing messages](#missing-messages)
  * [Missing message handler](#missing-message-handler)
  * [Missing message detection](#missing-message-detection)
* [Whitespace normalization](#whitespace-normalization)
* [DevMode](#devmode)


## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-i18n:0.0.8"
}
```

## Basic usage

```java
I18nMessagePack messagePack = I18nMessagePack.builder()
    .scanClassPath("/i18n/messages-{locale}.yml")
    .setDefaultLocale(Locales.EN_US)
    .build();

// ...when request arrives
I18nMessages messages = messagePack.localize(req.getLocale());
print(messages.getMessage("greeting", userName));
```

## Message formatting

Message formatting is fully handled by [ICU4J](https://github.com/unicode-org/icu).
ICU is a mature, widely used set of libraries providing Unicode and Globalization support for software applications.
It's a standard handled by multiple translation centric systems.

Some examples:

```
# Simple argument (indexed and named)
{0} sent you a message.
{name} sent you a message.

# Argument formatting
Message was sent on {0,date}.

# Select statement
{gender, select, female {She} male {He} other {They}} sent you a message.

# Pluralization
You have {0, plural, zero {no new messages}, one {one new message} other {# new messages}}
```

> For more examples go to [advanced message formatting examples](./README-FORMAT.md)

## Message loading

Messages can be created in 3 ways:

- Created manually using a builder
- Loaded from classpath
- Loaded from filesystem

### Manual messages creation

```java
I18nMessagePack messages = I18nMessagePack.builder()
    .addMessage(Loacles.EN_US, "hello", "Hello {0}")
    .addMessage(Loacles.PL_PL, "hello", "Cześć {0}")
    .setDefaultLocale(PL_PL)
    .build();

messages.getMessage(Loacles.EN_US, "hello", userName);
```

If you want to quickly load messages from a nested map (for example fetched from a document storage)
you can use `I18nParsers.parseEntries(map, locale)` to translate nested the map keys into localized message paths.

```java
I18nMessagePack messages = I18nMessagePack.builder()
    .addMessages(I18nParsers.parseEntries(Map.of("hello", "Hello {0}"), EN_US))
    .build();
```

### Loading messages from classpath or file system

```java
I18nMessagePack messages = I18nMessagePack.builder()
    .scanClassPath("/i18n/messages-{locale}.yml")
    .scanFileSystem("./overriddes/messages-{locale}.yml")
    .setDefaultLocale(PL_PL)
    .build();
```

Localization based path placeholders are used to assign all messages in the file to file's locale.
Available placeholders:

- `{locale}` - matches language code or language code with country code
- `{lang}` - matches language code (cannot be used with `{locale}`)
- `{country}` - matches country code (requires `{lang}`)

Prefixed based path placeholders are used to prefix all message paths (and [references](#message-references)) in the
file to file's prefix.
Available placeholders:

- `{prefix}` - matches single directory name or part of a file
- `{prefixes}` - matches multiple directory names

Supported formats:

- `YAML` - thanks to [SnakeYAML](https://mvnrepository.com/artifact/org.yaml/snakeyaml) library
- `JSON` - thanks to [Gson](https://mvnrepository.com/artifact/com.google.code.gson/gson) library
- `properties` - with UTF-8 encoding only

There is [dev mode](#devmode) that auto-reloads files during development.

### Single message file with multiple locales

When path pattern does not contain one of localization placeholders (`{locale}`, `{lang}`)
then locale is parsed from the last segment of the message path:

Example in `yml`
```yml
homepage.title:
  pl-PL: Strona główna
  en-US: Homepage
```

### Watching for file changes

To reload messages in file change use:

```java
I18nMessagePack.builder()
    .scanFileSystem("i18n/*")
    .buildAndWatchForChanges();
```

ATM, it works for messages loaded from filesystem only, but for add your own implementation of `WatchableI18nLoader`.

## Message resolution

Messages are resolved with locale and message path.
If there is no match for message path and locale then less strict locale is used.
If there is still no match then the default locale (followed by a less strict default locale) is used.

```java
I18nMessagePack messages = I18nMessagePack.builder()
    .scanClassPath("/i18n/messages-{locale}.yml")
    .setDefaultLocale(PL_PL)
    .addFallbackKeyPrefix("glossary")
    .build();

String message = messages.getMessage(Locales.en_US, "hello");
```

Locations used to find the message:

```
1. (en_US, en) hello
2. (pl_PL, pl) hello
```

### Message fallbacks

Sometimes it is useful to specify a common path prefix for all unmatched queries:

```java
I18nMessagePack messages = I18nMessagePack.builder()
    .scanClassPath("/i18n/messages-{locale}.yml")
    .setDefaultLocale(PL_PL)
    .addMessageFallbackKeyPrefix("common")
    .build();

String message = messages.getMessage(Locales.en_US, "hello");
```

Locations used to find the message:

```
1. (en_US, en) hello
2. (en_US, en) common.hello
3. (pl_PL, pl) hello
4. (pl_PL, pl) common.hello
```

### Prefixed queries

Sometimes it's useful to prefix all queries with some path, like in the example:

```java
I18nMessagePack messages = I18nMessagePack.builder()
    .scanClassPath("/i18n/messages-{locale}.yml")
    .setDefaultLocale(PL_PL)
    .build();

I18nMessagePack homepageMessages = messages.prefixQueries("pages.homepage");
String homepageTitle = homepageMessages.getMessage(en_US, "title");
String homepageSubtitle = homepageMessages.getMessage(en_US, "title");
```

Locations used to find the message:

```
1. (en_US, en) pages.homepage.title
2. (en_US, en) title
3. (pl_PL, pl) pages.homepage.title
4. (pl_PL, pl) title
```

### Localized queries

Sometimes it's useful to apply common locale to all queries:

```java
I18nMessagePack messagePack = I18nMessagePack.builder()
    .scanClassPath("/i18n/messages-{locale}.yml")
    .setDefaultLocale(PL_PL)
    .build();

// ...when request arrives
I18nMessages messages = messagePack.localize(req.getLocale());
String title = messages.getMessage("title");
String subtitle = messages.getMessage("subtitle");
```

Query localization mechanism can be used together with query prefixes:

```java
I18nMessages messages = messagePack
    .prefixQueries("pages.homepage")
    .localize(req.getLocale())
```

## Message references

Message references are the way to reuse text across multiple messages.

Example in yml:
```yml
# Common entries
company:
  name: ACME
  established: 1988
# Message
about-company: "${company.name} was established on ${company.established}"
```

```java
messages.getMessage("about-company") == "ACME was established on 1988"
```

- It's not a part of ICU standard
- Reference resolution mechanism can be disabled with: `i18nMessagePackBuilder.disableReferences()`
- You can add fallback path prefixes
  with `i18nMessagePackBuilder.addReferenceFallbackKeyPrefix()` ([example](#sample-reference-resolution))
- References in prefixed files are prefixed as
  well `${foo} -> ${<file-prefix>.foo}` ([example](#sample-reference-resolution-from-a-prefixed-file))
- References have short notation `$common.reference` and long one `${common.reference}`. The long one is useful when
  there reference is placed next to `[a-zA-Z0-9-_]`, like in `abc${common.reference}abc$`.

### Reference resolution order

Let's configure messages:

```java
I18nMessagePack messagePack = I18nMessagePack.builder()
    .addMessage(EN_US, "msg", "${company.name} was established on 1988")
    .scanClassPath("/i18n/messages-{locale}.yml")
    .setDefaultLocale(PL_PL)
    .addFallbackKeyPrefix("fallback")
    .build();
```

Locations used to find the message:

```
1. (en_US, en) company.name
2. (en_US, en) fallback.company.name
3. (pl_PL, pl) company.name
4. (pl_PL, pl) fallback.company.name
```

### References in a prefixed file

If the reference is defined in a message stored in a prefixed file it will be automatically prefixed:

```java
I18nMessagePack messagePack = I18nMessagePack.builder()
    .scanClassPathLocation("i18n/{prefix}/message_{locale}.yml")
    .setDefaultLocale(PL_PL)
    .addFallbackKeyPrefix("fallback")
    .build();
```

and file `i18n/company/message_en-US.yml` contains

```yml
msg: ${name} was established on 1988
```

Locations used to find the message:

```
1. (en_US, en) company.name
2. (en_US, en) name
3. (en_US, en) fallback.company.name
4. (pl_PL, pl) company.name
5. (pl_PL, pl) name
6. (pl_PL, pl) fallback.company.name
```

## Type based formatters

You can map arguments by their type using argument transformers:
- transformation is located in the definition order
- only the arguments used in the message are transformed
- transformation is transitive - one value can be transformed multiple times

Example:

```java
I18nMessages messages = I18nMessagePack.builder()
    .addMessage(EN, "msg", "{0,number,00000.00000}")
    .addArgumentTransformer(Foo, (foo) -> foo.getSomeNumber())
    .buildLocalized(EN);
        
messages.getMessage("msg", new Foo(123.456)) == "00123.45600"
```

## Missing messages

### Missing message handler

When message is missing, exception is thrown. This mechanism can be changed with:

```java
// add custom missing message handler
i18nMessagePackBuilder.setMissingMessageHandler(customHandler);

// ...or simply return message path when message is missing
i18nMessagePackBuilder.usePathOnMissingMessage()
```

### Missing message detection

It's important to find about missing messages as quickly as possible
and avoid finding them on production.

That's why there is an option to detect them during build phase:
```
i18nMessagePackBuilder.validateNoMissingMessages() - throws exception on missing message
i18nMessagePackBuilder.logMissingMessages() - simply logs a report about missing messages
```

#### Missing message sample report

```java
I18nMessagePack.builder()
      .addMessage(EN_US, "hello", "Hello")
      .addMessage(PL_PL, "hello", "Cześć")
      .addMessage(DE_DE, "bye", "Tschüss")
      .logMissingMessages()
      .build()
```

Will generate following report:
```
Missing Messages
================
   Path: bye
Missing: en_US, pl_PL
Sources: de_DE

   Path: hello
Missing: de_DE
Sources: en_US, pl_PL

Total: 2
```

#### Skipping paths during missing message detection

It's common to store glossary type of messages in the default language.
Those kind of values are deliberately defined in a single place and should not be detected as missing in other locales.
You can skip them using a custom missing message detector:

```java
I18nMissingMessagesDetector detector = I18nMissingMessagesDetector.builder()
    .skipPath(skipPath)
    .logMissingMessages()
    .build()

I18nMessagePack.builder()
    .addMessage(EN_US, "a.b.c.d", "MISSING")
    .addMessage(EN_US, "x", "X")
    .addMessage(EN_GB, "x", "X")
    .addMessage(PL_PL, "x", "X")
    .detectMissingMessages(detector)
    .build()

// to skip a.b.c.d use one of sample path patterns as a skipPath:
// - "a.b.c.d",
// - "a.b.c.*",
// - "a.b.**",
// - "a.**",
// - "a.**.d",
// - "**.d",
// - "*.*.*.*"
```

## Whitespace normalization

You can normalize whitespaces by trimming texts them and compressing all consecutive whitespace characters to a
single `' '`.
It makes working with complicated messages easier.

White space normalization
`' \n\tsome text  with, \nspaces\t' -> 'some text  with, spaces'`

This mechanism is disabled by default and can be enabled with: `i18nMessagePackBuilder.normalizeWhitespaces()`.

## DevMode

You can use file watching capabilities to speed up the development cycle:

```java
I18nMessagePackBuidler messagesBuilder = I18nMessagePack.builder()
    .setDefaultLocale(EN_US);
    // ... other common settings
 
I18nMessagePack messages = devMode
  ? messagesBuilder.scanFileSystem("src/main/resources/i18n/*").buildAndWatchForChanges()
  : messagesBuilder.scanClassPath("i18n/*").build()
```

Following setup will load messages directly from project structure and watch for changes.
