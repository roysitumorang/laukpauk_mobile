package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.data.model.orders.RoPenjual;
import id.laukpauk.data.remote.RepeatOrderApi;

/** Repeat order (legacy ROModel): the sellers of a past transaction and adding them to the cart. */
public class RepeatOrderRepository extends BaseRepository {

    private final RepeatOrderApi api;
    private final DeliveryRepository delivery;

    public RepeatOrderRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(RepeatOrderApi.class);
        this.delivery = delivery;
    }

    /** The sellers (with their lines and availability) for the saved delivery address and schedule. */
    public LiveData<Resource<List<RoPenjual>>> getItemTransaksiPerPenjual(String idTransaksi) {
        DeliverySelection s = delivery.current();
        return load(api.getItemTransaksi(authToken(), idTransaksi, nz(s.latitude), nz(s.longitude),
                        nz(s.jadwalDeliveryCode)),
                body -> body == null || body.itemTransaksi == null
                        ? Collections.<RoPenjual>emptyList() : body.itemTransaksi);
    }

    /** Adds the chosen sellers' lines to the cart (sellers joined with ", " like the legacy app). */
    public void addToCartThesePenjual(String idTransaksi, List<String> sellers,
                                      ResultCallback<RepeatOrderResponses.AddToCart> callback) {
        enqueue(api.addToCartThesePenjual(authToken(), idTransaksi, session.getCartCode(),
                String.join(", ", sellers)), body -> body, callback);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
