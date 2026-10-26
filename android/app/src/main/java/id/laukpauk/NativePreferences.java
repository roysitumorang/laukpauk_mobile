package id.laukpauk;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Native preferences for the Java application.
 *
 * Legacy Flutter SharedPreferences values are read as a migration fallback;
 * newly written values use the native "laukpauk" preference file. Do not
 * clear legacy data until the user/session migration has been verified.
 */
public final class NativePreferences {
    private static final String FILE = "laukpauk";
    private static final String LEGACY_FILE = "FlutterSharedPreferences";

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
