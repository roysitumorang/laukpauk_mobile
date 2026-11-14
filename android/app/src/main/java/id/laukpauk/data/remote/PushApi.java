package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.push.PushResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Device registration for Firebase Cloud Messaging. These endpoints are NEW (they replace OneSignal's
 * "external user id"); the backend contract is documented in android/README.md, "Push notifications".
 */
public interface PushApi {

    /** Links this device's FCM token to the signed-in user (idempotent: re-sending the same token is fine). */
    @POST("User/registerFcmToken")
    Call<PushResponses.Register> registerFcmToken(@Body Map<String, Object> body);

    /** Unlinks the token from the user (sign-out). */
    @POST("User/unregisterFcmToken")
    Call<PushResponses.Unregister> unregisterFcmToken(@Body Map<String, Object> body);
}
