package id.laukpauk.data.model.seller.shop;

import com.google.gson.annotations.SerializedName;

/** A market the seller can pick as selling location ({@code Pasar/getPasar?jenisPasar=pasarTersedia}). */
public class Pasar {
    @SerializedName("id")
    public String id;
    @SerializedName("nama")
    public String nama;
    @SerializedName("gambar")
    public String gambar;
}
