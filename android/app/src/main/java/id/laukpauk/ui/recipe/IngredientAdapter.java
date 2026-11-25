package id.laukpauk.ui.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.product.BuyerProductVariant;
import id.laukpauk.databinding.ItemRecipeIngredientBinding;
import id.laukpauk.util.Currency;

/** The ingredient rows of a recipe: what it needs, then the variant, quantity and cart toggle. */
class IngredientAdapter extends ListAdapter<IngredientRow, IngredientAdapter.Holder> {

    interface Listener {
        void onPickVariant(IngredientRow row, int variantIndex);

        void onPickQuantity(IngredientRow row, long quantity);

        void onCart(IngredientRow row);
    }

    private final Listener listener;

    IngredientAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<IngredientRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull IngredientRow a, @NonNull IngredientRow b) {
                return String.valueOf(a.item.id).equals(String.valueOf(b.item.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull IngredientRow a, @NonNull IngredientRow b) {
                return a.sameAs(b);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRecipeIngredientBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        IngredientRow row = getItem(position);
        ItemRecipeIngredientBinding b = h.b;
        Context ctx = b.getRoot().getContext();
        float d = ctx.getResources().getDisplayMetrics().density;

        int pad = row.selected ? Math.round(10 * d) : 0;
        b.root.setPadding(pad, pad, pad, pad);
        b.root.setBackgroundResource(row.selected ? R.drawable.bg_recipe_item_selected : 0);

        b.number.setText(row.index + ".");
        b.name.setText(row.item.productName == null ? "" : row.item.productName.toUpperCase(Locale.ROOT));
        b.portion.setText(required(ctx, row.item));

        boolean outOfStock = !row.item.productReadyStock;
        b.spinner.setVisibility(!row.variantsLoaded ? View.VISIBLE : View.GONE);
        b.noUnit.setText(outOfStock ? R.string.recipe_out_of_stock : R.string.recipe_no_unit);
        b.noUnit.setVisibility(row.variantsLoaded && (outOfStock || row.variants.isEmpty()) ? View.VISIBLE : View.GONE);
        boolean hasUnit = row.variantsLoaded && !outOfStock && !row.variants.isEmpty();
        b.controls.setVisibility(hasUnit ? View.VISIBLE : View.GONE);

        if (hasUnit) {
            b.unit.setText(row.selectedVariant == null ? ctx.getString(R.string.recipe_choose_unit)
                    : unitOf(row.selectedVariant));
            b.unit.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(ctx, b.unit);
                menu.getMenu().add(0, 0, 0, R.string.recipe_choose_unit);
                for (int i = 0; i < row.variants.size(); i++) {
                    BuyerProductVariant variant = row.variants.get(i);
                    menu.getMenu().add(0, i + 1, i + 1, unitOf(variant) + " - " + Currency.formatRupiah(variant.price(), 0));
                }
                menu.setOnMenuItemClickListener(m -> {
                    if (m.getItemId() > 0) {
                        listener.onPickVariant(row, m.getItemId() - 1);
                    }
                    return true;
                });
                menu.show();
            });

            b.quantity.setVisibility(View.VISIBLE);
            b.quantity.setText(ctx.getString(R.string.recipe_item_count, row.quantity));
            b.quantity.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(ctx, b.quantity);
                for (int q = 1; q <= Math.max(10, row.item.quantity); q++) {
                    menu.getMenu().add(0, q, q, ctx.getString(R.string.recipe_item_count, q));
                }
                menu.setOnMenuItemClickListener(m -> {
                    listener.onPickQuantity(row, m.getItemId());
                    return true;
                });
                menu.show();
            });

            b.cart.setImageResource(row.selected ? R.drawable.ic_delete : R.drawable.ic_add_shopping_cart);
            b.cart.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx,
                    row.selected ? R.color.text_danger : R.color.green)));
            b.cart.setOnClickListener(v -> listener.onCart(row));
        }

        b.selected.setVisibility(row.selected ? View.VISIBLE : View.GONE);
        if (row.selected) {
            String name = row.selectedVariant == null ? "" : unitOf(row.selectedVariant);
            b.ordered.setText(ctx.getString(R.string.recipe_ordered, Currency.formatUnit(row.quantity,
                    "x " + name + " @ " + Currency.formatRupiah(row.price(), 0))));
            b.subTotal.setText(ctx.getString(R.string.recipe_sub_total,
                    Currency.formatRupiah(row.price() * row.quantity, 0)));
        }
    }

    /** "Diperlukan 2 kg" plus the item's note, when it has one. */
    static String required(Context ctx, id.laukpauk.data.model.recipe.BuyerRecipe.Item item) {
        String unit = item.productVariantStockUnit == null ? "" : item.productVariantStockUnit;
        String text = ctx.getString(R.string.recipe_required, Currency.formatUnit(item.quantity, unit).trim());
        return item.note == null || item.note.trim().isEmpty() ? text : text + " (" + item.note.trim() + ")";
    }

    private static String unitOf(BuyerProductVariant v) {
        return v.stockUnit == null ? "" : v.stockUnit;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRecipeIngredientBinding b;

        Holder(ItemRecipeIngredientBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}
