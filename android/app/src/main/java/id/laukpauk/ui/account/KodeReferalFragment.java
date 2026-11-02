package id.laukpauk.ui.account;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.databinding.FragmentKodeReferalBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;

/** KodeReferal.dart: the user's referral code, copy and share. */
public class KodeReferalFragment extends Fragment {

    private FragmentKodeReferalBinding binding;
    private KodeReferalViewModel viewModel;
    private boolean started;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentKodeReferalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_referal_title));
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new KodeReferalViewModel(
                c.accountRepository, c.userRepository, c.settingRepository))).get(KodeReferalViewModel.class);
        binding.refresh.setOnRefreshListener(viewModel::load);
        started = false;
        AuthGate.require(this, this::start);
    }

    private void start() {
        if (binding == null || started) {
            return;
        }
        started = true;
        viewModel.getConfig().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading()) {
                if (!binding.refresh.isRefreshing()) {
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.noConfig.setVisibility(View.GONE);
                    binding.refresh.setVisibility(View.GONE);
                }
                return;
            }
            binding.progress.setVisibility(View.GONE);
            binding.refresh.setRefreshing(false);
            if (res.isSuccess() && res.data != null) {
                binding.noConfig.setVisibility(View.GONE);
                binding.refresh.setVisibility(View.VISIBLE);
                binding.penjelasan1.setText(getString(R.string.account_referal_penjelasan1,
                        Currency.formatRupiah(res.data, 0)));
            } else {
                binding.noConfig.setVisibility(View.VISIBLE);
                binding.refresh.setVisibility(View.GONE);
            }
        });
        viewModel.getUser().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.codeProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isLoading()) {
                binding.codeBox.setVisibility(View.GONE);
                binding.unknown.setVisibility(View.GONE);
                return;
            }
            final UserDetail user = res.isSuccess() ? res.data : null;
            binding.codeBox.setVisibility(user != null ? View.VISIBLE : View.GONE);
            binding.unknown.setVisibility(user == null ? View.VISIBLE : View.GONE);
            if (user != null) {
                binding.kode.setText(user.kodeReferal);
                binding.salin.setOnClickListener(v -> salin(user.kodeReferal));
                binding.bagikan.setOnClickListener(v -> bagikan(user));
            }
        });
        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    private void salin(String kode) {
        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("Kode Referal", kode));
        }
        Messages.snack(this, getString(R.string.account_referal_tersalin));
    }

    private void bagikan(UserDetail user) {
        viewModel.referralUserWallet(result -> {
            if (!isAdded()) {
                return;
            }
            String nilai = Currency.formatRupiah(result.data == null ? 0 : result.data, 0);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, "Kode Referal " + user.kodeReferal);
            send.putExtra(Intent.EXTRA_TEXT, getString(R.string.account_referal_share, user.nama, user.kodeReferal, nilai));
            startActivity(Intent.createChooser(send, null));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
