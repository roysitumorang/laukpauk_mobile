package id.laukpauk.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.databinding.ItemCheckoutChannelBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;

/** Payment channels in the payment sheet (legacy PilihanMetodePembayaran tiles). */
final class ChannelAdapter extends ListAdapter<PaymentChannel, ChannelAdapter.Holder> {

    interface OnChannel {
        void onChannel(PaymentChannel channel);
    }

    private final long totalTagihan;
    private final OnChannel listener;
    private String selectedCode;

    ChannelAdapter(long totalTagihan, OnChannel listener) {
        super(new DiffUtil.ItemCallback<PaymentChannel>() {
            @Override
            public boolean areItemsTheSame(@NonNull PaymentChannel a, @NonNull PaymentChannel b) {
                return a.channel_code != null && a.channel_code.equals(b.channel_code);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PaymentChannel a, @NonNull PaymentChannel b) {
                return areItemsTheSame(a, b);
            }
        });
        this.totalTagihan = totalTagihan;
        this.listener = listener;
    }

    void setSelected(String channelCodeLC) {
        selectedCode = channelCodeLC;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCheckoutChannelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PaymentChannel ch = getItem(position);
        ItemCheckoutChannelBinding b = h.b;
        String code = ch.channel_code == null ? "" : ch.channel_code;
        boolean enough = totalTagihan >= ch.minimalPembayaran;
        Images.asset(b.logo, "payment/" + code.toUpperCase(Locale.ROOT) + ".png");
        b.nama.setText(ch.name);
        b.radio.setChecked(code.toLowerCase(Locale.ROOT).equals(selectedCode));
        b.radio.setEnabled(enough);
        b.row.setAlpha(enough ? 1f : 0.38f);
        b.minimal.setVisibility(enough ? View.GONE : View.VISIBLE);
        b.minimal.setText(b.getRoot().getContext().getString(R.string.checkout_minimal_pembayaran,
                Currency.formatRupiah(ch.minimalPembayaran, 0)));
        b.row.setEnabled(enough);
        b.row.setOnClickListener(enough ? v -> listener.onChannel(ch) : null);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemCheckoutChannelBinding b;

        Holder(ItemCheckoutChannelBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
