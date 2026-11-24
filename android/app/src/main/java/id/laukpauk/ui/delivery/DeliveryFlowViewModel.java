package id.laukpauk.ui.delivery;

import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.address.BuyerAddress;
import id.laukpauk.data.model.delivery.DeliverySchedule;
import id.laukpauk.data.repository.AddressRepository;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.util.Json;

/**
 * State machine of {@link DeliveryFlow#ask} (legacy HeaderModel.adjd). Lives in the headless
 * {@link DeliveryFlowFragment}'s ViewModel so it survives rotation; the fragment renders {@link #step}.
 */
public class DeliveryFlowViewModel extends ViewModel {

    public enum Step {
        IDLE, ADDRESS_LOADING, ADDRESSES, ADDRESS_FAILED, DETECT, GEOCODE, GPS_OFF, GEOCODE_FAILED, CONFIRM, PIN,
        FORM, FORM_SAVING, SCHEDULE, SAVING
    }

    private final DeliveryRepository delivery;
    private final CartRepository cart;
    private final List<DeliveryFlow.Callback> callbacks = new ArrayList<>();

    public final MutableLiveData<Step> step = new MutableLiveData<>(Step.IDLE);
    public final MutableLiveData<Resource<List<DeliverySchedule>>> schedule = new MutableLiveData<>();
    public final MutableLiveData<DeliverySchedule> selected = new MutableLiveData<>();

    @Nullable public String latitude;
    @Nullable public String longitude;
    @Nullable public String address;
    /** A location detection (permission prompt / fix) is running. */
    public boolean detecting;
    /** The map screen was seen on top since it was opened. */
    public boolean pinSeen;
    /** The map screen was already opened for this PIN step. */
    public boolean pinOpened;

    /** The buyer's addresses (main first); empty until loaded or when there are none. */
    public final List<BuyerAddress> addresses = new ArrayList<>();
    /** The address being edited, null when adding a new one. */
    @Nullable public BuyerAddress editing;
    /** The form's values while it is open (kept across a failed save or a rotation). */
    @Nullable public BuyerAddress draft;
    /** The backend's reason of the last failed address load or save; null when none. */
    @Nullable public String error;
    /** A complete selection (address + schedule) was saved in this run. */
    private boolean savedThisRun;

    private final AddressRepository addressRepository;

    public DeliveryFlowViewModel(DeliveryRepository delivery, CartRepository cart, AddressRepository addressRepository) {
        this.delivery = delivery;
        this.cart = cart;
        this.addressRepository = addressRepository;
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
        address = null;
        selected.setValue(null);
        detecting = false;
        pinSeen = false;
        pinOpened = false;
        editing = null;
        draft = null;
        error = null;
        savedThisRun = false;
        addresses.clear();
        if (addressRepository.isSignedIn()) {
            loadAddresses();
        } else {
            finish(); // addresses and delivery plans need a signed-in buyer (DeliveryFlow.ask sends to Login)
        }
    }

    // ------------------------------------------------------------------ address book (signed in)

    /** {@code GET buyer/delivery_addresses}: the list, or straight to adding one when there is none. */
    private void loadAddresses() {
        step.setValue(Step.ADDRESS_LOADING);
        addressRepository.list(r -> {
            if (stepNow() != Step.ADDRESS_LOADING) {
                return;
            }
            if (!r.isSuccess()) {
                error = r.message;
                step.setValue(Step.ADDRESS_FAILED);
                return;
            }
            addresses.clear();
            if (r.data != null) {
                addresses.addAll(r.data);
            }
            if (addresses.isEmpty()) {
                addNewAddress();
            } else {
                step.setValue(Step.ADDRESSES);
            }
        });
    }

    /** An address of the list was chosen: it is the delivery address; the schedule comes next. */
    public void chooseAddress(BuyerAddress a) {
        latitude = a.destinationLatitude;
        longitude = a.destinationLongitude;
        address = a.streetAddress;
        startSchedule();
    }

    /** "Tambah Alamat Baru" (or no address yet): detect the location, then the form. */
    public void addNewAddress() {
        editing = null;
        startDetect();
    }

    /** "Ubah" of an address: detect the location again, then the form with the address's details. */
    public void editAddress(BuyerAddress a) {
        editing = a;
        startDetect();
    }

