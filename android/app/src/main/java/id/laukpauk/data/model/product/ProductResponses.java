package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;
import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the product endpoints. */
public final class ProductResponses {
    private ProductResponses() {}

    public static class Detail {
        /** null when the product doesn't exist (server answers false). */
        @Json(name = "detailProduk")
        @Nullable public Product productDetail;
    }

    /** {@code GET buyer/products}: the backend envelope; the page is in {@code data}. */
    public static class BuyerProducts {
        @Nullable public Page data;
    }

    /** {@code data} of {@code GET buyer/products}: {@code next} is the full URL of the next page, null on the last. */
    public static class Page {
        @Nullable public String current;
        @Nullable public String next;
        public int limit;
        @Nullable public List<BuyerProduct> rows;
    }

    public static class LowestPriceResponse {
        @Nullable public LowestPrice lowestPrice;
    }

    public static class PriceList {
        @Json(name = "hargaProduk")
        public List<ProductPrice> productPrice;
    }

    public static class OthersList {
        @Json(name = "produkLainnya")
        public List<Others> otherProducts;
    }

    public static class InCa {
        @Json(name = "isThisProductInCA")
        public boolean isThisProductInCA;
    }

}
