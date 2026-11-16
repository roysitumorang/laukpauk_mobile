package id.laukpauk.data.model.auth;

import com.squareup.moshi.Json;


/** Response wrappers of the /Auth (other than sign-in) and /User/getDetailUser endpoints used by the auth screens. */
public final class AuthResponses {
    private AuthResponses() {}

    /** returnData of several responses (only the fields the screens read). */
    public static class ReturnData {
        public String idUser;
        public String message;
        public String authToken;
    }

    /** Auth/konfirmasiOTP. */
    public static class ConfirmOtp {
        @Json(name = "statusKonfirmasiOTP")
        public boolean otpConfirmationStatus;
        public ReturnData returnData;
    }

    /** Auth/verifikasiUser. */
    public static class UserVerification {
        @Json(name = "statusVerifikasiUser")
        public boolean userVerificationStatus;
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

    /** User/getDetailUser (the fields the auth screens need). */
    public static class DetailUserResponse {
        public DetailUser detailUser;
    }

    public static class DetailUser {
        @Json(name = "nomorHP")
        public String phoneNumber;
    }
}
