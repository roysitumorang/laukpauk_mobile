package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.data.remote.RepeatOrderApi;

/** Repeat order (legacy ROModel): the lines of a past transaction and adding them to the cart. */
public class RepeatOrderRepository extends BaseRepository {

    private final RepeatOrderApi api;
    private final DeliveryRepository delivery;

    public RepeatOrderRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(RepeatOrderApi.class);
        this.delivery = delivery;
    }

    /** The lines (and their availability) for the saved delivery address and schedule; null data = nothing. */
    public LiveData<Resource<RepeatOrderResponses.Items>> getItemTransaksi(String idTransaksi) {
        DeliverySelection s = delivery.current();
        return load(api.getItemTransaksi(idTransaksi, nz(s.latitude), nz(s.longitude), nz(s.jadwalDeliveryCode)),
                body -> body);
    }

    /** Adds the transaction's lines to the cart. */
    public void addToCart(String idTransaksi, ResultCallback<RepeatOrderResponses.AddToCart> callback) {
        enqueue(api.addToCart(body("idTransaksi", idTransaksi, "cartCode", session.getCartCode())), body -> body,
                callback);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
