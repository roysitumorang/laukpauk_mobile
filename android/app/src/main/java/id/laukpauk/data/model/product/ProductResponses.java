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

    public static class ProductList {
        @Json(name = "listProduk")
        public List<Product> productList;
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
