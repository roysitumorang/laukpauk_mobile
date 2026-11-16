package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** An item of a konten block: a product (HomeProduct fields) or a banner ({@code item}, {@code pilihan}, {@code keterangan}). */
public class ContentItem extends HomeProduct {
    /** Banner: the id passed to the banner's route. */
    @Json(name = "pilihan")
    public String choice;
    /** Banner: the image file. */
    public String item;
    @Json(name = "keterangan")
    public String remarks;
}
