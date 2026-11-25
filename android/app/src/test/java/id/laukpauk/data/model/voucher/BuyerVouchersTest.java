package id.laukpauk.data.model.voucher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/vouchers?purchase=...}: the vouchers on the checkout page. */
public class BuyerVouchersTest {

    private static final String JSON = "{\"status_code\":200,\"status\":\"OK\",\"message\":\"\",\"data\":[{"
            + "\"id\":\"v1\",\"record_status\":1,\"voucher_type\":0,\"voucher_type_name\":\"delivery cost\","
            + "\"target_audience\":0,\"target_audience_name\":\"all buyers\",\"code\":\"ONGKIRHEMAT\","
            + "\"deleted_code\":null,\"photo\":\"https://sadia.id/photos/v1.jpg\",\"name\":\"Gratis Ongkir\","
            + "\"discount\":10000,\"minimum_purchase\":50000,\"quota_per_buyer\":1,"
            + "\"effective_start_time\":\"0001-01-01T00:00:00Z\",\"effective_end_time\":\"0001-01-01T23:59:59Z\","
            + "\"effective_start_date\":\"2026-11-01T00:00:00Z\",\"effective_end_date\":\"2026-11-30T00:00:00Z\","
            + "\"description\":\"Hemat ongkir\"},{\"id\":\"v2\",\"voucher_type\":1,\"code\":null,"
            + "\"name\":\"Diskon Belanja\",\"discount\":5000,\"minimum_purchase\":25000}]}";

    @Test
    public void readsTheVouchers() throws Exception {
        VoucherResponses.BuyerVouchers body = ApiClient.createMoshi()
                .adapter(VoucherResponses.BuyerVouchers.class).fromJson(JSON);
        assertEquals(2, body.data.size());
        Voucher v = body.data.get(0);
        assertEquals("v1", v.id);
        assertEquals("Gratis Ongkir", v.name);
        assertEquals("ONGKIRHEMAT", v.code);
        assertEquals(10000, v.value);
        assertEquals(50000, v.minimumShopping);
        assertEquals("ongkir", v.type());
        assertEquals("https://sadia.id/photos/v1.jpg", v.photo);
        Voucher shopping = body.data.get(1);
        assertEquals("belanja", shopping.type());
        assertNull(shopping.code);
        assertEquals(1, v.quotaPerBuyer);
        assertEquals("1 November 2026", v.validFrom());
        assertEquals("30 November 2026", v.validUntil());
        assertEquals("", shopping.validUntil());
    }

    @Test
    public void canUseNeedsMinimumPurchaseDateAndTime() throws Exception {
        Voucher v = first();
        // 2026-11-01..2026-11-30, 00:00:00..23:59:59, minimum Rp50.000
        assertTrue(v.canUse(50000, "2026-11-01", "00:00:00"));
        assertTrue(v.canUse(80000, "2026-11-30", "23:59:59"));
        assertFalse(v.canUse(49999, "2026-11-15", "12:00:00"));
        assertFalse(v.canUse(50000, "2026-10-31", "12:00:00"));
        assertFalse(v.canUse(50000, "2026-12-01", "12:00:00"));
        v.effectiveStartTime = "0000-01-01T08:00:00Z";
        v.effectiveEndTime = "0000-01-01T17:00:00Z";
        assertFalse(v.canUse(50000, "2026-11-15", "07:59:59"));
        assertTrue(v.canUse(50000, "2026-11-15", "08:00:00"));
        assertTrue(v.canUse(50000, "2026-11-15", "17:00:00"));
        assertFalse(v.canUse(50000, "2026-11-15", "17:00:01"));
        assertEquals("08:00", v.timeFrom());
        assertEquals("17:00", v.timeUntil());
    }

    @Test
    public void deliveryVoucherReducesTheDeliveryCostOthersTheShoppingCost() throws Exception {
        Voucher delivery = first(); // voucher_type 0, discount 10.000
        assertEquals(10000, delivery.deduction(80000, 15000));
        assertEquals(8000, delivery.deduction(80000, 8000));
        Voucher shopping = new Voucher();
        shopping.voucherType = Voucher.TYPE_SHOPPING_COST;
        shopping.value = 10000;
        assertEquals(10000, shopping.deduction(80000, 0));
        assertEquals(6000, shopping.deduction(6000, 15000));
    }

    private static Voucher first() throws Exception {
        return ApiClient.createMoshi().adapter(VoucherResponses.BuyerVouchers.class).fromJson(JSON).data.get(0);
    }
}
