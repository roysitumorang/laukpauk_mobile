package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.DetailProdukPenjual;
import id.laukpauk.data.model.seller.products.KategoriPilihan;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.ProdukPenjualRepository;

/** TambahProduk form state (draft values survive rotation), the category choices and the save action. */
public class TambahProdukViewModel extends ViewModel {

    private final ProdukPenjualRepository repository;
    private final String idProduk;
    private final String jenisArg;
    private final LiveData<Resource<List<KategoriPilihan>>> kategori;
    /** Bumped whenever the drafts were replaced by the loaded product. */
    private final MutableLiveData<Integer> draftVersion = new MutableLiveData<>(0);
    private final MutableLiveData<File> image = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MediatorLiveData<DetailProdukPenjual> detail = new MediatorLiveData<>();

    public String nama = "";
    public String keterangan = "";
    public String harga = "";
    public String diskon = "";
    public String jenis = "";
    public String kategoriSelected = "";

    public TambahProdukViewModel(ProdukPenjualRepository repository, String idProduk, String jenisArg) {
        this.repository = repository;
        this.idProduk = idProduk;
        this.jenisArg = jenisArg == null ? "satuan" : jenisArg;
        this.kategori = repository.getKategoriProduk();
        if (idProduk == null) {
            harga = "0";
            diskon = "0";
        } else {
            loadDetail();
        }
    }

    private void loadDetail() {
        repository.getDetailForEdit(idProduk, jenisArg, r -> {
            if (!r.isSuccess() || r.data == null) {
                return;
            }
            DetailProdukPenjual d = r.data;
            nama = nz(d.nama);
            keterangan = nz(d.keterangan);
            if (ProdukPenjualRepository.isSatuan(jenisArg)) {
                jenis = "satuan";
                kategoriSelected = nz(d.idKategoriProduk);
            } else {
                jenis = "paket";
                harga = nz(d.harga);
                diskon = nz(d.diskon);
            }
            detail.setValue(d);
            draftVersion.setValue(draftVersion.getValue() + 1);
        });
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public boolean isNew() {
        return idProduk == null;
    }

    public String getIdProduk() {
        return idProduk;
    }

    public String getJenisArg() {
        return jenisArg;
    }

    public LiveData<Resource<List<KategoriPilihan>>> getKategori() {
        return kategori;
    }

    public LiveData<Integer> getDraftVersion() {
        return draftVersion;
    }

    public LiveData<DetailProdukPenjual> getDetail() {
        return detail;
    }

    public LiveData<File> getImage() {
        return image;
    }

    public void setImage(File file) {
        image.setValue(file);
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    public boolean isFormCompleted() {
        if (jenis.equals("satuan")) {
            return !nama.isEmpty() && !kategoriSelected.equals("0");
        }
        if (jenis.equals("paket")) {
            return !nama.isEmpty() && !harga.isEmpty() && !diskon.isEmpty();
        }
        return false;
    }

    public void simpan(BaseRepository.ResultCallback<Boolean> callback) {
        busy.setValue(true);
        repository.saveProduk(idProduk, jenis.toLowerCase(Locale.ROOT), nama, keterangan, kategoriSelected, harga,
                diskon, image.getValue(), r -> {
                    busy.setValue(false);
                    callback.onResult(r);
                });
    }
}
