package id.laukpauk.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;

/** Form state of EditKeranjangItem: the chosen quantity and note, and the saving flag. */
public class EditKeranjangItemViewModel extends ViewModel {

    private final CartRepository repository;
    public final CartProduct product;
    public String quantity;
    public String catatan;
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);

    public EditKeranjangItemViewModel(CartRepository repository, CartProduct product) {
        this.repository = repository;
        this.product = product;
        this.quantity = product.quantity == null ? "" : product.quantity;
        this.catatan = product.catatan == null ? "" : product.catatan;
    }

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    /** Saves the change once; the callback gets true when the server accepted it. */
    public void save(BaseRepository.ResultCallback<Boolean> callback) {
        if (Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        saving.setValue(true);
        repository.updateItem(product.idCart, quantity, catatan, result -> {
            saving.setValue(false);
            callback.onResult(result);
        });
    }
}
