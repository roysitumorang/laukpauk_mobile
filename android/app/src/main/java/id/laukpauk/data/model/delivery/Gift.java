package id.laukpauk.data.model.delivery;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

/** A gift shown in the home start-up dialog (Gift/getGift). Only the fields the UI needs. */
public class Gift {
    @Nullable public Integer id;
    @Json(name = "foto")
    @Nullable public String photo;
    @Json(name = "nama")
    @Nullable public String name;
}
