package id.laukpauk.ui.search;

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
import id.laukpauk.databinding.ItemSearchKontenBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.home.AutoPager;
import id.laukpauk.ui.home.KontenSection;
import id.laukpauk.ui.home.SlideAdapter;
import id.laukpauk.util.ServerPath;

/** "headersearch" konten blocks: product rows or auto-playing banners. */
public class SearchKontenAdapter extends ListAdapter<KontenSection, SearchKontenAdapter.Holder> {

    public interface Listener {
        void onProduk(HomeProduk produk);

        void onBanner(Konten konten, KontenItem item);
    }

    private final Listener listener;

    public SearchKontenAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<KontenSection>() {
            @Override
            public boolean areItemsTheSame(@NonNull KontenSection a, @NonNull KontenSection b) {
                return Objects.equals(a.konten.id, b.konten.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull KontenSection a, @NonNull KontenSection b) {
                return a.items == b.items;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSearchKontenBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        KontenSection s = getItem(position);
        ItemSearchKontenBinding b = h.binding;
        h.konten = s.konten;
        b.judul.setText(s.konten.namaDivisi);
        boolean has = s.items != null && !s.items.isEmpty();
        b.produkList.setVisibility(has && s.konten.isProduk() ? View.VISIBLE : View.GONE);
        b.bannerBox.setVisibility(has && !s.konten.isProduk() ? View.VISIBLE : View.GONE);
        if (!has) {
            return;
        }
        if (s.konten.isProduk()) {
            h.rows.submitList(new ArrayList<HomeProduk>(s.items));
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
        final ItemSearchKontenBinding binding;
        final SearchProdukRowAdapter rows;
        final SlideAdapter slides;
        Konten konten;

        Holder(ItemSearchKontenBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            rows = new SearchProdukRowAdapter(listener::onProduk);
            binding.produkList.setAdapter(rows);
            slides = new SlideAdapter(slide -> listener.onBanner(konten, (KontenItem) slide.tag));
            binding.bannerPager.setAdapter(slides);
            AutoPager.attach(binding.bannerPager);
        }
    }
}
