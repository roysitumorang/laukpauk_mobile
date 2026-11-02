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
import id.laukpauk.databinding.ItemHomeListProdukBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** Product cards of the ListProduk grid (legacy ListProdukItem). */
public class ListProdukAdapter extends ListAdapter<HomeProduk, ListProdukAdapter.Holder> {

    private final PriceSource prices;
    private final ProdukListener listener;

    public ListProdukAdapter(PriceSource prices, ProdukListener listener) {
        super(HomeProdukDiff.INSTANCE);
        this.prices = prices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeListProdukBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final HomeProduk p = getItem(position);
        final ItemHomeListProdukBinding b = h.binding;
        h.boundId = p.id;
        b.foto.setScaleType(DefaultImage.defaultProduk.equals(p.foto)
                ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(b.foto, Images.server(ServerPath.uploadGambarProduk, "compress/" + p.foto));
        b.nama.setText(p.nama);
        boolean adaToko = p.namaToko != null && !p.namaToko.isEmpty();
        b.toko.setText(adaToko ? p.namaToko : b.getRoot().getContext().getString(R.string.home_penjual_tidak_diketahui));
        b.root.setOnClickListener(v -> listener.onProduk(p));
        b.toko.setOnClickListener(v -> listener.onToko(p));
        b.beli.setOnClickListener(v -> listener.onBeli(p));
        b.progress.setVisibility(View.VISIBLE);
        b.tanpaHarga.setVisibility(View.GONE);
        b.priceBox.setVisibility(View.GONE);
        prices.load(p.id, result -> {
            if (!p.id.equals(h.boundId) || result.isLoading()) {
                return;
            }
            LowestPrice price = result.isSuccess() ? result.data : null;
            b.progress.setVisibility(View.GONE);
            if (price == null) {
                b.tanpaHarga.setVisibility(View.VISIBLE);
                return;
            }
            b.priceBox.setVisibility(View.VISIBLE);
            double persen = price.harga >= 1 && price.diskon >= 1 ? price.persentaseDiskon() : 0;
            b.diskonRow.setVisibility(persen > 0 ? View.VISIBLE : View.GONE);
            b.hargaCoret.setText(Currency.formatRupiah(price.harga, 0));
            b.chip.setText(b.getRoot().getContext().getString(R.string.home_diskon_chip,
                    Currency.formatSatuan((long) persen, "%")));
            b.harga.setText(Currency.formatRupiah(price.hargaBersih, 0));
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeListProdukBinding binding;
        String boundId;

        Holder(ItemHomeListProdukBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
