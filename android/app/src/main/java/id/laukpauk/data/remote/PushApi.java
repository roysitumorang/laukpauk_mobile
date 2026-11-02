package id.laukpauk.data.remote;

import id.laukpauk.data.model.push.PushResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Device registration for Firebase Cloud Messaging. These endpoints are NEW (they replace OneSignal's
 * "external user id"); the backend contract is documented in android/README.md, "Push notifications".
 */
public interface PushApi {

    /** Links this device's FCM token to the signed-in user (idempotent: re-sending the same token is fine). */
    @FormUrlEncoded
    @POST("User/registerFcmToken")
    Call<PushResponses.Register> registerFcmToken(@Field("authToken") String authToken,
                                                  @Field("fcmToken") String fcmToken,
                                                  @Field("platform") String platform);

    /** Unlinks the token from the user (sign-out). */
    @FormUrlEncoded
    @POST("User/unregisterFcmToken")
    Call<PushResponses.Unregister> unregisterFcmToken(@Field("authToken") String authToken,
                                                      @Field("fcmToken") String fcmToken);
}
