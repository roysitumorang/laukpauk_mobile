package id.laukpauk.ui.recipe;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.recipe.Recipe;
import id.laukpauk.databinding.ItemRecipeCardBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** The recipe cards (legacy ResepCard). */
public class RecipeAdapter extends ListAdapter<Recipe, RecipeAdapter.Holder> {

    public interface Listener {
        void onOpen(Recipe recipe);

        void onLink(String link);
    }

    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<Recipe>() {
            @Override
            public boolean areItemsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                return String.valueOf(a.id).equals(String.valueOf(b.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull Recipe a, @NonNull Recipe b) {
                return String.valueOf(a.name).equals(String.valueOf(b.name))
                        && String.valueOf(a.photo).equals(String.valueOf(b.photo))
                        && String.valueOf(a.remarks).equals(String.valueOf(b.remarks))
                        && String.valueOf(a.fbLink).equals(String.valueOf(b.fbLink))
                        && String.valueOf(a.igLink).equals(String.valueOf(b.igLink));
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
        Recipe r = getItem(position);
        String remarks = r.remarks == null ? "" : r.remarks;
        if (remarks.length() >= 31) {
            remarks = h.b.getRoot().getContext().getString(R.string.recipe_read_more, remarks.substring(0, 30));
        }
        Images.url(h.b.photo, ServerPath.baseURL + "/" + ServerPath.uploadImageRecipe + "/" + r.photo);
        h.b.name.setText(r.name == null ? "" : r.name.toUpperCase(Locale.ROOT));
        h.b.remarks.setText(remarks);
        h.b.card.setOnClickListener(v -> listener.onOpen(r));
        h.b.fb.setOnClickListener(v -> listener.onLink(r.fbLink));
        h.b.ig.setOnClickListener(v -> listener.onLink(r.igLink));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRecipeCardBinding b;

        Holder(ItemRecipeCardBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }
    }
}
