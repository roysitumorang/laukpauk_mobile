package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;

import id.laukpauk.data.model.delivery.DeliverySelection;
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
    public static final String DEFAULT_ALAMAT = "Alamat Pengantaran";

    private final MutableLiveData<DeliverySelection> selection = new MutableLiveData<>();

    public DeliveryRepository(RepositoryDeps deps) {
        super(deps);
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
