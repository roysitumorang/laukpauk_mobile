package id.laukpauk.ui.market;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.market.Pasar;
import id.laukpauk.databinding.ItemMarketPasarBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** A market tile of the "Pasar Lain" lists (legacy PasarLainItem). */
public class PasarLainAdapter extends ListAdapter<Pasar, PasarLainAdapter.Holder> {

    public interface Listener {
        void onOpen(Pasar pasar);
    }

    private final boolean showDistance;
    private final Listener listener;

    public PasarLainAdapter(boolean showDistance, Listener listener) {
        super(new DiffUtil.ItemCallback<Pasar>() {
            @Override
            public boolean areItemsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return TextUtils.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Pasar a, @NonNull Pasar b) {
                return TextUtils.equals(a.nama, b.nama) && a.distance == b.distance;
            }
        });
        this.showDistance = showDistance;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemMarketPasarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemMarketPasarBinding b;

        Holder(ItemMarketPasarBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Pasar p) {
            if ("0".equals(p.pasarUmum)) {
                Images.asset(b.image, "laukpauk.png");
            } else {
                Images.url(b.image, Images.server(ServerPath.uploadGambarPasar + "/compress", p.gambar == null ? "" : p.gambar));
            }
            b.nama.setText(p.nama == null ? "" : p.nama.toUpperCase());
            b.distance.setVisibility(showDistance ? View.VISIBLE : View.GONE);
            if (showDistance) {
                b.distance.setText(b.getRoot().getContext().getString(R.string.market_distance_km,
                        String.format(Locale.US, "%.1f", p.distance)));
            }
            b.getRoot().setOnClickListener(v -> listener.onOpen(p));
        }
    }
}
