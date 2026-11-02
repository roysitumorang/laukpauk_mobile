package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonElement;

import id.laukpauk.core.Async;
import id.laukpauk.util.Json;
import id.laukpauk.data.local.Prefs;

/** Port of lib/models/SplashScreenModel.dart. All methods are BLOCKING (call from a background thread). */
public final class SplashScreenModel {
    private final Prefs prefs;
    private final UserModel userModel;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public SplashScreenModel(Prefs prefs, UserModel userModel) {
        this.prefs = prefs;
        this.userModel = userModel;
    }

    /** Includes Dart's one second Future.delayed. */
    public boolean checkHasAppStarted() {
        Async.sleep(1000);
        boolean isAppStarted = false;

        Boolean isAppStartedSavedStorage = prefs.getBoolOrNull("isAppStarted");
        if (isAppStartedSavedStorage != null) {
            if (isAppStartedSavedStorage) {
                isAppStarted = isAppStartedSavedStorage;
            }
        }

        return isAppStarted;
    }

    public boolean checkHasUserLoggedIn() {
        boolean hasUserLoggedIn = false;
        if (prefs.getString("authToken") != null) {
            hasUserLoggedIn = true;
        }

        return hasUserLoggedIn;
    }

    public boolean checkHasOTPSent() {
        return prefs.getBool("otpHasSent", false);
    }

    public boolean checkHasOTPConfirmed() {
        return prefs.getBool("otpHasConfirmed", false);
    }

    /** NETWORK. */
    public boolean hasOTPConfirmed() {
        JsonElement detailUser = userModel.getDetailActiveUser();
        if (!Json.isFalse(detailUser)) {
            String isOTPConfirmed = Json.str(Json.obj(detailUser), "statusOTP");
            return "1".equals(isOTPConfirmed);
        }

        return false;
    }
}
