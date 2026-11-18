package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.home.Banner;
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.ProductCategory;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;
import id.laukpauk.data.model.home.LowestPrice;
import id.laukpauk.data.model.home.WalletInfo;
import id.laukpauk.data.remote.HomeApi;

/**
 * Data of the home page and the screens built from the same blocks (product list, category list, konten blocks,
 * product tiles with their lowest price): legacy HomepageModel, BannerModel, KontenModel, KategoriProdukModel.
 * A failed request is reported as an ERROR resource; screens show it like an
 * empty list (the legacy models returned empty arrays on failure).
 */
public class HomeRepository extends BaseRepository {

    public static final String PAGE_HOMEPAGE = "homepage";
    public static final String PAGE_HEADERSEARCH = "headersearch";

    private final HomeApi api;
    private final DeliveryRepository delivery;
    /** productId -> lowest price (a NO_PRICE marker when the product has none). Cleared by pull-to-refresh. */
    private final Map<String, LowestPrice> priceCache = new HashMap<>();
    private static final LowestPrice NO_PRICE = new LowestPrice();

    public HomeRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(HomeApi.class);
        this.delivery = delivery;
    }

    /** True when a session token is stored (legacy authModel.isLogin()). */
    public boolean isLoggedIn() {
        return !authToken().isEmpty();
    }

    public LiveData<Resource<List<Banner>>> getBanner() {
        return load(api.getBanner(), body -> nonNull(body == null ? null : body.listBanner));
    }


    /** Dompet, voucher count and poin of the signed-in user (ERROR-free: a failed part counts as 0). */
    public LiveData<Resource<WalletInfo>> getWallet() {
        MutableLiveData<Resource<WalletInfo>> live = new MutableLiveData<>(Resource.loading());
        final long[] wallet = new long[1];
        final int[] voucher = new int[1];
        final long[] points = new long[1];
        final int[] pending = {3};
        ResultCallback<Long> done = r -> {
            if (--pending[0] == 0) {
                live.setValue(Resource.success(new WalletInfo(wallet[0], voucher[0], points[0])));
            }
        };
        enqueue(api.getWalletTotal(), body -> wallet[0] = body == null ? 0L : body.walletTotal, done);
        enqueue(api.getVoucherCount(),
                body -> (long) (voucher[0] = body == null ? 0 : body.voucherCount), done);
        enqueue(api.getPoint("true"),
                body -> points[0] = body == null || body.response == null ? 0L : body.response.point, done);
        return live;
    }

    /** The first products of the product list (useLimit "y", startFrom 0). */
    public LiveData<Resource<List<HomeProduct>>> getProduct() {
        DeliverySelection s = delivery.current();
        String code = null;
        if (s.isSet) {
            code = s.deliveryScheduleCode;
            if (code == null) {
                // legacy: the missing code made the request fail -> an empty list
                return new MutableLiveData<>(Resource.success(Collections.<HomeProduct>emptyList()));
            }
        }
        return load(api.getProduct(code, "0", "y"),
                body -> nonNull(body == null ? null : body.productList));
    }

    public LiveData<Resource<List<Content>>> getContent(String page) {
        return load(api.getContent(page), body -> nonNull(body == null ? null : body.contentData));
    }

    /** Items of a konten block; {@code limit} null = all. */
    public LiveData<Resource<List<ContentItem>>> getContentItem(String contentId, @Nullable Integer limit) {
        return load(api.getContentItem(contentId, limit), body -> nonNull(body == null ? null : body.contentItem));
    }

    /** One-shot variant of {@link #getContentItem} ("Lihat Semua"). */
    public void getContentItem(String contentId, @Nullable Integer limit, ResultCallback<List<ContentItem>> callback) {
        enqueue(api.getContentItem(contentId, limit), body -> nonNull(body == null ? null : body.contentItem), callback);
    }

    public LiveData<Resource<List<ProductCategory>>> getProductCategory() {
        return load(api.getProductCategory(), body -> nonNull(body == null ? null : body.productCategory));
    }

    public void getProductInThisCategory(String productCategoryId, ResultCallback<List<HomeProduct>> callback) {
        enqueue(api.getProductInThisCategory(productCategoryId),
                body -> nonNull(body == null ? null : body.productInThisCategory), callback);
    }

    /**
     * Lowest unit price of a product for its tile. The result is null when the product has no price list.
     * Answers from a memory cache when possible; errors are reported as null price.
     */
    public void getLowestPrice(String productId, ResultCallback<LowestPrice> callback) {
        LowestPrice cached = priceCache.get(productId);
        if (cached != null) {
            callback.onResult(Resource.success(cached == NO_PRICE ? null : cached));
            return;
        }
        enqueue(api.getLowestPrice(productId), body -> {
            LowestPrice price = body == null ? null : body.lowestPrice;
            priceCache.put(productId, price == null ? NO_PRICE : price);
            return price;
        }, callback);
    }

    /** Forget cached prices (pull-to-refresh). */
    public void clearPriceCache() {
        priceCache.clear();
    }

    private static <T> List<T> nonNull(@Nullable List<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }
}
