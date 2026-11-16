package id.laukpauk.data.model.promo;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the promo endpoints. */
public final class PromoResponses {
    private PromoResponses() {}

    public static class PromoProductList {
        @Json(name = "produkPromo")
        public List<PromoProduct> promoProduct;
    }

    public static class Detail {
        /** null when the server answers false. */
        public PromoItemDetail promoItem;
    }
}
