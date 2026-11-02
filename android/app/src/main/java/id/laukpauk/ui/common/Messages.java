package id.laukpauk.ui.common;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import id.laukpauk.R;

/** Snackbars and simple dialogs (legacy ShopModelUi.keranjangSnackbar, CustomAlert.info / konfirmasi). */
public final class Messages {
    private Messages() {}

    public static void snack(Fragment fragment, CharSequence text) {
        View v = fragment.getView();
        if (v != null) {
            Snackbar.make(v, text, Snackbar.LENGTH_SHORT).show();
        }
    }

    /** Legacy keranjangSnackbar: text + "Lihat Keranjang" action that opens the cart. */
    public static void cartSnack(Fragment fragment, CharSequence text) {
        View v = fragment.getView();
        if (v == null) {
            return;
        }
        Snackbar.make(v, text, 2000)
                .setAction(R.string.lihat_keranjang, a -> {
                    if (fragment.isAdded()) {
                        NavHostFragment.findNavController(fragment).navigate(R.id.keranjangFragment);
                    }
                })
                .show();
    }

    /** Legacy CustomAlert.info: title, message, one OK button. */
    public static void info(Fragment fragment, CharSequence title, @Nullable CharSequence message,
                            @Nullable Runnable afterOk) {
        if (!fragment.isAdded()) {
            return;
        }
        new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    if (afterOk != null) {
                        afterOk.run();
                    }
                })
                .show();
    }

    /** Legacy CustomAlert.konfirmasi: title, message, "Batal" / {@code yesLabel}. */
    public static void confirm(Fragment fragment, CharSequence title, @Nullable CharSequence message,
                               CharSequence yesLabel, Runnable onYes) {
        if (!fragment.isAdded()) {
            return;
        }
        new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.batal, null)
                .setPositiveButton(yesLabel, (d, w) -> onYes.run())
                .show();
    }
}
