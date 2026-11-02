package id.laukpauk.ui.seller;

import androidx.lifecycle.ViewModel;

import id.laukpauk.data.model.seller.products.SatuanProdukData;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.ProdukPenjualRepository;

/** Form state of EditSatuanProduk (kept across rotation) and the save action. */
public class EditSatuanProdukViewModel extends ViewModel {

    private final ProdukPenjualRepository repository;
    private final String idProduk;
    private final String idSatuan;

    public String nama = "";
    public String harga = "0";
    public String diskon = "0";
    public String jenisHarga = "";
    public String keterangan = "";

    public EditSatuanProdukViewModel(ProdukPenjualRepository repository, String idProduk, SatuanProdukData data) {
        this.repository = repository;
        this.idProduk = idProduk;
        if (data != null) {
            idSatuan = data.id;
            nama = nz(data.nama);
            harga = nz(data.harga);
            diskon = nz(data.diskon);
            jenisHarga = nz(data.jenisHarga);
            keterangan = nz(data.keterangan);
        } else {
            idSatuan = null;
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    public boolean isComplete() {
        return !nama.isEmpty() && !harga.isEmpty() && !diskon.isEmpty() && !jenisHarga.isEmpty();
    }

    public void simpan(BaseRepository.ResultCallback<Boolean> callback) {
        repository.saveSatuan(idSatuan, idProduk, nama, harga, diskon, jenisHarga, keterangan, callback);
    }
}
