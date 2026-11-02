package id.laukpauk.util;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Null-safe, type-tolerant helpers for untyped JSON, i.e. the tree Moshi produces for {@code Object}:
 * a JSON object is a {@code Map<String, Object>}, an array a {@code List<Object>}, a number a
 * {@code Double}, plus {@code String}, {@code Boolean} and {@code null}.
 *
 * Use it only for genuinely dynamic payloads (route arguments, settings, the stored delivery schedule);
 * API responses are typed DTOs. The backend is loosely typed (ids as strings, counters as numbers, flags as
 * true/"1"/1, "no result" as the literal false), so the accessors coerce instead of throwing.
 */
public final class Json {
    private Json() {}

    private static final JsonAdapter<Object> TREE = new JsonTreeAdapter().lenient();

    // ------------------------------------------------------------------ parsing / building

    /** Parses text into a tree. Returns null when the text is empty or not valid JSON. */
    @Nullable
    public static Object parse(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return TREE.fromJson(text);
        } catch (Exception e) {
            return null;
        }
    }

    /** Parses text as an object; returns an empty (mutable) object when it is not one. */
    public static Map<String, Object> parseObject(@Nullable String text) {
        return obj(parse(text));
    }

    /** Parses text as an array; returns an empty (mutable) array when it is not one. */
    public static List<Object> parseArray(@Nullable String text) {
        return arr(parse(text));
    }

    /** A new, ordered, mutable JSON object. */
    public static Map<String, Object> newObject() {
        return new LinkedHashMap<>();
    }

    /** Builds an object from alternating key, value arguments: Json.of("a", 1, "b", "x"). */
    public static Map<String, Object> of(Object... keyValues) {
        Map<String, Object> o = newObject();
        if (keyValues != null) {
            for (int i = 0; i + 1 < keyValues.length; i += 2) {
                o.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
            }
        }
        return o;
    }

    /** Serializes a tree (maps, lists, strings, numbers, booleans, null) to JSON text. */
    public static String toJson(@Nullable Object tree) {
        return TREE.toJson(tree);
    }

    // ------------------------------------------------------------------ type tests

    public static boolean isObject(@Nullable Object e) {
        return e instanceof Map;
    }

    public static boolean isArray(@Nullable Object e) {
        return e instanceof List;
    }

    /** True for the literal JSON {@code false} (the API answers {@code false} for "nothing found"). */
    public static boolean isFalse(@Nullable Object e) {
        return Boolean.FALSE.equals(e);
    }

    // ------------------------------------------------------------------ element coercion

    /** The element as an object, or a new EMPTY object (never null). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(@Nullable Object e) {
        return e instanceof Map ? (Map<String, Object>) e : newObject();
    }

    /** The element as an array, or a new EMPTY array (never null). */
    @SuppressWarnings("unchecked")
    public static List<Object> arr(@Nullable Object e) {
        return e instanceof List ? (List<Object>) e : new ArrayList<>();
    }

    public static String asStr(@Nullable Object e) {
        return asStr(e, "");
    }

    /** Text of a scalar; whole numbers without ".0" (3.0 -> "3"); objects/arrays as JSON text. */
    public static String asStr(@Nullable Object e, String def) {
        if (e == null) {
            return def;
        }
        if (e instanceof Number) {
            double d = ((Number) e).doubleValue();
            if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                return String.valueOf((long) d);
            }
            return String.valueOf(e);
        }
        if (e instanceof String || e instanceof Boolean) {
            return String.valueOf(e);
        }
        return toJson(e);
    }

    public static int asInt(@Nullable Object e, int def) {
        if (e instanceof Number) {
            return (int) ((Number) e).doubleValue();
        }
        if (e instanceof Boolean) {
            return (Boolean) e ? 1 : 0;
        }
        if (e instanceof String) {
            return parseInt((String) e, def);
        }
        return def;
    }

    public static long asLong(@Nullable Object e, long def) {
        if (e instanceof Number) {
            return (long) ((Number) e).doubleValue();
        }
        if (e instanceof Boolean) {
            return (Boolean) e ? 1L : 0L;
        }
        if (e instanceof String) {
            String t = ((String) e).trim();
            try {
                return Long.parseLong(t);
            } catch (NumberFormatException ex) {
                try {
                    return (long) Double.parseDouble(t);
                } catch (NumberFormatException ex2) {
                    return def;
                }
            }
        }
        return def;
    }

    public static double asDbl(@Nullable Object e, double def) {
        if (e instanceof Number) {
            return ((Number) e).doubleValue();
        }
        if (e instanceof Boolean) {
            return (Boolean) e ? 1.0 : 0.0;
        }
        if (e instanceof String) {
            try {
                return Double.parseDouble(((String) e).trim());
            } catch (NumberFormatException ex) {
                return def;
            }
        }
        return def;
    }

    /** Truthy values: true, 1, "1", "true", "ya", "yes". Falsy: false, 0, "0", "false", "", "tidak", "no". */
    public static boolean asBool(@Nullable Object e, boolean def) {
        if (e instanceof Boolean) {
            return (Boolean) e;
        }
        if (e instanceof Number) {
            return ((Number) e).doubleValue() != 0.0;
        }
        if (e instanceof String) {
            String s = ((String) e).trim().toLowerCase();
            if (s.equals("1") || s.equals("true") || s.equals("ya") || s.equals("yes")) {
                return true;
            }
            if (s.equals("0") || s.equals("false") || s.isEmpty() || s.equals("tidak") || s.equals("no")) {
                return false;
            }
        }
        return def;
    }

    // ------------------------------------------------------------------ object field access

    /** The raw member, or null when missing or JSON null. */
    @Nullable
    public static Object get(@Nullable Map<String, ?> o, String key) {
        return o == null ? null : o.get(key);
    }

    /** True when the member exists and is not JSON null. */
    public static boolean has(@Nullable Map<String, ?> o, String key) {
        return o != null && o.get(key) != null;
    }

    /** Empty string when missing/null. */
    public static String str(@Nullable Map<String, ?> o, String key) {
        return asStr(get(o, key), "");
    }

    public static String str(@Nullable Map<String, ?> o, String key, String def) {
        return asStr(get(o, key), def);
    }

    /** null when missing/null. */
    @Nullable
    public static String strOrNull(@Nullable Map<String, ?> o, String key) {
        return has(o, key) ? asStr(get(o, key), "") : null;
    }

    public static int intVal(@Nullable Map<String, ?> o, String key) {
        return asInt(get(o, key), 0);
    }

    public static int intVal(@Nullable Map<String, ?> o, String key, int def) {
        return asInt(get(o, key), def);
    }

    public static long longVal(@Nullable Map<String, ?> o, String key) {
        return asLong(get(o, key), 0L);
    }

    public static double dbl(@Nullable Map<String, ?> o, String key) {
        return asDbl(get(o, key), 0.0);
    }

    public static boolean bool(@Nullable Map<String, ?> o, String key) {
        return asBool(get(o, key), false);
    }

    public static boolean bool(@Nullable Map<String, ?> o, String key, boolean def) {
        return asBool(get(o, key), def);
    }

    /** Nested object, or a new EMPTY object when missing. */
    public static Map<String, Object> object(@Nullable Map<String, ?> o, String key) {
        return obj(get(o, key));
    }

    /** Nested object, or null when missing / not an object. */
    @Nullable
    public static Map<String, Object> objectOrNull(@Nullable Map<String, ?> o, String key) {
        Object e = get(o, key);
        return e instanceof Map ? obj(e) : null;
    }

    /** Nested array, or a new EMPTY array when missing. */
    public static List<Object> array(@Nullable Map<String, ?> o, String key) {
        return arr(get(o, key));
    }

    // ------------------------------------------------------------------ array helpers

    /** The i-th element as an object (EMPTY object when out of range / not an object). */
    public static Map<String, Object> objAt(@Nullable List<?> a, int i) {
        return a != null && i >= 0 && i < a.size() ? obj(a.get(i)) : newObject();
    }

    public static String strAt(@Nullable List<?> a, int i) {
        return a != null && i >= 0 && i < a.size() ? asStr(a.get(i), "") : "";
    }

    /** An unmodifiable empty list when null. */
    public static <T> List<T> nonNull(@Nullable List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    // ------------------------------------------------------------------ misc

    public static int parseInt(@Nullable String s, int def) {
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
