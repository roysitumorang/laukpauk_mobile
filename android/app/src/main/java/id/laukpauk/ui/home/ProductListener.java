package id.laukpauk.ui.home;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.ui.product.ProductActions;

/** Taps on a product tile: open the product or "Beli". */
public interface ProductListener {
    void onProduct(HomeProduct product);

    void onBuy(HomeProduct product);

    /** The standard behaviour: productDetailFragment, ProductActions.beli. */
    static ProductListener navigating(final Fragment fragment) {
        return new ProductListener() {
            @Override
            public void onProduct(HomeProduct p) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                NavHostFragment.findNavController(fragment).navigate(R.id.productDetailFragment, args);
            }

            @Override
            public void onBuy(HomeProduct p) {
                ProductActions.buy(fragment, p.id, p.name);
            }
        };
    }
}
