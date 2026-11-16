package id.laukpauk.data.model.promo;

import id.laukpauk.util.Json;

/** {@code Promo/getPromoItem/{id}}: a promo item with its promo, product and unit. */
public class PromoItemDetail {
    public String idPromo;
    public String statusPromo;
    @com.squareup.moshi.Json(name = "catatan")
    public String note;
    @com.squareup.moshi.Json(name = "diskon")
    public String discount;
    public DetailPromo detailPromo;
    @com.squareup.moshi.Json(name = "detailProduk")
    public ProductDetail productDetail;
    @com.squareup.moshi.Json(name = "detailSatuan")
    public UnitDetail unitDetail;

    public int promoDiscount() {
        return Json.parseInt(discount, 0);
    }

    public static class DetailPromo {
        @com.squareup.moshi.Json(name = "nama")
        public String name;
        public String status;
        @com.squareup.moshi.Json(name = "deskripsi")
        public String description;
    }

    public static class ProductDetail {
        public String id;
        @com.squareup.moshi.Json(name = "nama")
        public String name;
        @com.squareup.moshi.Json(name = "foto")
        public String photo;
        @com.squareup.moshi.Json(name = "namaKategoriProduk")
        public String productCategoryName;
        @com.squareup.moshi.Json(name = "keterangan")
        public String remarks;
    }

    public static class UnitDetail {
        public String id;
        @com.squareup.moshi.Json(name = "nama")
        public String name;
        @com.squareup.moshi.Json(name = "harga")
        public String price;
        @com.squareup.moshi.Json(name = "diskon")
        public String discount;
    }
}
