package id.laukpauk.ui.recipe;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.recipe.Resep;
import id.laukpauk.databinding.ItemRecipeCardBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** The recipe cards (legacy ResepCard). */
public class ResepAdapter extends ListAdapter<Resep, ResepAdapter.Holder> {

    public interface Listener {
        void onOpen(Resep resep);

        void onLink(String link);
    }

    private final Listener listener;

    public ResepAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<Resep>() {
            @Override
            public boolean areItemsTheSame(@NonNull Resep a, @NonNull Resep b) {
                return String.valueOf(a.id).equals(String.valueOf(b.id));
            }

            @Override
            public boolean areContentsTheSame(@NonNull Resep a, @NonNull Resep b) {
                return String.valueOf(a.nama).equals(String.valueOf(b.nama))
                        && String.valueOf(a.foto).equals(String.valueOf(b.foto))
                        && String.valueOf(a.keterangan).equals(String.valueOf(b.keterangan))
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
        Resep r = getItem(position);
        String keterangan = r.keterangan == null ? "" : r.keterangan;
        if (keterangan.length() >= 31) {
            keterangan = h.b.getRoot().getContext().getString(R.string.recipe_read_more, keterangan.substring(0, 30));
        }
        Images.url(h.b.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarResep + "/" + r.foto);
        h.b.nama.setText(r.nama == null ? "" : r.nama.toUpperCase(Locale.ROOT));
        h.b.keterangan.setText(keterangan);
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
