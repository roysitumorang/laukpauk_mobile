package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.search.SearchSuggestion;

/** One row of {@code GET buyer/products} ({@code data.rows}). Only the keys the app uses are declared. */
public class BuyerProduct {
    /** {@code product_type} of a pack (paket); other values are unit products. */
    public static final int TYPE_PACK = 1;

    public String id;
    @Json(name = "category_id")
    public String categoryId;
    @Json(name = "category_name")
    public String categoryName;
    @Json(name = "product_type")
    public int productType;
    @Json(name = "product_type_name")
    public String productTypeName;
    public String name;
    /** Full image URL. */
    public String photo;
    public String description;
    @Json(name = "ready_stock")
    public boolean readyStock;
    public boolean pinned;
    /** Cheapest selling price of the product's variants (0 = no price). */
    @Json(name = "lowest_selling_price")
    public long lowestSellingPrice;

    public boolean isPack() {
        return productType == TYPE_PACK;
    }

    /** Tile model of the home page / konten. */
    public HomeProduct toHomeProduct() {
        HomeProduct p = new HomeProduct();
        p.id = id;
        p.name = name;
        p.photo = photo;
        p.productCategoryName = categoryName;
        p.shortDescription = description;
        p.stockAvailable = readyStock;
        p.pack = isPack();
        return p;
    }

    /** Tile model of the product grid. */
    public Product toProduct() {
        Product p = new Product();
        p.id = id;
        p.name = name;
        p.photo = photo;
        p.productCategoryName = categoryName;
        p.remarks = description;
        p.shortDescription = description;
        p.stockAvailable = readyStock;
        p.pack = isPack();
        return p;
    }

    /** Row of the search suggestions. */
    public SearchSuggestion toSuggestion() {
        SearchSuggestion s = new SearchSuggestion();
        s.id = id;
        s.productName = name;
        s.productPhoto = photo;
        s.itemType = productTypeName == null || productTypeName.isEmpty()
                ? (isPack() ? "paket" : "produk") : productTypeName;
        s.pack = isPack();
        return s;
    }

    /** {@link #lowestSellingPrice} as a grid price; {@link LowestPrice#NONE} when there is none. */
    public LowestPrice gridPrice() {
        if (lowestSellingPrice <= 0) {
            return LowestPrice.NONE;
        }
        LowestPrice price = new LowestPrice();
        price.price = (int) lowestSellingPrice;
        price.netPrice = (int) lowestSellingPrice;
        return price;
    }

    /** {@link #lowestSellingPrice} as a home tile price; null when there is none. */
    @Nullable
    public id.laukpauk.data.model.home.LowestPrice tilePrice() {
        if (lowestSellingPrice <= 0) {
            return null;
        }
        id.laukpauk.data.model.home.LowestPrice price = new id.laukpauk.data.model.home.LowestPrice();
        price.price = lowestSellingPrice;
        price.netPrice = lowestSellingPrice;
        return price;
    }
}
