package id.laukpauk.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.Map;

/**
 * Null-safe, type-tolerant accessors for Gson trees.
 *
 * The backend returns loosely typed JSON (ids arrive as strings, counters as numbers, flags as
 * true/"1"/1, "no result" as the literal false ...). The Dart code treated everything as
 * `dynamic`; this class gives the Java port the same forgiving behaviour. Nothing here throws.
 */
public final class Json {
    private Json() {}

    // ------------------------------------------------------------------ parsing / building

    /** Parses text. Returns null when the text is empty or not valid JSON. */
    public static JsonElement parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return JsonParser.parseString(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Parses text as an object; returns an empty object when it is not one. */
    public static JsonObject parseObject(String text) {
        return obj(parse(text));
    }

    /** Parses text as an array; returns an empty array when it is not one. */
    public static JsonArray parseArray(String text) {
        return arr(parse(text));
    }

    /** Builds an object from alternating key, value arguments: Json.of("a", 1, "b", "x"). */
    public static JsonObject of(Object... keyValues) {
        JsonObject o = new JsonObject();
        if (keyValues == null) {
            return o;
        }
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            put(o, String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return o;
    }

    /** Puts any plain Java value (String, Number, Boolean, JsonElement, null) into the object. */
    public static JsonObject put(JsonObject o, String key, Object value) {
        if (value == null) {
            o.add(key, JsonNull.INSTANCE);
        } else if (value instanceof JsonElement) {
            o.add(key, (JsonElement) value);
        } else if (value instanceof Boolean) {
            o.addProperty(key, (Boolean) value);
        } else if (value instanceof Number) {
            o.addProperty(key, (Number) value);
        } else if (value instanceof Character) {
            o.addProperty(key, (Character) value);
        } else {
            o.addProperty(key, String.valueOf(value));
        }
        return o;
    }

    public static JsonObject fromMap(Map<String, ?> map) {
        JsonObject o = new JsonObject();
        if (map != null) {
            for (Map.Entry<String, ?> e : map.entrySet()) {
                put(o, e.getKey(), e.getValue());
            }
        }
        return o;
    }

    public static String toJson(JsonElement e) {
        return e == null ? "null" : e.toString();
    }

    // ------------------------------------------------------------------ type tests

    public static boolean isNull(JsonElement e) {
        return e == null || e.isJsonNull();
    }

    public static boolean isObject(JsonElement e) {
        return e != null && e.isJsonObject();
    }

    public static boolean isArray(JsonElement e) {
        return e != null && e.isJsonArray();
    }

    /** True for the literal JSON `false` (the API answers `false` for "nothing found"). */
    public static boolean isFalse(JsonElement e) {
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean()
                && !e.getAsBoolean();
    }

    // ------------------------------------------------------------------ element coercion

    /** The element as an object, or an EMPTY object (never null). */
    public static JsonObject obj(JsonElement e) {
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : new JsonObject();
    }

    /** The element as an array, or an EMPTY array (never null). */
    public static JsonArray arr(JsonElement e) {
        return (e != null && e.isJsonArray()) ? e.getAsJsonArray() : new JsonArray();
    }

    public static String asStr(JsonElement e) {
        return asStr(e, "");
    }

    public static String asStr(JsonElement e, String def) {
        if (e == null || e.isJsonNull()) {
            return def;
        }
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isNumber()) {
                double d = p.getAsDouble();
                if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                    return String.valueOf((long) d); // 3.0 -> "3", like most JSON producers intend
                }
            }
            return p.getAsString();
        }
        return e.toString();
    }

    public static int asInt(JsonElement e, int def) {
        if (e == null || e.isJsonNull() || !e.isJsonPrimitive()) {
            return def;
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isNumber()) {
            return (int) p.getAsDouble();
        }
        if (p.isBoolean()) {
            return p.getAsBoolean() ? 1 : 0;
        }
        return parseInt(p.getAsString(), def);
    }

    public static long asLong(JsonElement e, long def) {
        if (e == null || e.isJsonNull() || !e.isJsonPrimitive()) {
            return def;
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isNumber()) {
            return (long) p.getAsDouble();
        }
        if (p.isBoolean()) {
            return p.getAsBoolean() ? 1L : 0L;
        }
        try {
            return (long) Double.parseDouble(p.getAsString().trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    public static double asDbl(JsonElement e, double def) {
        if (e == null || e.isJsonNull() || !e.isJsonPrimitive()) {
            return def;
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isNumber()) {
            return p.getAsDouble();
        }
        if (p.isBoolean()) {
            return p.getAsBoolean() ? 1.0 : 0.0;
        }
        try {
            return Double.parseDouble(p.getAsString().trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    /** Truthy values: true, 1, "1", "true", "ya", "yes". Everything else (incl. null) is false. */
    public static boolean asBool(JsonElement e, boolean def) {
        if (e == null || e.isJsonNull() || !e.isJsonPrimitive()) {
            return def;
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isBoolean()) {
            return p.getAsBoolean();
        }
        if (p.isNumber()) {
            return p.getAsDouble() != 0.0;
        }
        String s = p.getAsString().trim().toLowerCase();
        if (s.equals("1") || s.equals("true") || s.equals("ya") || s.equals("yes")) {
            return true;
        }
        if (s.equals("0") || s.equals("false") || s.isEmpty() || s.equals("tidak")
                || s.equals("no")) {
            return false;
        }
        return def;
    }

    // ------------------------------------------------------------------ object field access

    /** The raw member, or null when missing. May be JsonNull. */
    public static JsonElement get(JsonObject o, String key) {
        return (o == null) ? null : o.get(key);
    }

    /** True when the member exists and is not JSON null. */
    public static boolean has(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull();
    }

    /** Empty string when missing/null. */
    public static String str(JsonObject o, String key) {
        return asStr(get(o, key), "");
    }

    public static String str(JsonObject o, String key, String def) {
        return asStr(get(o, key), def);
    }

    /** null when missing/null (Dart: `String?`). */
    public static String strOrNull(JsonObject o, String key) {
        return has(o, key) ? asStr(get(o, key), "") : null;
    }

    public static int intVal(JsonObject o, String key) {
        return asInt(get(o, key), 0);
    }

    public static int intVal(JsonObject o, String key, int def) {
        return asInt(get(o, key), def);
    }

    public static long longVal(JsonObject o, String key) {
        return asLong(get(o, key), 0L);
    }

    public static double dbl(JsonObject o, String key) {
        return asDbl(get(o, key), 0.0);
    }

    public static boolean bool(JsonObject o, String key) {
        return asBool(get(o, key), false);
    }

    public static boolean bool(JsonObject o, String key, boolean def) {
        return asBool(get(o, key), def);
    }

    /** Nested object, or an EMPTY object when missing. */
    public static JsonObject object(JsonObject o, String key) {
        return obj(get(o, key));
    }

    /** Nested object, or null when missing / not an object. */
    public static JsonObject objectOrNull(JsonObject o, String key) {
        JsonElement e = get(o, key);
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : null;
    }

    /** Nested array, or an EMPTY array when missing. */
    public static JsonArray array(JsonObject o, String key) {
        return arr(get(o, key));
    }

    // ------------------------------------------------------------------ array helpers

    /** The i-th element as an object (EMPTY object when out of range / not an object). */
    public static JsonObject objAt(JsonArray a, int i) {
        return (a != null && i >= 0 && i < a.size()) ? obj(a.get(i)) : new JsonObject();
    }

    public static String strAt(JsonArray a, int i) {
        return (a != null && i >= 0 && i < a.size()) ? asStr(a.get(i), "") : "";
    }

    // ------------------------------------------------------------------ misc

    public static int parseInt(String s, int def) {
        if (s == null) {
            return def;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(t);
            } catch (NumberFormatException e2) {
                return def;
            }
        }
    }
}
