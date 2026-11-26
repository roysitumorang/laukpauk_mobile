package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.model.cart.BuyerCartItem;
import id.laukpauk.data.model.orders.BuyerTransactionResponses;
import id.laukpauk.data.remote.ApiClient;

/** {@code POST buyer/transactions}: the body the checkout sends and the transaction it gets back. */
public class CreateTransactionTest {

    private static PaymentRepository.Order order() {
        BuyerCartItem a = new BuyerCartItem();
        a.id = "item-1";
        a.note = " Potong kecil ";
        BuyerCartItem b = new BuyerCartItem();
        b.id = "item-2";
        PaymentRepository.Order o = new PaymentRepository.Order();
        o.cartId = "cart-1";
        o.cartItems = Arrays.asList(a, b);
        o.recipientName = " Budi ";
        o.recipientMobilePhone = "081234567890";
        o.buyerNote = "Titip satpam";
        o.deliveryAddressId = "address-1";
        o.deliveryDatetime = "2026-11-26T07:00:00+07:00";
        o.voucherId = "voucher-1";
        o.walletAmount = 10000;
        o.paymentChannelId = "channel-1";
        return o;
    }

    @Test
    public void bodyCarriesTheChosenChannelId() {
        Map<String, Object> body = PaymentRepository.transactionBody(order());
        assertEquals("channel-1", body.get("payment_channel_id"));
        assertEquals("cart-1", body.get("cart_id"));
        assertEquals("Budi", body.get("recipient_name"));
        assertEquals("081234567890", body.get("recipient_mobile_phone"));
        assertEquals("Titip satpam", body.get("buyer_note"));
        assertEquals("address-1", body.get("delivery_address_id"));
        assertEquals("2026-11-26T07:00:00+07:00", body.get("delivery_datetime"));
        assertEquals(false, body.get("reward_points_discounted"));
        assertEquals("voucher-1", body.get("voucher_id"));
        assertEquals(true, body.get("wallet_discounted"));
        assertEquals(10000L, body.get("wallet_amount"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        assertEquals(2, items.size());
        assertEquals("item-1", items.get(0).get("cart_item_id"));
        assertEquals("Potong kecil", items.get(0).get("buyer_note"));
        assertEquals("", items.get(1).get("buyer_note"));
    }

    @Test
    public void pointsLeaveTheVoucherOutAndNoWalletIsNotDiscounted() {
        PaymentRepository.Order o = order();
        o.rewardPointsDiscounted = true;
        o.walletAmount = 0;
        o.paymentChannelId = null;
        Map<String, Object> body = PaymentRepository.transactionBody(o);
        assertEquals(true, body.get("reward_points_discounted"));
        assertFalse(body.containsKey("voucher_id"));
        assertEquals(false, body.get("wallet_discounted"));
        assertFalse(body.containsKey("payment_channel_id"));
    }

    @Test
    public void readsTheCreatedTransaction() throws Exception {
        String json = "{\"status_code\":201,\"status\":\"Created\",\"message\":\"transaction created successfully\","
                + "\"data\":{\"id\":\"t1\",\"reference_number\":\"26330-1-ABCD\",\"status\":0,\"payment_amount\":54321,"
                + "\"payment_due_date\":\"2026-11-26T00:34:00+07:00\",\"payment_channel_id\":\"channel-1\","
                + "\"payment_channel_name\":\"BCA Virtual Account\",\"payment_virtual_account_number\":\"1234567890\","
                + "\"payment_virtual_account_expired_at\":null,\"items\":[]}}";
        BuyerTransactionResponses.One body = ApiClient.createMoshi().adapter(BuyerTransactionResponses.One.class).fromJson(json);
        assertEquals("t1", body.data.id);
        assertEquals("26330-1-ABCD", body.data.referenceNumber);
        assertEquals(54321, body.data.paymentAmount);
        assertEquals("BCA Virtual Account", body.data.paymentChannelName);
        assertEquals("1234567890", body.data.paymentVirtualAccountNumber);
        assertNull(body.data.paymentVirtualAccountExpiredAt);
    }

    @Test
    public void validPromoCodeSendsItsPromotionId() {
        PaymentRepository.Order o = order();
        o.promotionId = "promotion-1";
        assertEquals("promotion-1", PaymentRepository.transactionBody(o).get("promotion_id"));
        assertFalse(PaymentRepository.transactionBody(order()).containsKey("promotion_id"));
    }
}
