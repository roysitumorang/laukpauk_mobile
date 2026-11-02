package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliveryResponses;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.delivery.Gift;
import id.laukpauk.data.model.delivery.JadwalDelivery;
import id.laukpauk.data.model.delivery.PlaceSuggestion;
import id.laukpauk.data.remote.DeliveryApi;
import id.laukpauk.util.Json;

/**
 * The buyer's delivery address + schedule ("ADJD"), stored on the device (legacy ADJDModel / HeaderModel
 * .setNewADJD / AppState.isADJDSet + alamatDelivery). Observed by the header, the product buy flow, the
 * cart and checkout.
 *
 * OWNER: the delivery slice. The public methods below are a CONTRACT used by other slices: keep their
 * signatures; add whatever else the delivery screens need (schedule list, reverse geocoding, ...).
 */
public class DeliveryRepository extends BaseRepository {

    /** Shown when the address text is unknown (legacy ADJDModel.defaultAlamat). */
    public static final String DEFAULT_ALAMAT = "Merdeka Walk Medan";

    private final MutableLiveData<DeliverySelection> selection = new MutableLiveData<>();

    private final DeliveryApi api;

    public DeliveryRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(DeliveryApi.class);
        selection.setValue(read());
    }

    /** The current selection; never null ({@link DeliverySelection#NOT_SET}). Observe it for changes. */
    public LiveData<DeliverySelection> selection() {
        return selection;
    }

    /** The current selection right now (main thread). */
    public DeliverySelection current() {
        DeliverySelection s = selection.getValue();
        return s != null ? s : DeliverySelection.NOT_SET;
    }

    /** Re-reads the stored selection and publishes it (main thread). */
    public void reload() {
        selection.setValue(read());
    }

    /**
     * Legacy ADJDModel.deleteAlamatDanJadwalDelivery(): moves the saved address/schedule into the "coppied*"
     * keys (restorable if the buyer abandons choosing a new one), removes the saved ones, marks ADJD unset.
     */
    public void clear() {
        copy("savedLatitude", "coppiedLatitude");
        copy("savedLongitude", "coppiedLongitude");
        copy("savedJadwalDeliveryCode", "coppiedJadwalDeliveryCode");
        copy("savedJadwalDeliveryMap", "coppiedJadwalDeliveryMap");
        copy("savedAlamat", "coppiedAlamat");
        prefs.remove("savedLatitude");
        prefs.remove("savedLongitude");
        prefs.remove("savedJadwalDeliveryCode");
        prefs.remove("savedJadwalDeliveryMap");
        prefs.remove("savedAlamat");
        prefs.setBool("isAlamatDeliveryDanJadwalDeliverySet", false);
        reload();
    }

    /** Legacy ADJDModel.deleteCoppiedADJD(). */
    public void clearCopied() {
        prefs.remove("coppiedLatitude");
        prefs.remove("coppiedLongitude");
        prefs.remove("coppiedJadwalDeliveryCode");
        prefs.remove("coppiedJadwalDeliveryMap");
        prefs.remove("coppiedAlamat");
    }


    // ------------------------------------------------------------------ schedule / geocoding / places

    /** True when the stored flag says an address + schedule were chosen (legacy isAlamatDeliveryDanJadwalDeliverySet). */
    public boolean isSetNow() {
        return session.isAlamatDeliveryDanJadwalDeliverySet();
    }

    /** Legacy JadwalPengantaran.getJadwalDelivery: the schedules the buyer can choose from (empty on failure). */
    public void loadJadwal(ResultCallback<List<JadwalDelivery>> callback) {
        enqueue(api.getAvailableJadwalDelivery(), body -> {
            List<JadwalDelivery> list = new ArrayList<>();
            if (body != null && body.availableJadwalDelivery != null) {
                for (JsonObject o : body.availableJadwalDelivery) {
                    if (o != null) {
                        list.add(new JadwalDelivery(o));
                    }
                }
            }
            return list;
        }, callback);
    }

    /**
     * Legacy ADJDModel.reverseGeocoding: the formatted address of the first geocoding result, "Alamat Tidak diketahui"
     * when there is none; ERROR when the request failed.
     */
    public void reverseGeocode(String latitude, String longitude, ResultCallback<String> callback) {
        enqueue(api.reverseGeocode(latitude + "," + longitude, session.getGoogleMapsApiKey()), body -> {
            if (body != null && body.results != null && !body.results.isEmpty()
                    && body.results.get(0) != null && body.results.get(0).formattedAddress != null) {
                return body.results.get(0).formattedAddress;
            }
            return "Alamat Tidak diketahui";
        }, callback);
    }

    /** Legacy CariGoogleMaps.getWilayahFromGMaps: Google Places text search (empty list for an empty query). */
    public void searchPlaces(String query, ResultCallback<List<PlaceSuggestion>> callback) {
        if (query == null || query.isEmpty()) {
            callback.onResult(Resource.success(new ArrayList<>()));
            return;
        }
        enqueue(api.searchPlaces(query, session.getGoogleMapsApiKey()), body -> {
            List<PlaceSuggestion> list = new ArrayList<>();
            if (body != null && body.results != null) {
                for (DeliveryResponses.PlaceResult r : body.results) {
                    if (r != null && r.geometry != null && r.geometry.location != null) {
                        list.add(new PlaceSuggestion(r.name != null ? r.name : "",
                                r.formattedAddress != null ? r.formattedAddress : "",
                                r.geometry.location.lat, r.geometry.location.lng));
                    }
                }
            }
            return list;
        }, callback);
    }

    // ------------------------------------------------------------------ save (legacy HeaderModel.setNewADJD)

    /**
     * Legacy HeaderModel.setNewADJD. {@code completed} = the buyer chose address + schedule: saves them (the schedule
     * object gets a {@code setOn} timestamp), marks ADJD set and empties the server cart. Not completed: restores
     * the copied selection when it is complete, otherwise leaves ADJD unset. Calls {@link #reload()}; the callback
     * (main thread) gets true when a selection is set afterwards.
     */
    public void save(boolean completed, @Nullable String latitude, @Nullable String longitude,
                     @Nullable String jadwalDeliveryCode, @Nullable String alamat, @Nullable JsonObject jadwalDeliveryMap,
                     ResultCallback<Boolean> callback) {
        String cLat = prefs.getString("coppiedLatitude");
        String cLng = prefs.getString("coppiedLongitude");
        String cCode = prefs.getString("coppiedJadwalDeliveryCode");
        String cAlamat = prefs.getString("coppiedAlamat");
        String cMap = prefs.getString("coppiedJadwalDeliveryMap");
        boolean copiedComplete = cLat != null && cLng != null && cCode != null && cAlamat != null && cMap != null;

        boolean isSet = completed || copiedComplete;
        String mapText = jadwalDeliveryMap != null ? jadwalDeliveryMap.toString() : "null";
        if (!completed) {
            latitude = cLat;
            longitude = cLng;
            jadwalDeliveryCode = cCode;
            alamat = cAlamat;
            mapText = cMap;
        }
        if (isSet) {
            prefs.setBool("isAlamatDeliveryDanJadwalDeliverySet", true);
            if (latitude != null) {
                prefs.setString("savedLatitude", latitude);
            }
            if (longitude != null) {
                prefs.setString("savedLongitude", longitude);
            }
            if (jadwalDeliveryCode != null) {
                prefs.setString("savedJadwalDeliveryCode", jadwalDeliveryCode);
            }
            if (mapText != null) {
                JsonObject map = Json.parseObject(mapText);
                map.addProperty("setOn", nowText());
                prefs.setString("savedJadwalDeliveryMap", map.toString());
            }
            if (alamat != null) {
                prefs.setString("savedAlamat", alamat);
            }
        }
        reload();
        if (isSet && completed) {
            enqueue(api.bersihkanKeranjang(session.getCartCode()), body -> true,
                    r -> callback.onResult(Resource.success(current().isSet)));
            return;
        }
        callback.onResult(Resource.success(current().isSet));
    }

    /** Dart DateTime parts without padding: "2026-11-2 9:5:7" (legacy format, parsed by the server side as is). */
    private static String nowText() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH)
                + " " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
    }

    // ------------------------------------------------------------------ expiry (legacy MainModel.checkExpiry rules)

    /** The raw saved schedule text (savedJadwalDeliveryMap) or null; lets a watcher remember what it handled. */
    @Nullable
    public String savedJadwalText() {
        return prefs.getString("savedJadwalDeliveryMap");
    }

    /** True when the saved delivery schedule has expired (rules of MyIntentService / MainModel.isExpired). */
    public boolean isExpired() {
        String saved = savedJadwalText();
        if (saved == null) {
            return false;
        }
        try {
            JsonObject map = Json.parseObject(saved);
            String id = Json.str(map, "id");
            String day = Json.str(map, "day").toLowerCase(Locale.ROOT);
            LocalDateTime now = LocalDateTime.now();
            LocalTime maxTime = LocalTime.parse(Json.str(map, "maxTime"));
            LocalDate maxDay = LocalDate.parse(Json.str(map, "currentDate"));
            if (day.equals("besok")) {
                maxDay = maxDay.plusDays(1);
            }
            if (day.equals("lusa")) {
                maxDay = maxDay.plusDays(2);
            }
            if (id.equals("1") && (day.equals("besok") || day.equals("lusa"))) {
                maxDay = maxDay.minusDays(1);
            }
            return now.toLocalDate().isEqual(maxDay) && now.toLocalTime().isAfter(maxTime);
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ gifts + one-time reminders

    /** Legacy GiftModel.getGifts (empty list when the request fails). */
    public void getGifts(ResultCallback<List<Gift>> callback) {
        enqueue(api.getGifts(), body -> body != null && body.gift != null ? body.gift : new ArrayList<Gift>(), callback);
    }

    public boolean showAlertGift() {
        return prefs.getBool("showAlertGift", true);
    }

    public void setAlertGiftShown() {
        prefs.setBool("showAlertGift", false);
    }

    public boolean showMakeSure() {
        return prefs.getBool("showMakeSureADJD", true);
    }

    public void setMakeSureShown() {
        prefs.setBool("showMakeSureADJD", false);
    }

    private void copy(String from, String to) {
        String v = prefs.getString(from);
        if (v != null) {
            prefs.setString(to, v);
        }
    }

    private DeliverySelection read() {
        boolean isSet = session.isAlamatDeliveryDanJadwalDeliverySet();
        String alamat = prefs.getString("savedAlamat");
        JsonObject jadwal = session.savedJadwalDeliveryMap();
        String day = jadwal != null ? Json.strOrNull(jadwal, "day") : null;
        if (!isSet && alamat == null) {
            return DeliverySelection.NOT_SET;
        }
        return new DeliverySelection(isSet, prefs.getString("savedLatitude"), prefs.getString("savedLongitude"),
                alamat != null ? alamat : DEFAULT_ALAMAT, prefs.getString("savedJadwalDeliveryCode"), day);
    }
}
