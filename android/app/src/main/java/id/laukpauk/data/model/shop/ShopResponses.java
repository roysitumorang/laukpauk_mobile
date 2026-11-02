package id.laukpauk.data.model.shop;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.product.Paket;
import id.laukpauk.data.model.product.Produk;

import java.util.List;

/** Response envelopes of the Penjual endpoints. */
public final class ShopResponses {
    private ShopResponses() {}

    public static class Detail {
        /** null when the seller doesn't exist. */
        @Nullable public Penjual detailPenjual;
    }

    public static class ProdukList {
        public List<Produk> produkPenjual;
    }

    public static class PaketList {
        public List<Paket> paketPenjual;
    }
}
