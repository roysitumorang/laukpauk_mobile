package id.laukpauk.ui.account;

import android.content.Intent;
import android.net.Uri;
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

import id.laukpauk.R;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.databinding.FragmentUserAndDashboardBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.ui.common.StatusBar;
import id.laukpauk.util.Currency;

/** UserAndDashboard.dart: the "Akun" bottom-navigation tab. */
public class UserAndDashboardFragment extends Fragment {

    private FragmentUserAndDashboardBinding binding;
    private UserAndDashboardViewModel viewModel;
    private boolean observing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserAndDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StatusBar.green(this);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new UserAndDashboardViewModel(c.userRepository, c.settingRepository)))
                .get(UserAndDashboardViewModel.class);
        binding.header.bind(this);
        bindStaticViews();
        binding.refresh.setOnRefreshListener(viewModel::load);
        NavController nav = NavHostFragment.findNavController(this);
        binding.login.setOnClickListener(v -> nav.navigate(R.id.loginFragment));
        binding.register.setOnClickListener(v -> nav.navigate(R.id.registerFragment));
        NavResults.<Boolean>observe(this, AuthGate.LOGIN_RESULT, success -> {
            if (Boolean.TRUE.equals(success)) {
                enter();
            }
        });
        observing = false;
        enter();
    }

    /** Signed in: observe and (re)load; signed out: the Masuk / Daftar state. */
    private void enter() {
        if (binding == null) {
            return;
        }
        if (!AuthGate.isSignedIn(this)) {
            binding.progress.setVisibility(View.GONE);
            binding.refresh.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            binding.notFoundText.setText(R.string.account_not_logged_in);
            binding.signedOutButtons.setVisibility(View.VISIBLE);
            return;
        }
        binding.signedOutButtons.setVisibility(View.GONE);
        binding.notFoundText.setText(R.string.account_data_not_found);
        if (!observing) {
            observing = true;
            observe();
        }
        viewModel.load();
    }

    private void bindStaticViews() {
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.walletBalanceIcon, "icons/pembeli/dompet.png");
        Images.asset(binding.walletVoucherIcon, "icons/pembeli/voucher.png");
        Images.asset(binding.walletPointsIcon, "poin.png");
        Images.asset(binding.walletReferralIcon, "referal.png");
        Images.asset(binding.todayTrxIcon, "icons/pembeli/belanjaan-hari-ini.png");
        Images.asset(binding.trxHistoryIcon, "icons/pembeli/histori-belanja.png");
        Images.asset(binding.trxAccountIcon, "user-icon.png");

        NavController nav = NavHostFragment.findNavController(this);
        binding.walletBalance.setOnClickListener(v -> nav.navigate(R.id.walletHistoryFragment));
        binding.walletVoucher.setOnClickListener(v -> nav.navigate(R.id.voucherFragment));
        binding.walletPoints.setOnClickListener(v -> nav.navigate(R.id.pointsHistoryFragment));
        binding.walletReferral.setOnClickListener(v -> nav.navigate(R.id.referralCodeFragment));

        binding.todayTrx.setOnClickListener(v -> nav.navigate(R.id.myOrdersFragment));
        binding.trxHistory.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("statusTransaksi", 2);
            nav.navigate(R.id.myOrdersFragment, args);
        });
        binding.trxAccount.setOnClickListener(v -> nav.navigate(R.id.myProfileFragment));

        binding.menuReferral.setOnClickListener(v -> nav.navigate(R.id.referralCodeFragment));
        binding.menuContact.setOnClickListener(v -> nav.navigate(R.id.contactUsFragment));
        binding.menuPrivacy.setOnClickListener(v -> viewModel.privacyPolicyUrl(result -> {
            if (!isAdded()) {
                return;
            }
            String url = result.data == null ? "" : result.data;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (RuntimeException e) {
                Messages.info(this, getString(R.string.account_url_failed_title),
                        getString(R.string.account_url_failed_message, url), null);
            }
        }));
    }

    private void observe() {
        viewModel.getUser().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading()) {
                if (!binding.refresh.isRefreshing() && binding.refresh.getVisibility() != View.VISIBLE) {
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.notFound.setVisibility(View.GONE);
                }
                return;
            }
            binding.progress.setVisibility(View.GONE);
            binding.refresh.setRefreshing(false);
            UserDetail user = res.isSuccess() ? res.data : null;
            binding.notFound.setVisibility(user == null ? View.VISIBLE : View.GONE);
            binding.refresh.setVisibility(user != null ? View.VISIBLE : View.GONE);
            if (user != null) {
                showUser(user);
            }
        });
        viewModel.getWallet().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                binding.walletValue.setText(res.isLoading() ? getString(R.string.account_fetching_data)
                        : Currency.formatRupiah(res.data == null ? 0 : res.data, 0));
            }
        });
        viewModel.getVoucher().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                int n = res.data == null ? 0 : res.data;
                binding.voucherValue.setText(res.isLoading() ? getString(R.string.account_fetching_data)
                        : n >= 1 ? Currency.formatUnit(n, "Voucher") : getString(R.string.account_no_voucher_yet));
            }
        });
        viewModel.getPoints().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                binding.pointsValue.setText(res.isLoading() ? getString(R.string.account_fetching_data)
                        : Currency.formatRupiah(res.data == null ? 0 : res.data, 0));
            }
        });
        viewModel.getReferralWallet().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                binding.referralCodeSub.setText(getString(R.string.account_menu_referral_sub,
                        Currency.formatRupiah(value, 0)));
            }
        });
    }

    private void showUser(UserDetail user) {
        Images.circle(binding.photo, Images.user(user.photo));
        binding.name.setText(user.name);
        binding.referralCode.setText(user.referralCode);
        boolean hasAddress = user.address != null && user.address.length() >= 1;
        binding.address.setText(hasAddress ? user.address : getString(R.string.account_address_unset));
        binding.address.setTypeface(null, hasAddress ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.ITALIC);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
