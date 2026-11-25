package id.laukpauk.data.model.recipe;

import androidx.annotation.Nullable;

import java.util.List;

/** Envelope of {@code GET buyer/recipes}: page-based ({@code data.links}, {@code data.info}), rows in {@code data.rows}. */
public final class BuyerRecipeResponses {
    private BuyerRecipeResponses() {}

    public static class Page {
        @Nullable public PageData data;
    }

    public static class PageData {
        @Nullable public Links links;
        @Nullable public List<BuyerRecipe> rows;
    }

    public static class Links {
        /** Full URL of the next page; "" on the last page. */
        @Nullable public String next;
    }
}
