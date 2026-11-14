package id.laukpauk.data.local;

import java.util.Map;

import id.laukpauk.util.Json;
import androidx.annotation.Nullable;
import androidx.core.content.pm.PackageInfoCompat;
import android.content.Context;
import android.content.pm.PackageInfo;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

import id.laukpauk.BuildConfig;

/**
 * The signed-in session and the values the app keeps between launches (auth token, user id, cart code,
 * saved delivery address/schedule, ...), stored in Flutter's SharedPreferences file so users stay logged in
 * after updating from the Flutter build. Pure storage: no network calls (those live in the repositories).
 */
public final class SessionStore {

    private static final String KEY_EXPIRED_AT = "authTokenExpiredAt";
    private static final String KEY_CURRENT_USER = "currentUser";

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
    public Map<String, Object> getSavedLatitudeAndLongitude() {
        Map<String, Object> o = Json.newObject();
        o.put("latitude", prefs.getString("savedLatitude"));
        o.put("longitude", prefs.getString("savedLongitude"));
        o.put("jadwalDeliveryCode", prefs.getString("savedJadwalDeliveryCode"));
        o.put("alamat", prefs.getString("savedAlamat"));
        return o;
    }

    /** Used for Geocoding / Places web-service calls. */
    public String getGoogleMapsApiKey() {
        return BuildConfig.MAPS_API_KEY;
    }

    /** The session JWT; "" when signed out or when the token has passed its expiry. */
    public String getAuthToken() {
        Instant expiredAt = getAuthTokenExpiredAt();
        if (expiredAt != null && !Instant.now().isBefore(expiredAt)) {
            return "";
        }
        return prefs.getString("authToken", "");
    }

    /** When the session JWT expires (sign-in's {@code expired_at}); null when unknown. */
    @Nullable
    public Instant getAuthTokenExpiredAt() {
        String text = prefs.getString(KEY_EXPIRED_AT);
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The signed-in user as sign-in answered it ({@code user}); null when signed out or unknown. */
    @Nullable
    public Map<String, Object> getCurrentUser() {
        if (getAuthToken().isEmpty()) {
            return null;
        }
        Object user = Json.parse(prefs.getString(KEY_CURRENT_USER));
        return Json.isObject(user) ? Json.obj(user) : null;
    }

    /** Stores a sign-in: the JWT, its expiry (RFC 3339, may be null) and the current user. */
    public void saveSignIn(String idToken, @Nullable String expiredAt, Map<String, Object> user) {
        prefs.setString("authToken", idToken);
        if (expiredAt == null || expiredAt.isEmpty()) {
            prefs.remove(KEY_EXPIRED_AT);
        } else {
            prefs.setString(KEY_EXPIRED_AT, expiredAt);
        }
        prefs.setString(KEY_CURRENT_USER, Json.toJson(user));
        prefs.setString("idUser", Json.str(user, "id", ""));
    }

    /** Forgets the JWT, its expiry and the current user (sign-out, or a session that starts another way). */
    public void clearSignIn() {
        prefs.remove("authToken");
        prefs.remove(KEY_EXPIRED_AT);
        prefs.remove(KEY_CURRENT_USER);
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

    public String getApplicationID() {
        return "id.laukpauk";
    }


    /** The saved delivery schedule (JSON map) or null. */
    public Map<String, Object> savedJadwalDeliveryMap() {
        String encoded = prefs.getString("savedJadwalDeliveryMap");
        if (encoded != null) {
            Object e = Json.parse(encoded);
            if (Json.isObject(e)) {
                return Json.obj(e);
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
}
