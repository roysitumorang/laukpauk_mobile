package id.laukpauk.ui.seller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.shop.HistoriPenjualan;
import id.laukpauk.databinding.ItemSellerTokoHistoriBinding;
import id.laukpauk.util.Currency;

/** Rows of the sales history (HistoriPenjualanItem.dart). */
class HistoriPenjualanAdapter extends ListAdapter<HistoriPenjualan, HistoriPenjualanAdapter.Holder> {

    interface OnClick {
        void onClick(HistoriPenjualan item);
    }

    private final OnClick onClick;

    HistoriPenjualanAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<HistoriPenjualan>() {
            @Override
            public boolean areItemsTheSame(@NonNull HistoriPenjualan a, @NonNull HistoriPenjualan b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull HistoriPenjualan a, @NonNull HistoriPenjualan b) {
                return Objects.equals(a.status, b.status) && Objects.equals(a.namaUser, b.namaUser)
                        && Objects.equals(a.nomorTransaksi, b.nomorTransaksi)
                        && Objects.equals(a.waktuCheckout, b.waktuCheckout) && Objects.equals(a.total, b.total);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerTokoHistoriBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        HistoriPenjualan h = getItem(position);
        ItemSellerTokoHistoriBinding b = holder.binding;
        android.content.Context ctx = b.getRoot().getContext();
        b.status.setText(h.status == null ? "" : h.status.toUpperCase(Locale.ROOT));
        b.transaksi.setText(ctx.getString(R.string.seller_toko_histori_transaksi, h.namaUser));
        b.invoice.setText(ctx.getString(R.string.seller_toko_histori_invoice, h.nomorTransaksi));
        b.waktu.setText(ctx.getString(R.string.seller_toko_histori_pada, h.waktuCheckout));
        b.totalProgress.setVisibility(h.total == null ? View.VISIBLE : View.GONE);
        b.total.setVisibility(h.total == null ? View.GONE : View.VISIBLE);
        if (h.total != null) {
            b.total.setText(Currency.formatRupiah(h.total, 0));
        }
        b.root.setOnClickListener(v -> onClick.onClick(h));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemSellerTokoHistoriBinding binding;

        Holder(ItemSellerTokoHistoriBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
