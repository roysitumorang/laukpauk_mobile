package id.laukpauk.data.model.auth;

import com.squareup.moshi.Json;


/** Response wrappers of the /Auth endpoints (other than sign-in) used by the auth screens. */
public final class AuthResponses {
    private AuthResponses() {}

    /** returnData of several responses (only the fields the screens read). */
    public static class ReturnData {
        public String idUser;
        public String message;
        public String authToken;
    }

    /** Auth/resendOTP. */
    public static class ResendOTP {
        public boolean statusResendOTP;
        public String messageResendOTP;
    }

    /** Auth/lupaPassword. */
    public static class ForgotPassword {
        public boolean statusRecoveryPassword;
        public ReturnData returnData;
    }

}
