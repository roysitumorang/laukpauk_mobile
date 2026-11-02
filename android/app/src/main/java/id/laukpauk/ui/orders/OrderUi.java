package id.laukpauk.ui.orders;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.laukpauk.R;

/** Small helpers shared by the buyer order screens. */
final class OrderUi {
    private OrderUi() {}

    static String nz(@Nullable String s) {
        return s == null ? "" : s;
    }

    /** Legacy PesananSayaModel.readAbleStatusForUser ("" for an unknown status). */
    static String statusReadable(Fragment f, @Nullable String status) {
        String lc = nz(status).toLowerCase();
        switch (lc) {
            case "pending":
                return f.getString(R.string.orders_status_pending);
            case "processing":
                return f.getString(R.string.orders_status_processing);
            case "otw":
                return f.getString(R.string.orders_status_otw);
            case "received":
                return f.getString(R.string.orders_status_received);
            case "rejected":
                return f.getString(R.string.orders_status_rejected);
            default:
                return "";
        }
    }

    /** ACTION_VIEW for {@code url}; false when nothing can open it. */
    static boolean openUrl(Fragment f, String url) {
        try {
            f.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }

    /** "Pesanan Diterima" confirmation (buttons "Belum Diterima" / "OK"). */
    static void confirmDiterima(Fragment f, Runnable onOk) {
        if (!f.isAdded()) {
            return;
        }
        new MaterialAlertDialogBuilder(f.requireContext())
                .setTitle(R.string.orders_diterima_title)
                .setMessage(R.string.orders_diterima_message)
                .setNegativeButton(R.string.orders_belum_diterima, null)
                .setPositiveButton(R.string.orders_ok, (d, w) -> onOk.run())
                .show();
    }
}
