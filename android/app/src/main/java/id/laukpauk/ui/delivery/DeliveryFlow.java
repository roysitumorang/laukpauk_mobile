package id.laukpauk.ui.delivery;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import id.laukpauk.R;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.databinding.DialogDeliveryConfirmAddressBinding;
import id.laukpauk.databinding.DialogDeliveryExpiredBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;

/**
 * The delivery address + schedule flows (legacy HeaderModel.adjd / showAlamatPengantaran / makeSureADJD /
 * giftChecking / alertADJDKadaluarsa, ProdukModel/PaketModel.adjdNotSetAlert, ADJDModel). Results are
 * stored by DeliveryRepository, so callers usually just observe {@code deliveryRepository.selection()}.
 *
 * {@link #ask} runs in a headless {@link DeliveryFlowFragment} on the activity; the callbacks are only invoked
 * while the calling fragment is still added. The map screen returns through NavResults key
 * {@code PinGMAPSFragment.RESULT_KEY}.
 */
public final class DeliveryFlow {
    private DeliveryFlow() {}

    private static DeliveryRepository repo(Fragment f) {
        return AppContainer.from(f.requireContext()).deliveryRepository;
    }

    /** Legacy HeaderModel.adjd(context): asks the address (detect/PinGMAPS) then the schedule; onDone(true) when a selection was saved. */
    public static void ask(Fragment fragment, @Nullable Callback onDone) {
        FragmentActivity a = fragment.getActivity();
        if (a == null) {
            return;
        }
        DeliveryFlowFragment.request(a, ok -> {
            if (onDone != null && fragment.isAdded()) {
                onDone.onDone(ok);
            }
        });
    }

    /**
     * Checkout is the only place that needs a delivery address and schedule: runs {@code onReady} at once
     * when a selection is set, otherwise asks for one ({@link #ask}) and runs {@code onReady} when it was saved.
     */
    public static void ensureForCheckout(Fragment fragment, Runnable onReady) {
        if (!fragment.isAdded()) {
            return;
        }
        if (repo(fragment).isSetNow()) {
            onReady.run();
            return;
        }
        ask(fragment, ok -> {
            if (ok) {
                onReady.run();
            }
        });
    }

    /** The header's address pill: legacy showAlamatPengantaran; "ubah lokasi" clears and re-asks. */
    public static void change(Fragment fragment, @Nullable Callback onDone) {
        if (!fragment.isAdded()) {
            return;
        }
        AppContainer c = AppContainer.from(fragment.requireContext());
        String alamat = c.deliveryRepository.current().alamat;
        DialogDeliveryConfirmAddressBinding b = DialogDeliveryConfirmAddressBinding.inflate(
                LayoutInflater.from(fragment.requireContext()));
        b.message.setText(R.string.delivery_change_subtitle);
        b.address.setText(alamat != null ? alamat : DeliveryRepository.DEFAULT_ALAMAT);
        new MaterialAlertDialogBuilder(fragment.requireContext()).setTitle(R.string.delivery_alamat_title)
                .setView(b.getRoot()).setCancelable(false)
                .setNeutralButton(R.string.delivery_tutup, null)
                .setNegativeButton(R.string.delivery_change_reset,
                        (d, w) -> c.cartRepository.clearTransactionSession(r -> c.deliveryRepository.reload()))
                .setPositiveButton(R.string.delivery_change_ubah, (d, w) -> {
                    c.deliveryRepository.clear();
                    ask(fragment, onDone);
                }).show();
    }

    /** The home screen's start-up dialog: legacy giftChecking (shown once). */
    public static void homeChecks(Fragment fragment) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (!fragment.isAdded()) {
                return;
            }
            DeliveryRepository repo = repo(fragment);
            if (!repo.showAlertGift()) {
                return;
            }
            repo.getGifts(r -> {
                if (!fragment.isAdded()) {
                    return;
                }
                if (r.isSuccess() && r.data != null && !r.data.isEmpty()) {
                    GiftAlert.show(fragment, r.data, repo::setAlertGiftShown);
                }
            });
        }, 3000);
    }

    /** Legacy alertADJDKadaluarsa: the saved schedule has expired; {@code onClosed} runs when dismissed. */
    public static void alertExpired(Fragment fragment, @Nullable Runnable onClosed) {
        if (!fragment.isAdded()) {
            if (onClosed != null) {
                onClosed.run();
            }
            return;
        }
        DialogDeliveryExpiredBinding b = DialogDeliveryExpiredBinding.inflate(
                LayoutInflater.from(fragment.requireContext()));
        Images.asset(b.icon, "laukpauk-icon.png");
        new MaterialAlertDialogBuilder(fragment.requireContext()).setTitle(R.string.delivery_expired_title)
                .setView(b.getRoot()).setCancelable(false)
                .setPositiveButton(R.string.delivery_ok, (d, w) -> {
                    if (onClosed != null) {
                        onClosed.run();
                    }
                }).show();
    }

    public interface Callback {
        void onDone(boolean selectionSaved);
    }
}
