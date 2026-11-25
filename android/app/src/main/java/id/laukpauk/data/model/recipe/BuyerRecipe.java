package id.laukpauk.data.model.recipe;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/** A recipe of {@code GET buyer/recipes} ({@code data.rows[]}). */
public class BuyerRecipe {
    @Nullable public String id;
    @Nullable public String name;
    /** A full URL, or a file name as stored. */
    @Nullable public String photo;
    @Nullable public String description;
    @Json(name = "instagram_link")
    @Nullable public String instagramLink;
    @Json(name = "facebook_link")
    @Nullable public String facebookLink;
}
