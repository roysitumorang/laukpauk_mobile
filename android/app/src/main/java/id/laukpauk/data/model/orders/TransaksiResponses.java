package id.laukpauk.data.model.orders;

import java.util.List;

/** Response envelopes of the transaction endpoints. */
public final class TransaksiResponses {
    private TransaksiResponses() {}

    public static class RiwayatList {
        public List<Transaksi> riwayatTransaksi;
    }

    public static class Detail {
        /** null when the server answers false. */
        public Transaksi transaksi;
    }

    public static class Items {
        public List<ItemTransaksi> itemTransaksi;
    }

    public static class Diantar {
        public int jumlahBarangDiantar;
    }

    public static class PesananDiterima {
        public boolean pesananDiterima;
    }

    public static class BelanjaUlang {
        public boolean statusBelanjaUlang;
        public String message;
    }

    public static class GiftList {
        public List<GiftTransaksi> giftTransaksi;
    }

    public static class GiftDetail {
        /** null when the gift does not exist (the API answers false). */
        public GiftInfo gift;
    }
}
