package id.laukpauk.data.model.promo;

import java.util.List;

/** Response envelopes of the promo endpoints. */
public final class PromoResponses {
    private PromoResponses() {}

    public static class ProdukPromoList {
        public List<ProdukPromo> produkPromo;
    }

    public static class Detail {
        /** null when the server answers false. */
        public PromoItemDetail promoItem;
    }
}
