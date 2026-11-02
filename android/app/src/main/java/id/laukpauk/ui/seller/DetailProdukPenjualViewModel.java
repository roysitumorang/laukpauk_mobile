package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.DetailProdukPenjual;
import id.laukpauk.data.model.seller.products.KategoriPilihan;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.ProdukPenjualRepository;

/** DetailProdukPenjual state: the product/paket detail, the category choices and the description edit mode. */
public class DetailProdukPenjualViewModel extends ViewModel {

    private final ProdukPenjualRepository repository;
    private final String id;
    private final String jenisProduk;
    private final MediatorLiveData<Resource<DetailProdukPenjual>> detail = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> editingDeskripsi = new MutableLiveData<>(false);
    private LiveData<Resource<DetailProdukPenjual>> source;
    private LiveData<Resource<List<KategoriPilihan>>> kategori;

    public DetailProdukPenjualViewModel(ProdukPenjualRepository repository, String id, String jenisProduk) {
        this.repository = repository;
        this.id = id;
        this.jenisProduk = jenisProduk;
    }

    public boolean isSatuan() {
        return ProdukPenjualRepository.isSatuan(jenisProduk);
    }

    public String getJenisProduk() {
        return jenisProduk;
    }

    public LiveData<Resource<DetailProdukPenjual>> getDetail() {
        if (source == null) {
            load();
        }
        return detail;
    }

    public void load() {
        if (source != null) {
            detail.removeSource(source);
        }
        source = repository.getDetail(id, jenisProduk);
        detail.addSource(source, detail::setValue);
    }

    public LiveData<Resource<List<KategoriPilihan>>> getKategori() {
        if (kategori == null) {
            kategori = repository.getKategoriProduk();
        }
        return kategori;
    }

    public LiveData<Boolean> getEditingDeskripsi() {
        return editingDeskripsi;
    }

    public void toggleEditingDeskripsi() {
        editingDeskripsi.setValue(!Boolean.TRUE.equals(editingDeskripsi.getValue()));
    }

    public void stopEditingDeskripsi() {
        editingDeskripsi.setValue(false);
    }

    public void toggleLaris(BaseRepository.ResultCallback<Boolean> callback) {
        repository.toggleProdukPalingLaris(id, jenisProduk, callback);
    }

    public void ubahKategori(String idKategori, BaseRepository.ResultCallback<Boolean> callback) {
        repository.ubahKategoriProduk(id, idKategori, callback);
    }

    public void ubahDeskripsi(String text, BaseRepository.ResultCallback<Boolean> callback) {
        repository.ubahDeskripsi(id, jenisProduk, text, callback);
    }
}
