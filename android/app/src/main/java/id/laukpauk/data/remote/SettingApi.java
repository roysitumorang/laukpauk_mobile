package id.laukpauk.data.remote;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Server settings ("Pengaturan"). */
public interface SettingApi {

    /** {@code {"settingValue": <any>}}. */
    @GET("Pengaturan/getSettingValue/{key}")
    Call<JsonObject> getSettingValue(@Path("key") String key);

    /** {@code {"contactUsWhatsappConfiguration": <object|false>}}. */
    @GET("Pengaturan/getContactUsWhatsappConfiguration")
    Call<JsonObject> getContactUsWhatsappConfiguration();
}
