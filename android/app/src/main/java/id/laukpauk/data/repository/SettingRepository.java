package id.laukpauk.data.repository;

import id.laukpauk.util.Json;

import java.util.Map;

import androidx.annotation.Nullable;


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
    public void getSettingValue(String key, ResultCallback<Object> callback) {
        enqueue(api.getSettingValue(key), body -> value(body, "settingValue"), callback);
    }

    /** The WhatsApp "contact us" configuration object. */
    public void getContactUsWhatsappConfiguration(ResultCallback<Object> callback) {
        enqueue(api.getContactUsWhatsappConfiguration(), body -> value(body, "contactUsWhatsappConfiguration"), callback);
    }

    /** A setting as a String (numbers/booleans stringified), or null. */
    @Nullable
    public static String asString(@Nullable Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean ? Json.asStr(value) : null;
    }

    @Nullable
    private static Object value(@Nullable Map<String, Object> body, String key) {
        Object e = Json.get(body, key);
        return e == null || Json.isFalse(e) ? null : e;
    }
}
