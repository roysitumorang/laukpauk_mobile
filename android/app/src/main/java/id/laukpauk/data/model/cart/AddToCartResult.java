package id.laukpauk.data.model.cart;

import androidx.annotation.Nullable;

/** Response of {@code Keranjang/saveCart} (and the package / repeat-order / recipe add-to-cart calls). */
public final class AddToCartResult {
    public boolean statusAddCart;
    @Nullable public String message;
}
