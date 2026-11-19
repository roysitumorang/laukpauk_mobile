package id.laukpauk.ui.cart;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.cart.BuyerCartItem;
import id.laukpauk.databinding.ItemCartProductBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;

/** The cart lines of {@code GET buyer/cart}: photo, name, line total, "2 x ikat", unit price, edit, delete. */
public class CartProductAdapter extends ListAdapter<BuyerCartItem, CartProductAdapter.Holder> {

    public interface Listener {
        void onOpenProduct(BuyerCartItem item);

        void onEditQuantity(BuyerCartItem item);

        void onDelete(BuyerCartItem item);
    }

    private final Listener listener;

    public CartProductAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCartProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        bind(holder.binding.getRoot().getContext(), holder.binding, getItem(position));
    }

    private void bind(Context ctx, ItemCartProductBinding b, BuyerCartItem item) {
        String unit = item.stockUnit == null ? "" : item.stockUnit;
        Images.url(b.photo, Images.product(item.productPhoto));
        b.photo.setOnClickListener(v -> listener.onOpenProduct(item));
        b.name.setText(item.productName == null ? "" : item.productName);
        b.name.setOnClickListener(v -> listener.onOpenProduct(item));
        b.total.setText(Currency.formatRupiah(item.lineTotal(), 0));
        b.quantity.setText(ctx.getString(R.string.cart_qty_unit, String.valueOf(item.quantity), unit));
        b.unitPrice.setText(ctx.getString(R.string.cart_unit_price, unit, Currency.formatRupiah(item.unitPrice(), 0)));
        boolean discounted = item.discounted();
        b.strikePrice.setVisibility(discounted ? View.VISIBLE : View.GONE);
        b.strikePrice.setText(Currency.formatRupiah(item.sellingPrice, 0));
        b.strikePrice.setPaintFlags(b.strikePrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        b.outOfStock.setVisibility(item.productReadyStock ? View.GONE : View.VISIBLE);
        b.editQuantity.setOnClickListener(v -> listener.onEditQuantity(item));
        b.quantity.setOnClickListener(v -> listener.onEditQuantity(item));
        b.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemCartProductBinding binding;

        Holder(ItemCartProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<BuyerCartItem> DIFF = new DiffUtil.ItemCallback<BuyerCartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull BuyerCartItem a, @NonNull BuyerCartItem b) {
            return Objects.equals(a.productVariantId, b.productVariantId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull BuyerCartItem a, @NonNull BuyerCartItem b) {
            return a.quantity == b.quantity && a.lineTotal() == b.lineTotal()
                    && a.productReadyStock == b.productReadyStock
                    && Objects.equals(a.productName, b.productName)
                    && Objects.equals(a.productPhoto, b.productPhoto)
                    && Objects.equals(a.stockUnit, b.stockUnit);
        }
    };
}
