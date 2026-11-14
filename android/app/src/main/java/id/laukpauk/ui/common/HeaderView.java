package id.laukpauk.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.databinding.ViewHomeHeaderBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.delivery.DeliveryFlow;

/**
 * The green buyer header (legacy ui/Header): "Diantarin ke mana ?" + address pill (shown when a delivery
 * selection is set; tap = DeliveryFlow.change), a search field (tap = searchFragment), cart and bell
 * buttons with badges (cartRepository.cartCount(), notifikasiRepository.unreadCount()).
 * Use it in a layout as {@code <id.laukpauk.ui.common.HeaderView android:id="@+id/header" .../>}.
 */
public class HeaderView extends LinearLayout {

    private static final int ALAMAT_MAX = 30;

    private final ViewHomeHeaderBinding binding;
    private Fragment owner;

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        binding = ViewHomeHeaderBinding.inflate(LayoutInflater.from(context), this, true);
    }

    /** Binds the header to a screen: observes the badges/address with the owner's view lifecycle. */
    public void bind(Fragment owner) {
        this.owner = owner;
        AppContainer c = AppContainer.from(getContext());
        LifecycleOwner lifecycle = owner.getViewLifecycleOwner();
        StatusBar.green(owner);

        c.deliveryRepository.selection().observe(lifecycle, this::showSelection);
        c.cartRepository.cartCount().observe(lifecycle, n -> showBadge(binding.cartBadge, n));
        c.notifikasiRepository.unreadCount().observe(lifecycle, n -> showBadge(binding.bellBadge, n));
        c.cartRepository.refreshCartCount();
        c.notifikasiRepository.refreshUnreadCount();

        binding.alamat.setOnClickListener(v -> DeliveryFlow.change(owner, null));
        binding.searchField.setOnClickListener(v ->
                NavHostFragment.findNavController(owner).navigate(R.id.searchFragment));
        binding.cartButton.setOnClickListener(v ->
                NavHostFragment.findNavController(owner).navigate(R.id.keranjangFragment));
        binding.bellButton.setOnClickListener(v ->
                NavHostFragment.findNavController(owner).navigate(R.id.notifikasiFragment));
    }

    private void showSelection(@Nullable DeliverySelection selection) {
        boolean set = selection != null && selection.isSet;
        binding.deliveryRow.setVisibility(set ? View.VISIBLE : View.GONE);
        String alamat = selection == null || selection.alamat == null ? "" : selection.alamat;
        binding.alamat.setText(alamat.length() >= ALAMAT_MAX
                ? getContext().getString(R.string.home_alamat_lanjut, alamat.substring(0, ALAMAT_MAX)) : alamat);
    }

    private void showBadge(TextView badge, @Nullable Integer count) {
        int n = count == null ? 0 : count;
        badge.setText(n >= 10 ? getContext().getString(R.string.home_badge_9plus) : String.valueOf(n));
    }
}
