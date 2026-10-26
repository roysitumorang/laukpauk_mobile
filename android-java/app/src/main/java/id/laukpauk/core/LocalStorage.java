package id.laukpauk.core;

import androidx.core.content.pm.PackageInfoCompat;
import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Calendar;

import id.laukpauk.BuildConfig;

/**
 * Port of lib/libraries/LocalStorage.dart.
 * Methods are synchronous; the ones that hit the network (marked NETWORK) must be called from a
 * background thread (Async.run / BaseActivity.async).
 */
public final class LocalStorage {
    private LocalStorage() {}

    public static boolean isAlamatDeliveryDanJadwalDeliverySet() {
        return Prefs.getBool("isAlamatDeliveryDanJadwalDeliverySet", false);
    }

    /**
     * Keys: latitude, longitude, jadwalDeliveryCode, alamat. Missing values are JSON null
     * (read them with Json.strOrNull).
     */
    public static JsonObject getSavedLatitudeAndLongitude() {
        JsonObject o = new JsonObject();
        o.addProperty("latitude", Prefs.getString("savedLatitude"));
        o.addProperty("longitude", Prefs.getString("savedLongitude"));
        o.addProperty("jadwalDeliveryCode", Prefs.getString("savedJadwalDeliveryCode"));
        o.addProperty("alamat", Prefs.getString("savedAlamat"));
        return o;
    }

    /** Used for Geocoding / Places web-service calls. */
    public static String getGoogleMapsApiKey() {
        return BuildConfig.MAPS_API_KEY;
    }

    public static String getAuthToken() {
        return Prefs.getString("authToken", "");
    }

    public static String getCurrentUserID() {
        return Prefs.getString("idUser", "");
    }

    /** Creates (and stores) the anonymous cart code on first use: HHmmssSSS with Dart's padding quirks. */
    public static String getCartCode() {
        String cartCode = Prefs.getString("cartCode");
        if (cartCode == null) {
            Calendar now = Calendar.getInstance();
            int jam = now.get(Calendar.HOUR_OF_DAY);
            int menit = now.get(Calendar.MINUTE);
            int detik = now.get(Calendar.SECOND);
            int mili = now.get(Calendar.MILLISECOND);

            String jamString = String.valueOf(jam);
            if (jam <= 9) {
                jamString = "0" + jamString;
            }
            String menitString = String.valueOf(menit);
            if (menit <= 9) {
                menitString = "0" + menitString;
            }
            String detikString = String.valueOf(detik);
            if (detik <= 9) {
                detikString = "0" + detikString;
            }
            String miliString = String.valueOf(mili);
            if (mili <= 9) {
                miliString = "0" + miliString;
            }

            cartCode = jamString + menitString + detikString + miliString;
            Prefs.setString("cartCode", cartCode);
        }
        return cartCode;
    }

    public static void clearCartCode() {
        Prefs.remove("cartCode");
    }

    public static String getUsedVoucher() {
        return Prefs.getString("usedVoucher");
    }

    public static void setUsedVoucher(String idVoucher) {
        Prefs.setString("usedVoucher", idVoucher);
    }

    public static void clearUsedVoucher() {
        Prefs.remove("usedVoucher");
    }

    /** NETWORK. Returns 'kadaluarsa' when the request fails. */
    public static String getStatusVoucher(String idVoucher) {
        String authToken = getAuthToken();
        Http.Response r = Http.getApi("/Voucher/getStatusVoucher/" + idVoucher
                + "?authToken=" + authToken);
        if (r.statusCode == 200) {
            return Json.str(r.jsonObject(), "statusVoucher");
        }
        return "kadaluarsa";
    }

    public static void setExternalUserID(String externalUserID) {
        Prefs.setString("externalUserID", externalUserID);
    }

    /** null when unset. */
    public static String getExternalUserID() {
        return Prefs.getString("externalUserID");
    }

    public static void removeExternalUserID() {
        Prefs.remove("externalUserID");
    }

    /** NETWORK. 0 when not logged in / request failed. */
    public static int getIdPenjualFromActiveUser() {
        String authToken = getAuthToken();
        Http.Response r = Http.getApi("/User/getDetailUser?getUserEntities=id&authToken="
                + authToken);
        if (r.statusCode == 200) {
            JsonElement detailUser = Json.obj(r.json()).get("detailUser");
            if (detailUser == null || Json.isFalse(detailUser) || !detailUser.isJsonObject()) {
                return 0;
            }
            return Json.intVal(detailUser.getAsJsonObject(), "id", 0);
        }
        return 0;
    }

    public static String getApplicationID() {
        return "id.laukpauk";
    }

    /** NETWORK. */
    public static boolean isUserVerified() {
        String authToken = getAuthToken();
        Http.Response r = Http.getApi("/Auth/cekVerifikasiUser?authToken=" + authToken);
        if (r.statusCode == 200) {
            return Json.bool(r.jsonObject(), "isVerified");
        }
        return false;
    }

    /** The saved delivery schedule (JSON map) or null. */
    public static JsonObject savedJadwalDeliveryMap() {
        String encoded = Prefs.getString("savedJadwalDeliveryMap");
        if (encoded != null) {
            JsonElement e = Json.parse(encoded);
            if (e != null && e.isJsonObject()) {
                return e.getAsJsonObject();
            }
        }
        return null;
    }

    public static void lewatiOTPRegistrasi() {
        Prefs.setBool("lewatiOTPRegistrasi", true);
    }

    public static boolean isOTPRegistrasiPassed() {
        return Prefs.getBool("lewatiOTPRegistrasi", false);
    }

    /** The Android versionCode as a string (Flutter's PackageInfo.buildNumber). */
    public static String getBuildNumber() {
        try {
            Context ctx = App.context();
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return String.valueOf(PackageInfoCompat.getLongVersionCode(info));
        } catch (Exception e) {
            return String.valueOf(BuildConfig.VERSION_CODE);
        }
    }

    public static void setPasarPalingDekat(JsonObject pasarPalingDekat) {
        Prefs.setString("pasarPalingDekat", pasarPalingDekat.toString());
    }

    /** null when nothing is stored. */
    public static JsonObject getPasarPalingDekat() {
        String s = Prefs.getString("pasarPalingDekat");
        if (s == null) {
            return null;
        }
        JsonElement e = Json.parse(s);
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : null;
    }
}
