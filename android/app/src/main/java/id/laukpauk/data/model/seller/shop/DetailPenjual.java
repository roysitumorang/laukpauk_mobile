package id.laukpauk.data.model.seller.shop;

import com.squareup.moshi.Json;

/** {@code Penjual/getDetailPenjual}: the seller's own shop (only the fields the shop screens use). */
public class DetailPenjual {
    @Json(name = "id")
    public String id;
    /** pending / approved / rejected. */
    @Json(name = "status")
    public String status;
    @Json(name = "statusReason")
    public String statusReason;
    @Json(name = "namaToko")
    public String namaToko;
    /** "0" = the seller sells at a location of their own instead of a market. */
    @Json(name = "pasarUmum")
    public String pasarUmum;
    @Json(name = "namaPasar")
    public String namaPasar;
    @Json(name = "alamatToko")
    public String alamatToko;
    @Json(name = "banner")
    public String banner;
    @Json(name = "nomorRekening")
    public String nomorRekening;
    @Json(name = "namaUser")
    public String namaUser;

    public boolean isApproved() {
        return status != null && status.toLowerCase().equals("approved");
    }
}
