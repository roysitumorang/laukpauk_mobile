package id.laukpauk.data.remote;

import java.util.Map;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Server settings ("Pengaturan"). */
public interface SettingApi {

    /** {@code {"settingValue": <any>}}. */
    @GET("Pengaturan/getSettingValue/{key}")
    Call<Map<String, Object>> getSettingValue(@Path("key") String key);

    /** {@code {"contactUsWhatsappConfiguration": <object|false>}}. */
    @GET("Pengaturan/getContactUsWhatsappConfiguration")
    Call<Map<String, Object>> getContactUsWhatsappConfiguration();
}
