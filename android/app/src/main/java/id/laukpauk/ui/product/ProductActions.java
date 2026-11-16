package id.laukpauk.ui.product;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.laukpauk.data.Resource;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Messages;

/**
 * "Beli" buttons shared by every product list (legacy ProdukModel.showSatuanProduk / PaketModel.addToCart):
 * the unit-picker bottom sheet (ShowProductUnit),
 * add to cart via CartRepository and show Messages.cartSnack / the failure dialog.
 */
public final class ProductActions {
    private ProductActions() {}

    /** Guards against opening two sheets / adding a paket twice while a request is running. */
    private static boolean busy;

    /** A unit product (productType "satuan"/nonpaket). */
    public static void buy(Fragment fragment, String productId, @Nullable String productName) {
        buy(fragment, productId, productName, null);
    }

    /** Same, for a product bought through a promo item ({@code idPromo} sent to saveCart). */
    public static void buy(Fragment fragment, String productId, @Nullable String productName, @Nullable String idPromo) {
        if (!fragment.isAdded() || busy) {
            return;
        }
        busy = true;
        AppContainer c = AppContainer.from(fragment.requireContext());
        c.productRepository.loadUnitSheetData(productId, result -> {
            busy = false;
            if (fragment.isAdded() && result.isSuccess() && result.data != null) {
                ShowProductUnit.show(fragment, productId, result.data, idPromo);
            }
        });
    }

    /** A package (paket): legacy PaketModel.adjdNotSetAlert + addToCart + snackbar. */
    public static void buyPack(Fragment fragment, String packId, @Nullable String packName) {
        if (!fragment.isAdded() || busy) {
            return;
        }
        busy = true;
        AppContainer c = AppContainer.from(fragment.requireContext());
        c.packRepository.addToCart(packId, (Resource<Boolean> result) -> {
            busy = false;
            if (!fragment.isAdded()) {
                return;
            }
            boolean ok = result.isSuccess() && Boolean.TRUE.equals(result.data);
            if (ok) {
                Messages.cartSnack(fragment, fragment.getString(id.laukpauk.R.string.product_pack_success));
            } else {
                Messages.snack(fragment, fragment.getString(id.laukpauk.R.string.product_pack_failed));
            }
            c.cartRepository.refreshCartCount();
        });
    }
}
