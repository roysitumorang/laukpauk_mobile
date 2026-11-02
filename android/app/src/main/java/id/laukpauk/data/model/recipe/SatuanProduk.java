package id.laukpauk.data.model.recipe;

import id.laukpauk.util.Json;

/** A unit/price of a product ({@code Produk/getHargaProduk}). */
public class SatuanProduk {
    public String id;
    public String nama;
    public String harga;
    public String diskon;
    /** "pas" or "tidak pas". */
    public String jenisHarga;

    public int hargaBersih() {
        return Json.parseInt(harga, 0) - Json.parseInt(diskon, 0);
    }

    public boolean isPas() {
        return jenisHarga != null && jenisHarga.toLowerCase(java.util.Locale.ROOT).equals("pas");
    }

    public boolean isTidakPas() {
        return jenisHarga != null && jenisHarga.toLowerCase(java.util.Locale.ROOT).equals("tidak pas");
    }
}
