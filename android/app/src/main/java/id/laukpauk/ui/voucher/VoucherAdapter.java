package id.laukpauk.ui.voucher;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.databinding.ItemVoucherBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** VoucherItem.dart: ticket-like cards. */
public class VoucherAdapter extends ListAdapter<Voucher, VoucherAdapter.Holder> {

    public interface OnClick {
        void onClick(Voucher voucher);
    }

    private final OnClick onClick;

    public VoucherAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Voucher v = getItem(position);
        ItemVoucherBinding b = holder.binding;
        // Legacy: the picture is 20% of the screen height; the notches sit on its vertical middle.
        int height = Math.round(b.getRoot().getResources().getDisplayMetrics().heightPixels * 0.20f);
        b.foto.getLayoutParams().height = height;
        for (android.view.View notch : new android.view.View[]{b.notchStart, b.notchEnd}) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) notch.getLayoutParams();
            lp.topMargin = height / 2;
            notch.setLayoutParams(lp);
        }
        Images.url(b.foto, Images.server(ServerPath.uploadGambarVoucher, v.foto));
        b.berlaku.setText(b.getRoot().getContext().getString(R.string.voucher_valid_until, v.berlakuSampai));
        b.minimum.setText(b.getRoot().getContext().getString(R.string.voucher_min_purchase_short,
                Currency.formatRupiah(Voucher.toInt(v.minimumBelanja), 0)));
        b.foto.setOnClickListener(view -> onClick.onClick(v));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemVoucherBinding binding;

        Holder(ItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<Voucher> DIFF = new DiffUtil.ItemCallback<Voucher>() {
        @Override
        public boolean areItemsTheSame(@NonNull Voucher a, @NonNull Voucher b) {
            return Objects.equals(a.id, b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Voucher a, @NonNull Voucher b) {
            return Objects.equals(a.foto, b.foto) && Objects.equals(a.berlakuSampai, b.berlakuSampai)
                    && Objects.equals(a.minimumBelanja, b.minimumBelanja);
        }
    };
}
