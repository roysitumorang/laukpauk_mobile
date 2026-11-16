package id.laukpauk.data.model.orders;

import com.squareup.moshi.Json;

/** One line of a transaction (Transaksi/getItemTransaksiFromTransaksi, RO/getItemTransaksi). */
public class TransactionItem {
    public String id;
    @Json(name = "namaProduk")
    public String productName;
    @Json(name = "namaSatuan")
    public String unitName;
    @Json(name = "jenisProduk")
    public String productType;
    @Json(name = "catatan")
    public String note;
    public String status;
    public int quantity;
    @Json(name = "harga")
    public int price;
    @Json(name = "diskon")
    public int discount;
    @Json(name = "diskonPromo")
    public int promoDiscount;

    /** "nonpaket" lines show their unit, packages show "paket". */
    public String unit() {
        if (productType != null && productType.toLowerCase().equals("nonpaket")) {
            return unitName == null ? "" : unitName;
        }
        return "paket";
    }
}
