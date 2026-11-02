package id.laukpauk.data.model.recipe;

import java.util.List;

/** Response envelopes of the recipe endpoints. */
public final class RecipeResponses {
    private RecipeResponses() {}

    public static class ResepList {
        public List<Resep> dataResep;
    }

    public static class ResepDetail {
        /** null when the server answers false. */
        public Resep dataResep;
    }

    public static class ItemList {
        public List<ResepItem> dataResepItem;
    }

    public static class SatuanList {
        public List<SatuanProduk> hargaProduk;
    }

    public static class Belanja {
        public boolean statusBelanjaResep;
    }
}
