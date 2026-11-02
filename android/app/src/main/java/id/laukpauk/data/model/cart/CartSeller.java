package id.laukpauk.data.model.cart;

import java.util.List;

/** One seller block of {@code User/getCartDividedBySeller}. */
public final class CartSeller {
    public String idPenjual;
    public String namaPenjual;
    public String lokasiPasar;
    /** Id of the seller's market. */
    public String pasarPenjual;
    public List<CartProduct> produkPenjual;
}
