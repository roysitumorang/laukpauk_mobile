package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.product.ProductResponses;
import id.laukpauk.data.Paged;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.model.product.ProductPrice;
import id.laukpauk.data.model.product.Others;
import id.laukpauk.data.model.product.LowestPrice;
import id.laukpauk.data.model.product.Product;
import id.laukpauk.data.model.product.UnitSheetData;
import id.laukpauk.data.remote.ProductApi;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;

/** Unit products: the list, detail, prices, "other products" and the data of the unit picker sheet. */
public class ProductRepository extends BaseRepository {

    private final ProductApi api;
    private final DeliveryRepository delivery;
    private final SettingRepository settings;

    public ProductRepository(RepositoryDeps deps, DeliveryRepository delivery, SettingRepository settings) {
        super(deps);
        this.api = api(ProductApi.class);
        this.delivery = delivery;
        this.settings = settings;
    }

    /** Products per page of {@code GET buyer/products} unless a screen asks for another count. */
    public static final int DEFAULT_LIMIT = 10;

    /** The first page of {@code GET buyer/products}; {@code q} (trimmed, null or blank = no filter) searches. */
    public void getBuyerProducts(@Nullable String q, int limit, ResultCallback<Paged<BuyerProduct>> callback) {
        String query = q == null || q.trim().isEmpty() ? null : q.trim();
        enqueue(api.buyerProducts(limit, query), ProductRepository::page, callback);
    }

    /** The page at {@code next} ({@code data.next} of the previous page). */
    public void getBuyerProductsPage(String next, ResultCallback<Paged<BuyerProduct>> callback) {
        enqueue(api.buyerProductsPage(next), ProductRepository::page, callback);
    }

    /** Rows and next-page URL of a {@code GET buyer/products} response (the payload is in {@code data}). */
    public static Paged<BuyerProduct> page(@Nullable ProductResponses.BuyerProducts body) {
        ProductResponses.Page data = body == null ? null : body.data;
        if (data == null) {
            return Paged.empty();
        }
        List<BuyerProduct> rows = new ArrayList<>();
        if (data.rows != null) {
            for (BuyerProduct p : data.rows) {
                if (p != null) {
                    rows.add(p);
                }
            }
        }
        return new Paged<>(rows, data.next);
    }

    /** Legacy ProdukModel.getProductDetail; SUCCESS with null data = product not found. */
    public LiveData<Resource<Product>> getProductDetail(String productId) {
        return load(api.productDetail(productId == null ? "" : productId),
                body -> body == null ? null : body.productDetail);
    }

    /** "Produk Lainnya" (empty list on an empty answer). */
    public LiveData<Resource<List<Others>>> getOtherProducts(String productId) {
        return load(api.getOtherProducts(productId),
                body -> body == null || body.otherProducts == null ? Collections.<Others>emptyList() : body.otherProducts);
    }

    /** Legacy ProdukModel.getLowestPrice; SUCCESS with {@link LowestPrice#NONE} when the product has no price. */
    public void getLowestPrice(String productId, ResultCallback<LowestPrice> callback) {
        enqueue(api.getLowestPrice(productId),
                body -> body == null || body.lowestPrice == null ? LowestPrice.NONE : body.lowestPrice, callback);
    }

    /** The unit/price rows of the picker sheet (legacy PilihanSatuanProduk.getProductPrice). */
    public void getProductPrice(String productId, ResultCallback<List<ProductPrice>> callback) {
        enqueue(api.getProductPrice(productId),
                body -> body == null || body.productPrice == null ? Collections.<ProductPrice>emptyList() : body.productPrice,
                callback);
    }

    /**
     * Legacy ProdukModel.showSatuanProduk (background part): product info, whether it is inside the delivery
     * coverage area, and the extra "ongkir per KM". A failed
     * request counts as false (like the legacy `!`-unwrapped null). Always SUCCESS.
     */
    public void loadUnitSheetData(String productId, ResultCallback<UnitSheetData> callback) {
        final UnitSheetData data = new UnitSheetData();
        final DeliverySelection sel = delivery.current();
        final boolean hasSelection = sel.latitude != null && sel.longitude != null && sel.deliveryScheduleCode != null;
        final int[] pending = {hasSelection ? 3 : 1};
        final Runnable done = () -> {
            if (--pending[0] == 0) {
                callback.onResult(Resource.success(data));
            }
        };
        enqueue(api.productDetail(productId), body -> body == null ? null : body.productDetail, r -> {
            data.product = r.data;
            done.run();
        });
        if (!hasSelection) {
            return;
        }
        enqueue(api.isThisProductInCA(productId, sel.latitude, sel.longitude),
                body -> body != null && body.isThisProductInCA,
                r -> {
                    data.inCoverageArea = r.isSuccess() && Boolean.TRUE.equals(r.data);
                    done.run();
                });
        settings.getSettingValue("costPerKm", r -> {
            String value = r.isSuccess() ? SettingRepository.asString(r.data) : null;
            if (value != null) {
                int cost = Json.parseInt(value, Integer.MIN_VALUE);
                if (cost != Integer.MIN_VALUE) {
                    data.shippingFeePerKm = Currency.formatRupiah(cost, 0);
                }
            }
            done.run();
        });
    }
}
