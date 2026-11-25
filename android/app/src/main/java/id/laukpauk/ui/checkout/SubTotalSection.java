package id.laukpauk.ui.checkout;

import id.laukpauk.databinding.ViewCheckoutSubtotalBinding;
import id.laukpauk.util.Currency;

/** The TOTAL + "Bayar" bar at the top of the Checkout screen (legacy SubTotal). */
final class SubTotalSection {

    private final ViewCheckoutSubtotalBinding b;

    SubTotalSection(ViewCheckoutSubtotalBinding binding, Runnable onPay) {
        this.b = binding;
        b.btnPay.setOnClickListener(v -> onPay.run());
    }

    void render(CheckoutSummary s) {
        b.tvTotal.setText(Currency.formatRupiah(s.overallTotal, 0));
        b.btnPay.setAlpha(s.isFormCompleted ? 1f : .35f);
    }
}
