package com.coditory.quark.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;
import static java.util.stream.Collectors.toMap;

final class ArgumentResolver {
    private final Map<Class<?>, I18nArgTransformer<?>> transformers;

    static ArgumentResolver of(List<I18nArgTransformer<?>> transformers) {
        expectNonNull(transformers, "transformers");
        Map<Class<?>, I18nArgTransformer<?>> map = transformers.stream().collect(toMap(I18nArgTransformer::getArgType, it -> it));
        return new ArgumentResolver(map);
    }

    private ArgumentResolver(Map<Class<?>, I18nArgTransformer<?>> transformers) {
        expectNonNull(transformers, "transformers");
        this.transformers = Map.copyOf(transformers);
    }

    Object[] resolveArguments(Object[] args, Set<Integer> usedIndexes) {
        expectNonNull(args, "args");
        expectNonNull(usedIndexes, "usedIndexes");
        boolean transformable = false;
        for (int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            if (arg != null && usedIndexes.contains(i) && transformers.containsKey(arg.getClass())) {
                transformable = true;
                break;
            }
        }
        if (!transformable) {
            return args;
        }
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            if (arg != null && usedIndexes.contains(i)) {
                result[i] = transformArgument(args[i], i);
            }
        }
        return result;
    }

    Map<String, Object> resolveArguments(Map<String, Object> args, Set<String> usedArgumentNames) {
        expectNonNull(args, "args");
        expectNonNull(usedArgumentNames, "usedArgumentNames");
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && usedArgumentNames.contains(key)) {
                value = transformArgument(value, key);
            }
            result.put(key, value);
        }
        return result;
    }

    private Object transformArgument(Object argument, Object nameOrIndex) {
        try {
            return transformArgumentWithLimit(argument, 0);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not transform argument: " + nameOrIndex + "=" + argument, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Object transformArgumentWithLimit(Object argument, int count) {
        if (count > 10) {
            throw new IllegalArgumentException("Too many argument transformations");
        }
        if (argument == null) {
            return null;
        }
        I18nArgTransformer<Object> transformer = (I18nArgTransformer<Object>) transformers.get(argument.getClass());
        if (transformer == null) {
            return argument;
        }
        Object transformed = transformer.transform(argument);
        return transformArgumentWithLimit(transformed, count + 1);
    }
}
