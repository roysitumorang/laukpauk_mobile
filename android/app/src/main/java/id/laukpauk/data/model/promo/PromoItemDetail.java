package id.laukpauk.data.model.promo;

import com.squareup.moshi.Json;

import id.laukpauk.util.Json;

/** {@code Promo/getPromoItem/{id}}: a promo item with its promo, product and unit. */
public class PromoItemDetail {
    public String idPromo;
    public String statusPromo;
    @Json(name = "catatan")
    public String note;
    @Json(name = "diskon")
    public String discount;
    public DetailPromo detailPromo;
    @Json(name = "detailProduk")
    public ProductDetail productDetail;
    @Json(name = "detailSatuan")
    public UnitDetail unitDetail;

    public int promoDiscount() {
        return Json.parseInt(discount, 0);
    }

    public static class DetailPromo {
        @Json(name = "nama")
        public String name;
        public String status;
        @Json(name = "deskripsi")
        public String description;
    }

    public static class ProductDetail {
        public String id;
        @Json(name = "nama")
        public String name;
        @Json(name = "foto")
        public String photo;
        @Json(name = "namaKategoriProduk")
        public String productCategoryName;
        @Json(name = "keterangan")
        public String remarks;
    }

    public static class UnitDetail {
        public String id;
        @Json(name = "nama")
        public String name;
        @Json(name = "harga")
        public String price;
        @Json(name = "diskon")
        public String discount;
    }
}
