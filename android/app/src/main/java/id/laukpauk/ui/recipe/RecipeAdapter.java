package id.laukpauk.ui.recipe;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.databinding.ItemRecipeCardBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** The recipe cards (legacy ResepCard). */
public class RecipeAdapter extends ListAdapter<BuyerRecipe, RecipeAdapter.Holder> {

    public interface Listener {
        void onOpen(BuyerRecipe recipe);

        void onLink(String link);
    }

    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<BuyerRecipe>() {
            @Override
            public boolean areItemsTheSame(@NonNull BuyerRecipe a, @NonNull BuyerRecipe b) {
                return String.valueOf(a.id).equals(String.valueOf(b.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull BuyerRecipe a, @NonNull BuyerRecipe b) {
                return String.valueOf(a.name).equals(String.valueOf(b.name))
                        && String.valueOf(a.photo).equals(String.valueOf(b.photo))
                        && String.valueOf(a.description).equals(String.valueOf(b.description))
                        && String.valueOf(a.facebookLink).equals(String.valueOf(b.facebookLink))
                        && String.valueOf(a.instagramLink).equals(String.valueOf(b.instagramLink));
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        BuyerRecipe r = getItem(position);
        String remarks = r.description == null ? "" : r.description;
        if (remarks.length() >= 31) {
            remarks = h.b.getRoot().getContext().getString(R.string.recipe_read_more, remarks.substring(0, 30));
        }
        Images.url(h.b.photo, Images.recipe(r.photo));
        h.b.name.setText(r.name == null ? "" : r.name.toUpperCase(Locale.ROOT));
        h.b.remarks.setText(remarks);
        h.b.card.setOnClickListener(v -> listener.onOpen(r));
        h.b.fb.setOnClickListener(v -> listener.onLink(r.facebookLink));
        h.b.ig.setOnClickListener(v -> listener.onLink(r.instagramLink));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRecipeCardBinding b;

        Holder(ItemRecipeCardBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}
