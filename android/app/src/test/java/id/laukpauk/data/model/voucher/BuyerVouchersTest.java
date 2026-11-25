package id.laukpauk.data.model.voucher;

import static org.junit.Assert.assertEquals;
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
    public void statusFollowsTheEffectiveDates() throws Exception {
        Voucher v = ApiClient.createMoshi().adapter(VoucherResponses.BuyerVouchers.class).fromJson(JSON).data.get(0);
        assertEquals("aktif", v.status("2026-11-01"));
        assertEquals("aktif", v.status("2026-11-30"));
        assertEquals("kadaluarsa", v.status("2026-10-31"));
        assertEquals("kadaluarsa", v.status("2026-12-01"));
        assertEquals("aktif", new Voucher().status("2026-12-01"));
    }
}
