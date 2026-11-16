package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** An article of the home slider. */
public class Article {
    @Json(name = "judul")
    public String heading;
    @Json(name = "foto")
    public String photo;
    public String link;
}
