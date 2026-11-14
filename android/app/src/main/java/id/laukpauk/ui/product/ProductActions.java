package id.laukpauk.ui.product;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.laukpauk.data.Resource;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Messages;

/**
 * "Beli" buttons shared by every product list (legacy ProdukModel.showSatuanProduk / PaketModel.addToCart):
 * the unit-picker bottom sheet (ShowSatuanProduk),
 * add to cart via CartRepository and show Messages.cartSnack / the failure dialog.
 */
public final class ProductActions {
    private ProductActions() {}

    /** Guards against opening two sheets / adding a paket twice while a request is running. */
    private static boolean busy;

    /** A unit product (jenisProduk "satuan"/nonpaket). */
    public static void beli(Fragment fragment, String idProduk, @Nullable String namaProduk) {
        beli(fragment, idProduk, namaProduk, null);
    }

    /** Same, for a product bought through a promo item ({@code idPromo} sent to saveCart). */
    public static void beli(Fragment fragment, String idProduk, @Nullable String namaProduk, @Nullable String idPromo) {
        if (!fragment.isAdded() || busy) {
            return;
        }
        busy = true;
        AppContainer c = AppContainer.from(fragment.requireContext());
        c.produkRepository.loadSatuanSheetData(idProduk, result -> {
            busy = false;
            if (fragment.isAdded() && result.isSuccess() && result.data != null) {
                ShowSatuanProduk.show(fragment, idProduk, result.data, idPromo);
            }
        });
    }

    /** A package (paket): legacy PaketModel.adjdNotSetAlert + addToCart + snackbar. */
    public static void beliPaket(Fragment fragment, String idPaket, @Nullable String namaPaket) {
        if (!fragment.isAdded() || busy) {
            return;
        }
        busy = true;
        AppContainer c = AppContainer.from(fragment.requireContext());
        c.paketRepository.addToCart(idPaket, (Resource<Boolean> result) -> {
            busy = false;
            if (!fragment.isAdded()) {
                return;
            }
            boolean ok = result.isSuccess() && Boolean.TRUE.equals(result.data);
            if (ok) {
                Messages.cartSnack(fragment, fragment.getString(id.laukpauk.R.string.product_paket_berhasil));
            } else {
                Messages.snack(fragment, fragment.getString(id.laukpauk.R.string.product_paket_gagal));
            }
            c.cartRepository.refreshCartCount();
        });
    }
}
