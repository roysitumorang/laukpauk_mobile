package id.laukpauk.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;

import id.laukpauk.databinding.FragmentOTPRegistrasiBinding;

public class OTPRegistrasiFragment extends Fragment {

    private FragmentOTPRegistrasiBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOTPRegistrasiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.konfirmasiButton, binding.konfirmasiSpinner, busy));
        binding.lewatiButton.setOnClickListener(v -> {
            AppContainer.from(requireContext()).session.lewatiOTPRegistrasi();
            AuthFlow.goHome(this);
        });
        binding.konfirmasiButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.konfirmasiOTP(binding.otpCode.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess()) {
                    Messages.snack(this, getString(R.string.auth_gagal_koneksi));
                } else if (Boolean.TRUE.equals(r.data)) {
                    AuthFlow.alert(this, R.string.auth_konfirmasi_otp_aksi, getString(R.string.auth_otp_berhasil),
                            R.string.auth_masuk, () -> AuthFlow.finish(this), 0, null);
                } else {
                    AuthFlow.alert(this, R.string.auth_konfirmasi_otp_aksi, getString(R.string.auth_otp_gagal),
                            R.string.auth_ok, null, 0, null);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
