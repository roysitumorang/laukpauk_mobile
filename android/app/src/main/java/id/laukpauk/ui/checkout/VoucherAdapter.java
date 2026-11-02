package id.laukpauk.ui.checkout;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.checkout.Voucher;
import id.laukpauk.databinding.ItemCheckoutVoucherBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** Vouchers in the "Voucher Kamu" sheet (legacy VoucherItemCheckout). */
final class VoucherAdapter extends ListAdapter<Voucher, VoucherAdapter.Holder> {

    interface OnVoucher {
        void onVoucher(Voucher voucher);
    }

    private final String selectedId;
    private final OnVoucher listener;

    VoucherAdapter(String selectedId, OnVoucher listener) {
        super(new DiffUtil.ItemCallback<Voucher>() {
            @Override
            public boolean areItemsTheSame(@NonNull Voucher a, @NonNull Voucher b) {
                return a.id != null && a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Voucher a, @NonNull Voucher b) {
                return areItemsTheSame(a, b);
            }
        });
        this.selectedId = selectedId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCheckoutVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Voucher v = getItem(position);
        ItemCheckoutVoucherBinding b = h.b;
        boolean selected = selectedId != null && selectedId.equals(v.id);
        b.row.setBackgroundResource(selected ? R.drawable.bg_checkout_selected : 0);
        Images.circle(b.foto, Images.server(ServerPath.uploadGambarVoucher + "/compress", v.foto));
        b.nama.setText(v.nama);
        b.untuk.setText(b.getRoot().getContext().getString(R.string.checkout_voucher_untuk, v.jenis));
        b.potongan.setText(b.getRoot().getContext().getString(R.string.checkout_voucher_potongan,
                Currency.formatRupiah(v.nilai, 0), Currency.formatRupiah(v.minimumBelanja, 0)));
        b.tile.setOnClickListener(x -> listener.onVoucher(v));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemCheckoutVoucherBinding b;

        Holder(ItemCheckoutVoucherBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
