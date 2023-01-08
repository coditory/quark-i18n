package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.Collections.unmodifiableMap;

public final class Currencies {
    /**
     * US Dollar
     */
    public static final Currency USD = Currency.getInstance("USD");

    /**
     * Euro
     */
    public static final Currency EUR = Currency.getInstance("EUR");

    /**
     * Canadian Dollar
     */
    public static final Currency CAD = Currency.getInstance("CAD");

    /**
     * Yen
     */
    public static final Currency JPY = Currency.getInstance("JPY");

    /**
     * Pound Sterling
     */
    public static final Currency GBP = Currency.getInstance("GBP");

    /**
     * Yuan Renminbi
     */
    public static final Currency CNY = Currency.getInstance("CNY");

    /**
     * Polish Zloty
     */
    public static final Currency PLN = Currency.getInstance("PLN");

    private static final Map<Currency, Locale> LOCALE_BY_CURRENCY;

    static {
        Map<Currency, Locale> localeByCurrency = new HashMap<>();
        localeByCurrency.put(USD, Locales.EN_US);
        localeByCurrency.put(EUR, Locales.DE_DE);
        for (Locale locale : NumberFormat.getAvailableLocales()) {
            Currency currency = NumberFormat.getCurrencyInstance(locale).getCurrency();
            localeByCurrency.putIfAbsent(currency, locale);
        }
        LOCALE_BY_CURRENCY = unmodifiableMap(localeByCurrency);
    }

    private Currencies() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    @NotNull
    public static String formatByCurrency(@NotNull BigDecimal amount, @NotNull Currency currency) {
        expectNonNull(amount, "amount");
        expectNonNull(currency, "currency");
        Locale currencyLocale = LOCALE_BY_CURRENCY.get(currency);
        if (currencyLocale == null) {
            throw new IllegalArgumentException("Unrecognized currency: " + currency);
        }
        return NumberFormat.getCurrencyInstance(currencyLocale).format(amount);
    }

    @NotNull
    public static String formatByCurrency(long amount, @NotNull Currency currency) {
        expectNonNull(currency, "currency");
        Locale currencyLocale = LOCALE_BY_CURRENCY.get(currency);
        if (currencyLocale == null) {
            throw new IllegalArgumentException("Missing locale for currency: " + currency);
        }
        return NumberFormat.getCurrencyInstance(currencyLocale).format(amount);
    }

    @NotNull
    public static String formatByCurrency(double amount, @NotNull Currency currency) {
        expectNonNull(currency, "currency");
        Locale currencyLocale = LOCALE_BY_CURRENCY.get(currency);
        if (currencyLocale == null) {
            throw new IllegalArgumentException("Missing locale for currency: " + currency);
        }
        return NumberFormat.getCurrencyInstance(currencyLocale).format(amount);
    }

    @NotNull
    public static Currency parseCurrency(@NotNull String value) {
        expectNonNull(value, "value");
        Currency currency;
        try {
            currency = Currency.getInstance(value.toUpperCase(Locale.ROOT).trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse Currency: '" + value + "'");
        }
        boolean isAvailable = Currency.getAvailableCurrencies()
                .contains(currency);
        if (!isAvailable) {
            throw new IllegalArgumentException("Currency not available: '" + value + "'");
        }
        return currency;
    }

    @Nullable
    public static Currency parseCurrencyOrNull(@NotNull String value) {
        try {
            return parseCurrency(value);
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public static Currency parseCurrencyOrDefault(@NotNull String value, @NotNull Currency defaultValue) {
        Currency result = parseCurrencyOrNull(value);
        return result == null ? defaultValue : result;
    }

    @NotNull
    public static Optional<Currency> parseCurrencyOrEmpty(@NotNull String value) {
        return Optional.ofNullable(parseCurrencyOrNull(value));
    }
}
