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
import id.laukpauk.databinding.ItemHomeProductListBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** Product cards of the ListProduk grid (legacy ListProdukItem). */
public class ProductListAdapter extends ListAdapter<HomeProduct, ProductListAdapter.Holder> {

    private final PriceSource prices;
    private final ProductListener listener;

    public ProductListAdapter(PriceSource prices, ProductListener listener) {
        super(HomeProductDiff.INSTANCE);
        this.prices = prices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHomeProductListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final HomeProduct p = getItem(position);
        final ItemHomeProductListBinding b = h.binding;
        h.boundId = p.id;
        b.photo.setScaleType(DefaultImage.defaultProduct.equals(p.photo)
                ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(b.photo, Images.server(ServerPath.uploadImageProduct, "compress/" + p.photo));
        b.name.setText(p.name);
        b.root.setOnClickListener(v -> listener.onProduct(p));
        b.buy.setOnClickListener(v -> listener.onBuy(p));
        b.progress.setVisibility(View.VISIBLE);
        b.withoutPrice.setVisibility(View.GONE);
        b.priceBox.setVisibility(View.GONE);
        prices.load(p.id, result -> {
            if (!p.id.equals(h.boundId) || result.isLoading()) {
                return;
            }
            LowestPrice price = result.isSuccess() ? result.data : null;
            b.progress.setVisibility(View.GONE);
            if (price == null) {
                b.withoutPrice.setVisibility(View.VISIBLE);
                return;
            }
            b.priceBox.setVisibility(View.VISIBLE);
            double percent = price.price >= 1 && price.discount >= 1 ? price.discountPercentage() : 0;
            b.discountRow.setVisibility(percent > 0 ? View.VISIBLE : View.GONE);
            b.strikePrice.setText(Currency.formatRupiah(price.price, 0));
            b.chip.setText(b.getRoot().getContext().getString(R.string.home_discount_chip,
                    Currency.formatUnit((long) percent, "%")));
            b.price.setText(Currency.formatRupiah(price.netPrice, 0));
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHomeProductListBinding binding;
        String boundId;

        Holder(ItemHomeProductListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
