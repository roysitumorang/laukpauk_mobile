package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.product.HargaProduk;
import id.laukpauk.data.model.product.Lainnya;
import id.laukpauk.data.model.product.LowestPrice;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.model.product.SatuanSheetData;
import id.laukpauk.data.remote.ProdukApi;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;

/** Unit products: detail, prices, "other products" and the data of the unit picker sheet. */
public class ProdukRepository extends BaseRepository {

    private final ProdukApi api;
    private final DeliveryRepository delivery;
    private final SettingRepository settings;

    public ProdukRepository(RepositoryDeps deps, DeliveryRepository delivery, SettingRepository settings) {
        super(deps);
        this.api = api(ProdukApi.class);
        this.delivery = delivery;
        this.settings = settings;
    }

    /** Legacy ProdukModel.getDetailProduk; SUCCESS with null data = product not found. */
    public LiveData<Resource<Produk>> getDetailProduk(String idProduk) {
        return load(api.detailProduk(idProduk == null ? "" : idProduk), body -> body == null ? null : body.detailProduk);
    }

    /** "Produk Lainnya" (empty list on an empty answer). */
    public LiveData<Resource<List<Lainnya>>> getProdukLainnya(String idProduk) {
        return load(api.getProdukLainnya(idProduk),
                body -> body == null || body.produkLainnya == null ? Collections.<Lainnya>emptyList() : body.produkLainnya);
    }

    /** Legacy ProdukModel.getLowestPrice; SUCCESS with {@link LowestPrice#NONE} when the product has no price. */
    public void getLowestPrice(String idProduk, ResultCallback<LowestPrice> callback) {
        enqueue(api.getLowestPrice(idProduk),
                body -> body == null || body.lowestPrice == null ? LowestPrice.NONE : body.lowestPrice, callback);
    }

    /** The unit/price rows of the picker sheet (legacy PilihanSatuanProduk.getHargaProduk). */
    public void getHargaProduk(String idProduk, ResultCallback<List<HargaProduk>> callback) {
        enqueue(api.getHargaProduk(idProduk),
                body -> body == null || body.hargaProduk == null ? Collections.<HargaProduk>emptyList() : body.hargaProduk,
                callback);
    }

    /**
     * Legacy ProdukModel.showSatuanProduk (background part): product info, whether it is inside the delivery
     * coverage area, and the extra "ongkir per KM". A failed
     * request counts as false (like the legacy `!`-unwrapped null). Always SUCCESS.
     */
    public void loadSatuanSheetData(String idProduk, ResultCallback<SatuanSheetData> callback) {
        final SatuanSheetData data = new SatuanSheetData();
        final DeliverySelection sel = delivery.current();
        final boolean hasSelection = sel.latitude != null && sel.longitude != null && sel.jadwalDeliveryCode != null;
        final int[] pending = {hasSelection ? 3 : 1};
        final Runnable done = () -> {
            if (--pending[0] == 0) {
                callback.onResult(Resource.success(data));
            }
        };
        enqueue(api.detailProduk(idProduk), body -> body == null ? null : body.detailProduk, r -> {
            data.produk = r.data;
            done.run();
        });
        if (!hasSelection) {
            return;
        }
        enqueue(api.isThisProductInCA(idProduk, sel.latitude, sel.longitude), body -> body != null && body.isThisProductInCA,
                r -> {
                    data.inCoverageArea = r.isSuccess() && Boolean.TRUE.equals(r.data);
                    done.run();
                });
        settings.getSettingValue("costPerKm", r -> {
            String value = r.isSuccess() ? SettingRepository.asString(r.data) : null;
            if (value != null) {
                int cost = Json.parseInt(value, Integer.MIN_VALUE);
                if (cost != Integer.MIN_VALUE) {
                    data.ongkirPerKm = Currency.formatRupiah(cost, 0);
                }
            }
            done.run();
        });
    }
}
