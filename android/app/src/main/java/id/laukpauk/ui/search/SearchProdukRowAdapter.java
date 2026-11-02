package id.laukpauk.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.databinding.ItemSearchProdukRowBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** The dense product rows of a "headersearch" konten block (photo, name, category). */
public class SearchProdukRowAdapter extends ListAdapter<HomeProduk, SearchProdukRowAdapter.Holder> {

    public interface OnClick {
        void onClick(HomeProduk produk);
    }

    private final OnClick onClick;

    public SearchProdukRowAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<HomeProduk>() {
            @Override
            public boolean areItemsTheSame(@NonNull HomeProduk a, @NonNull HomeProduk b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull HomeProduk a, @NonNull HomeProduk b) {
                return Objects.equals(a.nama, b.nama) && Objects.equals(a.foto, b.foto);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSearchProdukRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        HomeProduk p = getItem(position);
        Images.circle(h.binding.foto, Images.server(ServerPath.uploadGambarProduk, "compress/" + p.foto));
        h.binding.nama.setText(p.nama);
        h.binding.kategori.setText(p.namaKategoriProduk);
        h.binding.getRoot().setOnClickListener(v -> onClick.onClick(p));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemSearchProdukRowBinding binding;

        Holder(ItemSearchProdukRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
