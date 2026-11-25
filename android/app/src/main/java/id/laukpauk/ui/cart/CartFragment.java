package id.laukpauk.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.CartGift;
import id.laukpauk.data.model.cart.BuyerCart;
import id.laukpauk.data.model.cart.BuyerCartItem;
import id.laukpauk.databinding.FragmentCartBinding;
import id.laukpauk.databinding.ItemCartGiftBinding;
import id.laukpauk.data.repository.CheckoutRepository;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.product.ProductActions;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Currency;

/**
 * Keranjang: the buyer's cart from {@code GET buyer/cart} (lines, notes, subtotal), quantity and note changes, delete and
 * "Bersihkan Keranjang" through {@code POST buyer/cart}, checkout, and earned gifts.
 */
public class CartFragment extends Fragment {

    /** The subtotal on screen (the buyer cart's total); checkout needs it above 0. */
    private long shownTotal;

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new CartViewModel(c.cartRepository)))
                .get(CartViewModel.class);
        AppBars.setup(this, binding.appBar, getString(R.string.cart_title));
        final NavController nav = NavHostFragment.findNavController(this);

        adapter = new CartProductAdapter(new CartProductAdapter.Listener() {
            @Override
            public void onOpenProduct(BuyerCartItem item) {
                if (item.productId != null) {
                    ProductActions.open(CartFragment.this, item.productId);
                }
            }

            @Override
            public void onEditQuantity(BuyerCartItem item) {
                editQuantity(item);
            }

            @Override
            public void onDelete(BuyerCartItem item) {
                confirmDelete(item);
            }

            @Override
            public void onEditNote(BuyerCartItem item) {
                editNote(item);
            }
        });
        binding.products.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.products.setAdapter(adapter);
        Images.asset(binding.emptyImage, "empty-cart.png");

        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(viewModel::load);
        binding.checkout.setOnClickListener(v -> onCheckout(nav));
        binding.clear.setOnClickListener(v -> confirmClear());
        binding.shop.setOnClickListener(v -> {
            if (!nav.popBackStack(R.id.homePageFragment, false)) {
                nav.navigate(R.id.homePageFragment);
            }
        });

        NavResults.<Boolean>observe(this, AuthGate.LOGIN_RESULT, success -> {
            if (!Boolean.TRUE.equals(success)) {
                return;
            }
            viewModel.load(); // the signed-in buyer's cart
            if (viewModel.checkoutAfterLogin) {
                viewModel.checkoutAfterLogin = false;
                if (phoneConfirmed()) {
                    openCheckout(nav); // the next checkout step: the delivery address
                } else {
                    // "Lewati" on the confirmation screen: no checkout without a confirmed phone
                    Messages.snack(this, getString(R.string.cart_confirm_phone_first));
                }
            }
        });

        observe();
        viewModel.load();
    }

    private void observe() {
        viewModel.getCart().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
            binding.totalProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                return;
            }
            binding.refresh.setRefreshing(false);
            if (!res.isSuccess()) {
                Messages.snack(this, getString(R.string.cart_load_failed));
            }
            BuyerCart cart = res.isSuccess() ? res.data : null;
            List<BuyerCartItem> items = new ArrayList<>();
            if (cart != null && cart.items != null) {
                for (BuyerCartItem item : cart.items) {
                    if (item != null && item.quantity > 0) {
                        items.add(item);
                    }
                }
            }
            boolean any = !items.isEmpty();
            adapter.submitList(items);
            shownTotal = cart == null ? 0 : cart.total();
            binding.total.setText(Currency.formatRupiah(shownTotal, 0));
            binding.total.setVisibility(View.VISIBLE);
            binding.productsCard.setVisibility(any ? View.VISIBLE : View.GONE);
            binding.summaryGroup.setVisibility(any ? View.VISIBLE : View.GONE);
            binding.emptyGroup.setVisibility(any ? View.GONE : View.VISIBLE);
        });
        viewModel.getGifts().observe(getViewLifecycleOwner(), res -> {
            if (res == null || !res.isSuccess()) {
                return;
            }
            binding.giftList.removeAllViews();
            List<CartGift> gifts = res.data;
            boolean any = gifts != null && !gifts.isEmpty();
            binding.giftCard.setVisibility(any ? View.VISIBLE : View.GONE);
            if (any) {
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                for (CartGift gift : gifts) {
                    ItemCartGiftBinding gb = ItemCartGiftBinding.inflate(inflater, binding.giftList, false);
                    gb.name.setText(getString(R.string.cart_gift_pcs, gift.name == null ? "" : gift.name));
                    gb.description.setText(gift.description == null ? "" : gift.description);
                    binding.giftList.addView(gb.getRoot());
                }
            }
        });
    }

    /**
     * Checkout: a signed-out buyer logs in first (Login opens the phone confirmation when needed); a signed-in buyer
     * whose phone is not confirmed confirms it first. Both come back here through LOGIN_RESULT and continue with
     * the delivery address.
     */
    private void onCheckout(NavController nav) {
        if (!CheckoutRepository.hasPositiveTotal(shownTotal)) {
            Messages.snack(this, getString(R.string.checkout_insufficient));
            return;
        }
        if (!AuthGate.isSignedIn(this)) {
            viewModel.checkoutAfterLogin = true;
            nav.navigate(R.id.loginFragment);
        } else if (!phoneConfirmed()) {
            viewModel.checkoutAfterLogin = true;
            nav.navigate(R.id.registrationOtpFragment);
        } else {
            openCheckout(nav);
        }
    }

    private boolean phoneConfirmed() {
        return AppContainer.from(requireContext()).session.isMobilePhoneConfirmed();
    }

    /** Checkout is where the delivery address and schedule are required: asks for them first when none is set. */
    private void openCheckout(NavController nav) {
        DeliveryFlow.ensureForCheckout(this, () -> {
            if (isAdded()) {
                nav.navigate(R.id.checkoutFragment);
            }
        });
    }

    /** The quantity dialog of a line: a whole number from 1; 0 asks to delete the line. */
    private void editQuantity(BuyerCartItem item) {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.quantity));
        input.setSelectAllOnFocus(true);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout box = new FrameLayout(requireContext());
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.productName == null ? getString(R.string.cart_quantity) : item.productName)
                .setMessage(getString(R.string.cart_quantity_hint, item.stockUnit == null ? "" : item.stockUnit))
                .setView(box)
                .setNegativeButton(R.string.cart_no, null)
                .setPositiveButton(R.string.cart_save, (d, w) -> {
                    long quantity = parseQuantity(input.getText().toString());
                    if (quantity == item.quantity || quantity < 0) {
                        return;
                    }
                    if (quantity == 0) {
                        confirmDelete(item);
                    } else {
                        viewModel.setQuantity(item.productVariantId, quantity, this::onSaved);
                    }
                })
                .show();
    }

    /** "Tambah Catatan" / the note's pencil: a multi-line text dialog; an empty note removes it. */
    private void editNote(BuyerCartItem item) {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setMinLines(2);
        input.setHint(R.string.cart_note_input);
        final String current = item.note == null ? "" : item.note.trim();
        input.setText(current);
        input.setSelection(current.length());
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout box = new FrameLayout(requireContext());
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.productName == null ? getString(R.string.cart_note_input) : item.productName)
                .setView(box)
                .setNegativeButton(R.string.cart_no, null)
                .setPositiveButton(R.string.cart_save, (d, w) -> {
                    String note = input.getText().toString().trim();
                    if (!note.equals(current)) {
                        viewModel.setNote(item.productVariantId, note, this::onSaved);
                    }
                })
                .show();
    }

    private static long parseQuantity(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void confirmDelete(BuyerCartItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cart_delete_title)
                .setMessage(getString(R.string.cart_delete_message, item.productName == null ? "" : item.productName))
                .setNegativeButton(R.string.cart_no, null)
                .setPositiveButton(R.string.cart_yes, (d, w) ->
                        viewModel.setQuantity(item.productVariantId, 0, this::onSaved))
                .show();
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cart_clear_title)
                .setMessage(R.string.cart_clear_message)
                .setNegativeButton(R.string.cart_no, null)
                .setPositiveButton(R.string.cart_clear_yes, (d, w) -> viewModel.clearCart(this::onSaved))
                .show();
    }

    /** After {@code POST buyer/cart}: the saved cart is already shown; a failure shows the backend's reason. */
    private void onSaved(Resource<BuyerCart> result) {
        if (!isAdded() || result.isSuccess()) {
            return;
        }
        String reason = result.message == null ? "" : result.message.trim();
        Messages.snack(this, getString(R.string.cart_edit_failed, reason));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
