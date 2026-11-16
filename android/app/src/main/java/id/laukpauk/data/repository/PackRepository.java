package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.product.Others;
import id.laukpauk.data.model.product.Pack;
import id.laukpauk.data.remote.PackApi;

/** Packages (paket): detail, "other packages" and add to cart. */
public class PackRepository extends BaseRepository {

    private final PackApi api;
    private final DeliveryRepository delivery;

    public PackRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(PackApi.class);
        this.delivery = delivery;
    }

    /** Legacy PaketModel.packDetail; SUCCESS with null data = not found. */
    public LiveData<Resource<Pack>> getPackDetail(String packId) {
        return load(api.packDetail(packId == null ? "" : packId), body -> body == null ? null : body.packDetail);
    }

    public LiveData<Resource<List<Others>>> getOtherPacks(String packId) {
        return load(api.getOtherPacks(packId),
                body -> body == null || body.otherPacks == null ? Collections.<Others>emptyList() : body.otherPacks);
    }

    /**
     * Legacy PaketModel.addToCart: Keranjang/saveCart with productType=paket. SUCCESS(true) when the server
     * accepted it; SUCCESS(false) / ERROR otherwise. The delivery schedule is sent only when one is chosen. Does not
     * refresh the cart badge - callers use CartRepository.refreshCartCount().
     */
    public void addToCart(String packId, ResultCallback<Boolean> callback) {
        DeliverySelection sel = delivery.current();
        String day = sel.deliveryScheduleCode == null ? null
                : sel.dayDelivery != null ? sel.dayDelivery : "Hari Pengantaran";
        enqueue(api.saveCart(body("cartCode", session.getCartCode(), "idProduk", packId, "jenisProduk", "paket",
                "quantity", "1", "jadwalDeliveryCode", sel.deliveryScheduleCode, "dayDelivery", day)),
                (AddToCartResult body) -> body != null && body.statusAddCart, callback);
    }
}
