package id.laukpauk.data.repository;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.data.remote.SettingApi;

/**
 * Server settings (legacy SettingModel). Values are returned as raw JSON because each setting has its
 * own shape; {@code null} data means the setting is missing / null / false on the server.
 */
public class SettingRepository extends BaseRepository {

    private final SettingApi api;

    public SettingRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(SettingApi.class);
    }

    /** One setting value, e.g. {@code "costPerKm"}. */
    public void getSettingValue(String key, ResultCallback<JsonElement> callback) {
        enqueue(api.getSettingValue(key), body -> value(body, "settingValue"), callback);
    }

    /** The WhatsApp "contact us" configuration object. */
    public void getContactUsWhatsappConfiguration(ResultCallback<JsonElement> callback) {
        enqueue(api.getContactUsWhatsappConfiguration(), body -> value(body, "contactUsWhatsappConfiguration"), callback);
    }

    /** A setting as a String (numbers/booleans stringified), or null. */
    @Nullable
    public static String asString(@Nullable JsonElement value) {
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    @Nullable
    private static JsonElement value(@Nullable JsonObject body, String key) {
        if (body == null || !body.has(key)) {
            return null;
        }
        JsonElement e = body.get(key);
        if (e == null || e.isJsonNull()) {
            return null;
        }
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() && !e.getAsBoolean()) {
            return null;
        }
        return e;
    }
}
