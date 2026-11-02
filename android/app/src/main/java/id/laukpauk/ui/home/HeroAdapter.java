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
        if (b.gambar == null || b.gambar.length() <= 1) {
            v.gambar.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Images.asset(v.gambar, "no-image-img.png");
        } else {
            v.gambar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Images.url(v.gambar, Images.server(ServerPath.uploadGambarBanner, b.gambar));
        }
        v.nama.setText(b.nama);
        boolean ada = b.keterangan != null && b.keterangan.length() >= 1;
        v.keterangan.setVisibility(ada ? View.VISIBLE : View.GONE);
        v.keterangan.setText(b.keterangan);
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
