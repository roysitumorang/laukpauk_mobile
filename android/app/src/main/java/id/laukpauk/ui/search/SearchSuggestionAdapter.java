package id.laukpauk.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.databinding.ItemSearchSuggestionBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Rows of the suggestion list: photo, PRODUK tag and title (legacy SearchResult). */
public class SearchSuggestionAdapter extends ListAdapter<SearchSuggestion, SearchSuggestionAdapter.Holder> {

    public interface OnClick {
        void onClick(SearchSuggestion item);
    }

    private final OnClick onClick;

    public SearchSuggestionAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<SearchSuggestion>() {
            @Override
            public boolean areItemsTheSame(@NonNull SearchSuggestion a, @NonNull SearchSuggestion b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull SearchSuggestion a, @NonNull SearchSuggestion b) {
                return Objects.equals(a.namaProduk, b.namaProduk) && Objects.equals(a.fotoProduk, b.fotoProduk);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSearchSuggestionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        SearchSuggestion s = getItem(position);
        ItemSearchSuggestionBinding b = h.binding;
        String leading = s.fotoProduk;
        String title = s.namaProduk;
        if (title == null || title.isEmpty()) {
            title = b.getRoot().getContext().getString(R.string.search_judul_produk_kosong);
        }
        if (leading == null || leading.isEmpty()) {
            Images.asset(b.foto, "laukpauk.png");
        } else {
            Images.circle(b.foto, Images.server(ServerPath.uploadGambarProduk, "compress/" + leading));
        }
        b.jenis.setText(s.jenisItem == null ? "" : s.jenisItem.toUpperCase(Locale.ROOT));
        b.jenis.setBackgroundResource(R.drawable.bg_home_tag_produk);
        b.judul.setText(title);
        b.getRoot().setOnClickListener(v -> onClick.onClick(s));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemSearchSuggestionBinding binding;

        Holder(ItemSearchSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
