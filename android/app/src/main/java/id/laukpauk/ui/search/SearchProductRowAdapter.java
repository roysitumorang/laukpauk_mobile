package id.laukpauk.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.databinding.ItemSearchProductRowBinding;
import id.laukpauk.ui.common.Images;

/** The dense product rows of a "headersearch" konten block (photo, name, category). */
public class SearchProductRowAdapter extends ListAdapter<HomeProduct, SearchProductRowAdapter.Holder> {

    public interface OnClick {
        void onClick(HomeProduct product);
    }

    private final OnClick onClick;

    public SearchProductRowAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<HomeProduct>() {
            @Override
            public boolean areItemsTheSame(@NonNull HomeProduct a, @NonNull HomeProduct b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull HomeProduct a, @NonNull HomeProduct b) {
                return Objects.equals(a.name, b.name) && Objects.equals(a.photo, b.photo);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSearchProductRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        HomeProduct p = getItem(position);
        Images.circle(h.binding.photo, Images.product(p.photo));
        h.binding.name.setText(p.name);
        h.binding.category.setText(p.productCategoryName);
        h.binding.getRoot().setOnClickListener(v -> onClick.onClick(p));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemSearchProductRowBinding binding;

        Holder(ItemSearchProductRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
