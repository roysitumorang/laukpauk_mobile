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
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;
import id.laukpauk.databinding.ItemHomeContentBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Konten blocks (legacy Konten + ContentItem + KontenItemBanner): products row or auto-playing banners. */
public class ContentAdapter extends ListAdapter<ContentSection, ContentAdapter.Holder> {

    public interface Listener {
        void onViewAll(Content content);

        void onBanner(Content content, ContentItem item);
    }

    private final PriceSource prices;
    private final ProductListener productListener;
    private final Listener listener;

    public ContentAdapter(PriceSource prices, ProductListener productListener, Listener listener) {
        super(new DiffUtil.ItemCallback<ContentSection>() {
            @Override
            public boolean areItemsTheSame(@NonNull ContentSection a, @NonNull ContentSection b) {
                return Objects.equals(a.content.id, b.content.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ContentSection a, @NonNull ContentSection b) {
                return a.items == b.items && Objects.equals(a.content.divisionName, b.content.divisionName);
            }
        });
        this.prices = prices;
        this.productListener = productListener;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final ContentSection s = getItem(position);
        ItemHomeContentBinding b = h.binding;
        h.content = s.content;
        b.heading.setText(s.content.divisionName);
        b.view.setVisibility(s.content.isProduct() ? View.VISIBLE : View.GONE);
        b.view.setOnClickListener(v -> listener.onViewAll(s.content));
        boolean loading = s.items == null;
        boolean empty = !loading && s.items.isEmpty();
        b.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        b.productList.setVisibility(!loading && !empty && s.content.isProduct() ? View.VISIBLE : View.GONE);
        b.bannerBox.setVisibility(!loading && !empty && !s.content.isProduct() ? View.VISIBLE : View.GONE);
        if (loading || empty) {
            return;
        }
        if (s.content.isProduct()) {
            h.tiles.submitList(new ArrayList<HomeProduct>(s.items));
        } else {
            List<SlideAdapter.Slide> slides = new ArrayList<>();
            for (ContentItem item : s.items) {
                String remark = item.remarks == null ? "" : item.remarks;
                if (remark.length() >= 31) {
                    remark = b.getRoot().getContext().getString(R.string.home_content_long_note, remark.substring(0, 30));
                }
                slides.add(new SlideAdapter.Slide(Images.server(ServerPath.uploadImageContentItem, item.item), remark, item));
            }
            h.slides.submit(slides);
        }
    }

    final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeContentBinding binding;
        final ProductTileAdapter tiles;
        final SlideAdapter slides;
        Content content;

        Holder(ItemHomeContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            tiles = new ProductTileAdapter(prices, productListener);
            binding.productList.setAdapter(tiles);
            slides = new SlideAdapter(slide -> listener.onBanner(content, (ContentItem) slide.tag));
            binding.bannerPager.setAdapter(slides);
            AutoPager.attach(binding.bannerPager);
        }
    }
}
