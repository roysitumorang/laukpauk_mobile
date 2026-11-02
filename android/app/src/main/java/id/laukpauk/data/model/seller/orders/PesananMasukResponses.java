package id.laukpauk.data.model.seller.orders;

import java.util.List;

/** Response envelopes of the incoming order endpoints. */
public final class PesananMasukResponses {
    private PesananMasukResponses() {}

    public static class Daftar {
        public List<PesananMasuk> pesananMasuk;
    }

    public static class Items {
        public List<PesananMasukItem> pesananMasukItem;
    }

    public static class Detail {
        /** null when the server answers false (not found). */
        public TransaksiPenjual transaksi;
    }

    public static class Status {
        public boolean statusProsesPesanan;
        public boolean statusPembatalanKlaimTidakTersedia;
        public boolean statusKlaim;
    }
}
