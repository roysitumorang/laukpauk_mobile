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
import id.laukpauk.util.ServerPath;

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
        binding.masuk.setOnClickListener(v -> nav.navigate(R.id.loginFragment));
        binding.daftar.setOnClickListener(v -> nav.navigate(R.id.registerFragment));
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
            binding.notFoundText.setText(R.string.account_belum_masuk);
            binding.signedOutButtons.setVisibility(View.VISIBLE);
            return;
        }
        binding.signedOutButtons.setVisibility(View.GONE);
        binding.notFoundText.setText(R.string.account_data_tidak_ditemukan);
        if (!observing) {
            observing = true;
            observe();
        }
        viewModel.load();
    }

    private void bindStaticViews() {
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.walletDompetIcon, "icons/pembeli/dompet.png");
        Images.asset(binding.walletVoucherIcon, "icons/pembeli/voucher.png");
        Images.asset(binding.walletPoinIcon, "poin.png");
        Images.asset(binding.walletReferalIcon, "referal.png");
        Images.asset(binding.trxHariIniIcon, "icons/pembeli/belanjaan-hari-ini.png");
        Images.asset(binding.trxHistoriIcon, "icons/pembeli/histori-belanja.png");
        Images.asset(binding.trxAkunIcon, "user-icon.png");
        Images.asset(binding.trxTokoIcon, "store.png");

        NavController nav = NavHostFragment.findNavController(this);
        binding.walletDompet.setOnClickListener(v -> nav.navigate(R.id.historiDompetFragment));
        binding.walletVoucher.setOnClickListener(v -> nav.navigate(R.id.voucherFragment));
        binding.walletPoin.setOnClickListener(v -> nav.navigate(R.id.historiPoinFragment));
        binding.walletReferal.setOnClickListener(v -> nav.navigate(R.id.kodeReferalFragment));

        binding.trxHariIni.setOnClickListener(v -> nav.navigate(R.id.pesananSayaFragment));
        binding.trxHistori.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("statusTransaksi", 2);
            nav.navigate(R.id.pesananSayaFragment, args);
        });
        binding.trxAkun.setOnClickListener(v -> nav.navigate(R.id.profilSayaFragment));

        binding.menuReferal.setOnClickListener(v -> nav.navigate(R.id.kodeReferalFragment));
        binding.menuHubungi.setOnClickListener(v -> nav.navigate(R.id.hubungiKamiFragment));
        binding.menuPrivasi.setOnClickListener(v -> viewModel.kebijakanPrivasiUrl(result -> {
            if (!isAdded()) {
                return;
            }
            String url = result.data == null ? "" : result.data;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (RuntimeException e) {
                Messages.info(this, getString(R.string.account_url_gagal_title),
                        getString(R.string.account_url_gagal_message, url), null);
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
        viewModel.getDompet().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                binding.dompetValue.setText(res.isLoading() ? getString(R.string.account_mengambil_data)
                        : Currency.formatRupiah(res.data == null ? 0 : res.data, 0));
            }
        });
        viewModel.getVoucher().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                int n = res.data == null ? 0 : res.data;
                binding.voucherValue.setText(res.isLoading() ? getString(R.string.account_mengambil_data)
                        : n >= 1 ? Currency.formatSatuan(n, "Voucher") : getString(R.string.account_belum_ada_voucher));
            }
        });
        viewModel.getPoin().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                binding.poinValue.setText(res.isLoading() ? getString(R.string.account_mengambil_data)
                        : Currency.formatRupiah(res.data == null ? 0 : res.data, 0));
            }
        });
        viewModel.getReferralWallet().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                binding.kodeReferalSub.setText(getString(R.string.account_menu_referal_sub,
                        Currency.formatRupiah(value, 0)));
            }
        });
    }

    private void showUser(UserDetail user) {
        Images.circle(binding.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarUser + "/" + user.foto);
        binding.nama.setText(user.nama);
        binding.kodeReferal.setText(user.kodeReferal);
        boolean hasAlamat = user.alamat != null && user.alamat.length() >= 1;
        binding.alamat.setText(hasAlamat ? user.alamat : getString(R.string.account_alamat_belum));
        binding.alamat.setTypeface(null, hasAlamat ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.ITALIC);
        binding.trxToko.setOnClickListener(v -> {
            if (user.isSeller) {
                AccountNav.toTokoSaya(this);
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.requestJadiPenjualFragment);
            }
        });
        binding.trxTokoLabel.setText(user.isSeller ? R.string.account_trx_toko_saya : R.string.account_trx_jualan);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
