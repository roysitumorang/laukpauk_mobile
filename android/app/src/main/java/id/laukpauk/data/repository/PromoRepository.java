package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.promo.ProdukPromo;
import id.laukpauk.data.model.promo.PromoItemDetail;
import id.laukpauk.data.remote.PromoApi;

/** Promo products around the chosen delivery address and the promo item detail (legacy PromoModel). */
public class PromoRepository extends BaseRepository {

    private static final String DETAIL_SELECT = "namaPromo, statusPromo, catatan, idPromo, diskon";

    private final PromoApi api;
    private final DeliveryRepository delivery;

    public PromoRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(PromoApi.class);
        this.delivery = delivery;
    }

    /** The delivery selection (the promo list depends on it). */
    public LiveData<DeliverySelection> selection() {
        return delivery.selection();
    }

    /**
     * Promo products. The chosen address + schedule narrow the list when there is one; without a selection
     * the request carries no location and the server answers every promo.
     */
    public LiveData<Resource<List<ProdukPromo>>> getProdukPromo() {
        DeliverySelection s = delivery.current();
        return load(api.getPromoAroundOfThisLatLong(s.latitude, s.longitude, s.jadwalDeliveryCode),
                body -> body == null || body.produkPromo == null
                        ? Collections.<ProdukPromo>emptyList() : body.produkPromo);
    }

    /** One promo item; SUCCESS with null data when the server says it doesn't exist. */
    public LiveData<Resource<PromoItemDetail>> getDetailPromoItem(String idPromoItem) {
        return load(api.getPromoItem(idPromoItem, DETAIL_SELECT), body -> body == null ? null : body.promoItem);
    }
}
