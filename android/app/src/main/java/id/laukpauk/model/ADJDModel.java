package id.laukpauk.model;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.view.Gravity;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.Prefs;
import id.laukpauk.core.TanyaJadwal;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/models/ADJDModel.dart (ADJD = Alamat Dan Jadwal Delivery: the delivery address and
 * delivery schedule the buyer has to pick before shopping).
 *
 * Methods that need a screen (permission prompts, dialogs) are UI-flow methods: call them on the
 * main thread; `done` runs on the main thread exactly once. The others are BLOCKING.
 */
public final class ADJDModel {
    private ADJDModel() {}

    public static final String defaultLatitude = "3.5889242";
    public static final String defaultLongitude = "98.6790958";
    public static final String defaultAlamat = "Merdeka Walk Medan";

    private static <T> void call(Async.Done<T> done, T value) {
        if (done != null) {
            done.run(value);
        }
    }

    // ------------------------------------------------------------------ location

    private static boolean hasLocationPermission(BaseActivity activity) {
        return activity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || activity.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /** geolocator's isLocationServiceEnabled(). */
    private static boolean isLocationServiceEnabled(BaseActivity activity) {
        LocationManager lm = (LocationManager) activity.getSystemService(android.content.Context.LOCATION_SERVICE);
        if (lm == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Dart: detectMyLocation(context) -> Map? {permissionRequest, latitude, longitude} (latitude and
     * longitude as strings). `done` gets null when the location could not be detected (permission
     * denied, GPS off, no fix). Asks for the location permission when it is not granted yet.
     */
    public static void detectMyLocation(final BaseActivity activity, final Async.Done<JsonObject> done) {
        // LocationPermission? _permissionRequest = LocationPermission.always, set to null when the
        // permission was not granted at the start (denied / deniedForever).
        final String permissionRequest = hasLocationPermission(activity) ? "always" : null;

        if (hasLocationPermission(activity)) {
            locate(activity, permissionRequest, done);
        } else {
            activity.askPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, result -> locate(activity, permissionRequest, done));
        }
    }

    private static void locate(BaseActivity activity, final String permissionRequest,
                               final Async.Done<JsonObject> done) {
        // Geolocator.getCurrentPosition throws (-> null) without permission or with the location
        // services switched off.
        if (!hasLocationPermission(activity) || !isLocationServiceEnabled(activity)) {
            call(done, null);
            return;
        }

        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
            // desiredAccuracy: LocationAccuracy.bestForNavigation
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnCompleteListener(task -> {
                        Location myLocation = null;
                        try {
                            if (task.isSuccessful()) {
                                myLocation = task.getResult();
                            }
                        } catch (RuntimeException e) {
                            myLocation = null;
                        }
                        if (myLocation == null) {
                            call(done, null);
                            return;
                        }

                        String latitude = String.valueOf(myLocation.getLatitude());
                        String longitude = String.valueOf(myLocation.getLongitude());

                        JsonObject myLocationMap = Json.of(
                                "permissionRequest", permissionRequest,
                                "latitude", latitude,
                                "longitude", longitude);
                        call(done, myLocationMap);
                    });
        } catch (RuntimeException e) {
            call(done, null);
        }
    }

    // ------------------------------------------------------------------ dialogs

    /**
     * Dart: askAlamatPengantaran(context, {latitude = '', longitude = '', title, content}).
     * `done` gets {answer: 'ya' | 'tidak', address} or null (closed with "Tutup", or the geocoding
     * request failed). Pass null for title / content to use the defaults.
     */
    public static void askAlamatPengantaran(final BaseActivity activity, String latitude, String longitude,
                                            String title, String content, final Async.Done<JsonObject> done) {
        final String lat = latitude == null ? "" : latitude;
        final String lng = longitude == null ? "" : longitude;
        final String dialogTitle = title == null ? "Alamat Pengantaran" : title;
        final String dialogContent = content == null
                ? "Apakah belanjaan akan diantarkan ke alamat tempat anda berada sekarang ?"
                : content;

        Async.run(() -> {
            Async.sleep(1);
            String geocodingApiKey = LocalStorage.getGoogleMapsApiKey();
            return Http.get("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng
                    + "&key=" + geocodingApiKey);
        }, httpResponse -> {
            if (httpResponse != null && httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                JsonArray results = Json.array(dataObject, "results");
                final String formattedAddress = (results.size() >= 1)
                        ? Json.str(Json.objAt(results, 0), "formatted_address")
                        : "Alamat Tidak diketahui";

                Modal.Content<JsonObject> actions = handle -> CustomAlert.actionsRow(activity,
                        CustomAlert.textButton(activity, "Tutup", () -> handle.close(null)),
                        CustomAlert.textButton(activity, "Alamat Lain", () -> handle.close(
                                Json.of("answer", "tidak", "address", formattedAddress))),
                        CustomAlert.textButton(activity, "Ya", () -> handle.close(
                                Json.of("answer", "ya", "address", formattedAddress))));

                TextView moreWidgets = Ui.text(activity, formattedAddress,
                        new Txt.Style(Ui.hb(activity) * 3.25f).color(CustomColors.textMuted));
                moreWidgets.setGravity(Gravity.CENTER);

                CustomAlert.<JsonObject>textComponent(activity, null, dialogTitle, dialogContent, actions,
                        moreWidgets, false, done);
            } else {
                // Dart shows the dialog without awaiting it and returns null right away.
                if (activity.isAlive()) {
                    try {
                        new android.app.AlertDialog.Builder(activity)
                                .setTitle("Pendeteksian Lokasi")
                                .setMessage("Gagal mendeteksi lokasi anda saat ini !")
                                .setPositiveButton("OK", null)
                                .show();
                    } catch (RuntimeException ignored) {
                        // window could not be attached (screen is going away)
                    }
                }
                call(done, null);
            }
        });
    }

    /**
     * Dart: askJadwalDelivery(context, {latitude, longitude, currentLocation}).
     * `done` gets {jadwalDeliverySelected: kode, jadwalDeliveryTerpilih: map} or null ("Nanti Saja").
     */
    public static void askJadwalDelivery(final BaseActivity activity, String latitude, String longitude,
                                         String currentLocation, Async.Done<JsonObject> done) {
        String title = "Jadwal Delivery";
        String subTitle = "Silahkan pilih jadwal pengantaran yang anda inginkan :";

        final TanyaJadwal tanyaJadwal = new TanyaJadwal(activity, currentLocation);
        // TanyaJadwal pops the dialog itself (Navigator.pop(context, value)): hand it the dialog handle.
        Modal.Content<JsonObject> actions = handle -> {
            tanyaJadwal.setOnClose(handle::close);
            return null;
        };
        CustomAlert.<JsonObject>textComponent(activity, null, title, subTitle, actions, tanyaJadwal, false, done);
    }

    // ------------------------------------------------------------------ geocoding

    /**
     * Dart: reverseGeocoding({latitude, longitude}) (untyped arguments: double or String).
     * BLOCKING (network).
     */
    public static String reverseGeocoding(Object latitude, Object longitude) {
        String geocodingApiKey = LocalStorage.getGoogleMapsApiKey();

        Http.Response httpResponse = Http.get("https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + dartString(latitude) + "," + dartString(longitude) + "&key=" + geocodingApiKey);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            JsonArray results = Json.array(dataObject, "results");
            return (results.size() >= 1)
                    ? Json.str(Json.objAt(results, 0), "formatted_address")
                    : "Alamat Tidak diketahui";
        }

        return "Alamat Tidak Diketahui";
    }

    /** Dart string interpolation of a dynamic value. */
    private static String dartString(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof com.google.gson.JsonElement) {
            return Json.asStr((com.google.gson.JsonElement) value, "null");
        }
        return String.valueOf(value);
    }

