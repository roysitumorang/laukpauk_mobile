package id.laukpauk.data.model.checkout;


import java.util.List;

/** Response envelopes of the checkout endpoints. */
public final class CheckoutResponses {
    private CheckoutResponses() {}

    public static class TotalKeranjang {
        public long totalKeranjang;
    }

    public static class TotalOngkir {
        public long totalOngkir;
    }

    public static class JumlahCart {
        public long jumlahKeranjang;
    }

    public static class TotalDompet {
        public long totalDompet;
    }

    /** GET Transaksi/getJenisOngkir: the fields are at the top level of the answer. */
    public static class JenisOngkir {
        /** "perpasar" or "perkm". */
        public String jenisOngkir;
        public int banyakPasar;
        public String namaPasarTerjauh;
        public double jarakPasarTerjauh;
    }

    public static class WalletConfig {
        /** null when the server answers false. */
        public WalletConfiguration walletConfiguration;
    }

    public static class DiskonKategori {
        /** Dynamic: an object, or false when there is no category discount. */
        public Object diskonKategori;
    }

    public static class Point {
        public long point;
    }

    public static class PointResponse {
        /** null when the server answers false. */
        public Point response;
    }

    public static class DetailUser {
        public String nama;
        public String nomorHP;
        public String alamat;
        public String statusOTP;
    }

    public static class DetailUserResponse {
        public DetailUser detailUser;
    }

    public static class Verified {
        public boolean isVerified;
    }

    public static class JadwalOpened {
        public boolean isJadwalDeliveryOpened;
    }

    public static class VoucherList {
        public List<Voucher> listVoucher;
    }

    public static class StatusVoucher {
        public String statusVoucher;
    }

    public static class Promo {
        public long diskon;
    }

    public static class PromoData {
        public Promo promo;
    }

    /** GET Promo/cekKodePromo/{kode}. */
    public static class KodePromo {
        public boolean status;
        public String message;
        public PromoData data;
    }
}
