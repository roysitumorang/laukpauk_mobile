package id.laukpauk.data.model.market;

import id.laukpauk.data.model.product.Produk;

import java.util.List;

/** Response envelopes of the Pasar endpoints. */
public final class PasarResponses {
    private PasarResponses() {}

    public static class ProdukList {
        public List<Produk> listProduk;
    }

    public static class PasarList {
        public List<Pasar> listPasar;
    }
}