    private void startDetect() {
        draft = null;
        error = null;
        latitude = null;
        longitude = null;
        address = null;
        detecting = false;
        step.setValue(Step.DETECT);
    }

    /** Closing the list (or the failure notice): no address, checkout does not open. */
    public void closeAddresses() {
        finish();
    }

    /** The detected (or map-picked) location goes into the address form. */
    private void openForm() {
        draft = null;
        error = null;
        step.setValue(Step.FORM);
    }

    /** "Simpan" of the form: creates or updates the address, then it is the delivery address. */
    public void saveForm(BuyerAddress a) {
        draft = a;
        a.id = editing != null ? editing.id : null;
        a.destinationLatitude = latitude;
        a.destinationLongitude = longitude;
        step.setValue(Step.FORM_SAVING);
        addressRepository.save(a, r -> {
            if (stepNow() != Step.FORM_SAVING) {
                return;
            }
            if (!r.isSuccess()) {
                error = r.message;
                step.setValue(Step.FORM);
                return;
            }
            BuyerAddress saved = r.data != null ? r.data : a;
            latitude = saved.destinationLatitude != null ? saved.destinationLatitude : latitude;
            longitude = saved.destinationLongitude != null ? saved.destinationLongitude : longitude;
            address = saved.streetAddress != null ? saved.streetAddress : a.streetAddress;
            draft = null;
            error = null;
            startSchedule();
        });
    }

    /** "Batal" of the form: back to the list, or the flow ends when there is no address. */
    public void cancelForm() {
        draft = null;
        error = null;
        if (addresses.isEmpty()) {
            finish();
        } else {
            step.setValue(Step.ADDRESSES);
        }
    }

    private void finish() {
        step.setValue(Step.IDLE);
        // Only a selection saved in this run counts: closing the list must not open checkout.
        boolean set = savedThisRun;
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
                address = r.data;
                step.setValue(Step.CONFIRM);
            } else {
                step.setValue(Step.GEOCODE_FAILED);
            }
        });
    }

    /** "OK" of the GPS-off hint: the flow ends without saving (legacy finishAdjd). */
    public void gpsOk() {
        if (!addresses.isEmpty()) {
            step.setValue(Step.ADDRESSES);
        } else {
            finish();
        }
    }

    /** "OK" of the failed-geocoding notice: pick the place on the map. */
    public void geocodeFailedOk() {
        confirmOtherAddress();
    }

    public void confirmClose() {
        cancelForm();
    }

    /** The detected address is right: on to the address form. */
    public void confirmYes() {
        openForm();
    }

    public void confirmOtherAddress() {
        pinSeen = false;
        pinOpened = false;
        step.setValue(Step.PIN);
    }

    /** The map screen closed: {@code json} = {formattedAddress, lat, lng} or null when the buyer just went back. */
    public void pinResult(@Nullable String json) {
        if (json != null) {
            try {
                Map<String, Object> o = Json.parseObject(json);
                latitude = Json.strOrNull(o, "lat");
                longitude = Json.strOrNull(o, "lng");
                address = Json.strOrNull(o, "formattedAddress");
            } catch (RuntimeException ignored) {
                // keep the detected location
            }
        }
        if (latitude == null || longitude == null) {
            cancelForm(); // nothing detected or picked
        } else {
            openForm();
        }
    }

    private void startSchedule() {
        selected.setValue(null);
        schedule.setValue(Resource.loading());
        step.setValue(Step.SCHEDULE);
        delivery.loadSchedule(r -> schedule.setValue(r));
    }

    public void selectSchedule(DeliverySchedule j) {
        if (j.isSelectable()) {
            selected.setValue(j);
        }
    }

    /** "Nanti Saja". */
    public void scheduleLater() {
        save(false);
    }

    /** "OK" (only visible once a schedule is selected). */
    public void scheduleOk() {
        save(true);
    }

    private void save(boolean completed) {
        DeliverySchedule j = selected.getValue();
        final boolean ok = completed && j != null && j.code != null;
        step.setValue(Step.SAVING);
        delivery.save(ok, latitude, longitude, j != null ? j.code : null, address, j != null ? j.raw : null, r -> {
            if (ok) {
                cart.refreshCartCount();
            }
            savedThisRun = ok && r.isSuccess();
            finish();
        });
    }
}
