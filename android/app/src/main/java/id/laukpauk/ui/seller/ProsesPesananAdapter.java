package id.laukpauk.ui.seller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.SellerNumbers;
import id.laukpauk.data.model.seller.orders.PesananMasukItem;
import id.laukpauk.databinding.ItemSellerPesananProsesBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** ProsesPesananItem: an ordered item with its availability chip and the red / green action icon. */
public class ProsesPesananAdapter extends ListAdapter<PesananMasukItem, ProsesPesananAdapter.Holder> {

    public interface Listener {
        /** @param rejected true when the item is currently marked unavailable (the icon then undoes it). */
        void onAction(PesananMasukItem item, boolean rejected);
    }

    private final Listener listener;

    public ProsesPesananAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<PesananMasukItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull PesananMasukItem a, @NonNull PesananMasukItem b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PesananMasukItem a, @NonNull PesananMasukItem b) {
                return Objects.equals(a.status, b.status) && Objects.equals(a.quantity, b.quantity)
                        && Objects.equals(a.harga, b.harga) && Objects.equals(a.diskon, b.diskon)
                        && Objects.equals(a.catatan, b.catatan) && Objects.equals(a.namaProduk, b.namaProduk);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerPesananProsesBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final PesananMasukItem p = getItem(position);
        ItemSellerPesananProsesBinding b = h.binding;
        Context ctx = b.getRoot().getContext();
        boolean paket = "paket".equalsIgnoreCase(p.jenisProduk);
        String namaSatuan = paket ? "pcs" : p.namaSatuan;
        if (namaSatuan == null) {
            namaSatuan = ctx.getString(R.string.seller_pesanan_satuan_tidak_diketahui);
        }
        int diskon = SellerNumbers.toInt(p.diskon);
        boolean checked = p.status != null;
        boolean rejected = checked && p.status.equalsIgnoreCase("rejected");

        b.card.setCardBackgroundColor(ContextCompat.getColor(ctx, rejected ? R.color.seller_pesanan_item_rejected_bg
                : R.color.nearly_white));
        b.card.setStrokeColor(ContextCompat.getColor(ctx, rejected ? R.color.text_danger : R.color.green));
        Images.circle(b.foto, Images.server(ServerPath.uploadGambarProduk + "/compress",
                p.fotoProduk == null ? "" : p.fotoProduk));
        b.nama.setText(p.namaProduk);
        int chipColor = checked ? (rejected ? R.color.text_danger : R.color.text_info) : R.color.text_muted;
        b.chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, chipColor)));
        b.chip.setText(checked ? (rejected ? R.string.seller_pesanan_tidak_tersedia
                : R.string.seller_pesanan_tersedia) : R.string.seller_pesanan_belum_diperiksa);
        b.qty.setText(Currency.formatSatuan(SellerNumbers.toInt(p.quantity),
                ctx.getString(R.string.seller_pesanan_x_satuan, namaSatuan)));
        b.harga.setText(ctx.getString(R.string.seller_pesanan_harga,
                Currency.formatRupiah(SellerNumbers.toInt(p.harga), 0)));
        b.diskon.setVisibility(diskon >= 1 ? View.VISIBLE : View.GONE);
        b.diskon.setText(ctx.getString(R.string.seller_pesanan_diskon, Currency.formatRupiah(diskon, 0)));
        boolean hasCatatan = p.catatan != null && !p.catatan.isEmpty();
        b.catatanBox.setVisibility(hasCatatan ? View.VISIBLE : View.GONE);
        b.catatan.setText(p.catatan);
        b.trailing.setImageResource(rejected ? R.drawable.ic_fa_check_circle_regular
                : R.drawable.ic_fa_minus_circle);
        b.trailing.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx,
                rejected ? R.color.green : R.color.text_danger)));
        b.trailing.setContentDescription(ctx.getString(rejected ? R.string.seller_pesanan_batalkan_klaim
                : R.string.seller_pesanan_tandai_tidak_tersedia));
        b.trailing.setOnClickListener(v -> listener.onAction(p, rejected));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemSellerPesananProsesBinding binding;

        Holder(ItemSellerPesananProsesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
