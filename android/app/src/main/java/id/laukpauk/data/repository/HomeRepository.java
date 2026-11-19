package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.home.Banner;
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.HomeResponses;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.model.product.ProductResponses;
import id.laukpauk.data.model.home.ProductCategory;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;
import id.laukpauk.data.model.home.LowestPrice;
import id.laukpauk.data.model.home.WalletInfo;
import id.laukpauk.data.remote.HomeApi;
import id.laukpauk.data.remote.ProductApi;

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
    private final ProductApi productApi;
    private final DeliveryRepository delivery;
    /** productId -> lowest price (a NO_PRICE marker when the product has none). Cleared by pull-to-refresh. */
    private final Map<String, LowestPrice> priceCache = new HashMap<>();
    private static final LowestPrice NO_PRICE = new LowestPrice();

    public HomeRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(HomeApi.class);
        this.productApi = api(ProductApi.class);
        this.delivery = delivery;
    }

    /** True when a session token is stored (legacy authModel.isLogin()). */
    public boolean isLoggedIn() {
        return !authToken().isEmpty();
    }

    /** Hero banners from {@code GET buyer/banners}, without the ones marked {@code visible: false}. */
    public LiveData<Resource<List<Banner>>> getBanner() {
        return load(api.getBanner(), (HomeResponses.Banners body) -> {
            List<Banner> all = nonNull(body == null ? null : body.data);
            List<Banner> shown = new ArrayList<>();
            for (Banner b : all) {
                if (b != null && !Boolean.FALSE.equals(b.visible)) {
                    shown.add(b);
                }
            }
            return shown;
        });
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

    /**
     * The first products of {@code GET buyer/products} ({@link ProductRepository#DEFAULT_LIMIT} of them). Their
     * {@code lowest_selling_price} goes into the price cache, so the tiles need no price request.
     */
    public LiveData<Resource<List<HomeProduct>>> getProduct() {
        return load(productApi.buyerProducts(ProductRepository.DEFAULT_LIMIT, null), (ProductResponses.BuyerProducts body) -> {
            List<HomeProduct> list = new ArrayList<>();
            for (BuyerProduct p : ProductRepository.page(body).items) {
                LowestPrice price = p.tilePrice();
                priceCache.put(p.id, price == null ? NO_PRICE : price);
                list.add(p.toHomeProduct());
            }
            return list;
        });
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
