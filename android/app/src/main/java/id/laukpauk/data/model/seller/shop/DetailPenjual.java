package id.laukpauk.data.model.seller.shop;

import com.google.gson.annotations.SerializedName;

/** {@code Penjual/getDetailPenjual}: the seller's own shop (only the fields the shop screens use). */
public class DetailPenjual {
    @SerializedName("id")
    public String id;
    /** pending / approved / rejected. */
    @SerializedName("status")
    public String status;
    @SerializedName("statusReason")
    public String statusReason;
    @SerializedName("namaToko")
    public String namaToko;
    /** "0" = the seller sells at a location of their own instead of a market. */
    @SerializedName("pasarUmum")
    public String pasarUmum;
    @SerializedName("namaPasar")
    public String namaPasar;
    @SerializedName("alamatToko")
    public String alamatToko;
    @SerializedName("banner")
    public String banner;
    @SerializedName("nomorRekening")
    public String nomorRekening;
    @SerializedName("namaUser")
    public String namaUser;

    public boolean isApproved() {
        return status != null && status.toLowerCase().equals("approved");
    }
}
