package id.laukpauk.model;

import com.google.gson.JsonElement;

import id.laukpauk.core.Async;
import id.laukpauk.core.Json;
import id.laukpauk.core.Prefs;

/** Port of lib/models/SplashScreenModel.dart. All methods are BLOCKING (call from a background thread). */
public final class SplashScreenModel {
    private SplashScreenModel() {}

    /** Includes Dart's one second Future.delayed. */
    public static boolean checkHasAppStarted() {
        Async.sleep(1000);
        boolean isAppStarted = false;

        Boolean isAppStartedSavedStorage = Prefs.getBoolOrNull("isAppStarted");
        if (isAppStartedSavedStorage != null) {
            if (isAppStartedSavedStorage) {
                isAppStarted = isAppStartedSavedStorage;
            }
        }

        return isAppStarted;
    }

    public static boolean checkHasUserLoggedIn() {
        boolean hasUserLoggedIn = false;
        if (Prefs.getString("authToken") != null) {
            hasUserLoggedIn = true;
        }

        return hasUserLoggedIn;
    }

    public static boolean checkHasOTPSent() {
        return Prefs.getBool("otpHasSent", false);
    }

    public static boolean checkHasOTPConfirmed() {
        return Prefs.getBool("otpHasConfirmed", false);
    }

    /** NETWORK. */
    public static boolean hasOTPConfirmed() {
        JsonElement detailUser = UserModel.getDetailActiveUser();
        if (!Json.isFalse(detailUser)) {
            String isOTPConfirmed = Json.str(Json.obj(detailUser), "statusOTP");
            return "1".equals(isOTPConfirmed);
        }

        return false;
    }
}
