package id.laukpauk.ui.home;

import android.view.View;

import androidx.fragment.app.Fragment;

import id.laukpauk.R;
import id.laukpauk.databinding.ViewHomeProductBinding;
import id.laukpauk.di.AppContainer;

/** The "Produk" block of the home page and the search screen: the first products and "Lihat semua". */
final class ProductSection {

    private boolean loaded;

    ProductSection(Fragment f, ViewHomeProductBinding b, ProductSectionViewModel vm) {
        AppContainer c = AppContainer.from(f.requireContext());
        ProductTileAdapter adapter = new ProductTileAdapter(c.homeRepository::getLowestPrice,
                ProductListener.navigating(f));
        b.productList.setAdapter(adapter);

        b.viewAll.setOnClickListener(v -> HomeNav.openAllProducts(f));
        b.empty.emptyText.setText(R.string.home_product_empty);

        vm.getProduct().observe(f.getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading() && !loaded) {
                b.loadingBox.setVisibility(View.VISIBLE);
                b.productList.setVisibility(View.GONE);
                b.empty.getRoot().setVisibility(View.GONE);
                return;
            }
            if (res.isLoading()) {
                return;
            }
            loaded = true;
            boolean has = res.isSuccess() && res.data != null && !res.data.isEmpty();
            adapter.submitList(has ? res.data : null);
            b.loadingBox.setVisibility(View.GONE);
            b.productList.setVisibility(has ? View.VISIBLE : View.GONE);
            b.empty.getRoot().setVisibility(has ? View.GONE : View.VISIBLE);
        });
        vm.start();
    }
}
