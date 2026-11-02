package id.laukpauk.data.local;

import id.laukpauk.core.AppContainer;
import id.laukpauk.core.Async;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;
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
    private final Context context;
    private final Http http;
    private final Prefs prefs;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public LocalStorage(Context context, Http http, Prefs prefs) {
        this.context = context;
        this.http = http;
        this.prefs = prefs;
    }

    public boolean isAlamatDeliveryDanJadwalDeliverySet() {
        return prefs.getBool("isAlamatDeliveryDanJadwalDeliverySet", false);
    }

    /**
     * Keys: latitude, longitude, jadwalDeliveryCode, alamat. Missing values are JSON null
     * (read them with Json.strOrNull).
     */
    public JsonObject getSavedLatitudeAndLongitude() {
        JsonObject o = new JsonObject();
        o.addProperty("latitude", prefs.getString("savedLatitude"));
        o.addProperty("longitude", prefs.getString("savedLongitude"));
        o.addProperty("jadwalDeliveryCode", prefs.getString("savedJadwalDeliveryCode"));
        o.addProperty("alamat", prefs.getString("savedAlamat"));
        return o;
    }

    /** Used for Geocoding / Places web-service calls. */
    public String getGoogleMapsApiKey() {
        return BuildConfig.MAPS_API_KEY;
    }

    public String getAuthToken() {
        return prefs.getString("authToken", "");
    }

    public String getCurrentUserID() {
        return prefs.getString("idUser", "");
    }

    /** Creates (and stores) the anonymous cart code on first use: HHmmssSSS with Dart's padding quirks. */
    public String getCartCode() {
        String cartCode = prefs.getString("cartCode");
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
            prefs.setString("cartCode", cartCode);
        }
        return cartCode;
    }

    public void clearCartCode() {
        prefs.remove("cartCode");
    }

    public String getUsedVoucher() {
        return prefs.getString("usedVoucher");
    }

    public void setUsedVoucher(String idVoucher) {
        prefs.setString("usedVoucher", idVoucher);
    }

    public void clearUsedVoucher() {
        prefs.remove("usedVoucher");
    }

    /** NETWORK. Returns 'kadaluarsa' when the request fails. */
    public String getStatusVoucher(String idVoucher) {
        String authToken = getAuthToken();
        Http.Response r = http.getApi("/Voucher/getStatusVoucher/" + idVoucher
                + "?authToken=" + authToken);
        if (r.statusCode == 200) {
            return Json.str(r.jsonObject(), "statusVoucher");
        }
        return "kadaluarsa";
    }

    public void setExternalUserID(String externalUserID) {
        prefs.setString("externalUserID", externalUserID);
    }

    /** null when unset. */
    public String getExternalUserID() {
        return prefs.getString("externalUserID");
    }

    public void removeExternalUserID() {
        prefs.remove("externalUserID");
    }

    /** NETWORK. 0 when not logged in / request failed. */
    public int getIdPenjualFromActiveUser() {
        String authToken = getAuthToken();
        Http.Response r = http.getApi("/User/getDetailUser?getUserEntities=id&authToken="
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

    public String getApplicationID() {
        return "id.laukpauk";
    }

    /** NETWORK. */
    public boolean isUserVerified() {
        String authToken = getAuthToken();
        Http.Response r = http.getApi("/Auth/cekVerifikasiUser?authToken=" + authToken);
        if (r.statusCode == 200) {
            return Json.bool(r.jsonObject(), "isVerified");
        }
        return false;
    }

    /** The saved delivery schedule (JSON map) or null. */
    public JsonObject savedJadwalDeliveryMap() {
        String encoded = prefs.getString("savedJadwalDeliveryMap");
        if (encoded != null) {
            JsonElement e = Json.parse(encoded);
            if (e != null && e.isJsonObject()) {
                return e.getAsJsonObject();
            }
        }
        return null;
    }

    public void lewatiOTPRegistrasi() {
        prefs.setBool("lewatiOTPRegistrasi", true);
    }

    public boolean isOTPRegistrasiPassed() {
        return prefs.getBool("lewatiOTPRegistrasi", false);
    }

    /** The Android versionCode as a string (Flutter's PackageInfo.buildNumber). */
    public String getBuildNumber() {
        try {
            Context ctx = context;
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return String.valueOf(PackageInfoCompat.getLongVersionCode(info));
        } catch (Exception e) {
            return String.valueOf(BuildConfig.VERSION_CODE);
        }
    }

    public void setPasarPalingDekat(JsonObject pasarPalingDekat) {
        prefs.setString("pasarPalingDekat", pasarPalingDekat.toString());
    }

    /** null when nothing is stored. */
    public JsonObject getPasarPalingDekat() {
        String s = prefs.getString("pasarPalingDekat");
        if (s == null) {
            return null;
        }
        JsonElement e = Json.parse(s);
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : null;
    }
}
