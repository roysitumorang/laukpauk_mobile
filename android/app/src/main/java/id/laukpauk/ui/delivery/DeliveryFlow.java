package id.laukpauk.ui.delivery;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

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
     * Before buying: if a selection is set runs {@code onReady} at once; otherwise shows the legacy
     * adjdNotSetAlert(namaProduk) and, if the buyer agrees, {@link #ask} and then {@code onReady}.
     */
    public static void ensure(Fragment fragment, @Nullable String namaProduk, Runnable onReady) {
        showNotSet(fragment, onReady, R.string.delivery_not_set_title, fragment.getString(R.string.delivery_not_set_message),
                R.string.delivery_not_set_ok);
    }

    /** Same as {@link #ensure} with the wording of the legacy PaketModel.adjdNotSetAlert (names the paket). */
    public static void ensurePaket(Fragment fragment, @Nullable String namaPaket, Runnable onReady) {
        String nama = namaPaket == null ? "" : namaPaket.toUpperCase(Locale.ROOT);
        showNotSet(fragment, onReady, R.string.delivery_not_set_paket_title,
                fragment.getString(R.string.delivery_not_set_paket_message, nama), R.string.delivery_not_set_paket_ok);
    }

    private static void showNotSet(Fragment fragment, Runnable onReady, int title, String message, int okLabel) {
        if (!fragment.isAdded()) {
            return;
        }
        if (repo(fragment).isSetNow()) {
            onReady.run();
            return;
        }
        new MaterialAlertDialogBuilder(fragment.requireContext()).setTitle(title).setMessage(message)
                .setNegativeButton(R.string.delivery_tutup, null)
                .setPositiveButton(okLabel, (d, w) -> ask(fragment, ok -> {
                    if (ok) {
                        onReady.run();
                    }
                })).show();
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

    /** The home screen's start-up dialogs: legacy giftChecking, then makeSureADJD (each shown once). */
    public static void homeChecks(Fragment fragment) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (!fragment.isAdded()) {
                return;
            }
            DeliveryRepository repo = repo(fragment);
            if (!repo.showAlertGift()) {
                makeSure(fragment, handler);
                return;
            }
            repo.getGifts(r -> {
                if (!fragment.isAdded()) {
                    return;
                }
                if (r.isSuccess() && r.data != null && !r.data.isEmpty()) {
                    GiftAlert.show(fragment, r.data, () -> {
                        repo.setAlertGiftShown();
                        makeSure(fragment, handler);
                    });
                } else {
                    makeSure(fragment, handler);
                }
            });
        }, 3000);
    }

    private static void makeSure(Fragment fragment, Handler handler) {
        handler.postDelayed(() -> {
            if (!fragment.isAdded()) {
                return;
            }
            DeliveryRepository repo = repo(fragment);
            if (!repo.showMakeSure()) {
                return;
            }
            new MaterialAlertDialogBuilder(fragment.requireContext()).setTitle(R.string.delivery_laukpauk)
                    .setMessage(R.string.delivery_make_sure).setCancelable(false)
                    .setPositiveButton(R.string.delivery_ok, (d, w) -> repo.setMakeSureShown()).show();
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
