package id.laukpauk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Others;
import id.laukpauk.data.model.product.Product;
import id.laukpauk.data.repository.ProductRepository;

/** State of ProductDetail: the product and "Produk Lainnya". */
public class ProductDetailViewModel extends ViewModel {

    private final ProductRepository repository;
    private final String productId;
    private final MediatorLiveData<Resource<Product>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Others>>> others = new MediatorLiveData<>();
    private LiveData<Resource<Product>> detailSource;
    private LiveData<Resource<List<Others>>> othersSource;

    public ProductDetailViewModel(ProductRepository repository, String productId) {
        this.repository = repository;
        this.productId = productId;
        load();
    }

    public LiveData<Resource<Product>> getDetail() {
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
        detailSource = repository.getProductDetail(productId);
        detail.addSource(detailSource, detail::setValue);
        othersSource = repository.getOtherProducts(productId);
        others.addSource(othersSource, others::setValue);
    }
}
