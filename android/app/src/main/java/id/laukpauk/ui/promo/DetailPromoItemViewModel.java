package id.laukpauk.ui.promo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.promo.PromoItemDetail;
import id.laukpauk.data.repository.PromoRepository;

/** State of the promo item detail: the item and the signed-in user's id (to tell "my own product"). */
public class DetailPromoItemViewModel extends ViewModel {

    private final PromoRepository repository;
    private final String idPromoItem;

    private final MediatorLiveData<Resource<PromoItemDetail>> detail = new MediatorLiveData<>();
    private LiveData<Resource<PromoItemDetail>> source;

    public DetailPromoItemViewModel(PromoRepository repository, String idPromoItem) {
        this.repository = repository;
        this.idPromoItem = idPromoItem;
    }

    public LiveData<Resource<PromoItemDetail>> getDetail() {
        return detail;
    }

    /** Loads the promo item (first open and pull-to-refresh). */
    public void load() {
        if (source != null) {
            detail.removeSource(source);
            source = null;
        }
        detail.setValue(Resource.<PromoItemDetail>loading());
        source = repository.getDetailPromoItem(idPromoItem);
        detail.addSource(source, detail::setValue);
    }
}