    // ------------------------------------------------------------------ saved / copied ADJD

    /**
     * Dart: deleteAlamatDanJadwalDelivery(). Moves the saved address / schedule into the "copied"
     * keys (so they can be restored when the user does not finish choosing a new one) and removes
     * the saved ones.
     */
    public static void deleteAlamatDanJadwalDelivery() {
        String latitude = Prefs.getString("savedLatitude");
        String longitude = Prefs.getString("savedLongitude");
        String jadwalDeliveryCode = Prefs.getString("savedJadwalDeliveryCode");
        String jadwalDeliveryMap = Prefs.getString("savedJadwalDeliveryMap");
        String alamat = Prefs.getString("savedAlamat");

        if (latitude != null) {
            Prefs.setString("coppiedLatitude", latitude);
        }
        if (longitude != null) {
            Prefs.setString("coppiedLongitude", longitude);
        }
        if (jadwalDeliveryCode != null) {
            Prefs.setString("coppiedJadwalDeliveryCode", jadwalDeliveryCode);
        }
        if (jadwalDeliveryMap != null) {
            Prefs.setString("coppiedJadwalDeliveryMap", jadwalDeliveryMap);
        }
        if (alamat != null) {
            Prefs.setString("coppiedAlamat", alamat);
        }

        Prefs.remove("isAlamatDeliveryDanJadwalDeliverySet");
        Prefs.remove("savedLatitude");
        Prefs.remove("savedLongitude");
        Prefs.remove("savedJadwalDeliveryCode");
        Prefs.remove("savedJadwalDeliveryMap");
        Prefs.remove("savedAlamat");
    }

    /**
     * Dart: getCoppiedADJD(). Keys coppiedLatitude, coppiedLongitude, coppiedJadwalDeliveryCode,
     * coppiedJadwalDeliveryMap (a JSON text), coppiedAlamat; missing values are JSON null.
     */
    public static JsonObject getCoppiedADJD() {
        JsonObject coppiedADJD = new JsonObject();
        coppiedADJD.addProperty("coppiedLatitude", Prefs.getString("coppiedLatitude"));
        coppiedADJD.addProperty("coppiedLongitude", Prefs.getString("coppiedLongitude"));
        coppiedADJD.addProperty("coppiedJadwalDeliveryCode", Prefs.getString("coppiedJadwalDeliveryCode"));
        coppiedADJD.addProperty("coppiedJadwalDeliveryMap", Prefs.getString("coppiedJadwalDeliveryMap"));
        coppiedADJD.addProperty("coppiedAlamat", Prefs.getString("coppiedAlamat"));
        return coppiedADJD;
    }

    public static void deleteCoppiedADJD() {
        Prefs.remove("coppiedLatitude");
        Prefs.remove("coppiedLongitude");
        Prefs.remove("coppiedJadwalDeliveryCode");
        Prefs.remove("coppiedJadwalDeliveryMap");
        Prefs.remove("coppiedAlamat");
    }
}
