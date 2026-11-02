package id.laukpauk.util;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moshi adapter for untyped JSON ({@code Object}): objects become ordered {@code LinkedHashMap}s, arrays
 * {@code ArrayList}s. Unlike Moshi's built-in one, whole numbers are read as {@code Long} (and decimals as
 * {@code Double}), so ids and amounts written back as JSON or turned into text stay "1500", not "1500.0".
 */
public final class JsonTreeAdapter extends JsonAdapter<Object> {

    @Nullable
    @Override
    public Object fromJson(JsonReader in) throws IOException {
        switch (in.peek()) {
            case BEGIN_OBJECT: {
                Map<String, Object> map = new LinkedHashMap<>();
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    map.put(name, fromJson(in));
                }
                in.endObject();
                return map;
            }
            case BEGIN_ARRAY: {
                List<Object> list = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    list.add(fromJson(in));
                }
                in.endArray();
                return list;
            }
            case STRING:
                return in.nextString();
            case NUMBER:
                return number(in.nextString());
            case BOOLEAN:
                return in.nextBoolean();
            case NULL:
                return in.nextNull();
            default:
                in.skipValue();
                return null;
        }
    }

    @Override
    public void toJson(JsonWriter out, @Nullable Object value) throws IOException {
        out.jsonValue(value);
    }

    private static Number number(String s) {
        if (s.matches("-?\\d{1,18}")) {
            return Long.parseLong(s);
        }
        double d = Double.parseDouble(s);
        if (d == Math.rint(d) && Math.abs(d) < 1e15 && !s.contains(".")) {
            return (long) d; // 1e3
        }
        return d;
    }
}
