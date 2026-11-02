package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Makes Moshi accept the backend's loosely typed JSON:
 * <ul>
 *   <li>booleans: {@code true/false}, {@code 1/0}, {@code "1"/"0"}, {@code "true"/"yes"};</li>
 *   <li>numbers: JSON numbers or numeric strings ({@code "1500"}, {@code "1,5"}); {@code ""}/null → 0 for primitives;</li>
 *   <li>strings: numbers and booleans are accepted as text; an object/array where text was expected keeps its JSON;</li>
 *   <li>DTOs, lists and maps: {@code false}, {@code ""}, {@code "0"} mean "nothing" (null); a JSON document
 *       encoded as a string ({@code "{\"a\":1}"}) is decoded.</li>
 * </ul>
 * {@code Object} fields (untyped trees, see {@link id.laukpauk.util.Json}) are left to Moshi.
 */
public final class LenientJsonAdapterFactory implements JsonAdapter.Factory {

    @Nullable
    @Override
    public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
        if (!annotations.isEmpty()) {
            return null;
        }
        Class<?> raw = Types.getRawType(type);
        if (raw == boolean.class || raw == Boolean.class) {
            return new BooleanAdapter(raw == boolean.class);
        }
        if (raw == int.class || raw == Integer.class || raw == long.class || raw == Long.class
                || raw == double.class || raw == Double.class || raw == float.class || raw == Float.class) {
            return new NumberAdapter(raw);
        }
        if (raw == String.class) {
            return new StringAdapter();
        }
        boolean container = Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw)
                || raw.isArray() || !(raw.isPrimitive() || raw.isEnum() || isPlatform(raw));
        if (!container) {
            return null;
        }
        return new EmptyMeansNullAdapter<>(moshi.nextAdapter(this, type, annotations));
    }

    private static boolean isPlatform(Class<?> c) {
        String n = c.getName();
        return n.startsWith("java.") || n.startsWith("javax.") || n.startsWith("android.")
                || n.startsWith("kotlin.");
    }

    private static final class EmptyMeansNullAdapter<T> extends JsonAdapter<T> {
        private final JsonAdapter<T> delegate;

        EmptyMeansNullAdapter(JsonAdapter<T> delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public T fromJson(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    return in.nextNull();
                case BOOLEAN:
                    in.nextBoolean();
                    return null;
                case STRING:
                case NUMBER:
                    String s = in.nextString();
                    String t = s.trim();
                    if (t.isEmpty() || t.equals("0") || t.equalsIgnoreCase("false")) {
                        return null;
                    }
                    try {
                        return delegate.lenient().fromJson(t);
                    } catch (IOException | RuntimeException e) {
                        return null;
                    }
                default:
                    return delegate.fromJson(in);
            }
        }

        @Override
        public void toJson(JsonWriter out, @Nullable T value) throws IOException {
            delegate.toJson(out, value);
        }
    }

    private static final class BooleanAdapter extends JsonAdapter<Boolean> {
        private final boolean primitive;

        BooleanAdapter(boolean primitive) {
            this.primitive = primitive;
        }

        @Nullable
        @Override
        public Boolean fromJson(JsonReader in) throws IOException {
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

        @Override
        public void toJson(JsonWriter out, @Nullable Boolean value) throws IOException {
            out.value(value);
        }
    }

    private static final class NumberAdapter extends JsonAdapter<Number> {
        private final Class<?> type;

        NumberAdapter(Class<?> type) {
            this.type = type;
        }

        @Nullable
        @Override
        public Number fromJson(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return empty();
                case NUMBER:
                case STRING:
                    String s = in.nextString().trim();
                    if (s.isEmpty()) {
                        return empty();
                    }
                    if (type == long.class || type == Long.class) {
                        try {
                            return Long.parseLong(s);
                        } catch (NumberFormatException ignored) {
                            // fall through to the double parse
                        }
                    }
                    try {
                        return convert(Double.parseDouble(s.replace(",", ".")));
                    } catch (NumberFormatException e) {
                        return empty();
                    }
                case BOOLEAN:
                    return convert(in.nextBoolean() ? 1 : 0);
                default:
                    in.skipValue();
                    return empty();
            }
        }

        @Override
        public void toJson(JsonWriter out, @Nullable Number value) throws IOException {
            out.value(value);
        }

        @Nullable
        private Number empty() {
            return type.isPrimitive() ? convert(0) : null;
        }

        private Number convert(double d) {
            if (type == int.class || type == Integer.class) return (int) d;
            if (type == long.class || type == Long.class) return (long) d;
            if (type == float.class || type == Float.class) return (float) d;
            return d;
        }
    }

    private static final class StringAdapter extends JsonAdapter<String> {
        private static final JsonAdapter<Object> TREE = new id.laukpauk.util.JsonTreeAdapter();

        @Nullable
        @Override
        public String fromJson(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    return in.nextNull();
                case BOOLEAN:
                    return String.valueOf(in.nextBoolean());
                case NUMBER:
                case STRING:
                    return in.nextString();
                default:
                    // an object or array where text was expected: keep its JSON text
                    Object tree = TREE.fromJson(in);
                    return tree == null ? null : id.laukpauk.util.Json.toJson(tree);
            }
        }

        @Override
        public void toJson(JsonWriter out, @Nullable String value) throws IOException {
            out.value(value);
        }
    }
}
