package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;

import id.laukpauk.data.model.address.AddressResponses;
import id.laukpauk.data.model.address.BuyerAddress;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/addresses} payload and the create / update body. */
public class AddressRepositoryTest {

    @Test
    public void list_readsRowsAndNext() throws Exception {
        String json = "{\"status_code\":200,\"data\":{\"links\":{\"first\":\"x\",\"previous\":\"\","
                + "\"current\":\"x\",\"next\":\"\",\"last\":\"x\"},\"info\":{\"limit\":100,\"page\":1,\"pages\":1,"
                + "\"total\":1},\"rows\":[{\"id\":\"a1\",\"record_status\":0,\"user_id\":\"u1\","
                + "\"longitude\":\"98.67\",\"latitude\":\"3.59\",\"street_address\":\"Jl. Merdeka 1\","
                + "\"delivery_note\":\"Pagar hijau\",\"label\":\"Rumah\",\"recipient_name\":\"Budi\","
                + "\"recipient_mobile_phone\":\"+6281234567890\",\"main\":true}]}}";
        AddressResponses.AddressList body = ApiClient.createMoshi()
                .adapter(AddressResponses.AddressList.class).fromJson(json);
        BuyerAddress a = body.data.rows.get(0);
        assertEquals("Rumah", a.label);
        assertEquals("Jl. Merdeka 1", a.streetAddress);
        assertEquals("+6281234567890", a.recipientMobilePhone);
        assertTrue(a.main);
        assertTrue(a.hasLocation());
        assertEquals("", body.data.links.next);
    }

    @Test
    public void body_hasEveryFieldTrimmed() {
        BuyerAddress a = new BuyerAddress();
        a.latitude = " 3.59 ";
        a.longitude = "98.67";
        a.streetAddress = "Jl. Merdeka 1 ";
        a.label = "Kantor";
        a.recipientName = "Budi";
        a.recipientMobilePhone = "0812 3456 7890";
        a.main = true;
        Map<String, Object> body = AddressRepository.body(a);
        assertEquals("3.59", body.get("latitude"));
        assertEquals("98.67", body.get("longitude"));
        assertEquals("Jl. Merdeka 1", body.get("street_address"));
        assertEquals("", body.get("delivery_note"));
        assertEquals("Kantor", body.get("label"));
        assertEquals("Budi", body.get("recipient_name"));
        assertEquals("0812 3456 7890", body.get("recipient_mobile_phone"));
        assertEquals(true, body.get("main"));
        assertEquals(8, body.size());
    }
}
