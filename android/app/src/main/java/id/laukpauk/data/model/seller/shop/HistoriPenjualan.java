package id.laukpauk.data.model.seller.shop;

import com.google.gson.annotations.SerializedName;

/** One row of the sales history. {@link #total} is loaded separately per transaction (not part of the JSON). */
public class HistoriPenjualan {
    @SerializedName("id")
    public String id;
    @SerializedName("namaUser")
    public String namaUser;
    @SerializedName("waktuCheckout")
    public String waktuCheckout;
    @SerializedName("nomorTransaksi")
    public String nomorTransaksi;
    @SerializedName("status")
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
