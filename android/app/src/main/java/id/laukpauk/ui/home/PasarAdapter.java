package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.home.Pasar;
import id.laukpauk.databinding.ItemHomePasarBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Market tiles (legacy Pasar): photo, dark overlay, name and optionally the distance. */
public class PasarAdapter extends ListAdapter<Pasar, PasarAdapter.Holder> {

    public interface OnClick {
        void onClick(Pasar pasar);
    }

    private final OnClick onClick;
    private boolean showJarak;

    public PasarAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<Pasar>() {
            @Override
            public boolean areItemsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return Objects.equals(a.nama, b.nama) && Objects.equals(a.gambar, b.gambar)
                        && Objects.equals(a.distance, b.distance);
            }
        });
        this.onClick = onClick;
    }

    public void setShowJarak(boolean value) {
        if (showJarak != value) {
            showJarak = value;
            notifyDataSetChanged();
        }
    }

    /** Distance as shown on the tile and in "Produk Pasar" rows: one decimal, e.g. "2.5". */
    static String fixed1(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomePasarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Pasar p = getItem(position);
        ItemHomePasarBinding b = h.binding;
        Images.url(b.gambar, Images.server(ServerPath.uploadGambarPasar, "compress/" + p.gambar));
        b.nama.setText(p.nama);
        Double km = p.distanceKm();
        if (showJarak && km != null) {
            b.jarak.setText(b.getRoot().getContext().getString(R.string.home_pasar_jarak, fixed1(km)));
            b.jarak.setVisibility(View.VISIBLE);
        } else {
            b.jarak.setVisibility(View.GONE);
        }
        b.root.setOnClickListener(v -> onClick.onClick(p));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomePasarBinding binding;

        Holder(ItemHomePasarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
