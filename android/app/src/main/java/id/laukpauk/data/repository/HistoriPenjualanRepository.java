package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.HistoriPenjualan;
import id.laukpauk.data.remote.HistoriPenjualanApi;

/** The seller's sales history (legacy HistoriPenjualanModel). */
public class HistoriPenjualanRepository extends BaseRepository {

    /** Columns the history list needs. */
    public static final String SELECT = "namaUser, waktuCheckout, nomorTransaksi, status, id";

    private final HistoriPenjualanApi api;
    private final TokoSayaRepository toko;

    public HistoriPenjualanRepository(RepositoryDeps deps, TokoSayaRepository toko) {
        super(deps);
        this.api = api(HistoriPenjualanApi.class);
        this.toko = toko;
    }

    /** Sales history of {@code idPenjual} (null = the signed-in seller), optionally only "received" ones. */
    public LiveData<Resource<List<HistoriPenjualan>>> getHistoriPenjualan(String idPenjual, String statusTransaksi) {
        retrofit2.Call<id.laukpauk.data.model.seller.shop.TokoSayaResponses.HistoriList> call = idPenjual == null
                ? api.getHistoriPenjualan(SELECT, statusTransaksi, authToken())
                : api.getHistoriPenjualan(idPenjual, SELECT, statusTransaksi, authToken());
        return load(call, body -> body == null || body.historiPenjualan == null
                ? Collections.<HistoriPenjualan>emptyList() : body.historiPenjualan);
    }

    /** Total of one transaction for the seller; 0 when the request failed (legacy). */
    public void getTotalTransaksi(String idTransaksi, ResultCallback<Integer> callback) {
        enqueue(api.getTotalTransaksi(idTransaksi, authToken()), body -> body == null ? 0 : body.totalTransaksi,
                result -> callback.onResult(Resource.success(result.isSuccess() ? result.data : 0)));
    }

    /** The signed-in seller's id (needed to open a transaction), "0" when unknown. */
    public void getIdPenjual(ResultCallback<String> callback) {
        toko.getIdPenjual(callback);
    }
}
