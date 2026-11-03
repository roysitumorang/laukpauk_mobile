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
import id.laukpauk.ui.common.PhoneNumberHint;
import id.laukpauk.ui.common.Messages;

import id.laukpauk.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private final PhoneNumberHint phoneHint = new PhoneNumberHint(this);
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        phoneHint.attach(binding.nomorHP);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        AuthFlow.bindPasswordToggle(binding.password, binding.togglePassword);
        AuthFlow.bindPasswordToggle(binding.konfirmasiPassword, binding.toggleKonfirmasi);
        binding.loginLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.loginFragment));
        binding.lupaLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.lupaPasswordFragment));
        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.registerButton, binding.registerSpinner, busy));
        binding.registerButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.register(binding.nama.getText().toString(), binding.nomorHP.getText().toString(),
                    binding.password.getText().toString(), binding.konfirmasiPassword.getText().toString(),
                    binding.kodeReferal.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess() || r.data == null) {
                    Messages.snack(this, getString(R.string.auth_gagal_koneksi));
                    return;
                }
                boolean failed = r.data.failed;
                String message = r.data.success ? getString(R.string.auth_register_berhasil)
                        : getString(R.string.auth_register_gagal, r.data.message);
                AuthFlow.alert(this, R.string.auth_register_judul, message,
                        failed ? R.string.auth_ok : R.string.auth_konfirmasi_otp_aksi,
                        failed ? null : () -> NavHostFragment.findNavController(this)
                                .navigate(R.id.oTPRegistrasiFragment), 0, null);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
