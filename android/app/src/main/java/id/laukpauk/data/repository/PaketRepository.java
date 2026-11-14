package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.product.Lainnya;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.data.remote.PaketApi;

/** Packages (paket): detail, "other packages" and add to cart. */
public class PaketRepository extends BaseRepository {

    private final PaketApi api;
    private final DeliveryRepository delivery;

    public PaketRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(PaketApi.class);
        this.delivery = delivery;
    }

    /** Legacy PaketModel.detailPaket; SUCCESS with null data = not found. */
    public LiveData<Resource<Paket>> getDetailPaket(String idPaket) {
        return load(api.detailPaket(idPaket == null ? "" : idPaket), body -> body == null ? null : body.detailPaket);
    }

    public LiveData<Resource<List<Lainnya>>> getPaketLainnya(String idPaket) {
        return load(api.getPaketLainnya(idPaket),
                body -> body == null || body.paketLainnya == null ? Collections.<Lainnya>emptyList() : body.paketLainnya);
    }

    /**
     * Legacy PaketModel.addToCart: Keranjang/saveCart with jenisProduk=paket. SUCCESS(true) when the server
     * accepted it; SUCCESS(false) / ERROR otherwise. The delivery schedule is sent only when one is chosen. Does not
     * refresh the cart badge - callers use CartRepository.refreshCartCount().
     */
    public void addToCart(String idPaket, ResultCallback<Boolean> callback) {
        DeliverySelection sel = delivery.current();
        String day = sel.jadwalDeliveryCode == null ? null
                : sel.dayDelivery != null ? sel.dayDelivery : "Hari Pengantaran";
        enqueue(api.saveCart(body("cartCode", session.getCartCode(), "idProduk", idPaket, "jenisProduk", "paket",
                "quantity", "1", "jadwalDeliveryCode", sel.jadwalDeliveryCode, "dayDelivery", day)),
                (AddToCartResult body) -> body != null && body.statusAddCart, callback);
    }
}
