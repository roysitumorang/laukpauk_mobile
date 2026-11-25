package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.data.model.payment.PaymentChannelsResponse;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/payment_channels?amount=...}: the payment options at checkout. */
public class BuyerPaymentChannelsTest {

    @Test
    public void readsTheChannels() throws Exception {
        String json = "{\"status_code\":200,\"status\":\"OK\",\"message\":\"\",\"data\":["
                + "{\"id\":\"c1\",\"name\":\"BCA Virtual Account\",\"channel_code\":\"BCA_VIRTUAL_ACCOUNT\"},"
                + "{\"id\":\"c2\",\"name\":\"QRIS\",\"channel_code\":\"QRIS\"}]}";
        PaymentChannelsResponse body = ApiClient.createMoshi().adapter(PaymentChannelsResponse.class).fromJson(json);
        assertEquals(2, PaymentRepository.channels(body).size());
        PaymentChannel ch = PaymentRepository.channels(body).get(0);
        assertEquals("c1", ch.id);
        assertEquals("BCA Virtual Account", ch.name);
        assertEquals("BCA_VIRTUAL_ACCOUNT", ch.channel_code);
    }

    @Test
    public void noChannelsIsAnEmptyList() throws Exception {
        PaymentChannelsResponse body = ApiClient.createMoshi().adapter(PaymentChannelsResponse.class)
                .fromJson("{\"status_code\":200,\"data\":null}");
        assertEquals(0, PaymentRepository.channels(body).size());
        assertEquals(0, PaymentRepository.channels(null).size());
    }
}
