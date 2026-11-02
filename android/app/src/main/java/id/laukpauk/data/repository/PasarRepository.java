package id.laukpauk.data.repository;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.market.Pasar;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.remote.PasarApi;

/** Markets (pasar): their products and the other markets around / beyond the delivery address. */
public class PasarRepository extends BaseRepository {

    private final PasarApi api;
    private final DeliveryRepository delivery;

    public PasarRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(PasarApi.class);
        this.delivery = delivery;
    }

    /**
     * Legacy PasarModel.getProdukInThisPasar (useLimit=y). {@code startFrom} / {@code cariProduk} may be null.
     * The schedule code is sent only when a delivery selection is set.
     */
    public void getProdukInThisPasar(String idPasar, @Nullable String startFrom, @Nullable String cariProduk,
                                     ResultCallback<List<Produk>> callback) {
        DeliverySelection sel = delivery.current();
        String jadwal = sel.isSet ? sel.jadwalDeliveryCode : null;
        enqueue(api.getProdukInThisPasar(idPasar, authToken(), jadwal, startFrom, "y", cariProduk),
                body -> body == null || body.listProduk == null ? Collections.<Produk>emptyList() : body.listProduk,
                callback);
    }

    /** Markets inside the coverage area of the chosen address (empty when no selection). */
    public void getPasarAround(@Nullable String excludeIdPasar, ResultCallback<List<Pasar>> callback) {
        DeliverySelection sel = delivery.current();
        if (!sel.isSet || sel.latitude == null || sel.longitude == null || sel.jadwalDeliveryCode == null) {
            callback.onResult(Resource.success(Collections.<Pasar>emptyList()));
            return;
        }
        enqueue(api.getPasarAround(sel.latitude, sel.longitude, excludeIdPasar == null ? "" : excludeIdPasar,
                sel.jadwalDeliveryCode), body -> body == null || body.listPasar == null ? Collections.<Pasar>emptyList() : body.listPasar,
                callback);
    }

    /** Markets outside the coverage area of the chosen address (empty when no selection). */
    public void getPasarOver(ResultCallback<List<Pasar>> callback) {
        DeliverySelection sel = delivery.current();
        if (!sel.isSet || sel.latitude == null || sel.longitude == null || sel.jadwalDeliveryCode == null) {
            callback.onResult(Resource.success(Collections.<Pasar>emptyList()));
            return;
        }
        enqueue(api.getPasarOver(sel.latitude, sel.longitude, sel.jadwalDeliveryCode),
                body -> body == null || body.listPasar == null ? Collections.<Pasar>emptyList() : body.listPasar, callback);
    }
}
