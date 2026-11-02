package id.laukpauk.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * The green buyer header (legacy ui/Header): "Diantarin ke mana ?" + address pill (shown when a delivery
 * selection is set; tap = DeliveryFlow.change), a search field (tap = searchFragment), cart and bell
 * buttons with badges (cartRepository.cartCount(), notifikasiRepository.unreadCount()).
 * Use it in a layout as {@code <id.laukpauk.ui.common.HeaderView android:id="@+id/header" .../>}.
 *
 * PLACEHOLDER (contract only) - implemented by the home slice. Keep these signatures.
 */
public class HeaderView extends LinearLayout {

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /** Binds the header to a screen: observes the badges/address with the owner's view lifecycle. */
    public void bind(Fragment owner) {
        bind(owner, false, null);
    }

    /**
     * @param askDeliveryOnStart legacy Header(askADJD: true): runs DeliveryFlow.ask once when first bound.
     * @param onDeliverySet      runs after that flow (or a change from the address pill) completed.
     */
    public void bind(Fragment owner, boolean askDeliveryOnStart, @Nullable Runnable onDeliverySet) {
    }
}
