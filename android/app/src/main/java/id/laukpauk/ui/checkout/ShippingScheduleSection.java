package id.laukpauk.ui.checkout;

import android.view.View;

import androidx.annotation.Nullable;

import id.laukpauk.R;
import id.laukpauk.data.model.checkout.ShippingSchedule;
import id.laukpauk.databinding.ViewCheckoutScheduleBinding;

/** Saved delivery schedule and address with "Ubah Alamat dan Jadwal" (legacy ShippingSchedule). */
final class ShippingScheduleSection {

    private final ViewCheckoutScheduleBinding b;

    ShippingScheduleSection(ViewCheckoutScheduleBinding binding, Runnable onEdit) {
        this.b = binding;
        b.tvEdit.setOnClickListener(v -> onEdit.run());
    }

    void render(@Nullable ShippingSchedule j, @Nullable String address) {
        if (j != null) {
            b.scheduleBox.setVisibility(View.VISIBLE);
            b.tvSchedule.setText(b.getRoot().getContext().getString(R.string.checkout_delivery,
                    nz(j.day), nz(j.session)));
            b.tvEstimate.setText(b.getRoot().getContext().getString(R.string.checkout_estimate,
                    nz(j.timeRange)));
        } else {
            b.scheduleBox.setVisibility(View.GONE);
        }
        b.addressBox.setVisibility(View.VISIBLE);
        b.tvShippingAddress.setVisibility(address != null ? View.VISIBLE : View.GONE);
        b.tvShippingAddress.setText(address);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
