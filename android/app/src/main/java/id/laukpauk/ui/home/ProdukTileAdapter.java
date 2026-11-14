package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.LowestPrice;
import id.laukpauk.databinding.ItemHomeProdukBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** The horizontal product tiles of the home page (legacy ProdukDiDalamPasarItem / ProdukInThisPasarItem). */
public class ProdukTileAdapter extends ListAdapter<HomeProduk, ProdukTileAdapter.Holder> {

    private final PriceSource prices;
    private final ProdukListener listener;

    public ProdukTileAdapter(PriceSource prices, ProdukListener listener) {
        super(HomeProdukDiff.INSTANCE);
        this.prices = prices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeProdukBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final HomeProduk p = getItem(position);
        final ItemHomeProdukBinding b = h.binding;
        h.boundId = p.id;

        if (DefaultImage.defaultProduk.equals(p.foto)) {
            b.foto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            b.foto.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        Images.url(b.foto, Images.server(ServerPath.uploadGambarProduk, "compress/" + p.foto));
        b.promo.setVisibility(p.isInActivePromo() ? View.VISIBLE : View.GONE);
        b.hemat.setVisibility(View.GONE);
        b.progress.setVisibility(View.VISIBLE);
        b.content.setVisibility(View.GONE);

        b.nama.setText(p.nama);
        boolean adaDesk = p.keteranganSingkat != null && !p.keteranganSingkat.isEmpty();
        b.deskripsi.setVisibility(adaDesk ? View.VISIBLE : View.GONE);
        b.deskripsi.setText(p.keteranganSingkat);
        b.root.setOnClickListener(v -> listener.onProduk(p));
        b.beli.setOnClickListener(v -> listener.onBeli(p));

        prices.load(p.id, result -> {
            if (!p.id.equals(h.boundId) || result.isLoading()) {
                return;
            }
            showPrice(b, p, result.isSuccess() ? result.data : null);
        });
    }

    private static void showPrice(ItemHomeProdukBinding b, HomeProduk p, LowestPrice price) {
        b.progress.setVisibility(View.GONE);
        b.content.setVisibility(View.VISIBLE);
        double persen = price == null ? 0 : price.persentaseDiskon();
        if (persen > 0) {
            b.hemat.setText(b.getRoot().getContext().getString(R.string.home_hemat,
                    Currency.formatSatuan((long) Math.ceil(persen), "%")));
            b.hemat.setVisibility(View.VISIBLE);
        }
        b.harga.setVisibility(price == null ? View.GONE : View.VISIBLE);
        b.hargaCoret.setVisibility(persen > 0 ? View.VISIBLE : View.GONE);
        b.beli.setVisibility(View.GONE);
        b.info.setVisibility(View.GONE);
        if (price != null) {
            b.harga.setText(Currency.formatRupiah(price.hargaBersih, 0));
            b.hargaCoret.setText(Currency.formatRupiah(price.harga, 0));
            b.hargaCoret.setPaintFlags(b.hargaCoret.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (!p.isStokAvailable()) {
            b.info.setText(R.string.home_stok_habis);
            b.info.setTextColor(b.getRoot().getContext().getColor(R.color.text_danger));
            b.info.setVisibility(View.VISIBLE);
        } else if (price != null) {
            b.beli.setVisibility(View.VISIBLE);
        } else {
            b.info.setText(R.string.home_tanpa_harga);
            b.info.setTextColor(b.getRoot().getContext().getColor(R.color.nearly_black));
            b.info.setVisibility(View.VISIBLE);
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeProdukBinding binding;
        String boundId;

        Holder(ItemHomeProdukBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
