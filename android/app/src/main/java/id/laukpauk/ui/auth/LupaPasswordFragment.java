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

import id.laukpauk.databinding.FragmentLupaPasswordBinding;

public class LupaPasswordFragment extends Fragment {

    private FragmentLupaPasswordBinding binding;
    private final PhoneNumberHint phoneHint = new PhoneNumberHint(this);
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLupaPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        phoneHint.attach(binding.nomorHP);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        binding.registerLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.registerFragment));
        binding.loginLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.loginFragment));
        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.resetButton, binding.resetSpinner, busy));
        binding.resetButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.lupaPassword(binding.nomorHP.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess() || r.data == null) {
                    Messages.snack(this, getString(R.string.auth_gagal_koneksi));
                    return;
                }
                if (r.data.statusRecoveryPassword) {
                    AuthFlow.alert(this, R.string.auth_recovery_judul, getString(R.string.auth_recovery_berhasil),
                            R.string.auth_login, () -> AuthFlow.replaceWith(this, R.id.loginFragment),
                            R.string.auth_ok, null);
                } else {
                    String m = r.data.returnData == null || r.data.returnData.message == null
                            ? "" : r.data.returnData.message;
                    AuthFlow.alert(this, R.string.auth_recovery_judul, m, R.string.auth_ok, null, 0, null);
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
