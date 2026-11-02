package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.home.PenjualItem;
import id.laukpauk.databinding.ItemHomeListPenjualBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** Seller cards of the ListProduk grid (legacy ListPenjualItem). */
public class ListPenjualAdapter extends ListAdapter<PenjualItem, ListPenjualAdapter.Holder> {

    public interface OnClick {
        void onClick(PenjualItem item);
    }

    private final OnClick onClick;

    public ListPenjualAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<PenjualItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull PenjualItem a, @NonNull PenjualItem b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PenjualItem a, @NonNull PenjualItem b) {
                return Objects.equals(a.namaToko, b.namaToko) && Objects.equals(a.alamatToko, b.alamatToko)
                        && Objects.equals(a.bannerToko, b.bannerToko);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeListPenjualBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PenjualItem p = getItem(position);
        ItemHomeListPenjualBinding b = h.binding;
        b.banner.setScaleType(DefaultImage.defautBannerPenjual.equals(p.bannerToko)
                ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(b.banner, Images.server(ServerPath.uploadGambarBannerPenjual, "compress/" + p.bannerToko));
        b.nama.setText(p.namaToko);
        boolean ada = p.namaToko != null && !p.namaToko.isEmpty();
        b.toko.setText(ada ? p.namaToko : b.getRoot().getContext().getString(R.string.home_penjual_tidak_diketahui));
        b.alamat.setText(p.alamatToko);
        b.root.setOnClickListener(v -> onClick.onClick(p));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeListPenjualBinding binding;

        Holder(ItemHomeListPenjualBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
