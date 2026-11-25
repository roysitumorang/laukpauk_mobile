package id.laukpauk.data.model.orders;

import com.squareup.moshi.Json;


/** Response envelopes of the transaction endpoints. */
public final class TransactionResponses {
    private TransactionResponses() {}

    public static class Detail {
        /** null when the server answers false. */
        @Json(name = "transaksi")
        public Transaction transaction;
    }

    public static class Delivered {
        @Json(name = "jumlahBarangDiantar")
        public int deliveredGoodsCount;
    }

    public static class OrderReceived {
        @Json(name = "pesananDiterima")
        public boolean orderReceived;
    }

    public static class Reorder {
        @Json(name = "statusBelanjaUlang")
        public boolean reorderStatus;
        public String message;
    }
}
