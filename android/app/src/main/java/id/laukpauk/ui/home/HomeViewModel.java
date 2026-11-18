package id.laukpauk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.home.Banner;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.WalletInfo;
import id.laukpauk.data.repository.HomeRepository;

/** State of the home page besides the products: hero banners, wallet summary and konten blocks. */
public class HomeViewModel extends ViewModel {

    private final HomeRepository home;

    private final MediatorLiveData<Resource<List<Banner>>> banners = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<WalletInfo>> wallet = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<ContentSection>>> content = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> loggedIn = new MutableLiveData<>(false);

    private LiveData<Resource<List<Banner>>> bannerSource;
    private LiveData<Resource<WalletInfo>> walletSource;
    private LiveData<Resource<List<Content>>> contentSource;
    private final List<LiveData<?>> itemSources = new ArrayList<>();
    private boolean started;
    private int contentGeneration;

    private final String contentPage;
    private final Integer itemLimit;

    public HomeViewModel(HomeRepository home) {
        this(home, HomeRepository.PAGE_HOMEPAGE, 8);
    }

    /** @param contentPage the konten page to show; @param itemLimit items per block (null = all) */
    public HomeViewModel(HomeRepository home, String contentPage, Integer itemLimit) {
        this.home = home;
        this.contentPage = contentPage;
        this.itemLimit = itemLimit;
    }

    /** Loads only the konten blocks (the search screen). */
    public void startContent() {
        if (!started) {
            started = true;
            loadContent();
        }
    }

    public LiveData<Resource<List<Banner>>> getBanners() {
        return banners;
    }


    /** Dompet / voucher / poin; only loaded while {@link #getLoggedIn()} is true. */
    public LiveData<Resource<WalletInfo>> getWallet() {
        return wallet;
    }

    public LiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public LiveData<Resource<List<ContentSection>>> getContent() {
        return content;
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
        loadContent();
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

    private void loadContent() {
        final int mine = ++contentGeneration;
        if (contentSource != null) {
            content.removeSource(contentSource);
        }
        for (LiveData<?> s : itemSources) {
            content.removeSource(s);
        }
        itemSources.clear();
        contentSource = home.getContent(contentPage);
        final LiveData<Resource<List<Content>>> source = contentSource;
        content.addSource(source, res -> {
            if (mine != contentGeneration) {
                return;
            }
            if (res.isLoading()) {
                content.setValue(Resource.<List<ContentSection>>loading());
                return;
            }
            content.removeSource(source);
            List<Content> list = res.isSuccess() && res.data != null ? res.data : new ArrayList<Content>();
            final List<ContentSection> sections = new ArrayList<>();
            for (Content k : list) {
                sections.add(new ContentSection(k, null));
            }
            content.setValue(Resource.success(new ArrayList<>(sections)));
            for (int i = 0; i < list.size(); i++) {
                loadItems(sections, i, mine);
            }
        });
    }

    private void loadItems(final List<ContentSection> sections, final int index, final int mine) {
        final Content k = sections.get(index).content;
        final LiveData<Resource<List<id.laukpauk.data.model.home.ContentItem>>> source = home.getContentItem(k.id, itemLimit);
        itemSources.add(source);
        content.addSource(source, res -> {
            if (mine != contentGeneration || res.isLoading()) {
                return;
            }
            content.removeSource(source);
            itemSources.remove(source);
            sections.set(index, new ContentSection(k, res.isSuccess() && res.data != null ? res.data
                    : new ArrayList<id.laukpauk.data.model.home.ContentItem>()));
            content.setValue(Resource.success(new ArrayList<>(sections)));
        });
    }
}
