package id.laukpauk.data.model.orders;

/**
 * A buyer transaction, both as a row of the history list (User/getRiwayatTransaksi) and as the detail
 * (Transaksi/getTransaksi). Numbers arrive as numeric strings; the lenient Moshi adapters turn them into ints.
 */
public class Transaksi {
    public String id;
    public String cartCode;
    public String nomorTransaksi;
    public String status;
    public String statusReason;
    public String catatan;
    public String dayDelivery;
    public String jadwalDelivery;
    public String formattedTanggalDelivery;
    public String waktuCheckout;
    public String alamatDelivery;
    public String alamatDetailLengkap;
    public String metodePembayaran;
    public String namaMetodePembayaran;
    public String jenisVoucher;

    public int totalBelanja;
    public int ongkir;
    public int potonganWallet;
    public int nilaiVoucher;
    public int pointOut;
    public int diskonKategori;
    public int diskonPromo;
    /** The unique transfer code for "tf_bca". */
    public int moreDataMetodePembayaran;
    public int refundedOngkir;
    public int refundedUangBelanja;

    /** (totalBelanja + ongkir) - all deductions; the unique code is not included. */
    public int totalKeseluruhan() {
        return (totalBelanja + ongkir)
                - (nilaiVoucher + potonganWallet + pointOut + diskonKategori + diskonPromo);
    }

    public boolean isTfBca() {
        return metodePembayaran != null && metodePembayaran.toLowerCase().equals("tf_bca");
    }

    /** Total shown in the list / to be paid: {@link #totalKeseluruhan()} plus the unique code for "tf_bca". */
    public int totalBayar() {
        return totalKeseluruhan() + (isTfBca() ? moreDataMetodePembayaran : 0);
    }

    public String statusLower() {
        return status == null ? "" : status.toLowerCase();
    }
}
