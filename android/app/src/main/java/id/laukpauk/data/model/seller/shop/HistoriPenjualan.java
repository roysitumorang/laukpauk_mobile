package id.laukpauk.data.model.seller.shop;

import com.squareup.moshi.Json;

/** One row of the sales history. {@link #total} is loaded separately per transaction (not part of the JSON). */
public class HistoriPenjualan {
    @Json(name = "id")
    public String id;
    @Json(name = "namaUser")
    public String namaUser;
    @Json(name = "waktuCheckout")
    public String waktuCheckout;
    @Json(name = "nomorTransaksi")
    public String nomorTransaksi;
    @Json(name = "status")
    public String status;

    /** Total of this transaction for the seller; null until loaded. */
    public transient Integer total;

    public HistoriPenjualan withTotal(Integer newTotal) {
        HistoriPenjualan copy = new HistoriPenjualan();
        copy.id = id;
        copy.namaUser = namaUser;
        copy.waktuCheckout = waktuCheckout;
        copy.nomorTransaksi = nomorTransaksi;
        copy.status = status;
        copy.total = newTotal;
        return copy;
    }
}
