package id.laukpauk.ui.startup;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
        JsonObject o = Json.parseObject(json);
        for (Map.Entry<String, JsonElement> e : o.entrySet()) {
            JsonElement v = e.getValue();
            if (v == null || v.isJsonNull()) {
                continue;
            }
            if (v.isJsonPrimitive()) {
                JsonPrimitive p = v.getAsJsonPrimitive();
                if (p.isBoolean()) {
                    b.putBoolean(e.getKey(), p.getAsBoolean());
                } else if (p.isNumber() && p.getAsString().matches("-?\\d{1,9}")) {
                    b.putInt(e.getKey(), p.getAsInt());
                } else {
                    b.putString(e.getKey(), p.getAsString());
                }
            } else {
                b.putString(e.getKey(), v.toString());
            }
        }
        return b;
    }
}
