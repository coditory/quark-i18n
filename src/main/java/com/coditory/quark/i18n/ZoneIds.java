package com.coditory.quark.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import static com.coditory.quark.i18n.Preconditions.expectNonNull;

public class ZoneIds {
    // Australia/Darwin
    public static final ZoneId ACT = ZoneId.of("ACT", ZoneId.SHORT_IDS);
    // Australia/Sydney
    public static final ZoneId AET = ZoneId.of("AET", ZoneId.SHORT_IDS);
    // America/Argentina/Buenos_Aires
    public static final ZoneId AGT = ZoneId.of("AGT", ZoneId.SHORT_IDS);
    // Africa/Cairo
    public static final ZoneId ART = ZoneId.of("ART", ZoneId.SHORT_IDS);
    // America/Anchorage
    public static final ZoneId AST = ZoneId.of("AST", ZoneId.SHORT_IDS);
    // America/Sao_Paulo
    public static final ZoneId BET = ZoneId.of("BET", ZoneId.SHORT_IDS);
    // Asia/Dhaka
    public static final ZoneId BST = ZoneId.of("BST", ZoneId.SHORT_IDS);
    // Africa/Harare
    public static final ZoneId CAT = ZoneId.of("CAT", ZoneId.SHORT_IDS);
    // America/St_Johns
    public static final ZoneId CNT = ZoneId.of("CNT", ZoneId.SHORT_IDS);
    // America/Chicago
    public static final ZoneId CST = ZoneId.of("CST", ZoneId.SHORT_IDS);
    // Asia/Shanghai
    public static final ZoneId CTT = ZoneId.of("CTT", ZoneId.SHORT_IDS);
    // Africa/Addis_Ababa
    public static final ZoneId EAT = ZoneId.of("EAT", ZoneId.SHORT_IDS);
    // Europe/Paris
    public static final ZoneId ECT = ZoneId.of("ECT", ZoneId.SHORT_IDS);
    // America/Indiana/Indianapolis
    public static final ZoneId IET = ZoneId.of("IET", ZoneId.SHORT_IDS);
    // Asia/Kolkata
    public static final ZoneId IST = ZoneId.of("IST", ZoneId.SHORT_IDS);
    // Asia/Tokyo
    public static final ZoneId JST = ZoneId.of("JST", ZoneId.SHORT_IDS);
    // Pacific/Apia
    public static final ZoneId MIT = ZoneId.of("MIT", ZoneId.SHORT_IDS);
    // Asia/Yerevan
    public static final ZoneId NET = ZoneId.of("NET", ZoneId.SHORT_IDS);
    // Pacific/Auckland
    public static final ZoneId NST = ZoneId.of("NST", ZoneId.SHORT_IDS);
    // Asia/Karachi
    public static final ZoneId PLT = ZoneId.of("PLT", ZoneId.SHORT_IDS);
    // America/Phoenix
    public static final ZoneId PNT = ZoneId.of("PNT", ZoneId.SHORT_IDS);
    // America/Puerto_Rico
    public static final ZoneId PRT = ZoneId.of("PRT", ZoneId.SHORT_IDS);
    // America/Los_Angeles
    public static final ZoneId PST = ZoneId.of("PST", ZoneId.SHORT_IDS);
    // Pacific/Guadalcanal
    public static final ZoneId SST = ZoneId.of("SST", ZoneId.SHORT_IDS);
    // Asia/Ho_Chi_Minh
    public static final ZoneId VST = ZoneId.of("VST", ZoneId.SHORT_IDS);
    // -05:00
    public static final ZoneId EST = ZoneId.of("EST", ZoneId.SHORT_IDS);
    // -07:00
    public static final ZoneId MST = ZoneId.of("MST", ZoneId.SHORT_IDS);
    // -10:00
    public static final ZoneId HST = ZoneId.of("HST", ZoneId.SHORT_IDS);
    // UTC
    public static final ZoneId UTC = ZoneId.of("UTC");

    public static class Europe {
        public static final ZoneId Amsterdam = ZoneId.of("Europe/Amsterdam");
        public static final ZoneId Berlin = ZoneId.of("Europe/Berlin");
        public static final ZoneId Brussels = ZoneId.of("Europe/Brussels");
        public static final ZoneId Copenhagen = ZoneId.of("Europe/Copenhagen");
        public static final ZoneId Dublin = ZoneId.of("Europe/Dublin");
        public static final ZoneId Warsaw = ZoneId.of("Europe/Warsaw");
        public static final ZoneId Paris = ZoneId.of("Europe/Paris");
        public static final ZoneId Zurich = ZoneId.of("Europe/Zurich");
    }

    public static class America {
        public static final ZoneId Chicago = ZoneId.of("America/Chicago");
        public static final ZoneId LosAngeles = ZoneId.of("America/Los_Angeles");
        public static final ZoneId Phoenix = ZoneId.of("America/Phoenix");
        public static final ZoneId PuertoRico = ZoneId.of("America/Puerto_Rico");
        public static final ZoneId Vancouver = ZoneId.of("America/Vancouver");
    }

    public static class Africa {
        public static final ZoneId Cairo = ZoneId.of("Africa/Cairo");
        public static final ZoneId Casablanca = ZoneId.of("Africa/Casablanca");
        public static final ZoneId Nairobi = ZoneId.of("Africa/Nairobi");
        public static final ZoneId Tunis = ZoneId.of("Africa/Tunis");
    }

    public static class Asia {
        public static final ZoneId Baghdad = ZoneId.of("Asia/Baghdad");
        public static final ZoneId Baku = ZoneId.of("Asia/Baku");
        public static final ZoneId Bangkok = ZoneId.of("Asia/Bangkok");
        public static final ZoneId Beirut = ZoneId.of("Asia/Beirut");
        public static final ZoneId Damascus = ZoneId.of("Asia/Damascus");
        public static final ZoneId Dubai = ZoneId.of("Asia/Dubai");
        public static final ZoneId Saigon = ZoneId.of("Asia/Saigon");
        public static final ZoneId TelAviv = ZoneId.of("Asia/Tel_Aviv");
        public static final ZoneId Tokyo = ZoneId.of("Asia/Tokyo");
    }

    private static final Set<String> AVAILABLE_ZONES = ZoneId.getAvailableZoneIds();

    private ZoneIds() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    @NotNull
    public static ZoneId parseZoneId(@NotNull String value) {
        expectNonNull(value, "value");
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse ZoneId: '" + value + "'");
        }
        boolean isAvailable = AVAILABLE_ZONES
                .contains(zoneId.getId());
        if (!isAvailable) {
            throw new IllegalArgumentException("ZoneId not available: '" + value + "'");
        }
        return zoneId;
    }

    @Nullable
    public static ZoneId parseZoneIdOrNull(@NotNull String value) {
        if (!AVAILABLE_ZONES.contains(value)) {
            return null;
        }
        try {
            return ZoneId.of(value);
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public static ZoneId parseZoneIdOrDefault(@NotNull String value, @NotNull ZoneId defaultValue) {
        ZoneId result = parseZoneIdOrNull(value);
        return result == null ? defaultValue : result;
    }

    @NotNull
    public static Optional<ZoneId> parseZoneIdOrEmpty(@NotNull String value) {
        return Optional.ofNullable(parseZoneIdOrNull(value));
    }
}
