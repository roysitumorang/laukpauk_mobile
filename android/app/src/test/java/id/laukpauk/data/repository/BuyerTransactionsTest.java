package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import id.laukpauk.data.Paged;
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.data.model.orders.BuyerTransactionItem;
import id.laukpauk.data.model.orders.BuyerTransactionResponses;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/transactions} (pages) and {@code GET buyer/transactions/:id}. */
public class BuyerTransactionsTest {

    private static final String ROW = "{\"id\":\"t1\",\"reference_number\":\"LP26330001ABCD\",\"status\":6,"
            + "\"recipient_name\":\"Budi\",\"buyer_note\":\"Titip satpam\",\"unique_code\":123,\"shopping_cost\":50123,"
            + "\"voucher_type\":0,\"voucher_discount\":5000,\"reward_points_discount\":0,\"wallet_discount\":10000,"
            + "\"delivery_estimated_cost\":15000,\"delivery_street_address\":\"Jl. Merdeka 1\","
            + "\"delivery_street_address_note\":\"Pagar hijau\",\"delivery_datetime\":\"2026-11-26T07:00:00+07:00\","
            + "\"payment_amount\":50123,\"payment_channel_name\":\"BCA Virtual Account\","
            + "\"created_at\":\"2026-11-25T21:00:00.123456+07:00\",\"withdrawal_reason\":null,"
            + "\"revised_payment_amount\":45123,\"refund_amount\":5000,\"items\":[{\"id\":\"i1\","
            + "\"product_name\":\"Bayam\",\"stock_unit\":\"ikat\",\"selling_price\":5000,\"quantity\":2,"
            + "\"subtotal\":10000,\"buyer_note\":\"Segar\",\"revised_quantity\":1,\"revised_subtotal\":5000,"
            + "\"revision_note\":\"Stok tinggal 1\",\"out_of_stock\":false}]}";

    @Test
    public void pageHasTheRowsAndTheNextUrl() throws Exception {
        String json = "{\"status_code\":200,\"data\":{\"links\":{\"next\":\"https://sadia.id/v1/buyer/transactions?page=2\"},"
                + "\"info\":{\"limit\":25,\"page\":1,\"pages\":2,\"total\":30},\"rows\":[" + ROW + "]}}";
        BuyerTransactionResponses.Page body = ApiClient.createMoshi().adapter(BuyerTransactionResponses.Page.class)
                .fromJson(json);
        Paged<BuyerTransaction> page = TransactionRepository.page(body);
        assertEquals(1, page.items.size());
        assertEquals("https://sadia.id/v1/buyer/transactions?page=2", page.next);
        BuyerTransaction t = page.items.get(0);
        assertEquals("LP26330001ABCD", t.referenceNumber);
        assertEquals(BuyerTransaction.STATUS_SHIPPED, t.status);
        assertEquals(50000, t.itemsTotal());
        assertTrue(t.isDeliveryVoucher());
        assertFalse(t.isCancelled());
        assertEquals(45123, t.revisedPaymentAmount);
        BuyerTransactionItem item = t.items.get(0);
        assertEquals("Bayam", item.productName);
        assertEquals(1, item.revisedQuantity);
        assertEquals("Stok tinggal 1", item.revisionNote);
    }

    @Test
    public void lastPageHasNoNext() throws Exception {
        String json = "{\"status_code\":200,\"data\":{\"links\":{\"next\":\"\"},\"rows\":[]}}";
        Paged<BuyerTransaction> page = TransactionRepository.page(ApiClient.createMoshi()
                .adapter(BuyerTransactionResponses.Page.class).fromJson(json));
        assertEquals(0, page.items.size());
        assertNull(page.next);
        assertEquals(0, TransactionRepository.page(null).items.size());
    }

    @Test
    public void detailIsInData() throws Exception {
        BuyerTransactionResponses.One body = ApiClient.createMoshi().adapter(BuyerTransactionResponses.One.class)
                .fromJson("{\"status_code\":200,\"data\":" + ROW + "}");
        assertEquals("t1", body.data.id);
        assertEquals(1, body.data.items.size());
    }
}
