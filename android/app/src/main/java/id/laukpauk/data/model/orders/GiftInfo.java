package id.laukpauk.data.model.orders;

import com.squareup.moshi.Json;

/** Name and description of a gift (Gift/getGift/{id}). */
public class GiftInfo {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "deskripsi")
    public String description;
}
