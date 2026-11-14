package id.laukpauk.data.model.cart;

import androidx.annotation.Nullable;

/** One product line of {@code User/getCart} (also the argument of the edit screen). */
public final class CartProduct {
    public String idCart;
    public String idProduk;
    /** "nonpaket" or "paket". */
    public String jenisProduk;
    public String idSatuan;
    public String namaSatuan;
    /** "tidak pas" lets the buyer change the quantity. */
    public String jenisSatuan;
    public String namaProduk;
    public String fotoProduk;
    public String harga;
    public String diskon;
    public String quantity;
    public String catatan;
    @Nullable public String idPromo;

    public boolean isPaket() {
        return jenisProduk != null && jenisProduk.equalsIgnoreCase("paket");
    }

    public static int toInt(@Nullable String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value.trim());
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    /** (harga - diskon) * quantity. */
    public long total() {
        return (long) (toInt(harga) - toInt(diskon)) * toInt(quantity);
    }
}
