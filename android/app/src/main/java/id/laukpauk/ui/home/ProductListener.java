package id.laukpauk.ui.home;

import androidx.fragment.app.Fragment;


import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.ui.product.ProductActions;

/** Taps on a product tile: open the product or "Beli". */
public interface ProductListener {
    void onProduct(HomeProduct product);

    void onBuy(HomeProduct product);

    /** The standard behaviour: the product (or pack) detail, ProductActions.buy / buyPack. */
    static ProductListener navigating(final Fragment fragment) {
        return new ProductListener() {
            @Override
            public void onProduct(HomeProduct p) {
                ProductActions.open(fragment, p.id, p.pack);
            }

            @Override
            public void onBuy(HomeProduct p) {
                if (p.pack) {
                    ProductActions.buyPack(fragment, p.id, p.name);
                } else {
                    ProductActions.buy(fragment, p.id, p.name);
                }
            }
        };
    }
}
