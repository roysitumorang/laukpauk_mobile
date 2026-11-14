package id.laukpauk.data.model.auth;


/** Response wrappers of the /Auth and /User/getDetailUser endpoints used by the auth screens. */
public final class AuthResponses {
    private AuthResponses() {}

    /** Auth/loginAuthentication. */
    public static class Login {
        public boolean loginAuthentication;
        public String authToken;
        public String idUser;
        public boolean isVerified;
    }

    /** returnData of several responses (only the fields the screens read). */
    public static class ReturnData {
        public String idUser;
        public String message;
        public String authToken;
    }

    /** Auth/register. {@code statusRegister} stays null when the server omits it (legacy treated it as "not false"). */
    public static class Register {
        public Boolean statusRegister;
        public ReturnData returnData;
    }

    /** Auth/konfirmasiOTP. */
    public static class KonfirmasiOTP {
        public boolean statusKonfirmasiOTP;
        public ReturnData returnData;
    }

    /** Auth/verifikasiUser. */
    public static class VerifikasiUser {
        public boolean statusVerifikasiUser;
    }

    /** Auth/resendOTP. */
    public static class ResendOTP {
        public boolean statusResendOTP;
        public String messageResendOTP;
    }

    /** Auth/lupaPassword. */
    public static class LupaPassword {
        public boolean statusRecoveryPassword;
        public ReturnData returnData;
    }

    /** User/getDetailUser (the fields the auth screens need). */
    public static class DetailUserResponse {
        public DetailUser detailUser;
    }

    public static class DetailUser {
        public String nomorHP;
    }
}
