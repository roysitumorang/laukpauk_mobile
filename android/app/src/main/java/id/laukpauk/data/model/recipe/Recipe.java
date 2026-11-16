package id.laukpauk.data.model.recipe;

import com.squareup.moshi.Json;

/** A recipe (list item and detail). */
public class Recipe {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "foto")
    public String photo;
    @Json(name = "keterangan")
    public String remarks;
    public String igLink;
    public String fbLink;
}
