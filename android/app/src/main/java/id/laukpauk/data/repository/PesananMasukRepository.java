package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.orders.PesananMasuk;
import id.laukpauk.data.model.seller.orders.PesananMasukItem;
import id.laukpauk.data.model.seller.orders.TransaksiPenjual;
import id.laukpauk.data.remote.PesananMasukApi;

/** Seller incoming orders: the list per status, processing an order, claiming items as unavailable. */
public class PesananMasukRepository extends BaseRepository {

    private static final String ITEM_ENTITIES =
            "id, quantity, namaProduk, namaSatuan, harga, diskon, fotoProduk, catatan, jenisProduk, status";

    private final PesananMasukApi api;

    public PesananMasukRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(PesananMasukApi.class);
    }

    /** Orders with the given server status ("processing", "otw", "received", "rejected"). */
    public LiveData<Resource<List<PesananMasuk>>> getPesananMasuk(String statusTransaksi) {
        return load(api.getPesananMasuk(authToken(), statusTransaksi), body -> body == null
                || body.pesananMasuk == null ? Collections.<PesananMasuk>emptyList() : body.pesananMasuk);
    }

    /** The order's status; SUCCESS with null data when the order doesn't exist. */
    public LiveData<Resource<TransaksiPenjual>> getTransaksi(String idTransaksi) {
        return load(api.getTransaksi(idTransaksi, authToken()), body -> body == null ? null : body.transaksi);
    }

    public LiveData<Resource<List<PesananMasukItem>>> getPesananMasukItem(String idTransaksi) {
        return load(api.getPesananMasukItem(idTransaksi, authToken(), ITEM_ENTITIES), body -> body == null
                || body.pesananMasukItem == null ? Collections.<PesananMasukItem>emptyList()
                : body.pesananMasukItem);
    }

    public void prosesPesanan(String idTransaksi, ResultCallback<Boolean> callback) {
        enqueue(api.prosesPesanan(idTransaksi, authToken(), new HashMap<String, String>()),
                body -> body != null && body.statusProsesPesanan, callback);
    }

    /** Undoes a "product not available" claim. */
    public void pembatalanKlaimTidakAda(String idItem, ResultCallback<Boolean> callback) {
        enqueue(api.pembatalanKlaimTidakAda(authToken(), idItem),
                body -> body != null && body.statusPembatalanKlaimTidakTersedia, callback);
    }

    public void klaimProdukTidakTersedia(String idItem, String keterangan, ResultCallback<Boolean> callback) {
        enqueue(api.klaimProdukTidakTersedia(authToken(), idItem, keterangan),
                body -> body != null && body.statusKlaim, callback);
    }
}
