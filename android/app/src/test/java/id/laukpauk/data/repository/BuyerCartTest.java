package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import id.laukpauk.data.model.cart.BuyerCart;
import id.laukpauk.data.model.cart.BuyerCartItem;
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

    @Test
    public void quantityBody_setsOrRemovesAVariant() throws Exception {
        BuyerCart cart = parse("{\"data\":{\"id\":\"c1\",\"items\":["
                + "{\"product_variant_id\":\"v1\",\"quantity\":1},"
                + "{\"product_variant_id\":\"v2\",\"quantity\":2}]}}");
        List<Map<String, Object>> set = items(CartRepository.quantityBody(cart, "v2", 5));
        assertEquals(2, set.size());
        assertEquals(5L, set.get(1).get("quantity"));
        List<Map<String, Object>> removed = items(CartRepository.quantityBody(cart, "v1", 0));
        assertEquals(1, removed.size());
        assertEquals("v2", removed.get(0).get("product_variant_id"));
    }

    @Test
    public void items_carryProductAndPrices() throws Exception {
        BuyerCart cart = parse("{\"data\":{\"id\":\"c1\",\"quantity\":4,\"items\":["
                + "{\"id\":\"i1\",\"product_variant_id\":\"v1\",\"product_id\":\"p1\","
                + "\"product_name\":\"Bayam\",\"product_photo\":\"https://sadia.id/images/bayam.webp\","
                + "\"product_ready_stock\":true,\"stock_unit\":\"ikat\",\"selling_price\":5000,"
                + "\"discount_type\":1,\"discount_rate\":10,\"discount_minimum_quantity\":3,"
                + "\"discount_amount\":500,\"sale_price\":4500,\"quantity\":3},"
                + "{\"id\":\"i2\",\"product_variant_id\":\"v2\",\"product_name\":\"Tahu\","
                + "\"stock_unit\":\"pcs\",\"selling_price\":2000,\"discount_minimum_quantity\":5,"
                + "\"discount_amount\":200,\"sale_price\":1800,\"quantity\":1}]}}");
        BuyerCartItem bayam = cart.items.get(0);
        assertEquals("Bayam", bayam.productName);
        assertEquals("ikat", bayam.stockUnit);
        assertTrue(bayam.discounted());       // 3 >= minimum 3
        assertEquals(4500, bayam.unitPrice());
        assertEquals(13500, bayam.lineTotal());
        BuyerCartItem tahu = cart.items.get(1);
        assertFalse(tahu.discounted());       // 1 < minimum 5
        assertEquals(2000, tahu.unitPrice());
        assertEquals(15500, cart.total());
    }
}
