package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the home endpoints. */
public final class HomeResponses {
    private HomeResponses() {}

    public static class Banners {
        /** {@code GET buyer/banners}: the backend envelope's {@code data}. */
        public List<Banner> data;
    }


    public static class ContentList {
        @Json(name = "dataKonten")
        public List<Content> contentData;
    }

    public static class ContentItems {
        @Json(name = "kontenItem")
        public List<ContentItem> contentItem;
    }

    public static class VoucherCount {
        @Json(name = "jumlahVoucher")
        public int voucherCount;
    }

    public static class LowestPriceResponse {
        /** false when the product has no price list. */
        public LowestPrice lowestPrice;
    }

    public static class CategoryList {
        @Json(name = "kategoriProduk")
        public List<ProductCategory> productCategory;
    }

    public static class ProductInCategory {
        public List<HomeProduct> productInThisCategory;
    }
}
