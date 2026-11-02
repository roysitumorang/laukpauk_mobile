package id.laukpauk.data.local;

import id.laukpauk.util.Json;
import androidx.core.content.pm.PackageInfoCompat;
import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Calendar;

import id.laukpauk.BuildConfig;

/**
 * The signed-in session and the values the app keeps between launches (auth token, user id, cart code,
 * saved delivery address/schedule, ...), stored in Flutter's SharedPreferences file so users stay logged in
 * after updating from the Flutter build. Pure storage: no network calls (those live in the repositories).
 */
public final class SessionStore {

    public SessionStore(Context context, Prefs prefs) {
        this.context = context;
        this.prefs = prefs;
    }
    private final Context context;
    private final Prefs prefs;


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


    public String getApplicationID() {
        return "id.laukpauk";
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
