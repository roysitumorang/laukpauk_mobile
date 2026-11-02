package id.laukpauk.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Replacement for Flutter's shared_preferences plugin.
 *
 * It deliberately uses the SAME file ("FlutterSharedPreferences") and the SAME key prefix
 * ("flutter.") as the plugin, so users who update from the Flutter build keep their login
 * token, cart code, saved delivery address/schedule, etc.
 *
 * Callers use the plain Dart key names (e.g. "authToken"); the prefix is added here.
 */
public final class Prefs {
    private final Context context;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public Prefs(Context context) {
        this.context = context;
    }

    public static final String FILE_NAME = "FlutterSharedPreferences";
    public static final String KEY_PREFIX = "flutter.";

    private SharedPreferences sp() {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key) {
        return sp().getString(KEY_PREFIX + key, null);
    }

    public String getString(String key, String defaultValue) {
        String v = sp().getString(KEY_PREFIX + key, null);
        return v != null ? v : defaultValue;
    }

    public boolean getBool(String key, boolean defaultValue) {
        try {
            return sp().getBoolean(KEY_PREFIX + key, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /** Returns null when the key is absent (Dart: getBool(key) ?? null). */
    public Boolean getBoolOrNull(String key) {
        if (!sp().contains(KEY_PREFIX + key)) {
            return null;
        }
        try {
            return sp().getBoolean(KEY_PREFIX + key, false);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public boolean contains(String key) {
        return sp().contains(KEY_PREFIX + key);
    }

    public void setString(String key, String value) {
        if (value == null) {
            remove(key);
        } else {
            sp().edit().putString(KEY_PREFIX + key, value).apply();
        }
    }

    public void setBool(String key, boolean value) {
        sp().edit().putBoolean(KEY_PREFIX + key, value).apply();
    }

    public void remove(String key) {
        sp().edit().remove(KEY_PREFIX + key).apply();
    }
}
