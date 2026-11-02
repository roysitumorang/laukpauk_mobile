package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.model.shop.Penjual;
import id.laukpauk.data.remote.ShopApi;

/** The buyer's view of a seller's shop (legacy PenjualActivity). */
public class ShopRepository extends BaseRepository {

    private final ShopApi api;

    public ShopRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(ShopApi.class);
    }

    public String currentUserId() {
        return session.getCurrentUserID();
    }

    /** SUCCESS with null data = seller not found. */
    public LiveData<Resource<Penjual>> getDetailPenjual(String idPenjual) {
        return load(api.getDetailPenjual(idPenjual), body -> body == null ? null : body.detailPenjual);
    }

    public LiveData<Resource<List<Produk>>> getProdukPenjual(String idPenjual) {
        return load(api.getProdukPenjual(idPenjual, "nama, foto, id, namaToko, namaKategoriProduk, idPenjual, stokAvailable"),
                body -> body == null || body.produkPenjual == null ? Collections.<Produk>emptyList() : body.produkPenjual);
    }

    public LiveData<Resource<List<Paket>>> getPaketPenjual(String idPenjual) {
        return load(api.getPaketPenjual(idPenjual, "nama, foto, id, harga, diskon, idPenjual"),
                body -> body == null || body.paketPenjual == null ? Collections.<Paket>emptyList() : body.paketPenjual);
    }
}
