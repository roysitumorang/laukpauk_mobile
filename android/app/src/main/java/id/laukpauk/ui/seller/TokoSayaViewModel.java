package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonElement;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.DetailPenjual;
import id.laukpauk.data.model.seller.shop.Pasar;
import id.laukpauk.data.model.seller.shop.TokoSayaResponses;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.TokoSayaRepository;

/** State of the seller dashboard (TokoSaya.dart): shop detail and the independent dashboard sections. */
public class TokoSayaViewModel extends ViewModel {

    /** A LiveData whose source can be swapped for a fresh repository call on every reload. */
    private static final class Slot<T> {
        final MediatorLiveData<Resource<T>> live = new MediatorLiveData<>();
        private LiveData<Resource<T>> source;

        void set(LiveData<Resource<T>> next, Observer<Resource<T>> extra) {
            if (source != null) {
                live.removeSource(source);
            }
            source = next;
            live.addSource(next, r -> {
                live.setValue(r);
                if (extra != null) {
                    extra.onChanged(r);
                }
            });
        }
    }

    private final TokoSayaRepository repository;
    private final SettingRepository settings;

    private final Slot<DetailPenjual> detail = new Slot<>();
    private final Slot<Integer> totalPenjualan = new Slot<>();
    private final Slot<Integer> tagihan = new Slot<>();
    private final Slot<TokoSayaResponses.Jumlah> jumlah = new Slot<>();
    private final Slot<boolean[]> jadwal = new Slot<>();
    private final Slot<List<Pasar>> pasar = new Slot<>();
    private final MutableLiveData<boolean[]> jadwalAktif = new MutableLiveData<>(new boolean[3]);

    final TokoLokasiState lokasi = new TokoLokasiState();
    private boolean started;

    public TokoSayaViewModel(TokoSayaRepository repository, SettingRepository settings) {
        this.repository = repository;
        this.settings = settings;
    }

    public LiveData<Resource<DetailPenjual>> getDetail() {
        return detail.live;
    }

    public LiveData<Resource<Integer>> getTotalPenjualan() {
        return totalPenjualan.live;
    }

    public LiveData<Resource<Integer>> getTagihan() {
        return tagihan.live;
    }

    public LiveData<Resource<TokoSayaResponses.Jumlah>> getJumlah() {
        return jumlah.live;
    }

    /** Active delivery schedules [pagi, siang, sore]. */
    public LiveData<boolean[]> getJadwalAktif() {
        return jadwalAktif;
    }

    public LiveData<Resource<List<Pasar>>> getPasar() {
        return pasar.live;
    }

    /** Loads once (first open); screens call {@link #refresh()} afterwards. */
    public void start() {
        if (!started) {
            started = true;
            refresh();
        }
    }

    public void refresh() {
        detail.set(repository.getDetailToko(), r -> {
            if (r.isSuccess() && r.data != null && r.data.isApproved()) {
                loadSections();
            }
        });
    }

    private void loadSections() {
        totalPenjualan.set(repository.getTotalPenjualan(), null);
        tagihan.set(repository.getTagihan(), null);
        jumlah.set(repository.getJumlahProduk(), null);
        jadwal.set(repository.getJadwalDelivery(), r -> {
            if (r.isSuccess() && r.data != null) {
                jadwalAktif.setValue(r.data);
            }
        });
    }

    /** Toggles schedule {@code index} (0..2); the checkbox only changes once the server agreed. */
    public void toggleJadwal(int index, BaseRepository.ResultCallback<Boolean> callback) {
        repository.toggleJadwalDelivery(String.valueOf(index + 1), result -> {
            boolean ok = result.isSuccess() && Boolean.TRUE.equals(result.data);
            if (ok) {
                boolean[] now = jadwalAktif.getValue() == null ? new boolean[3] : jadwalAktif.getValue().clone();
                now[index] = !now[index];
                jadwalAktif.setValue(now);
            }
            callback.onResult(Resource.success(ok));
        });
    }

    public void loadPasarIfNeeded() {
        Resource<List<Pasar>> current = pasar.live.getValue();
        if (current == null || current.isError()) {
            pasar.set(repository.getPasarTersedia(), null);
        }
    }

    public void reverseGeocoding(String lat, String lng, BaseRepository.ResultCallback<String> callback) {
        repository.reverseGeocoding(lat, lng, callback);
    }

    public void tetapkanLokasi(String idPenjual, BaseRepository.ResultCallback<Boolean> callback) {
        if (TokoLokasiState.LOKASI_TERSENDIRI.equals(lokasi.jenis)) {
            repository.ubahLokasiBerjualan(idPenjual, lokasi.jenis, null, lokasi.latitude, lokasi.longitude,
                    callback);
        } else {
            repository.ubahLokasiBerjualan(idPenjual, lokasi.jenis, lokasi.idPasar, null, null, callback);
        }
    }

    /** The WhatsApp contact configuration (pending / rejected seller screen). */
    public void loadWhatsapp(BaseRepository.ResultCallback<JsonElement> callback) {
        settings.getContactUsWhatsappConfiguration(callback);
    }
}
