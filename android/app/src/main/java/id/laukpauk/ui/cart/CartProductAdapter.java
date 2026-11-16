package id.laukpauk.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.databinding.ItemCartProductBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** The cart lines. */
public class CartProductAdapter extends ListAdapter<CartProduct, CartProductAdapter.Holder> {

    public interface Listener {
        void onOpenProduct(CartProduct product);

        void onEdit(CartProduct product, int failMessage, int failButton);

        void onDelete(CartProduct product);
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
        bindProduct(holder.binding.getRoot().getContext(), holder.binding, getItem(position));
    }

    private void bindProduct(Context ctx, ItemCartProductBinding pb, CartProduct p) {
        int price = CartProduct.toInt(p.price);
        int discount = CartProduct.toInt(p.discount);
        String unitName = p.isPack() ? ctx.getString(R.string.cart_pack_unit) : p.unitName;
        String folder = p.isPack() ? ServerPath.uploadImagePack : ServerPath.uploadImageProduct;
        Images.url(pb.photo, Images.server(folder + "/compress", p.productPhoto));
        pb.photo.setOnClickListener(v -> listener.onOpenProduct(p));
        pb.name.setText(p.productName);
        pb.name.setOnClickListener(v -> listener.onOpenProduct(p));
        pb.total.setText(Currency.formatRupiah(p.total(), 0));
        pb.quantity.setText(ctx.getString(R.string.cart_qty_unit, p.quantity, unitName));
        pb.unitPrice.setText(ctx.getString(R.string.cart_unit_price, unitName,
                Currency.formatRupiah(price - discount, 0)));
        pb.editQuantity.setOnClickListener(v ->
                listener.onEdit(p, R.string.cart_edit_failed_qty, R.string.cart_retry));
        pb.delete.setOnClickListener(v -> listener.onDelete(p));

        boolean hasNote = p.note != null && !p.note.isEmpty();
        pb.addNote.setVisibility(hasNote ? android.view.View.GONE : android.view.View.VISIBLE);
        pb.addNote.setOnClickListener(v ->
                listener.onEdit(p, R.string.cart_edit_failed_add_note, R.string.cart_ok));
        pb.noteBox.setVisibility(hasNote ? android.view.View.VISIBLE : android.view.View.GONE);
        if (hasNote) {
            pb.note.setText(p.note);
            pb.editNote.setOnClickListener(v ->
                    listener.onEdit(p, R.string.cart_edit_failed_edit_note, R.string.cart_ok));
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemCartProductBinding binding;

        Holder(ItemCartProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<CartProduct> DIFF = new DiffUtil.ItemCallback<CartProduct>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartProduct a, @NonNull CartProduct b) {
            return Objects.equals(a.idCart, b.idCart);
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartProduct a, @NonNull CartProduct b) {
            return false; // lines change in place (quantity / note); always rebind
        }
    };
}
