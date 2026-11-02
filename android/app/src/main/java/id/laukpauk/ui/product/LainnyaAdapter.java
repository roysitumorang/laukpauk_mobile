package id.laukpauk.ui.product;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.product.Lainnya;
import id.laukpauk.databinding.ItemProductLainnyaBinding;
import id.laukpauk.ui.common.Images;

/** "Produk Lainnya" / "Paket Lainnya" horizontal tiles (legacy LainnyaList). */
public class LainnyaAdapter extends ListAdapter<Lainnya, LainnyaAdapter.Holder> {

    public interface Listener {
        void onOpen(Lainnya item);
    }

    private final String uploadFolder;
    private final boolean withBorder;
    private final float nameSizeSp;
    private final int gapDp;
    private final Listener listener;

    public LainnyaAdapter(String uploadFolder, boolean withBorder, float nameSizeSp, int gapDp, Listener listener) {
        super(new DiffUtil.ItemCallback<Lainnya>() {
            @Override
            public boolean areItemsTheSame(@NonNull Lainnya a, @NonNull Lainnya b) {
                return TextUtils.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Lainnya a, @NonNull Lainnya b) {
                return TextUtils.equals(a.nama, b.nama) && TextUtils.equals(a.foto, b.foto);
            }
        });
        this.uploadFolder = uploadFolder;
        this.withBorder = withBorder;
        this.nameSizeSp = nameSizeSp;
        this.gapDp = gapDp;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemProductLainnyaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemProductLainnyaBinding b;

        Holder(ItemProductLainnyaBinding b) {
            super(b.getRoot());
            this.b = b;
            float d = b.getRoot().getResources().getDisplayMetrics().density;
            b.nama.setTextSize(nameSizeSp);
            ((ViewGroup.MarginLayoutParams) b.nama.getLayoutParams()).topMargin = (int) (gapDp * d);
            if (withBorder) {
                b.card.setStrokeColor(ContextCompat.getColor(b.getRoot().getContext(), R.color.green));
                b.card.setStrokeWidth((int) Math.max(1, 0.5f * d));
            }
        }

        void bind(Lainnya item) {
            Images.url(b.image, Images.server(uploadFolder + "/compress", item.foto == null ? "" : item.foto));
            b.nama.setText(item.nama);
            b.card.setOnClickListener(v -> listener.onOpen(item));
        }
    }
}
