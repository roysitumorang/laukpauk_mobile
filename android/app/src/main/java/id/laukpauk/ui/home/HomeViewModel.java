package id.laukpauk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.home.Artikel;
import id.laukpauk.data.model.home.Banner;
import id.laukpauk.data.model.home.Konten;
import id.laukpauk.data.model.home.WalletInfo;
import id.laukpauk.data.repository.HomeRepository;

/** State of the home page besides the markets: hero banners, wallet summary, konten blocks and articles. */
public class HomeViewModel extends ViewModel {

    private final HomeRepository home;

    private final MediatorLiveData<Resource<List<Banner>>> banners = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Artikel>>> artikel = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<WalletInfo>> wallet = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<KontenSection>>> konten = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>(false);

    private LiveData<Resource<List<Banner>>> bannerSource;
    private LiveData<Resource<List<Artikel>>> artikelSource;
    private LiveData<Resource<WalletInfo>> walletSource;
    private LiveData<Resource<List<Konten>>> kontenSource;
    private final List<LiveData<?>> itemSources = new ArrayList<>();
    private boolean started;
    private int kontenGeneration;

    private final String kontenPage;
    private final Integer itemLimit;

    public HomeViewModel(HomeRepository home) {
        this(home, HomeRepository.PAGE_HOMEPAGE, 8);
    }

    /** @param kontenPage the konten page to show; @param itemLimit items per block (null = all) */
    public HomeViewModel(HomeRepository home, String kontenPage, Integer itemLimit) {
        this.home = home;
        this.kontenPage = kontenPage;
        this.itemLimit = itemLimit;
    }

    /** Loads only the konten blocks (the search screen). */
    public void startKonten() {
        if (!started) {
            started = true;
            loadKonten();
        }
    }

    public LiveData<Resource<List<Banner>>> getBanners() {
        return banners;
    }

    public LiveData<Resource<List<Artikel>>> getArtikel() {
        return artikel;
    }

    /** Dompet / voucher / poin; only loaded while {@link #getLoggedIn()} is true. */
    public LiveData<Resource<WalletInfo>> getWallet() {
        return wallet;
    }

    public LiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public LiveData<Resource<List<KontenSection>>> getKonten() {
        return konten;
    }

    /** First open: loads everything once (a rotation keeps the loaded data). */
    public void start() {
        if (!started) {
            started = true;
            refresh();
        }
    }

    /** Pull-to-refresh. */
    public void refresh() {
        home.clearPriceCache();
        loadBanners();
        loadWallet();
        loadKonten();
        loadArtikel();
    }

    /** The user may have signed in or out meanwhile: reload the wallet when that changed. */
    public void checkLogin() {
        if (started && home.isLoggedIn() != Boolean.TRUE.equals(loggedIn.getValue())) {
            loadWallet();
        }
    }

    private void loadBanners() {
        if (bannerSource != null) {
            banners.removeSource(bannerSource);
        }
        bannerSource = home.getBanner();
        banners.addSource(bannerSource, banners::setValue);
    }

    private void loadArtikel() {
        if (artikelSource != null) {
            artikel.removeSource(artikelSource);
        }
        artikelSource = home.getArtikel();
        artikel.addSource(artikelSource, artikel::setValue);
    }

    private void loadWallet() {
        if (walletSource != null) {
            wallet.removeSource(walletSource);
            walletSource = null;
        }
        boolean login = home.isLoggedIn();
        loggedIn.setValue(login);
        if (login) {
            walletSource = home.getWallet();
            wallet.addSource(walletSource, wallet::setValue);
        }
    }

    private void loadKonten() {
        final int mine = ++kontenGeneration;
        if (kontenSource != null) {
            konten.removeSource(kontenSource);
        }
        for (LiveData<?> s : itemSources) {
            konten.removeSource(s);
        }
        itemSources.clear();
        kontenSource = home.getKonten(kontenPage);
        final LiveData<Resource<List<Konten>>> source = kontenSource;
        konten.addSource(source, res -> {
            if (mine != kontenGeneration) {
                return;
            }
            if (res.isLoading()) {
                konten.setValue(Resource.<List<KontenSection>>loading());
                return;
            }
            konten.removeSource(source);
            List<Konten> list = res.isSuccess() && res.data != null ? res.data : new ArrayList<Konten>();
            final List<KontenSection> sections = new ArrayList<>();
            for (Konten k : list) {
                sections.add(new KontenSection(k, null));
            }
            konten.setValue(Resource.success(new ArrayList<>(sections)));
            for (int i = 0; i < list.size(); i++) {
                loadItems(sections, i, mine);
            }
        });
    }

    private void loadItems(final List<KontenSection> sections, final int index, final int mine) {
        final Konten k = sections.get(index).konten;
        final LiveData<Resource<List<id.laukpauk.data.model.home.KontenItem>>> source = home.getKontenItem(k.id, itemLimit);
        itemSources.add(source);
        konten.addSource(source, res -> {
            if (mine != kontenGeneration || res.isLoading()) {
                return;
            }
            konten.removeSource(source);
            itemSources.remove(source);
            sections.set(index, new KontenSection(k, res.isSuccess() && res.data != null ? res.data
                    : new ArrayList<id.laukpauk.data.model.home.KontenItem>()));
            konten.setValue(Resource.success(new ArrayList<>(sections)));
        });
    }
}
