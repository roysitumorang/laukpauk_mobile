package id.laukpauk.ui.home;

import androidx.fragment.app.Fragment;


import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.ui.product.ProductActions;

/** Taps on a product tile: open the product or "Beli". */
public interface ProductListener {
    void onProduct(HomeProduct product);

    void onBuy(HomeProduct product);

    /** The standard behaviour: both open the product detail, where the variant is chosen and added. */
    static ProductListener navigating(final Fragment fragment) {
        return new ProductListener() {
            @Override
            public void onProduct(HomeProduct p) {
                ProductActions.open(fragment, p.id);
            }

            @Override
            public void onBuy(HomeProduct p) {
                // the unit (variant) is chosen on the detail, which also adds it to the cart
                ProductActions.open(fragment, p.id);
            }
        };
    }
}
