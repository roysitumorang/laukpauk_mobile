package id.laukpauk.data.model.seller.shop;

import com.squareup.moshi.Json;

/** A market the seller can pick as selling location ({@code Pasar/getPasar?jenisPasar=pasarTersedia}). */
public class Pasar {
    @Json(name = "id")
    public String id;
    @Json(name = "nama")
    public String nama;
    @Json(name = "gambar")
    public String gambar;
}
