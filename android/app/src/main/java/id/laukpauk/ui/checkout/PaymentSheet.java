package id.laukpauk.ui.checkout;

import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.databinding.SheetCheckoutPaymentBinding;

/**
 * Bottom sheet "Metode Pembayaran": the channels of {@code GET buyer/payment_channels}; picking one shows "Buat
 * Pesanan", which places the order with that channel's id through the CheckoutViewModel ({@code POST
 * buyer/transactions}). The answer is handled by the fragment.
 */
final class PaymentSheet {

    private final CheckoutViewModel vm;
    private final BottomSheetDialog dialog;
    private final SheetCheckoutPaymentBinding b;
    private final ChannelAdapter adapter;

    private PaymentChannel selected;
    /** True while the order is being placed ({@code POST buyer/transactions}). */
    private boolean submitting;

    PaymentSheet(Fragment fragment, CheckoutViewModel vm, long totalBill) {
        this.vm = vm;
        this.dialog = new BottomSheetDialog(fragment.requireContext());
        this.b = SheetCheckoutPaymentBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(b.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        adapter = new ChannelAdapter(this::onChannel);
        b.list.setAdapter(adapter);
        b.btnOrder.setOnClickListener(v -> {
            if (selected != null) {
                vm.placeOrder(selected);
            }
        });

        vm.channels(totalBill).observe(fragment.getViewLifecycleOwner(), res -> {
            b.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                adapter.submitList(res.isSuccess() ? res.data : null);
            }
        });
        vm.orderBusy().observe(fragment.getViewLifecycleOwner(), busy -> showSubmitting(Boolean.TRUE.equals(busy)));
    }

    void show() {
        dialog.show();
    }

    void dismiss() {
        dialog.dismiss();
    }

    /**
     * While the order is being placed: a spinner in place of "Buat Pesanan", the channels dimmed and locked, and the
     * sheet can't be dismissed, so the buyer can't change the channel or leave before the answer comes.
     */
    private void showSubmitting(boolean busy) {
        submitting = busy;
        b.orderProgress.setVisibility(busy ? View.VISIBLE : View.GONE);
        b.btnOrder.setEnabled(!busy);
        b.btnOrder.setVisibility(busy ? View.INVISIBLE : selected != null ? View.VISIBLE : View.GONE);
        b.list.setAlpha(busy ? 0.5f : 1f);
        dialog.setCancelable(!busy);
    }

    private void onChannel(PaymentChannel ch) {
        if (submitting) {
            return;
        }
        selected = ch;
        adapter.setSelected(ch.channel_code == null ? "" : ch.channel_code.toLowerCase(Locale.ROOT));
        b.btnOrder.setVisibility(View.VISIBLE);
    }
}
