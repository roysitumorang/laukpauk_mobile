package id.laukpauk.ui.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import id.laukpauk.data.model.promotion.BuyerPromotion;
import id.laukpauk.data.model.promotion.BuyerPromotionResponses;
import id.laukpauk.data.remote.ApiClient;

/** The promo code: {@code GET buyer/promotions/:promo_code} and its discount off the shopping cost. */
public class PromotionDeductionTest {

    private static BuyerPromotion promotion(long discount) {
        BuyerPromotion p = new BuyerPromotion();
        p.discount = discount;
        return p;
    }

    @Test
    public void readsThePromotion() throws Exception {
        String json = "{\"status_code\":200,\"status\":\"OK\",\"message\":\"\",\"data\":{\"id\":\"p1\",\"record_status\":1,"
                + "\"name\":\"Hemat\",\"code\":\"HEMAT-10\",\"deleted_code\":null,\"description\":\"\",\"discount\":10000,"
                + "\"quota_per_buyer\":2,\"effective_start_date\":\"2026-11-01T00:00:00+07:00\","
                + "\"effective_end_date\":\"2026-12-01T00:00:00+07:00\",\"usages\":1}}";
        BuyerPromotion p = ApiClient.createMoshi().adapter(BuyerPromotionResponses.One.class).fromJson(json).data;
        assertEquals("p1", p.id);
        assertEquals("HEMAT-10", p.code);
        assertEquals(10000, p.discount);
        assertEquals(2, p.quotaPerBuyer);
        assertEquals(1, p.usages);
    }

    @Test
    public void discountComesOffTheShoppingCostUpToWhatIsLeft() {
        assertEquals(10000, CheckoutViewModel.promotionDeduction(promotion(10000), 50000));
        assertEquals(8000, CheckoutViewModel.promotionDeduction(promotion(10000), 8000));
        assertEquals(0, CheckoutViewModel.promotionDeduction(promotion(10000), -500));
        assertEquals(0, CheckoutViewModel.promotionDeduction(null, 50000));
    }

    @Test
    public void codeFormatMatchesTheBackend() {
        assertTrue(CheckoutViewModel.isPromoCodeFormat("HEMAT-10"));
        assertFalse(CheckoutViewModel.isPromoCodeFormat("HEMAT 10"));
        assertFalse(CheckoutViewModel.isPromoCodeFormat("a/b"));
        assertFalse(CheckoutViewModel.isPromoCodeFormat(""));
    }
}
