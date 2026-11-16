package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** A konten block of the app ("homepage" / "headersearch"): {@code itemType} is "produk" or a banner type. */
public class Content {
    public String id;
    @Json(name = "namaDivisi")
    public String divisionName;
    @Json(name = "jenisItem")
    public String itemType;
    /** Banner blocks: the named route a banner opens (with its {@code pilihan} as argument). */
    public String route;

    public boolean isProduct() {
        return itemType != null && itemType.toLowerCase().equals("produk");
    }
}
