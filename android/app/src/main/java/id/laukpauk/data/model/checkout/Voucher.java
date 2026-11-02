package id.laukpauk.data.model.checkout;

/** A buyer voucher (GET User/getVoucher -> listVoucher[]). */
public class Voucher {
    public String id;
    public String nama;
    /** File name in ServerPath.uploadGambarVoucher/compress. */
    public String foto;
    public long minimumBelanja;
    public long nilai;
    /** What the voucher reduces, e.g. "ongkir" or "belanja". */
    public String jenis;
    public String kode;
    public String maximumUsage;
}
