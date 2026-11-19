package id.laukpauk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Others;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.repository.ProductRepository;

/** State of ProductDetail: the product ({@code GET buyer/products/:id}) and "Produk Lainnya". */
public class ProductDetailViewModel extends ViewModel {

    private final ProductRepository repository;
    private final String productId;
    private final MediatorLiveData<Resource<BuyerProduct>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Others>>> others = new MediatorLiveData<>();
    private LiveData<Resource<BuyerProduct>> detailSource;
    private LiveData<Resource<List<Others>>> othersSource;

    public ProductDetailViewModel(ProductRepository repository, String productId) {
        this.repository = repository;
        this.productId = productId;
        load();
    }

    public LiveData<Resource<BuyerProduct>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<Others>>> getOthers() {
        return others;
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
            others.removeSource(othersSource);
        }
        detailSource = repository.getBuyerProduct(productId);
        detail.addSource(detailSource, detail::setValue);
        othersSource = repository.getOtherProducts(productId);
        others.addSource(othersSource, others::setValue);
    }
}
