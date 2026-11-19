package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.model.home.Banner;
import id.laukpauk.databinding.ItemHomeHeroBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Pages of the home hero slider (legacy HeroSlider). */
public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.Holder> {

    private final List<Banner> banners = new ArrayList<>();

    public void submit(List<Banner> list) {
        banners.clear();
        banners.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeHeroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Banner b = banners.get(position);
        ItemHomeHeroBinding v = h.binding;
        String photo = photoUrl(b.photo);
        if (photo == null) {
            v.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Images.asset(v.image, "no-image-img.png");
        } else {
            v.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Images.url(v.image, photo);
        }
        show(v.name, b.name);
        show(v.description, b.description);
    }

    /**
     * The banner's {@code photo} is a full URL. The app does not allow cleartext traffic, so an
     * {@code http://} URL is loaded over https; a bare path is resolved against BASE_URL.
     */
    @Nullable
    static String photoUrl(@Nullable String photo) {
        if (photo == null || photo.trim().isEmpty()) {
            return null;
        }
        String p = photo.trim();
        if (p.regionMatches(true, 0, "http://", 0, 7)) {
            return "https://" + p.substring(7);
        }
        if (p.regionMatches(true, 0, "https://", 0, 8)) {
            return p;
        }
        return ServerPath.baseURL + (p.startsWith("/") ? "" : "/") + p;
    }

    private static void show(TextView view, @Nullable String text) {
        boolean has = text != null && !text.trim().isEmpty();
        view.setVisibility(has ? View.VISIBLE : View.GONE);
        view.setText(has ? text : null);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeHeroBinding binding;

        Holder(ItemHomeHeroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
