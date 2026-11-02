package id.laukpauk.ui.product;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * "Beli" buttons shared by every product list (legacy ProdukModel.showSatuanProduk / PaketModel.addToCart):
 * ensure a delivery selection (DeliveryFlow.ensure), then the unit-picker bottom sheet (ShowSatuanProduk),
 * add to cart via CartRepository and show Messages.cartSnack / the failure dialog.
 *
 * PLACEHOLDER (contract only) - implemented by the product slice. Keep these signatures.
 */
public final class ProductActions {
    private ProductActions() {}

    /** A unit product (jenisProduk "satuan"/nonpaket). */
    public static void beli(Fragment fragment, String idProduk, @Nullable String namaProduk) {
        beli(fragment, idProduk, namaProduk, null);
    }

    /** Same, for a product bought through a promo item ({@code idPromo} sent to saveCart). */
    public static void beli(Fragment fragment, String idProduk, @Nullable String namaProduk, @Nullable String idPromo) {
    }

    /** A package (paket): legacy PaketModel.adjdNotSetAlert + addToCart + snackbar. */
    public static void beliPaket(Fragment fragment, String idPaket, @Nullable String namaPaket) {
    }
}
