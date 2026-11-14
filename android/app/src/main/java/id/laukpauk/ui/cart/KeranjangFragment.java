package id.laukpauk.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.CartGift;
import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.data.model.cart.WalletConfiguration;
import id.laukpauk.databinding.FragmentKeranjangBinding;
import id.laukpauk.databinding.ItemCartGiftBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Currency;

/** Keranjang.dart: the cart lines, subtotal, checkout, "Bersihkan Keranjang" and earned gifts. */
public class KeranjangFragment extends Fragment {

    /** Result of EditKeranjangItemFragment: Boolean, true when the item was saved. */
    public static final String EDIT_RESULT = "edit_keranjang_item_result";

    private FragmentKeranjangBinding binding;
    private KeranjangViewModel viewModel;
    private CartProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentKeranjangBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new KeranjangViewModel(c.cartRepository)))
                .get(KeranjangViewModel.class);
        AppBars.setup(this, binding.appBar, getString(R.string.cart_title));
        final NavController nav = NavHostFragment.findNavController(this);

        adapter = new CartProductAdapter(new CartProductAdapter.Listener() {
            @Override
            public void onOpenProduct(CartProduct product) {
                String jenis = product.jenisProduk == null ? "" : product.jenisProduk.toLowerCase();
                Bundle args = new Bundle();
                if (jenis.equals("nonpaket")) {
                    args.putString("idProduk", product.idProduk);
                    nav.navigate(R.id.detailProdukFragment, args);
                } else if (jenis.equals("paket")) {
                    args.putString("idPaket", product.idProduk);
                    nav.navigate(R.id.detailPaketFragment, args);
                }
            }

            @Override
            public void onEdit(CartProduct product, int failMessage, int failButton) {
                viewModel.pendingEditMessage = failMessage;
                viewModel.pendingEditButton = failButton;
                Bundle args = new Bundle();
                args.putString("dataKeranjangItem", c.apiClient.toJson(product, CartProduct.class));
                nav.navigate(R.id.editKeranjangItemFragment, args);
            }

            @Override
            public void onDelete(CartProduct product) {
                confirmDelete(product);
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

        NavResults.<Boolean>observe(this, EDIT_RESULT, saved -> {
            if (Boolean.TRUE.equals(saved)) {
                viewModel.load();
            } else if (saved != null) {
                showEditFailure();
            }
        });
        NavResults.<Boolean>observe(this, AuthGate.LOGIN_RESULT, success -> {
            if (Boolean.TRUE.equals(success) && viewModel.checkoutAfterLogin) {
                viewModel.checkoutAfterLogin = false;
                openCheckout(nav);
            }
        });

        observe();
        viewModel.load();
    }

    private void observe() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
            if (loading) {
                return;
            }
            binding.refresh.setRefreshing(false);
            List<CartProduct> list = res.data;
            boolean any = list != null && !list.isEmpty();
            adapter.submitList(list);
            binding.productsCard.setVisibility(any ? View.VISIBLE : View.GONE);
            binding.summaryGroup.setVisibility(any ? View.VISIBLE : View.GONE);
            binding.emptyGroup.setVisibility(any ? View.GONE : View.VISIBLE);
        });
        viewModel.getTotal().observe(getViewLifecycleOwner(), res -> {
            if (res != null && res.isSuccess() && res.data != null) {
                binding.totalProgress.setVisibility(View.GONE);
                binding.total.setText(Currency.formatRupiah(res.data, 0));
                binding.total.setVisibility(View.VISIBLE);
            } else if (res != null && res.isLoading()) {
                binding.totalProgress.setVisibility(View.VISIBLE);
                binding.total.setVisibility(View.GONE);
            }
        });
        viewModel.getWallet().observe(getViewLifecycleOwner(), this::renderWallet);
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
                    gb.nama.setText(getString(R.string.cart_gift_pcs, gift.nama == null ? "" : gift.nama));
                    gb.deskripsi.setText(gift.deskripsi == null ? "" : gift.deskripsi);
                    binding.giftList.addView(gb.getRoot());
                }
            }
        });
    }

    private void renderWallet(@Nullable Resource<WalletConfiguration> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.walletProgress.setVisibility(View.VISIBLE);
            binding.walletText.setVisibility(View.GONE);
            return;
        }
        binding.walletProgress.setVisibility(View.GONE);
        WalletConfiguration w = res.data;
        if (!res.isSuccess() || w == null) {
            binding.walletText.setText(R.string.cart_wallet_failed);
            binding.walletText.setTextSize(14f);
            binding.walletText.setTypeface(null, android.graphics.Typeface.NORMAL);
            binding.walletText.setGravity(android.view.Gravity.START);
            binding.walletText.setVisibility(View.VISIBLE);
        } else if (w.isWalletOn) {
            binding.walletText.setText(getString(R.string.cart_wallet_discount,
                    Currency.formatRupiah(CartProduct.toInt(w.walletDiscount), 0),
                    Currency.formatRupiah(CartProduct.toInt(w.minGrandTotalTransactionForWallet), 0)));
            binding.walletText.setTextSize(12f);
            binding.walletText.setTypeface(null, android.graphics.Typeface.ITALIC);
            binding.walletText.setGravity(android.view.Gravity.CENTER);
            binding.walletText.setVisibility(View.VISIBLE);
        } else {
            binding.walletText.setVisibility(View.GONE);
        }
    }

    private void onCheckout(NavController nav) {
        if (AuthGate.isSignedIn(this)) {
            openCheckout(nav);
        } else {
            viewModel.checkoutAfterLogin = true;
            nav.navigate(R.id.loginFragment);
        }
    }

    /** Checkout is where the delivery address and schedule are required: asks for them first when none is set. */
    private void openCheckout(NavController nav) {
        DeliveryFlow.ensureForCheckout(this, () -> {
            if (isAdded()) {
                nav.navigate(R.id.checkoutFragment);
            }
        });
    }

    private void confirmDelete(CartProduct product) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cart_delete_title)
                .setMessage(getString(R.string.cart_delete_message, product.namaProduk))
                .setNegativeButton(R.string.cart_no, null)
                .setPositiveButton(R.string.cart_yes, (d, w) -> viewModel.deleteItem(product.idCart, result -> {
                    if (isAdded() && result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                        viewModel.load();
                    }
                }))
                .show();
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cart_clear_title)
                .setMessage(R.string.cart_clear_message)
                .setNegativeButton(R.string.cart_no, null)
                .setPositiveButton(R.string.cart_clear_yes, (d, w) -> viewModel.clearCart(result -> {
                    if (isAdded() && result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                        viewModel.load();
                    }
                }))
                .show();
    }

    private void showEditFailure() {
        if (viewModel.pendingEditMessage == 0) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cart_title)
                .setMessage(viewModel.pendingEditMessage)
                .setCancelable(false)
                .setPositiveButton(viewModel.pendingEditButton, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
