package id.laukpauk.data.model.cart;

import java.util.List;

/** Response wrappers of the cart endpoints. */
public final class CartResponses {
    private CartResponses() {}

    public static final class Count {
        public int jumlahKeranjang;
    }

    public static final class ProductList {
        public List<CartProduct> listCart;
    }

    public static final class Total {
        public int totalKeranjang;
    }

    public static final class Clear {
        public boolean statusBersihkanKeranjang;
    }

    public static final class Delete {
        public boolean statusDeleteCart;
    }

    /** walletConfiguration is {@code false} on the server when unavailable (the lenient Moshi adapters map it to null). */
    public static final class Wallet {
        public WalletConfiguration walletConfiguration;
    }

    public static final class GiftTransaksi {
        public String id;
        public String idGift;
    }

    public static final class GiftTransaksiList {
        public List<GiftTransaksi> giftTransaksi;
    }

    /** {@code gift} is {@code false} when the gift does not exist (null here). */
    public static final class GiftDetail {
        public CartGift gift;
    }
}
