package id.laukpauk.data.remote;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Makes Gson tolerate the loosely typed JSON of the Laukpauk backend, so typed response classes don't crash:
 *
 * <ul>
 *   <li>booleans sent as {@code 1}/{@code 0}, {@code "1"}/{@code "0"}, {@code "true"}/{@code "false"};</li>
 *   <li>numbers sent as strings ({@code "12"}, {@code "12.5"}) or as {@code ""} (read as null / 0);</li>
 *   <li>strings sent as numbers or booleans (read as their text);</li>
 *   <li>the literal {@code false} or {@code ""} where an object, list or map is expected ("no result"):
 *       read as null.</li>
 * </ul>
 *
 * The Dart app read every field as `dynamic`; this keeps the same forgiving behaviour with typed classes.
 */
public final class LenientTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();
        if (raw == boolean.class || raw == Boolean.class) {
            return (TypeAdapter<T>) new BooleanAdapter(raw == boolean.class);
        }
        if (raw == int.class || raw == Integer.class || raw == long.class || raw == Long.class
                || raw == double.class || raw == Double.class || raw == float.class || raw == Float.class) {
            return (TypeAdapter<T>) new NumberAdapter(raw);
        }
        if (raw == String.class) {
            return (TypeAdapter<T>) new StringAdapter();
        }
        if (raw.isPrimitive() || raw == JsonElement.class || JsonElement.class.isAssignableFrom(raw)
                || raw.isEnum()) {
            return null;
        }
        // objects, lists, maps: "false" / "" mean "nothing"
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final boolean container = Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw)
                || raw.isArray() || !raw.getName().startsWith("java.");
        if (!container) {
            return null;
        }
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonToken t = in.peek();
                if (t == JsonToken.BOOLEAN) {
                    in.nextBoolean();
                    return null;
                }
                if (t == JsonToken.STRING || t == JsonToken.NUMBER) {
                    String s = in.nextString();
                    if (s.trim().isEmpty() || s.equals("0") || s.equalsIgnoreCase("false")) {
                        return null;
                    }
                    // a JSON document encoded as a string ("{\"a\":1}")
                    JsonElement parsed = com.google.gson.JsonParser.parseString(s);
                    return delegate.fromJsonTree(parsed);
                }
                return delegate.read(in);
            }
        };
    }

    private static final class BooleanAdapter extends TypeAdapter<Boolean> {
        private final boolean primitive;

        BooleanAdapter(boolean primitive) {
            this.primitive = primitive;
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return primitive ? Boolean.FALSE : null;
                case BOOLEAN:
                    return in.nextBoolean();
                case NUMBER:
                    return in.nextDouble() != 0;
                case STRING:
                    String s = in.nextString().trim();
                    return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes");
                default:
                    in.skipValue();
                    return primitive ? Boolean.FALSE : null;
            }
        }
    }

    private static final class NumberAdapter extends TypeAdapter<Number> {
        private final Class<?> type;

        NumberAdapter(Class<?> type) {
            this.type = type;
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            double d;
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return type.isPrimitive() ? convert(0) : null;
                case NUMBER:
                    if (type == long.class || type == Long.class) {
                        String raw = in.nextString();
                        try {
                            return Long.parseLong(raw);
                        } catch (NumberFormatException e) {
                            return (long) Double.parseDouble(raw);
                        }
                    }
                    d = in.nextDouble();
                    return convert(d);
                case STRING:
                    String s = in.nextString().trim();
                    if (s.isEmpty()) {
                        return type.isPrimitive() ? convert(0) : null;
                    }
                    try {
                        return convert(Double.parseDouble(s.replace(",", ".")));
                    } catch (NumberFormatException e) {
                        return type.isPrimitive() ? convert(0) : null;
                    }
                case BOOLEAN:
                    return convert(in.nextBoolean() ? 1 : 0);
                default:
                    in.skipValue();
                    return type.isPrimitive() ? convert(0) : null;
            }
        }

        private Number convert(double d) {
            if (type == int.class || type == Integer.class) return (int) d;
            if (type == long.class || type == Long.class) return (long) d;
            if (type == float.class || type == Float.class) return (float) d;
            return d;
        }
    }

    private static final class StringAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                case BOOLEAN:
                    return String.valueOf(in.nextBoolean());
                case NUMBER:
                case STRING:
                    return in.nextString();
                default:
                    // an object or array where text was expected: keep its JSON text
                    JsonElement e = com.google.gson.JsonParser.parseReader(in);
                    return e == null || e.isJsonNull() ? null : e.toString();
            }
        }
    }

    /** Helper for code that still receives a raw primitive. */
    static boolean asBoolean(JsonPrimitive p) {
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) return p.getAsDouble() != 0;
        String s = p.getAsString().trim();
        return s.equals("1") || s.equalsIgnoreCase("true");
    }
}
