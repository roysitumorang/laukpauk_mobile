package id.laukpauk;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Native preferences for the Java application.
 *
 * Legacy Flutter SharedPreferences values are read as a migration fallback.
 * migrateKnownLegacyValues() copies known keys without deleting legacy data,
 * so rollback to the previous app version remains possible.
 */
public final class NativePreferences {
    private static final String FILE = "laukpauk";
    private static final String LEGACY_FILE = "FlutterSharedPreferences";
    private static final List<String> STRING_KEYS = Arrays.asList(
            "authToken", "idUser", "cartCode", "savedLatitude", "savedLongitude",
            "savedJadwalDeliveryCode", "savedAlamat", "savedJadwalDeliveryMap",
            "usedVoucher", "externalUserID");
    private static final List<String> BOOLEAN_KEYS = Arrays.asList(
            "lewatiOTPRegistrasi", "isAlamatDeliveryDanJadwalDeliverySet");

    private NativePreferences() { }

    private static SharedPreferences current(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    private static SharedPreferences legacy(Context context) {
        return context.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE);
    }

    public static String getString(Context context, String key, String fallback) {
        SharedPreferences prefs = current(context);
        if (prefs.contains(key)) return prefs.getString(key, fallback);
        String legacyKey = key.startsWith("flutter.") ? key : "flutter." + key;
        return legacy(context).getString(legacyKey, fallback);
    }

    public static boolean getBoolean(Context context, String key, boolean fallback) {
        SharedPreferences prefs = current(context);
        if (prefs.contains(key)) return prefs.getBoolean(key, fallback);
        String legacyKey = key.startsWith("flutter.") ? key : "flutter." + key;
        return legacy(context).getBoolean(legacyKey, fallback);
    }

    public static void putString(Context context, String key, String value) {
        current(context).edit().putString(key, value).apply();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        current(context).edit().putBoolean(key, value).apply();
    }

    public static void remove(Context context, String key) {
        current(context).edit().remove(key).apply();
    }

    /**
     * Idempotently copies only known preference types. The legacy store is
     * intentionally preserved; call this before native auth/cart initialization.
     */
    public static void migrateKnownLegacyValues(Context context) {
        SharedPreferences old = legacy(context);
        SharedPreferences now = current(context);
        SharedPreferences.Editor editor = now.edit();
        for (String key : STRING_KEYS) {
            String oldKey = "flutter." + key;
            if (!now.contains(key) && old.contains(oldKey)) {
                String value = old.getString(oldKey, null);
                if (value != null) editor.putString(key, value);
            }
        }
        for (String key : BOOLEAN_KEYS) {
            String oldKey = "flutter." + key;
            if (!now.contains(key) && old.contains(oldKey)) {
                editor.putBoolean(key, old.getBoolean(oldKey, false));
            }
        }
        editor.apply();
    }

    public static String getAuthToken(Context context) {
        return getString(context, "authToken", "");
    }

    public static String getCurrentUserId(Context context) {
        return getString(context, "idUser", "");
    }

    public static String getCartCode(Context context) {
        String value = getString(context, "cartCode", null);
        if (value != null && !value.isEmpty()) return value;
        value = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        putString(context, "cartCode", value);
        return value;
    }

    public static void clearCartCode(Context context) {
        remove(context, "cartCode");
    }
}
