package id.laukpauk.ui.market;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Map;

import id.laukpauk.R;
import id.laukpauk.data.model.product.LowestPrice;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.databinding.ItemMarketProdukBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/**
 * Product tile of the 3-column grids (legacy ProdukDiDalamPasarItem): photo with "Hemat x%" / "Promo" badges,
 * name, shop, short description, lowest price and a "Beli" button. Prices arrive later through
 * {@link #setPrices(Map)} and are requested with {@link Listener#onNeedPrice}.
 */
public class ProdukPasarAdapter extends ListAdapter<Produk, ProdukPasarAdapter.Holder> {

    public interface Listener {
        void onOpen(Produk produk);

        void onBeli(Produk produk);

        void onNeedPrice(Produk produk);
    }

    private final float aspect;
    private final Listener listener;
    private Map<String, LowestPrice> prices = Collections.emptyMap();

    /** @param aspect width / height of a grid cell (legacy childAspectRatio: 0.45 market). */
    public ProdukPasarAdapter(float aspect, Listener listener) {
        super(new DiffUtil.ItemCallback<Produk>() {
            @Override
            public boolean areItemsTheSame(@NonNull Produk a, @NonNull Produk b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Produk a, @NonNull Produk b) {
                return TextUtils.equals(a.nama, b.nama) && TextUtils.equals(a.foto, b.foto)
                        && a.isStockAvailable() == b.isStockAvailable()
                        && a.isInActivePromo() == b.isInActivePromo();
            }
        });
        this.aspect = aspect;
        this.listener = listener;
    }

    public void setPrices(Map<String, LowestPrice> prices) {
        this.prices = prices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMarketProdukBinding b = ItemMarketProdukBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        float density = parent.getResources().getDisplayMetrics().density;
        int cellWidth = (int) ((parent.getResources().getDisplayMetrics().widthPixels - 20 * density) / 3);
        b.getRoot().setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (cellWidth / aspect)));
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemMarketProdukBinding b;

        Holder(ItemMarketProdukBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Produk p) {
            boolean isDefault = DefaultImage.defaultProduk.equals(p.foto);
            b.image.setScaleType(isDefault ? android.widget.ImageView.ScaleType.FIT_CENTER
                    : android.widget.ImageView.ScaleType.FIT_XY);
            Images.url(b.image, Images.server(ServerPath.uploadGambarProduk + "/compress", p.foto == null ? "" : p.foto));
            b.promo.setVisibility(p.isInActivePromo() ? View.VISIBLE : View.GONE);
            b.card.setOnClickListener(v -> listener.onOpen(p));

            LowestPrice price = prices.get(p.id);
            if (price == null) {
                b.hemat.setVisibility(View.GONE);
                b.textScroll.setVisibility(View.GONE);
                b.loading.setVisibility(View.VISIBLE);
                listener.onNeedPrice(p);
                return;
            }
            b.loading.setVisibility(View.GONE);
            b.textScroll.setVisibility(View.VISIBLE);

            double persen = price == LowestPrice.NONE ? 0 : price.persentaseDiskon();
            if (persen > 0) {
                b.hemat.setText(b.getRoot().getContext().getString(R.string.market_hemat,
                        Currency.formatSatuan((long) Math.ceil(persen), "%")));
                b.hemat.setVisibility(View.VISIBLE);
            } else {
                b.hemat.setVisibility(View.GONE);
            }

            b.nama.setText(p.nama);
            boolean adaDeskripsi = p.keteranganSingkat != null && !p.keteranganSingkat.isEmpty();
            b.deskripsi.setVisibility(adaDeskripsi ? View.VISIBLE : View.GONE);
            b.deskripsi.setText(p.keteranganSingkat);

            boolean hasPrice = price != LowestPrice.NONE;
            if (hasPrice) {
                b.hargaBersih.setText(Currency.formatRupiah(price.hargaBersih, 0));
                b.hargaCoret.setVisibility(persen > 0 ? View.VISIBLE : View.GONE);
                b.hargaCoret.setText(Currency.formatRupiah(price.harga, 0));
                b.hargaCoret.setPaintFlags(b.hargaCoret.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
            boolean stock = p.isStockAvailable();
            b.priceRow.setVisibility(hasPrice ? View.VISIBLE : View.GONE);
            b.beli.setVisibility(stock && hasPrice ? View.VISIBLE : View.GONE);
            b.noPrice.setVisibility(stock && !hasPrice ? View.VISIBLE : View.GONE);
            b.stokHabis.setVisibility(stock ? View.GONE : View.VISIBLE);
            b.beli.setOnClickListener(v -> listener.onBeli(p));
        }
    }
}
