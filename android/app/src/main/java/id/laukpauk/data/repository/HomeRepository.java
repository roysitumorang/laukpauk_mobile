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
import id.laukpauk.data.model.home.Artikel;
import id.laukpauk.data.model.home.Banner;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.KategoriProduk;
import id.laukpauk.data.model.home.Konten;
import id.laukpauk.data.model.home.KontenItem;
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
    /** idProduk -> lowest price (a NO_PRICE marker when the product has none). Cleared by pull-to-refresh. */
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

    public LiveData<Resource<List<Artikel>>> getArtikel() {
        return load(api.getArtikel(), body -> nonNull(body == null ? null : body.listArtikel));
    }

    /** Dompet, voucher count and poin of the signed-in user (ERROR-free: a failed part counts as 0). */
    public LiveData<Resource<WalletInfo>> getWallet() {
        MutableLiveData<Resource<WalletInfo>> live = new MutableLiveData<>(Resource.loading());
        final long[] dompet = new long[1];
        final int[] voucher = new int[1];
        final long[] poin = new long[1];
        final int[] pending = {3};
        ResultCallback<Long> done = r -> {
            if (--pending[0] == 0) {
                live.setValue(Resource.success(new WalletInfo(dompet[0], voucher[0], poin[0])));
            }
        };
        enqueue(api.getTotalDompet(authToken()), body -> dompet[0] = body == null ? 0L : body.totalDompet, done);
        enqueue(api.getJumlahVoucher(authToken()),
                body -> (long) (voucher[0] = body == null ? 0 : body.jumlahVoucher), done);
        enqueue(api.getPoint(authToken(), "true"),
                body -> poin[0] = body == null || body.response == null ? 0L : body.response.point, done);
        return live;
    }

    /** The first products of the product list (useLimit "y", startFrom 0). */
    public LiveData<Resource<List<HomeProduk>>> getProduk() {
        DeliverySelection s = delivery.current();
        String code = null;
        if (s.isSet) {
            code = s.jadwalDeliveryCode;
            if (code == null) {
                // legacy: the missing code made the request fail -> an empty list
                return new MutableLiveData<>(Resource.success(Collections.<HomeProduk>emptyList()));
            }
        }
        return load(api.getProduk(code, "0", "y", authToken()),
                body -> nonNull(body == null ? null : body.listProduk));
    }

    public LiveData<Resource<List<Konten>>> getKonten(String page) {
        return load(api.getKonten(page), body -> nonNull(body == null ? null : body.dataKonten));
    }

    /** Items of a konten block; {@code limit} null = all. */
    public LiveData<Resource<List<KontenItem>>> getKontenItem(String idKonten, @Nullable Integer limit) {
        return load(api.getKontenItem(idKonten, limit), body -> nonNull(body == null ? null : body.kontenItem));
    }

    /** One-shot variant of {@link #getKontenItem} ("Lihat Semua"). */
    public void getKontenItem(String idKonten, @Nullable Integer limit, ResultCallback<List<KontenItem>> callback) {
        enqueue(api.getKontenItem(idKonten, limit), body -> nonNull(body == null ? null : body.kontenItem), callback);
    }

    public LiveData<Resource<List<KategoriProduk>>> getKategoriProduk() {
        return load(api.getKategoriProduk(), body -> nonNull(body == null ? null : body.kategoriProduk));
    }

    public void getProdukInThisCategory(String idKategoriProduk, ResultCallback<List<HomeProduk>> callback) {
        enqueue(api.getProdukInThisCategory(idKategoriProduk),
                body -> nonNull(body == null ? null : body.productInThisCategory), callback);
    }

    /**
     * Lowest unit price of a product for its tile. The result is null when the product has no price list.
     * Answers from a memory cache when possible; errors are reported as null price.
     */
    public void getLowestPrice(String idProduk, ResultCallback<LowestPrice> callback) {
        LowestPrice cached = priceCache.get(idProduk);
        if (cached != null) {
            callback.onResult(Resource.success(cached == NO_PRICE ? null : cached));
            return;
        }
        enqueue(api.getLowestPrice(idProduk), body -> {
            LowestPrice price = body == null ? null : body.lowestPrice;
            priceCache.put(idProduk, price == null ? NO_PRICE : price);
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
