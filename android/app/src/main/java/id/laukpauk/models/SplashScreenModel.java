package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativePreferences;
/** Startup-state checks corresponding to lib/models/SplashScreenModel.dart. */
public final class SplashScreenModel {
 private SplashScreenModel() {}
 public static boolean checkHasAppStarted(Context c) { return NativePreferences.getBoolean(c,"hasAppStarted",false); }
 public static boolean checkHasUserLoggedIn(Context c) { String t=NativePreferences.getAuthToken(c); return t != null && !t.isEmpty(); }
 public static boolean checkHasOTPSent(Context c) { return !NativePreferences.getString(c,"otpSentAt","").isEmpty(); }
 public static boolean checkHasOTPConfirmed(Context c) { return NativePreferences.getBoolean(c,"otpConfirmed",false); }
 public static boolean hasOTPConfirmed(Context c) { return checkHasOTPConfirmed(c); }
}
