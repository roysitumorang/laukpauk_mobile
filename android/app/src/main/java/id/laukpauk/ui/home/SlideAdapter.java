package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.databinding.ItemHomeSlideBinding;
import id.laukpauk.ui.common.Images;

/** Rounded image slides with a white caption pill (konten banners and articles). */
public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.Holder> {

    public static final class Slide {
        public final String imageUrl;
        public final String caption;
        @Nullable
        public final Object tag;

        public Slide(String imageUrl, String caption, @Nullable Object tag) {
            this.imageUrl = imageUrl;
            this.caption = caption;
            this.tag = tag;
        }
    }

    public interface OnClick {
        void onClick(Slide slide);
    }

    private final List<Slide> slides = new ArrayList<>();
    private final OnClick onClick;

    public SlideAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Slide> list) {
        slides.clear();
        slides.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeSlideBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Slide s = slides.get(position);
        Images.url(h.binding.gambar, s.imageUrl);
        h.binding.caption.setText(s.caption);
        h.binding.root.setOnClickListener(v -> onClick.onClick(s));
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeSlideBinding binding;

        Holder(ItemHomeSlideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
