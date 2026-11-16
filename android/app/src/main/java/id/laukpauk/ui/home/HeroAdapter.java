package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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
        if (b.image == null || b.image.length() <= 1) {
            v.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Images.asset(v.image, "no-image-img.png");
        } else {
            v.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Images.url(v.image, Images.server(ServerPath.uploadImageBanner, b.image));
        }
        v.name.setText(b.name);
        boolean has = b.remarks != null && b.remarks.length() >= 1;
        v.remarks.setVisibility(has ? View.VISIBLE : View.GONE);
        v.remarks.setText(b.remarks);
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
