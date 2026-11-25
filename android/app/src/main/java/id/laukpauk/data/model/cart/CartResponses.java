package id.laukpauk.data.model.cart;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.List;

/** Response wrappers of the cart endpoints. */
public final class CartResponses {
    private CartResponses() {}

    /** {@code GET/POST buyer/cart}: the backend envelope; the cart is in {@code data}. */
    public static final class BuyerCartEnvelope {
        @Nullable public BuyerCart data;
    }

    public static final class Clear {
        @Json(name = "statusBersihkanKeranjang")
        public boolean clearCartStatus;
    }

    public static final class TransactionGift {
        public String id;
        public String idGift;
    }

    public static final class TransactionGiftList {
        @Json(name = "giftTransaksi")
        public List<TransactionGift> transactionGift;
    }

    /** {@code gift} is {@code false} when the gift does not exist (null here). */
    public static final class GiftDetail {
        public CartGift gift;
    }
}
