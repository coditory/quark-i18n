package com.coditory.quark.i18n;

import java.util.Locale;
import java.util.TimeZone;

public class I18nSystemDefaults {
    private I18nSystemDefaults() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static void setupGmtAndEnUsAsDefaults() {
        System.setProperty("user.timezone", "GMT");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        System.setProperty("user.language", "en");
        System.setProperty("user.region", "US");
        System.setProperty("user.variant", "");
        Locale.setDefault(Locale.US);
    }
}
