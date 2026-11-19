package id.laukpauk.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.LowestPrice;
import id.laukpauk.databinding.ItemHomeProductBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;

/** The horizontal product tiles of the home page. */
public class ProductTileAdapter extends ListAdapter<HomeProduct, ProductTileAdapter.Holder> {

    private final PriceSource prices;
    private final ProductListener listener;

    public ProductTileAdapter(PriceSource prices, ProductListener listener) {
        super(HomeProductDiff.INSTANCE);
        this.prices = prices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final HomeProduct p = getItem(position);
        final ItemHomeProductBinding b = h.binding;
        h.boundId = p.id;

        if (DefaultImage.defaultProduct.equals(p.photo)) {
            b.photo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            b.photo.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        Images.url(b.photo, Images.product(p.photo));
        b.promo.setVisibility(p.isInActivePromo() ? View.VISIBLE : View.GONE);
        b.saving.setVisibility(View.GONE);
        b.progress.setVisibility(View.VISIBLE);
        b.content.setVisibility(View.GONE);

        b.name.setText(p.name);
        boolean hasDesc = p.shortDescription != null && !p.shortDescription.isEmpty();
        b.description.setVisibility(hasDesc ? View.VISIBLE : View.GONE);
        b.description.setText(p.shortDescription);
        b.root.setOnClickListener(v -> listener.onProduct(p));
        b.buy.setOnClickListener(v -> listener.onBuy(p));

        prices.load(p.id, result -> {
            if (!p.id.equals(h.boundId) || result.isLoading()) {
                return;
            }
            showPrice(b, p, result.isSuccess() ? result.data : null);
        });
    }

    private static void showPrice(ItemHomeProductBinding b, HomeProduct p, LowestPrice price) {
        b.progress.setVisibility(View.GONE);
        b.content.setVisibility(View.VISIBLE);
        double percent = price == null ? 0 : price.discountPercentage();
        if (percent > 0) {
            b.saving.setText(b.getRoot().getContext().getString(R.string.home_saving,
                    Currency.formatUnit((long) Math.ceil(percent), "%")));
            b.saving.setVisibility(View.VISIBLE);
        }
        b.price.setVisibility(price == null ? View.GONE : View.VISIBLE);
        b.strikePrice.setVisibility(percent > 0 ? View.VISIBLE : View.GONE);
        b.buy.setVisibility(View.GONE);
        b.info.setVisibility(View.GONE);
        if (price != null) {
            b.price.setText(b.getRoot().getContext().getString(R.string.product_price_from,
                    Currency.formatRupiah(price.netPrice, 0)));
            b.strikePrice.setText(Currency.formatRupiah(price.price, 0));
            b.strikePrice.setPaintFlags(b.strikePrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (!p.isStockAvailable()) {
            b.info.setText(R.string.home_out_of_stock);
            b.info.setTextColor(b.getRoot().getContext().getColor(R.color.text_danger));
            b.info.setVisibility(View.VISIBLE);
        } else if (price != null) {
            b.buy.setVisibility(View.VISIBLE);
        } else {
            b.info.setText(R.string.home_without_price);
            b.info.setTextColor(b.getRoot().getContext().getColor(R.color.nearly_black));
            b.info.setVisibility(View.VISIBLE);
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeProductBinding binding;
        String boundId;

        Holder(ItemHomeProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
