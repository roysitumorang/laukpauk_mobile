package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.data.model.home.ProductCategory;
import id.laukpauk.databinding.ItemHomeCategoryBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Category tiles of "Semua Kategori Produk" (legacy SemuaKategoriProdukItem). */
public class CategoryAdapter extends ListAdapter<ProductCategory, CategoryAdapter.Holder> {

    public interface OnClick {
        void onClick(ProductCategory category);
    }

    private final OnClick onClick;

    public CategoryAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<ProductCategory>() {
            @Override
            public boolean areItemsTheSame(@NonNull ProductCategory a, @NonNull ProductCategory b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ProductCategory a, @NonNull ProductCategory b) {
                return Objects.equals(a.name, b.name) && Objects.equals(a.image, b.image);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ProductCategory k = getItem(position);
        Images.url(h.binding.image, Images.server(ServerPath.uploadImageProductCategory, "compress/" + k.image));
        h.binding.name.setText(k.name);
        h.binding.root.setOnClickListener(v -> onClick.onClick(k));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeCategoryBinding binding;

        Holder(ItemHomeCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
