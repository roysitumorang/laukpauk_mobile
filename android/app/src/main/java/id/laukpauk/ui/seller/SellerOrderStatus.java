package id.laukpauk.ui.seller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.core.content.ContextCompat;

import id.laukpauk.R;
import id.laukpauk.databinding.ViewSellerPesananStatusBinding;

/** StatusTransaksi.dart: the 4-step indicator (Diterima, Proses, Kirim, Selesai) and the readable status text. */
final class SellerOrderStatus {
    private SellerOrderStatus() {}

    /** approved = 1, processing = 2, otw = 3, received = 4, anything else 0. */
    static int step(String status) {
        String s = status == null ? "" : status.toLowerCase();
        switch (s) {
            case "approved":
                return 1;
            case "processing":
                return 2;
            case "otw":
                return 3;
            case "received":
                return 4;
            default:
                return 0;
        }
    }

    static void bind(ViewSellerPesananStatusBinding b, String status) {
        Context ctx = b.getRoot().getContext();
        int active = ContextCompat.getColor(ctx, R.color.green);
        int idle = ContextCompat.getColor(ctx, R.color.text_muted);
        int current = step(status);
        b.circle1.setBackgroundTintList(ColorStateList.valueOf(1 <= current ? active : idle));
        b.circle2.setBackgroundTintList(ColorStateList.valueOf(2 <= current ? active : idle));
        b.circle3.setBackgroundTintList(ColorStateList.valueOf(3 <= current ? active : idle));
        b.circle4.setBackgroundTintList(ColorStateList.valueOf(4 <= current ? active : idle));
        setLine(b.line1, 2 <= current ? active : idle);
        setLine(b.line2, 3 <= current ? active : idle);
        setLine(b.line3, 4 <= current ? active : idle);
    }

    private static void setLine(View line, int color) {
        line.setBackgroundColor(color);
    }

    /** PesananSayaModel.readAbleStatusForUser. */
    static int readable(String status) {
        String s = status == null ? "" : status.toLowerCase();
        switch (s) {
            case "pending":
                return R.string.seller_pesanan_user_pending;
            case "processing":
                return R.string.seller_pesanan_user_processing;
            case "otw":
                return R.string.seller_pesanan_user_otw;
            case "received":
                return R.string.seller_pesanan_user_received;
            case "rejected":
                return R.string.seller_pesanan_user_rejected;
            default:
                return 0;
        }
    }
}
