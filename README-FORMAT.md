# ICU Message formatting

Here you can find a combined examples and documentation from other pages

Useful resources:
- [ICU Format Tester](https://icu4j-demos.unicode.org/icu4jweb/formatTest.jsp)
- [ICU Message Format JavaDoc with examples](https://unicode-org.github.io/icu-docs/apidoc/dev/icu4j/com/ibm/icu/text/MessageFormat.html)
- [ICU Official Documentation](https://unicode-org.github.io/icu/userguide/format_parse/messages/)

## Indexed Argument

Use a `{<number>}` argument for placing an indexed value into the message.
The value is formatted using default a formatter matched by value type.

```yaml
greeting: Hello {0}
```

```java
messages.getMessage("greeting","John")=="Hello John"
```

## Named Argument

Similar to [indexed arguments](#indexed-arguments) but value is located by parameter name.

```
Hello {name}
```

```java
messages.getMessage("greeting",Map.of("name","John"))=="Hello John"
```

## Formatted argument

Customize [argument formatting](https://unicode-org.github.io/icu/userguide/format_parse/messages/#predefined-styles-recommended)
with syntax `{key, type, format}` or `{key, type, ::skeleton}`

- `key` is where in the input data to find the data ([indexed argument](#indexed-arguments)
  or [named argument](#named-arguments))
- `type` is how to interpret the value (see below)
- `format` is optional, and is a further refinement on how to display that type of data
- `skeleton` is optional, similar to format but uses
  special [ICU syntax](https://unicode-org.github.io/icu/userguide/format_parse/numbers/skeletons.html) to create a
  locale agnostic wat to format values

```
You have {count, number} new messages.
{spamPercent, number, percent} of them is spam.
```

## Format type: number

| format                      | value   | en_US      | pl_PL        |
|-----------------------------|---------|------------|--------------|
| `{0}`, `{0, number}`        | 1024.5  | 1,024.5    | 1 024,5      |
| `{0, number, integer}`      | 1024.5  | 1,024      | 1 024        |
| `{0, number, currency}`     | -1024.5 | -$1,024.50 | -1 024,50 zł |
| `{0, number, percent}`      | 1.5     | 150%       | 150%         |
| `{0, number, percent}`      | 5       | 500%       | 500%         |
| `{0, number,#,###.#}`       | 1234.56 | 1,234.6    | 1,234.6      |
| `{0, number,::percent .00}` | 0.25    | 0.25%      | 0,25%        |

- Both formats `{0}` `{0, number}` have the same effect. The formatter is resolved by argument type.
- `{0, number,#,###.#}` - uses [DecimalFormat](https://www.baeldung.com/java-decimalformat)
- `{0, number,::percent .00}` -
  uses [ICU skeleton](https://unicode-org.github.io/icu/userguide/format_parse/numbers/skeletons.html#examples)

## Format type: date

| format                                   | en_US                       | pl_PL                       |
|------------------------------------------|-----------------------------|-----------------------------|
| `{0}`                                    | 12/3/07, 10:15 AM           | 3.12.2007, 10:15            |
| `{0, date}`                              | Dec 3, 2007                 | 3.12.2007, 10:15            |
| `{0, date, short}`                       | 12/3/07                     | 3.12.2007, 10:15            |
| `{0, date, medium}`                      | Dec 3, 2007                 | 3.12.2007, 10:15            |
| `{0, date, long}`                        | December 3, 2007            | 3.12.2007, 10:15            |
| `{0, date, full}`                        | Monday, December 3, 2007    | 3.12.2007, 10:15            |
| `{0, date,yyyy-MM-dd'T'HH:mm:ss.SSS zz}` | 2007-12-03T10:15:30.000 GMT | 2007-12-03T10:15:30.000 GMT |
| `{0, date, ::dMMMM}`                     | December 3                  | 3 grudnia                   |

- `{0, date,yyyy-MM-dd'T'HH:mm:ss.SSS zz}` -
  uses [SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)
- `{0, date, ::dMMMM}` -
  uses [ICU skeleton](https://unicode-org.github.io/icu/userguide/format_parse/datetime/)

### Time conversion

[Icu4j does not natively support](https://github.com/unicode-org/icu/blob/main/icu4j/main/classes/core/src/com/ibm/icu/text/DateFormat.java#L623)
types: `Instant`, `ZonedDateTime`, `LocalDateTime`, `DateTime`.
That's why we convert object of those types to the supported `java.util.Date`.
The conversion is always done using `ZoneId.systemDefault()`.

Because the formatting is dependent on system defaults, it is recommended to specify proper ZoneId with:

- program argument: `--user.timezone=GMT+01:00`
- programmatically: `TimeZone.setDefault(TimeZone.getTimeZone("GMT+01:00"))`
- ...or normalize all localization related defaults using: `I18nSystemDefaults.setupNormalized()`

This conversion is automatically enabled.
If you don't like it, you can skip it using:

```java
// Disabling Java8 time types conversion
I18nMessagePack messages=I18nMessagePack.builder()
        .disableJava8ArgumentTransformers()
        .build()
```

### Format type: time

| format                                   | en_US                       | pl_PL                       |
|------------------------------------------|-----------------------------|-----------------------------|
| `{0, time}`                              | Dec 3, 2007                 | 3.12.2007, 10:15            |
| `{0, time, short}`                       | 12/3/07                     | 3.12.2007, 10:15            |
| `{0, time, medium}`                      | Dec 3, 2007                 | 3.12.2007, 10:15            |
| `{0, time, long}`                        | December 3, 2007            | 3.12.2007, 10:15            |
| `{0, time, full}`                        | Monday, December 3, 2007    | 3.12.2007, 10:15            |
| `{0, time,yyyy-MM-dd'T'HH:mm:ss.SSS zz}` | 2007-12-03T10:15:30.000 GMT | 2007-12-03T10:15:30.000 GMT |
| `{0, time, ::dMMMM}`                     | December 3                  | 3 grudnia                   |

- `{0, date,yyyy-MM-dd'T'HH:mm:ss.SSS zz}` -
  uses [SimpleDateFormat](https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/com/ibm/icu/text/SimpleDateFormat.html)
- `{0, date, ::dMMMM}` -
  uses [ICU skeleton](https://unicode-org.github.io/icu/userguide/format_parse/datetime)

## Format type: select

The `{key, select, matches}` is used to choose output by matching a value to one of many choices.
It's often used to format message based on a gender.

```
{0, select,
    male {He}
    female {She}
    other {They}
} sent you a message.
```

You can also nest arguments like:

```
{0, select,
    yes {An additional {taxRate, number, percent} tax will be collected.}
    other {No taxes apply.}
}
```

## Format type: plural

The `{key, plural, matches}` is used to choose output based on the pluralization rules of the current locale.
It is very similar to the [`{select}`](#format-type-select) format above except that the value is expected to be a
number and is mapped to a plural category.

The match is a literal value and is matched to one of these plural categories. Not all languages use
all [plural categories](http://cldr.unicode.org/index/cldr-spec/plural-rules).

- `zero` - zero items
- `one` - one item
- `two` - two items
- `few` - small number of items. For some languages this is used for 2-4 items, for some 3-10 items, and other languages
  have even more complex rules.
- `many` - larger number of items
- `other`- This category is used if the value doesn't match one of the other plural categories. Note that this is used
  for "plural" for languages (such as English) that have a simple "singular" versus "plural" dichotomy.
- `=value` - This is used to match a specific value regardless of the plural categories of the current locale.

```
You have {itemCount, plural,
    =0 {no messages}
    one {1 message}
    other {# messages}
}.
```

### Complex example using `plural` and `select`

**Hint:** If possible, use complex arguments as the outermost structure of a message, and write full sentences in their
sub-messages.

```
// Locale: en-US
{0} has sent you {2, plural,
  =0 {no messages}
  =1 {a message}
  other {{2, number, integer} messages}}.

// Locale: pl-PL
{0} {2, plural, =0 {nie} other {}}
  {1, select, male {wysłał} female {wysłała} other {wysłało}} ci
  {2, plural,
    =0 {żadnych wiadomości}
    =1 {wiadomość}
    other {{2, number, integer} wiadomości}}.
```

| arguments              | en-US                           | pl-PL                                   |
|------------------------|---------------------------------|-----------------------------------------|
| `"Alice", "female", 0` | Alice has sent you no messages. | Alice nie wysłała ci żadnej wiadomości. |
| `"Bob", "male", 1`     | Bob has sent you a message.     | Bob wysłał ci wiadomość.                |
| `"Alice", "female", 5` | Alice has sent you 5 messages.  | Alice żadnych ci 5 wiadomości.          |

## Other rule based format types: spellout, ordinal, duration

| format          | value | en_US             | pl_PL                    |
|-----------------|-------|-------------------|--------------------------|
| `{0, spellout}` | 12.5  | twelve point five | dwanaście przecinek pięć |
| `{0, ordinal}`  | 1     | 1st               | 1.                       |
| `{0, ordinal}`  | 12    | 12th              | 12.                      |
| `{0, duration}` | 12    | 12 sec.           | 12                       |
| `{0, duration}` | 1212  | 20:12             | 1 212                    |
