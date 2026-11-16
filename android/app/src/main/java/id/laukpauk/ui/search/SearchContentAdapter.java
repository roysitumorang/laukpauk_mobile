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
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;
import id.laukpauk.databinding.ItemSearchContentBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.home.AutoPager;
import id.laukpauk.ui.home.ContentSection;
import id.laukpauk.ui.home.SlideAdapter;
import id.laukpauk.util.ServerPath;

/** "headersearch" konten blocks: product rows or auto-playing banners. */
public class SearchContentAdapter extends ListAdapter<ContentSection, SearchContentAdapter.Holder> {

    public interface Listener {
        void onProduct(HomeProduct product);

        void onBanner(Content content, ContentItem item);
    }

    private final Listener listener;

    public SearchContentAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ContentSection>() {
            @Override
            public boolean areItemsTheSame(@NonNull ContentSection a, @NonNull ContentSection b) {
                return Objects.equals(a.content.id, b.content.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ContentSection a, @NonNull ContentSection b) {
                return a.items == b.items;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSearchContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ContentSection s = getItem(position);
        ItemSearchContentBinding b = h.binding;
        h.content = s.content;
        b.heading.setText(s.content.divisionName);
        boolean has = s.items != null && !s.items.isEmpty();
        b.productList.setVisibility(has && s.content.isProduct() ? View.VISIBLE : View.GONE);
        b.bannerBox.setVisibility(has && !s.content.isProduct() ? View.VISIBLE : View.GONE);
        if (!has) {
            return;
        }
        if (s.content.isProduct()) {
            h.rows.submitList(new ArrayList<HomeProduct>(s.items));
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
        final ItemSearchContentBinding binding;
        final SearchProductRowAdapter rows;
        final SlideAdapter slides;
        Content content;

        Holder(ItemSearchContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            rows = new SearchProductRowAdapter(listener::onProduct);
            binding.productList.setAdapter(rows);
            slides = new SlideAdapter(slide -> listener.onBanner(content, (ContentItem) slide.tag));
            binding.bannerPager.setAdapter(slides);
            AutoPager.attach(binding.bannerPager);
        }
    }
}
