package id.laukpauk.data.model.promo;

import id.laukpauk.util.Json;

/** {@code Promo/getPromoItem/{id}}: a promo item with its promo, product and unit. */
public class PromoItemDetail {
    public String idPromo;
    public String statusPromo;
    public String catatan;
    public String diskon;
    public DetailPromo detailPromo;
    public DetailProduk detailProduk;
    public DetailSatuan detailSatuan;

    public int diskonPromo() {
        return Json.parseInt(diskon, 0);
    }

    public static class DetailPromo {
        public String nama;
        public String status;
        public String deskripsi;
    }

    public static class DetailProduk {
        public String id;
        public String nama;
        public String foto;
        public String namaKategoriProduk;
        public String namaToko;
        public String idPenjual;
        public String keterangan;
    }

    public static class DetailSatuan {
        public String id;
        public String nama;
        public String harga;
        public String diskon;
    }
}
