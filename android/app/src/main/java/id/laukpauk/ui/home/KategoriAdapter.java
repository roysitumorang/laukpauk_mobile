package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.data.model.home.KategoriProduk;
import id.laukpauk.databinding.ItemHomeKategoriBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Category tiles of "Semua Kategori Produk" (legacy SemuaKategoriProdukItem). */
public class KategoriAdapter extends ListAdapter<KategoriProduk, KategoriAdapter.Holder> {

    public interface OnClick {
        void onClick(KategoriProduk kategori);
    }

    private final OnClick onClick;

    public KategoriAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<KategoriProduk>() {
            @Override
            public boolean areItemsTheSame(@NonNull KategoriProduk a, @NonNull KategoriProduk b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull KategoriProduk a, @NonNull KategoriProduk b) {
                return Objects.equals(a.nama, b.nama) && Objects.equals(a.gambar, b.gambar);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeKategoriBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        KategoriProduk k = getItem(position);
        Images.url(h.binding.gambar, Images.server(ServerPath.uploadGambarKategoriProduk, "compress/" + k.gambar));
        h.binding.nama.setText(k.nama);
        h.binding.root.setOnClickListener(v -> onClick.onClick(k));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeKategoriBinding binding;

        Holder(ItemHomeKategoriBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
