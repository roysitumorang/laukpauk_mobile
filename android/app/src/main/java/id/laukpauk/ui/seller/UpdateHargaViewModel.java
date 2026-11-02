package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.ProdukPenjual;
import id.laukpauk.data.repository.ProdukPenjualRepository;

/** UpdateHarga state: the search term and its result (an empty term gives an empty result, no request). */
public class UpdateHargaViewModel extends ViewModel {

    private final ProdukPenjualRepository repository;
    private final MutableLiveData<Resource<List<ProdukPenjual>>> result = new MutableLiveData<>();
    private String term = "";
    private int token;

    public UpdateHargaViewModel(ProdukPenjualRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<ProdukPenjual>>> getResult() {
        return result;
    }

    public String getTerm() {
        return term;
    }

    public void search(String newTerm) {
        term = newTerm;
        final int mine = ++token;
        result.setValue(Resource.<List<ProdukPenjual>>loading());
        repository.searchProduk(newTerm, r -> {
            if (mine == token) {
                result.setValue(r);
            }
        });
    }
}
