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
import id.laukpauk.data.model.recipe.ProductUnit;
import id.laukpauk.databinding.ItemRecipeIngredientBinding;
import id.laukpauk.util.Currency;

/** The ingredient rows of a recipe (legacy ResepItemItem). */
class IngredientAdapter extends ListAdapter<IngredientRow, IngredientAdapter.Holder> {

    interface Listener {
        void onPickUnit(IngredientRow row, int unitIndex);

        void onPickQuantity(IngredientRow row, int quantity);

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
        b.portion.setText(ctx.getString(R.string.recipe_required, row.item.portion == null ? "" : row.item.portion));

        b.spinner.setVisibility(!row.unitLoaded ? View.VISIBLE : View.GONE);
        b.noUnit.setVisibility(row.unitLoaded && row.unit.isEmpty() ? View.VISIBLE : View.GONE);
        boolean hasUnit = row.unitLoaded && !row.unit.isEmpty();
        b.controls.setVisibility(hasUnit ? View.VISIBLE : View.GONE);

        if (hasUnit) {
            b.unit.setText(row.selectedUnit == null ? ctx.getString(R.string.recipe_choose_unit)
                    : row.selectedUnit.name);
            b.unit.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(ctx, b.unit);
                menu.getMenu().add(0, 0, 0, R.string.recipe_choose_unit);
                for (int i = 0; i < row.unit.size(); i++) {
                    menu.getMenu().add(0, i + 1, i + 1, row.unit.get(i).name);
                }
                menu.setOnMenuItemClickListener(m -> {
                    if (m.getItemId() > 0) {
                        listener.onPickUnit(row, m.getItemId() - 1);
                    }
                    return true;
                });
                menu.show();
            });

            ProductUnit s = row.selectedUnit;
            boolean notExact = s != null && s.isNotExact();
            b.quantity.setVisibility(notExact ? View.VISIBLE : View.GONE);
            b.quantity.setText(ctx.getString(R.string.recipe_item_count, row.quantity));
            b.quantity.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(ctx, b.quantity);
                for (int q = 1; q <= 10; q++) {
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
            String name = row.selectedUnit == null ? "" : String.valueOf(row.selectedUnit.name);
            b.ordered.setText(ctx.getString(R.string.recipe_ordered, Currency.formatUnit(row.quantity,
                    "x " + name + " @ " + Currency.formatRupiah(row.netPrice(), 0))));
            b.subTotal.setText(ctx.getString(R.string.recipe_sub_total,
                    Currency.formatRupiah((long) row.netPrice() * row.quantity, 0)));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRecipeIngredientBinding b;

        Holder(ItemRecipeIngredientBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}
