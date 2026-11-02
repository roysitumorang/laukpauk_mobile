package id.laukpauk.ui.delivery;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * The delivery address + schedule flows (legacy HeaderModel.adjd / showAlamatPengantaran / makeSureADJD /
 * giftChecking / alertADJDKadaluarsa, ProdukModel/PaketModel.adjdNotSetAlert, ADJDModel). Results are
 * stored by DeliveryRepository, so callers usually just observe {@code deliveryRepository.selection()}.
 *
 * PLACEHOLDER (contract only) - implemented by the delivery slice. Keep these signatures.
 */
public final class DeliveryFlow {
    private DeliveryFlow() {}

    /** Legacy HeaderModel.adjd(context): asks the address (detect/PinGMAPS) then the schedule; onDone(true) when a selection was saved. */
    public static void ask(Fragment fragment, @Nullable Callback onDone) {
    }

    /**
     * Before buying: if a selection is set runs {@code onReady} at once; otherwise shows the legacy
     * adjdNotSetAlert(namaProduk) and, if the buyer agrees, {@link #ask} and then {@code onReady}.
     */
    public static void ensure(Fragment fragment, @Nullable String namaProduk, Runnable onReady) {
    }

    /** The header's address pill: legacy showAlamatPengantaran; "ubah lokasi" clears and re-asks. */
    public static void change(Fragment fragment, @Nullable Callback onDone) {
    }

    /** The home screen's start-up dialogs: legacy giftChecking, then makeSureADJD (each shown once). */
    public static void homeChecks(Fragment fragment) {
    }

    /** Legacy alertADJDKadaluarsa: the saved schedule has expired; {@code onClosed} runs when dismissed. */
    public static void alertExpired(Fragment fragment, @Nullable Runnable onClosed) {
    }

    public interface Callback {
        void onDone(boolean selectionSaved);
    }
}
