package id.laukpauk.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.data.model.cart.CartSeller;
import id.laukpauk.databinding.ItemCartProductBinding;
import id.laukpauk.databinding.ItemCartSellerBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** PenjualCard + ProductPenjual: one card per seller with that seller's cart lines. */
public class CartSellerAdapter extends ListAdapter<CartSeller, CartSellerAdapter.Holder> {

    public interface Listener {
        void onOpenSeller(CartSeller seller);

        void onOpenMarket(CartSeller seller, String namaPasar);

        void onOpenProduct(CartProduct product);

        void onEdit(CartProduct product, int failMessage, int failButton);

        void onDelete(CartProduct product);
    }

    private final Listener listener;

    public CartSellerAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCartSellerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CartSeller seller = getItem(position);
        ItemCartSellerBinding b = holder.binding;
        Context ctx = b.getRoot().getContext();

        String lokasiPasar = seller.lokasiPasar != null ? seller.lokasiPasar : ctx.getString(R.string.cart_market_unknown);
        String namaPenjual = seller.namaPenjual != null ? seller.namaPenjual : ctx.getString(R.string.cart_seller_unknown);
        b.penjual.setText(namaPenjual);
        b.penjual.setOnClickListener(v -> listener.onOpenSeller(seller));
        b.pasar.setText(lokasiPasar);
        b.pasar.setOnClickListener(v -> listener.onOpenMarket(seller, lokasiPasar));

        b.products.removeAllViews();
        List<CartProduct> products = seller.produkPenjual;
        if (products == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(ctx);
        for (CartProduct p : products) {
            ItemCartProductBinding pb = ItemCartProductBinding.inflate(inflater, b.products, false);
            bindProduct(ctx, pb, p);
            b.products.addView(pb.getRoot());
        }
    }

    private void bindProduct(Context ctx, ItemCartProductBinding pb, CartProduct p) {
        int harga = CartProduct.toInt(p.harga);
        int diskon = CartProduct.toInt(p.diskon);
        String namaSatuan = p.isPaket() ? ctx.getString(R.string.cart_unit_paket) : p.namaSatuan;
        String folder = p.isPaket() ? ServerPath.uploadGambarPaket : ServerPath.uploadGambarProduk;
        Images.url(pb.foto, Images.server(folder + "/compress", p.fotoProduk));
        pb.foto.setOnClickListener(v -> listener.onOpenProduct(p));
        pb.nama.setText(p.namaProduk);
        pb.nama.setOnClickListener(v -> listener.onOpenProduct(p));
        pb.total.setText(Currency.formatRupiah(p.total(), 0));
        pb.quantity.setText(ctx.getString(R.string.cart_qty_unit, p.quantity, namaSatuan));
        pb.unitPrice.setText(ctx.getString(R.string.cart_unit_price, namaSatuan,
                Currency.formatRupiah(harga - diskon, 0)));
        pb.editQuantity.setOnClickListener(v ->
                listener.onEdit(p, R.string.cart_edit_failed_qty, R.string.cart_retry));
        pb.delete.setOnClickListener(v -> listener.onDelete(p));

        boolean adaCatatan = p.catatan != null && !p.catatan.isEmpty();
        pb.addNote.setVisibility(adaCatatan ? android.view.View.GONE : android.view.View.VISIBLE);
        pb.addNote.setOnClickListener(v ->
                listener.onEdit(p, R.string.cart_edit_failed_add_note, R.string.cart_ok));
        pb.noteBox.setVisibility(adaCatatan ? android.view.View.VISIBLE : android.view.View.GONE);
        if (adaCatatan) {
            pb.note.setText(p.catatan);
            pb.editNote.setOnClickListener(v ->
                    listener.onEdit(p, R.string.cart_edit_failed_edit_note, R.string.cart_ok));
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemCartSellerBinding binding;

        Holder(ItemCartSellerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<CartSeller> DIFF = new DiffUtil.ItemCallback<CartSeller>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartSeller a, @NonNull CartSeller b) {
            return Objects.equals(a.idPenjual, b.idPenjual);
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartSeller a, @NonNull CartSeller b) {
            return false; // lines change in place (quantity / note); always rebind
        }
    };
}
