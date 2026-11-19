package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.model.product.LowestPrice;
import id.laukpauk.data.model.product.Paged;
import id.laukpauk.data.model.product.Product;
import id.laukpauk.data.model.product.ProductResponses;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/products}: the response payload, its next page and the tile models made from it. */
public class BuyerProductsTest {

    private static final String PAGE = "{\"request_id\":\"86609e54-c094-4ce5-aaf7-0a1b0d14c4d0\","
            + "\"status_code\":200,\"status\":\"OK\",\"message\":\"\",\"data\":{"
            + "\"current\":\"https://sadia.id/v1/buyer/products?limit=10\","
            + "\"next\":\"https://sadia.id/v1/buyer/products?limit=10&next_row_no=11\",\"limit\":10,\"rows\":["
            + "{\"id\":\"01a1f1aa-c5ff-73dc-9618-01b224bed65f\",\"record_status\":0,"
            + "\"category_id\":\"01a1f1a9-ec59-7f0c-ab72-6be87c24cc24\",\"category_name\":\"Paket\","
            + "\"product_type\":1,\"product_type_name\":\"paket\",\"name\":\"Belanja hemat 2 Hari\","
            + "\"photo\":\"https://sadia.id/images/bundle-default.png\",\"description\":\"Lele, kangkung\","
            + "\"ready_stock\":true,\"pinned\":false,\"hits\":0,\"lowest_selling_price\":37000,"
            + "\"created_at\":\"2021-11-25T10:15:48+07:00\",\"deleted_by\":null,\"variants\":[]},"
            + "{\"id\":\"p2\",\"product_type\":0,\"product_type_name\":\"\",\"name\":\"Bayam\","
            + "\"photo\":\"http://sadia.id/images/bayam.webp\",\"ready_stock\":true,\"lowest_selling_price\":0}"
            + "]},\"app\":\"laukpauk\"}";

    private static ProductResponses.BuyerProducts parse(String json) throws Exception {
        return ApiClient.createMoshi().adapter(ProductResponses.BuyerProducts.class).fromJson(json);
    }

    @Test
    public void page_readsRowsAndNext() throws Exception {
        Paged<BuyerProduct> page = ProductRepository.page(parse(PAGE));
        assertEquals(2, page.items.size());
        assertEquals("https://sadia.id/v1/buyer/products?limit=10&next_row_no=11", page.next);
        BuyerProduct p = page.items.get(0);
        assertEquals("01a1f1aa-c5ff-73dc-9618-01b224bed65f", p.id);
        assertEquals("Paket", p.categoryName);
        assertTrue(p.isPack());
        assertEquals(37000, p.lowestSellingPrice);
        assertFalse(page.items.get(1).isPack());
    }

    @Test
    public void page_lastPageHasNoNext() throws Exception {
        Paged<BuyerProduct> page = ProductRepository.page(parse(
                "{\"data\":{\"current\":\"x\",\"next\":null,\"limit\":10,\"rows\":[]}}"));
        assertTrue(page.items.isEmpty());
        assertNull(page.next);
    }

    @Test
    public void page_missingDataIsEmpty() throws Exception {
        assertTrue(ProductRepository.page(parse("{\"status_code\":200}")).items.isEmpty());
        assertNull(ProductRepository.page(null).next);
    }

    @Test
    public void tiles_carryNamePhotoPackAndPrice() throws Exception {
        Paged<BuyerProduct> page = ProductRepository.page(parse(PAGE));
        BuyerProduct pack = page.items.get(0);
        HomeProduct tile = pack.toHomeProduct();
        assertEquals("Belanja hemat 2 Hari", tile.name);
        assertEquals("https://sadia.id/images/bundle-default.png", tile.photo);
        assertTrue(tile.pack);
        assertTrue(tile.isStockAvailable());
        assertEquals(37000, tile.pack ? pack.tilePrice().netPrice : 0);

        Product grid = pack.toProduct();
        assertTrue(grid.pack);
        assertEquals(37000, pack.gridPrice().netPrice);
        assertSame(LowestPrice.NONE, page.items.get(1).gridPrice());
        assertNull(page.items.get(1).tilePrice());

        SearchSuggestion s = pack.toSuggestion();
        assertEquals("paket", s.itemType);
        assertEquals("Belanja hemat 2 Hari", s.productName);
        assertEquals("produk", page.items.get(1).toSuggestion().itemType);
    }
}
