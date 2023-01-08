package com.coditory.quark.i18n;

import java.util.HashSet;
import java.util.PrimitiveIterator;
import java.util.Set;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.Collections.unmodifiableSet;

final class ArgumentIndexExtractor {
    static Set<Integer> extractArgumentIndexes(String template) {
        expectNonNull(template, "template");
        Set<Integer> result = new HashSet<>();
        boolean escaped = false;
        int stack = 0;
        int prev = -1;
        for (PrimitiveIterator.OfInt it = template.codePoints().iterator(); it.hasNext(); ) {
            int c = it.next();
            if (c == '\'') {
                escaped = true;
            } else if (c == '{' && it.hasNext() && (!escaped || stack > 0 && prev == '\'')) {
                if (prev == '\'') {
                    stack = 0;
                    escaped = false;
                }
                int number = extractIndex(it);
                if (number >= 0) {
                    result.add(number);
                }
            } else if (escaped && c == '{') {
                stack += 1;
            } else if (escaped && c == '}') {
                stack = Math.max(0, stack - 1);
                if (stack == 0) {
                    escaped = false;
                }
            } else if (escaped && stack == 0) {
                escaped = false;
            }
            prev = c;
        }
        return unmodifiableSet(result);
    }

    private static int extractIndex(PrimitiveIterator.OfInt iterator) {
        int c = iterator.next();
        while (Character.isWhitespace(c)) {
            c = iterator.next();
        }
        int number = 0;
        while (Character.isDigit(c)) {
            number = number * 10 + Character.digit(c, 10);
            c = iterator.next();
        }
        while (Character.isWhitespace(c)) {
            c = iterator.next();
        }
        return c == '}' || c == ',' ? number : -1;
    }
}
