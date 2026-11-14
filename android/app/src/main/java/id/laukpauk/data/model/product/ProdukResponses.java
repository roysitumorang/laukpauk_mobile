package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;
import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the product endpoints. */
public final class ProdukResponses {
    private ProdukResponses() {}

    public static class Detail {
        /** null when the product doesn't exist (server answers false). */
        @Nullable public Produk detailProduk;
    }

    public static class LowestPriceResponse {
        @Nullable public LowestPrice lowestPrice;
    }

    public static class HargaList {
        public List<HargaProduk> hargaProduk;
    }

    public static class LainnyaList {
        public List<Lainnya> produkLainnya;
    }

    public static class InCa {
        @Json(name = "isThisProductInCA")
        public boolean isThisProductInCA;
    }

}
