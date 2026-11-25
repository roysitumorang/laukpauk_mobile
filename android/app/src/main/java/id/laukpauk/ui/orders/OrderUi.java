package id.laukpauk.ui.orders;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.laukpauk.R;
import id.laukpauk.data.model.orders.BuyerTransaction;

/** Small helpers shared by the buyer order screens. */
final class OrderUi {
    private OrderUi() {}

    static String nz(@Nullable String s) {
        return s == null ? "" : s;
    }

    /** Label of a {@code buyer/transactions} status (0..8; "" for an unknown one). */
    static String statusLabel(android.content.Context c, int status) {
        int[] labels = {R.string.orders_status_0, R.string.orders_status_1, R.string.orders_status_2,
                R.string.orders_status_3, R.string.orders_status_4, R.string.orders_status_5, R.string.orders_status_6,
                R.string.orders_status_7, R.string.orders_status_8};
        return status >= 0 && status < labels.length ? c.getString(labels[status]) : "";
    }

    /**
     * The step of the 4-step indicator ({@link TransactionStatusView}) a status has reached: paid "approved",
     * processed "processing", shipped or complained "otw", completed "received"; "" before payment or when cancelled.
     */
    static String step(int status) {
        switch (status) {
            case BuyerTransaction.STATUS_PAID:
            case BuyerTransaction.STATUS_WITHDRAWAL_PENDING:
                return "approved";
            case BuyerTransaction.STATUS_PROCESSED:
                return "processing";
            case BuyerTransaction.STATUS_SHIPPED:
            case BuyerTransaction.STATUS_COMPLAINED:
                return "otw";
            case BuyerTransaction.STATUS_COMPLETED:
                return "received";
            default:
                return "";
        }
    }

    /** The amount the buyer pays, after the seller's revisions (equal to the payment amount until there are any). */
    static long total(BuyerTransaction t) {
        return t.revisedPaymentAmount;
    }

    /** An RFC 3339 time ("2026-11-26T07:00:00+07:00") in the phone's time zone; null when it can't be read. */
    @Nullable
    static java.util.Date parse(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US);
            return in.parse(value.replaceFirst("\\.\\d+", ""));
        } catch (java.text.ParseException | IllegalArgumentException e) {
            return null;
        }
    }

    /** {@code value} formatted with {@code pattern} in Indonesian (e.g. "EEEE, d MMMM yyyy"); "" when unreadable. */
    static String format(@Nullable String value, String pattern) {
        java.util.Date d = parse(value);
        return d == null ? "" : new java.text.SimpleDateFormat(pattern, new java.util.Locale("id", "ID")).format(d);
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
    static void confirmReceived(Fragment f, Runnable onOk) {
        if (!f.isAdded()) {
            return;
        }
        new MaterialAlertDialogBuilder(f.requireContext())
                .setTitle(R.string.orders_received_title)
                .setMessage(R.string.orders_received_message)
                .setNegativeButton(R.string.orders_not_received, null)
                .setPositiveButton(R.string.orders_ok, (d, w) -> onOk.run())
                .show();
    }
}
