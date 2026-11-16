package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

import java.util.List;

/** Response envelopes of the paket endpoints. */
public final class PackResponses {
    private PackResponses() {}

    public static class Detail {
        /** null when the paket doesn't exist (server answers false). */
        @Json(name = "detailPaket")
        @Nullable public Pack packDetail;
    }

    public static class OthersList {
        @Json(name = "paketLainnya")
        public List<Others> otherPacks;
    }
}
