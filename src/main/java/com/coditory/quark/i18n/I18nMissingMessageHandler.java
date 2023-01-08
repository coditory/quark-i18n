package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.lang.String.format;

public interface I18nMissingMessageHandler {
    @NotNull
    static I18nMissingMessageHandler errorThrowingHandler() {
        return new ErrorThrowingI18NMissingMessageHandler();
    }

    @NotNull
    static I18nMissingMessageHandler pathPrintingHandler() {
        return new PathPrintingI18NMissingMessageHandler();
    }

    @NotNull
    String onUnresolvedMessage(@NotNull I18nKey key, @NotNull Object... args);

    @NotNull
    String onUnresolvedMessageWithNamedArguments(@NotNull I18nKey key, @NotNull Map<String, Object> args);
}

final class PathPrintingI18NMissingMessageHandler implements I18nMissingMessageHandler {
    @Override
    public @NotNull String onUnresolvedMessage(@NotNull I18nKey key, @NotNull Object... args) {
        return key.path().getValue();
    }

    @Override
    public @NotNull String onUnresolvedMessageWithNamedArguments(@NotNull I18nKey key, @NotNull Map<String, Object> args) {
        return key.path().getValue();
    }
}

final class ErrorThrowingI18NMissingMessageHandler implements I18nMissingMessageHandler {
    @Override
    public @NotNull String onUnresolvedMessage(@NotNull I18nKey key, @NotNull Object... args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        String argsString = args.length == 0 ? "" : Arrays.toString(args);
        String argsStringInParenthesis = argsString.isEmpty() ? "" : "(" + argsString.substring(1, argsString.length() - 1) + ')';
        throw new I18nMessagesException(format("Missing message %s%s", key.toShortString(), argsStringInParenthesis));
    }

    @Override
    public @NotNull String onUnresolvedMessageWithNamedArguments(@NotNull I18nKey key, @NotNull Map<String, Object> args) {
        expectNonNull(key, "key");
        expectNonNull(args, "args");
        String argsString = args.toString();
        String argsStringInParenthesis = argsString.isEmpty() ? "" : "(" + argsString.substring(1, argsString.length() - 1) + ')';
        throw new I18nMessagesException(format("Missing message %s%s", key.toShortString(), argsStringInParenthesis));
    }
}