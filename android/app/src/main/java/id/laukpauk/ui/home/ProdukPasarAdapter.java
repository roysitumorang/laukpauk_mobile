package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.home.Pasar;
import id.laukpauk.databinding.ItemHomeProdukPasarBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** "Produk Pasar X" rows (legacy ProdukPasar): the market header and a horizontal list of its products. */
public class ProdukPasarAdapter extends ListAdapter<ProdukPasarSection, ProdukPasarAdapter.Holder> {

    public interface OnLihat {
        void onLihat(Pasar pasar);
    }

    private final PriceSource prices;
    private final ProdukListener listener;
    private final OnLihat onLihat;

    public ProdukPasarAdapter(PriceSource prices, ProdukListener listener, OnLihat onLihat) {
        super(new DiffUtil.ItemCallback<ProdukPasarSection>() {
            @Override
            public boolean areItemsTheSame(@NonNull ProdukPasarSection a, @NonNull ProdukPasarSection b) {
                return Objects.equals(a.pasar.id, b.pasar.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ProdukPasarSection a, @NonNull ProdukPasarSection b) {
                return a.produk == b.produk && Objects.equals(a.pasar.distance, b.pasar.distance);
            }
        });
        this.prices = prices;
        this.listener = listener;
        this.onLihat = onLihat;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeProdukPasarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                new ProdukTileAdapter(prices, listener));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ProdukPasarSection s = getItem(position);
        ItemHomeProdukPasarBinding b = h.binding;
        Images.circle(b.foto, Images.server(ServerPath.uploadGambarPasar, "compress/" + s.pasar.gambar));
        b.nama.setText(b.getRoot().getContext().getString(R.string.home_produk_pasar, s.pasar.nama));
        Double km = s.pasar.distanceKm();
        b.jarak.setVisibility(km == null ? View.GONE : View.VISIBLE);
        if (km != null) {
            b.jarak.setText(b.getRoot().getContext().getString(R.string.home_pasar_jarak, PasarAdapter.fixed1(km)));
        }
        b.lihat.setOnClickListener(v -> onLihat.onLihat(s.pasar));
        b.progress.setVisibility(s.produk == null ? View.VISIBLE : View.GONE);
        h.tiles.submitList(s.produk);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeProdukPasarBinding binding;
        final ProdukTileAdapter tiles;

        Holder(ItemHomeProdukPasarBinding binding, ProdukTileAdapter tiles) {
            super(binding.getRoot());
            this.binding = binding;
            this.tiles = tiles;
            binding.list.setAdapter(tiles);
        }
    }
}
