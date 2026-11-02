package id.laukpauk.ui.delivery;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.JadwalDelivery;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.util.Json;

/**
 * State machine of {@link DeliveryFlow#ask} (legacy HeaderModel.adjd). Lives in the headless
 * {@link DeliveryFlowFragment}'s ViewModel so it survives rotation; the fragment renders {@link #step}.
 */
public class DeliveryFlowViewModel extends ViewModel {

    public enum Step { IDLE, DETECT, GEOCODE, GPS_OFF, GEOCODE_FAILED, CONFIRM, PIN, JADWAL, SAVING }

    private final DeliveryRepository delivery;
    private final CartRepository cart;
    private final List<DeliveryFlow.Callback> callbacks = new ArrayList<>();

    public final MutableLiveData<Step> step = new MutableLiveData<>(Step.IDLE);
    public final MutableLiveData<Resource<List<JadwalDelivery>>> jadwal = new MutableLiveData<>();
    public final MutableLiveData<JadwalDelivery> selected = new MutableLiveData<>();

    @Nullable public String latitude;
    @Nullable public String longitude;
    @Nullable public String alamat;
    /** A location detection (permission prompt / fix) is running. */
    public boolean detecting;
    /** The map screen was seen on top since it was opened. */
    public boolean pinSeen;
    /** The map screen was already opened for this PIN step. */
    public boolean pinOpened;

    public DeliveryFlowViewModel(DeliveryRepository delivery, CartRepository cart) {
        this.delivery = delivery;
        this.cart = cart;
    }

    private Step stepNow() {
        Step s = step.getValue();
        return s != null ? s : Step.IDLE;
    }

    /** Starts the flow (or joins the running one). */
    public void begin(@Nullable DeliveryFlow.Callback cb) {
        if (cb != null) {
            callbacks.add(cb);
        }
        if (stepNow() != Step.IDLE) {
            return;
        }
        latitude = null;
        longitude = null;
        alamat = null;
        selected.setValue(null);
        detecting = false;
        pinSeen = false;
        pinOpened = false;
        if (delivery.isSetNow()) {
            finish();
        } else {
            step.setValue(Step.DETECT);
        }
    }

    private void finish() {
        step.setValue(Step.IDLE);
        boolean set = delivery.current().isSet;
        List<DeliveryFlow.Callback> toCall = new ArrayList<>(callbacks);
        callbacks.clear();
        for (DeliveryFlow.Callback cb : toCall) {
            cb.onDone(set);
        }
    }

    /** Result of the location detection; both null when it failed (no permission, GPS off, no fix). */
    public void onLocation(@Nullable String lat, @Nullable String lng) {
        detecting = false;
        if (lat == null || lng == null) {
            step.setValue(Step.GPS_OFF);
            return;
        }
        latitude = lat;
        longitude = lng;
        step.setValue(Step.GEOCODE);
        delivery.reverseGeocode(lat, lng, r -> {
            if (r.isSuccess() && r.data != null) {
                alamat = r.data;
                step.setValue(Step.CONFIRM);
            } else {
                step.setValue(Step.GEOCODE_FAILED);
            }
        });
    }

    /** "OK" of the GPS-off hint: the flow ends without saving (legacy finishAdjd). */
    public void gpsOk() {
        finish();
    }

    /** "OK" of the failed-geocoding notice: legacy continues with setNewADJD(false). */
    public void geocodeFailedOk() {
        save(false);
    }

    public void confirmTutup() {
        save(false);
    }

    public void confirmYa() {
        startJadwal();
    }

    public void confirmAlamatLain() {
        pinSeen = false;
        pinOpened = false;
        step.setValue(Step.PIN);
    }

    /** The map screen closed: {@code json} = {formattedAddress, lat, lng} or null when the buyer just went back. */
    public void pinResult(@Nullable String json) {
        if (json != null) {
            try {
                JsonObject o = Json.parseObject(json);
                latitude = Json.strOrNull(o, "lat");
                longitude = Json.strOrNull(o, "lng");
                alamat = Json.strOrNull(o, "formattedAddress");
            } catch (RuntimeException ignored) {
                // keep the detected location
            }
        }
        startJadwal();
    }

    private void startJadwal() {
        selected.setValue(null);
        jadwal.setValue(Resource.loading());
        step.setValue(Step.JADWAL);
        delivery.loadJadwal(r -> jadwal.setValue(r));
    }

    public void selectJadwal(JadwalDelivery j) {
        if (j.isSelectable()) {
            selected.setValue(j);
        }
    }

    /** "Nanti Saja". */
    public void jadwalNantiSaja() {
        save(false);
    }

    /** "OK" (only visible once a schedule is selected). */
    public void jadwalOk() {
        save(true);
    }

    private void save(boolean completed) {
        JadwalDelivery j = selected.getValue();
        final boolean ok = completed && j != null && j.kode != null;
        step.setValue(Step.SAVING);
        delivery.save(ok, latitude, longitude, j != null ? j.kode : null, alamat, j != null ? j.raw : null, r -> {
            if (ok) {
                cart.refreshCartCount();
            }
            finish();
        });
    }
}
