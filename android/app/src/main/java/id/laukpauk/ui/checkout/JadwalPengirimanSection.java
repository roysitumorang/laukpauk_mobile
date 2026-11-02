package id.laukpauk.ui.checkout;

import android.view.View;

import androidx.annotation.Nullable;

import id.laukpauk.R;
import id.laukpauk.data.model.checkout.JadwalPengiriman;
import id.laukpauk.databinding.ViewCheckoutJadwalBinding;

/** Saved delivery schedule and address with "Ubah Alamat dan Jadwal" (legacy JadwalPengiriman). */
final class JadwalPengirimanSection {

    private final ViewCheckoutJadwalBinding b;

    JadwalPengirimanSection(ViewCheckoutJadwalBinding binding, Runnable onUbah) {
        this.b = binding;
        b.tvUbah.setOnClickListener(v -> onUbah.run());
    }

    void render(@Nullable JadwalPengiriman j, @Nullable String alamat) {
        if (j != null) {
            b.boxJadwal.setVisibility(View.VISIBLE);
            b.tvJadwal.setText(b.getRoot().getContext().getString(R.string.checkout_pengantaran,
                    nz(j.day), nz(j.sessi)));
            b.tvEstimasi.setText(b.getRoot().getContext().getString(R.string.checkout_estimasi,
                    nz(j.rentangWaktu)));
        } else {
            b.boxJadwal.setVisibility(View.GONE);
        }
        b.boxAlamat.setVisibility(View.VISIBLE);
        b.tvAlamatPengantaran.setVisibility(alamat != null ? View.VISIBLE : View.GONE);
        b.tvAlamatPengantaran.setText(alamat);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
