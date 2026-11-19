package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import id.laukpauk.data.model.cart.BuyerCart;
import id.laukpauk.data.model.cart.CartResponses;
import id.laukpauk.data.remote.ApiClient;

/** {@code POST buyer/cart} replaces the cart: the body keeps the current items and adds the chosen variant. */
public class BuyerCartTest {

    private static BuyerCart parse(String json) throws Exception {
        return ApiClient.createMoshi().adapter(CartResponses.BuyerCartEnvelope.class).fromJson(json).data;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get("items");
    }

    @Test
    public void addsToAnExistingVariant() throws Exception {
        BuyerCart cart = parse("{\"data\":{\"id\":\"c1\",\"user_id\":\"u1\",\"quantity\":3,\"items\":["
                + "{\"id\":\"i1\",\"product_variant_id\":\"v1\",\"quantity\":1},"
                + "{\"id\":\"i2\",\"product_variant_id\":\"v2\",\"quantity\":2}]}}");
        Map<String, Object> body = CartRepository.cartBody(cart, "v2", 1);
        assertEquals("c1", body.get("id"));
        List<Map<String, Object>> items = items(body);
        assertEquals(2, items.size());
        assertEquals("v1", items.get(0).get("product_variant_id"));
        assertEquals(1L, items.get(0).get("quantity"));
        assertEquals("v2", items.get(1).get("product_variant_id"));
        assertEquals(3L, items.get(1).get("quantity"));
    }

    @Test
    public void appendsANewVariant() throws Exception {
        BuyerCart cart = parse("{\"data\":{\"id\":\"c1\",\"quantity\":1,\"items\":["
                + "{\"product_variant_id\":\"v1\",\"quantity\":1}]}}");
        List<Map<String, Object>> items = items(CartRepository.cartBody(cart, "v9", 1));
        assertEquals(2, items.size());
        assertEquals("v9", items.get(1).get("product_variant_id"));
        assertEquals(1L, items.get(1).get("quantity"));
    }

    @Test
    public void emptyCartSendsNoId() throws Exception {
        BuyerCart cart = parse("{\"data\":{\"id\":\"00000000-0000-0000-0000-000000000000\","
                + "\"user_id\":\"u1\",\"quantity\":0,\"items\":null}}");
        Map<String, Object> body = CartRepository.cartBody(cart, "v1", 1);
        assertFalse(body.containsKey("id"));
        assertEquals(1, items(body).size());
        assertFalse(CartRepository.cartBody(null, "v1", 1).containsKey("id"));
    }
}
