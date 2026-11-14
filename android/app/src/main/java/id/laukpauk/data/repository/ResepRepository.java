package id.laukpauk.data.repository;

import id.laukpauk.util.Json;

import java.util.Map;

import androidx.lifecycle.LiveData;


import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.recipe.RecipeResponses;
import id.laukpauk.data.model.recipe.Resep;
import id.laukpauk.data.model.recipe.ResepItem;
import id.laukpauk.data.model.recipe.SatuanProduk;
import id.laukpauk.data.remote.ResepApi;

/** Recipes, their ingredients and "Belanja Resep" (legacy ResepModel). */
public class ResepRepository extends BaseRepository {

    private static final String ENTITAS_RESEP = "id, nama, foto, keterangan, igLink, fbLink";
    private static final String ENTITAS_RESEP_ITEM = "id, namaProduk, takaran, idProduk";
    private static final String DEFAULT_DAY = "Hari Pengataran";

    /** One ingredient picked for "Belanja Resep". */
    public static final class SelectedItem {
        public final String idItem;
        public final String idProduk;
        public final String idSatuan;
        public final int quantity;
        public final int subTotal;

        public SelectedItem(String idItem, String idProduk, String idSatuan, int quantity, int subTotal) {
            this.idItem = idItem;
            this.idProduk = idProduk;
            this.idSatuan = idSatuan;
            this.quantity = quantity;
            this.subTotal = subTotal;
        }
    }

    private final ResepApi api;
    private final DeliveryRepository delivery;

    public ResepRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(ResepApi.class);
        this.delivery = delivery;
    }

    public LiveData<Resource<List<Resep>>> getResep() {
        return load(api.getResep(),
                body -> body == null || body.dataResep == null ? Collections.<Resep>emptyList() : body.dataResep);
    }

    /** SUCCESS with null data when the recipe doesn't exist. */
    public LiveData<Resource<Resep>> getDetailResep(String idResep) {
        return load(api.getDetailResep(idResep, ENTITAS_RESEP), body -> body == null ? null : body.dataResep);
    }

    public LiveData<Resource<List<ResepItem>>> getResepItem(String idResep) {
        return load(api.getResepItem(idResep, ENTITAS_RESEP_ITEM),
                body -> body == null || body.dataResepItem == null
                        ? Collections.<ResepItem>emptyList() : body.dataResepItem);
    }

    /** Units + prices of an ingredient's product (empty list when there are none). */
    public void getSatuanProduk(String idProduk, ResultCallback<List<SatuanProduk>> callback) {
        enqueue(api.getHargaProduk(idProduk),
                (RecipeResponses.SatuanList body) -> body == null || body.hargaProduk == null
                        ? Collections.<SatuanProduk>emptyList() : body.hargaProduk,
                callback);
    }

    public boolean isDeliverySet() {
        return delivery.current().isSet;
    }

    /**
     * Adds all picked ingredients to the cart. SUCCESS(true) when the server's statusBelanjaResep is true;
     * ERROR when no delivery schedule is chosen (legacy: nothing happens).
     */
    public void belanjaResep(List<SelectedItem> items, ResultCallback<Boolean> callback) {
        DeliverySelection s = delivery.current();
        if (s.jadwalDeliveryCode == null) {
            callback.onResult(Resource.error("jadwalDeliveryCode", 0));
            return;
        }
        Map<String, Object> selected = Json.newObject();
        for (SelectedItem item : items) {
            Map<String, Object> o = Json.newObject();
            o.put("idProduk", item.idProduk);
            o.put("quantity", String.valueOf(item.quantity));
            o.put("idSatuan", item.idSatuan);
            o.put("subTotal", item.subTotal);
            selected.put(item.idItem, o);
        }
        String day = s.dayDelivery != null ? s.dayDelivery : DEFAULT_DAY;
        enqueue(api.belanjaResep(body("cartCode", session.getCartCode(), "selectedItems", selected, "dayDelivery",
                day, "jadwalDeliveryCode", s.jadwalDeliveryCode)),
                (RecipeResponses.Belanja body) -> body != null && body.statusBelanjaResep, callback);
    }
}
