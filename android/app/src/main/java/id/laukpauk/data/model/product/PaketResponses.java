package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;

import java.util.List;

/** Response envelopes of the paket endpoints. */
public final class PaketResponses {
    private PaketResponses() {}

    public static class Detail {
        /** null when the paket doesn't exist (server answers false). */
        @Nullable public Paket detailPaket;
    }

    public static class LainnyaList {
        public List<Lainnya> paketLainnya;
    }
}
