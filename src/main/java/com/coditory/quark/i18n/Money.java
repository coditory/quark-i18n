package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public record Money(@NotNull BigDecimal amount, @NotNull Currency currency) {
    public Money(BigDecimal amount, Currency currency) {
        this.amount = expectNonNull(amount, "amount");
        this.currency = expectNonNull(currency, "currency");
    }

    @NotNull
    static Money of(double amount, @NotNull Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    @NotNull
    static Money of(long amount, @NotNull Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    @NotNull
    static Money of(@NotNull String amount, @NotNull Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    @NotNull
    public Money add(@NotNull Money other) {
        expectNonNull(other, "other");
        expectSameCurrencies(other);
        BigDecimal result = amount.add(other.amount);
        return new Money(result, currency);
    }

    @NotNull
    public Money subtract(@NotNull Money other) {
        expectNonNull(other, "other");
        expectSameCurrencies(other);
        BigDecimal result = amount.subtract(other.amount);
        return new Money(result, currency);
    }

    @NotNull
    public Money multiply(@NotNull Money other) {
        expectNonNull(other, "other");
        expectSameCurrencies(other);
        BigDecimal result = amount.multiply(other.amount);
        return new Money(result, currency);
    }

    @NotNull
    public Money divide(@NotNull Money other) {
        expectNonNull(other, "other");
        expectSameCurrencies(other);
        BigDecimal result = amount.divide(other.amount);
        return new Money(result, currency);
    }

    public boolean isGreaterThanZero() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isLessThanZero() {
        return amount.signum() < 0;
    }

    public int intValue() {
        return amount.intValue();
    }

    public int intValueExact() {
        return amount.intValueExact();
    }

    public double doubleValue() {
        return amount.doubleValue();
    }

    public float floatValue() {
        return amount.floatValue();
    }

    private void expectSameCurrencies(Money money) {
        expectNonNull(money, "amountWithCurrency");
        if (!currency.equals(money.currency)) {
            throw new IllegalArgumentException("Expected same currencies. Got: " + currency + " != " + money.currency);
        }
    }

    @Override
    public String toString() {
        return Currencies.formatByCurrency(amount, currency);
    }
}
