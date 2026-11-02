package id.laukpauk.ui.checkout;

import android.view.View;

import id.laukpauk.R;
import id.laukpauk.databinding.ViewCheckoutSubtotalBinding;
import id.laukpauk.util.Currency;

/** The TOTAL + "Bayar" bar at the top of the Checkout screen (legacy SubTotal). */
final class SubTotalSection {

    private final ViewCheckoutSubtotalBinding b;

    SubTotalSection(ViewCheckoutSubtotalBinding binding, Runnable onBayar) {
        this.b = binding;
        b.btnBayar.setOnClickListener(v -> onBayar.run());
    }

    void render(CheckoutSummary s) {
        b.tvTotal.setText(Currency.formatRupiah(s.totalKeseluruhan, 0));
        b.btnBayar.setAlpha(s.isFormCompleted ? 1f : .35f);
        if (!s.isWalletOn) {
            b.tvWallet.setVisibility(View.GONE);
            return;
        }
        b.tvWallet.setVisibility(View.VISIBLE);
        if (s.walletConfiguration != null) {
            b.tvWallet.setText(b.getRoot().getContext().getString(R.string.checkout_wallet_info,
                    Currency.formatRupiah(s.walletConfiguration.walletDiscount, 0),
                    Currency.formatRupiah(s.walletConfiguration.minGrandTotalTransactionForWallet, 0)));
        } else {
            b.tvWallet.setText(R.string.checkout_wallet_gagal);
        }
    }
}
