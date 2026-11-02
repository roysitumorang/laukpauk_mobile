package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.TransaksiRepository;

/** State of Pesanan Saya: the selected status tab, today's status filter, the date range and the list. */
public class PesananSayaViewModel extends ViewModel {

    private final TransaksiRepository repository;
    private final SettingRepository settings;

    private final MediatorLiveData<Resource<List<Transaksi>>> list = new MediatorLiveData<>();
    private LiveData<Resource<List<Transaksi>>> source;

    private int tab;
    private String statusHariIni = "";
    private String tanggalAwal = "";
    private String tanggalAkhir = "";
    private boolean started;

    public PesananSayaViewModel(TransaksiRepository repository, SettingRepository settings, int initialTab) {
        this.repository = repository;
        this.settings = settings;
        this.tab = initialTab;
    }

    public LiveData<Resource<List<Transaksi>>> getList() {
        return list;
    }

    public int getTab() {
        return tab;
    }

    public String getStatusHariIni() {
        return statusHariIni;
    }

    public String getTanggalAwal() {
        return tanggalAwal;
    }

    public String getTanggalAkhir() {
        return tanggalAkhir;
    }

    public boolean hasDateRange() {
        return !tanggalAwal.isEmpty() && !tanggalAkhir.isEmpty();
    }

    /** First load only (a rotation keeps the list). */
    public void start() {
        if (!started) {
            started = true;
            load();
        }
    }

    public void selectTab(int newTab) {
        if (newTab != tab) {
            tab = newTab;
            load();
        }
    }

    public void setStatusHariIni(String value) {
        if (!value.equals(statusHariIni)) {
            statusHariIni = value;
            load();
        }
    }

    public void setTanggalAwal(String value) {
        tanggalAwal = value;
        load();
    }

    public void setTanggalAkhir(String value) {
        tanggalAkhir = value;
        load();
    }

    public void clearDates() {
        tanggalAwal = "";
        tanggalAkhir = "";
        load();
    }

    public void load() {
        if (source != null) {
            list.removeSource(source);
        }
        source = repository.getRiwayat(tab, statusHariIni, tanggalAwal, tanggalAkhir);
        list.addSource(source, list::setValue);
    }

    public void pesananDiterima(String idTransaksi, BaseRepository.ResultCallback<Boolean> callback) {
        repository.pesananDiterima(idTransaksi, callback);
    }

    /** The shop's WhatsApp number setting (legacy getContactUsWhatsappConfiguration). */
    public void contactWhatsapp(BaseRepository.ResultCallback<com.google.gson.JsonElement> callback) {
        settings.getContactUsWhatsappConfiguration(callback);
    }
}
