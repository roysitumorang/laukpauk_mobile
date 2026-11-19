package id.laukpauk.ui.product;

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
import id.laukpauk.data.model.product.Product;
import id.laukpauk.databinding.ItemProductGridBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;

/**
 * Product tile of the 3-column grid: photo with "Hemat x%" / "Promo" badges,
 * name, short description, lowest price and a "Beli" button. Prices arrive later through
 * {@link #setPrices(Map)} and are requested with {@link Listener#onNeedPrice}.
 */
public class ProductGridAdapter extends ListAdapter<Product, ProductGridAdapter.Holder> {

    public interface Listener {
        void onOpen(Product product);

        void onBuy(Product product);

        void onNeedPrice(Product product);
    }

    private final float aspect;
    private final Listener listener;
    private Map<String, LowestPrice> prices = Collections.emptyMap();

    /** @param aspect width / height of a grid cell (legacy childAspectRatio: 0.45). */
    public ProductGridAdapter(float aspect, Listener listener) {
        super(new DiffUtil.ItemCallback<Product>() {
            @Override
            public boolean areItemsTheSame(@NonNull Product a, @NonNull Product b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Product a, @NonNull Product b) {
                return TextUtils.equals(a.name, b.name) && TextUtils.equals(a.photo, b.photo)
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
        ItemProductGridBinding b = ItemProductGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemProductGridBinding b;

        Holder(ItemProductGridBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Product p) {
            boolean isDefault = DefaultImage.defaultProduct.equals(p.photo);
            b.image.setScaleType(isDefault ? android.widget.ImageView.ScaleType.FIT_CENTER
                    : android.widget.ImageView.ScaleType.FIT_XY);
            Images.url(b.image, Images.product(p.photo));
            b.promo.setVisibility(p.isInActivePromo() ? View.VISIBLE : View.GONE);
            b.card.setOnClickListener(v -> listener.onOpen(p));

            LowestPrice price = prices.get(p.id);
            if (price == null) {
                b.saving.setVisibility(View.GONE);
                b.textScroll.setVisibility(View.GONE);
                b.loading.setVisibility(View.VISIBLE);
                listener.onNeedPrice(p);
                return;
            }
            b.loading.setVisibility(View.GONE);
            b.textScroll.setVisibility(View.VISIBLE);

            double percent = price == LowestPrice.NONE ? 0 : price.discountPercentage();
            if (percent > 0) {
                b.saving.setText(b.getRoot().getContext().getString(R.string.product_list_saving,
                        Currency.formatUnit((long) Math.ceil(percent), "%")));
                b.saving.setVisibility(View.VISIBLE);
            } else {
                b.saving.setVisibility(View.GONE);
            }

            b.name.setText(p.name);
            boolean hasDescription = p.shortDescription != null && !p.shortDescription.isEmpty();
            b.description.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
            b.description.setText(p.shortDescription);

            boolean hasPrice = price != LowestPrice.NONE;
            if (hasPrice) {
                b.netPrice.setText(Currency.formatRupiah(price.netPrice, 0));
                b.strikePrice.setVisibility(percent > 0 ? View.VISIBLE : View.GONE);
                b.strikePrice.setText(Currency.formatRupiah(price.price, 0));
                b.strikePrice.setPaintFlags(b.strikePrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
            boolean stock = p.isStockAvailable();
            b.priceRow.setVisibility(hasPrice ? View.VISIBLE : View.GONE);
            b.buy.setVisibility(stock && hasPrice ? View.VISIBLE : View.GONE);
            b.noPrice.setVisibility(stock && !hasPrice ? View.VISIBLE : View.GONE);
            b.outOfStock.setVisibility(stock ? View.GONE : View.VISIBLE);
            b.buy.setOnClickListener(v -> listener.onBuy(p));
        }
    }
}
