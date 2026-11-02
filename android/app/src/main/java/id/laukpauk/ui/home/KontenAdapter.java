package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.Konten;
import id.laukpauk.data.model.home.KontenItem;
import id.laukpauk.databinding.ItemHomeKontenBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Konten blocks (legacy Konten + KontenItem + KontenItemBanner): products row or auto-playing banners. */
public class KontenAdapter extends ListAdapter<KontenSection, KontenAdapter.Holder> {

    public interface Listener {
        void onLihatSemua(Konten konten);

        void onBanner(Konten konten, KontenItem item);
    }

    private final PriceSource prices;
    private final ProdukListener produkListener;
    private final Listener listener;

    public KontenAdapter(PriceSource prices, ProdukListener produkListener, Listener listener) {
        super(new DiffUtil.ItemCallback<KontenSection>() {
            @Override
            public boolean areItemsTheSame(@NonNull KontenSection a, @NonNull KontenSection b) {
                return Objects.equals(a.konten.id, b.konten.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull KontenSection a, @NonNull KontenSection b) {
                return a.items == b.items && Objects.equals(a.konten.namaDivisi, b.konten.namaDivisi);
            }
        });
        this.prices = prices;
        this.produkListener = produkListener;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeKontenBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final KontenSection s = getItem(position);
        ItemHomeKontenBinding b = h.binding;
        h.konten = s.konten;
        b.judul.setText(s.konten.namaDivisi);
        b.lihat.setVisibility(s.konten.isProduk() ? View.VISIBLE : View.GONE);
        b.lihat.setOnClickListener(v -> listener.onLihatSemua(s.konten));
        boolean loading = s.items == null;
        boolean empty = !loading && s.items.isEmpty();
        b.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        b.produkList.setVisibility(!loading && !empty && s.konten.isProduk() ? View.VISIBLE : View.GONE);
        b.bannerBox.setVisibility(!loading && !empty && !s.konten.isProduk() ? View.VISIBLE : View.GONE);
        if (loading || empty) {
            return;
        }
        if (s.konten.isProduk()) {
            h.tiles.submitList(new ArrayList<HomeProduk>(s.items));
        } else {
            List<SlideAdapter.Slide> slides = new ArrayList<>();
            for (KontenItem item : s.items) {
                String ket = item.keterangan == null ? "" : item.keterangan;
                if (ket.length() >= 31) {
                    ket = b.getRoot().getContext().getString(R.string.home_konten_ket_panjang, ket.substring(0, 30));
                }
                slides.add(new SlideAdapter.Slide(Images.server(ServerPath.uploadGambarKontenItem, item.item), ket, item));
            }
            h.slides.submit(slides);
        }
    }

    final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeKontenBinding binding;
        final ProdukTileAdapter tiles;
        final SlideAdapter slides;
        Konten konten;

        Holder(ItemHomeKontenBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            tiles = new ProdukTileAdapter(prices, produkListener);
            binding.produkList.setAdapter(tiles);
            slides = new SlideAdapter(slide -> listener.onBanner(konten, (KontenItem) slide.tag));
            binding.bannerPager.setAdapter(slides);
            AutoPager.attach(binding.bannerPager);
        }
    }
}
