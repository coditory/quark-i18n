# Quark I18N

[![Build](https://github.com/coditory/quark-i18n/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-i18n/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/github/coditory/quark-i18n/badge.svg)](https://coveralls.io/github/coditory/quark-i18n)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-i18n/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-i18n)

## Additional features

- [ICU message formatting](#message-formatting)
- [Message references](#message-references)
- Messages resolution with [indexed](./README_FORMAT.md#indexed-argument) and [named](./README_FORMAT.md##named-argument) arguments
- [Type based formatters](#type-based-formatters)
- [Missing message detection](#devmode)
- Splitting big file into multiple smaller ones
- ...or use single file to define messages for multiple locales
- Supports multiple file formats: yaml, properties, json
- [Auto-reloadable dev mode](#devmode)

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-i18n:0.0.1"
}
```

## Usage

Build

## Message formatting

Message formatting is fully handled by [ICU4J](https://github.com/unicode-org/icu).
ICU is a mature, widely used set of libraries providing Unicode and Globalization support for software applications.
It's a standard handled by multiple translation focused systems.

Some examples:

```
# Simple argument (indexed and named)
Hello, Your friend {0} is now online.
Hello, Your friend {friend} is now online.

# Argument formatting
Order was sent on {0,date}.

# Select statement
{gender, select, female {She} male {He} other {They}} received you email.

# Pluralization
I bought {0, plural, one {# book} other {# books}}
```

For more examples go to [advanced message formatting examples](./README-FORMAT.md)

## Message references

## Type based formatters

## Missing message detection

## Files structure

## DevMode

## TODO

- Fix TODOs in code
- Test
- Unify exceptions
- Unify public and final modifiers
- Update readme
- Release
- Trim new lines?
