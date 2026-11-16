package id.laukpauk.data.model.cart;

import com.squareup.moshi.Json;

/** A gift earned by the cart ({@code Gift/getGift/{id}}). */
public final class CartGift {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "deskripsi")
    public String description;
    @Json(name = "foto")
    public String photo;
}
