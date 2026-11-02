package id.laukpauk.ui.startup;

import android.os.Bundle;

import androidx.annotation.Nullable;


import java.util.Map;

import id.laukpauk.util.Json;

/**
 * Splash screen's {@code routeArgs}: a JSON object text turned into navigation arguments (strings stay strings,
 * booleans and whole numbers keep their type, nested objects/arrays travel as JSON text).
 */
final class RouteArgs {
    private RouteArgs() {}

    static Bundle toBundle(@Nullable String json) {
        Bundle b = new Bundle();
        if (json == null || json.trim().isEmpty()) {
            return b;
        }
        Map<String, Object> o = Json.parseObject(json);
        for (Map.Entry<String, Object> e : o.entrySet()) {
            Object v = e.getValue();
            if (v == null) {
                continue;
            }
            if (v instanceof Boolean) {
                b.putBoolean(e.getKey(), (Boolean) v);
            } else if (v instanceof Number && Json.asStr(v).matches("-?\\d{1,9}")) {
                b.putInt(e.getKey(), ((Number) v).intValue());
            } else {
                b.putString(e.getKey(), Json.asStr(v));
            }
        }
        return b;
    }
}
