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

import id.laukpauk.databinding.FragmentRegistrationOtpBinding;

public class RegistrationOtpFragment extends Fragment {

    private FragmentRegistrationOtpBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.confirmButton, binding.confirmSpinner, busy));
        binding.skipButton.setOnClickListener(v -> {
            AppContainer.from(requireContext()).session.skipRegistrationOtp();
            AuthFlow.goHome(this);
        });
        binding.confirmButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.confirmOtp(binding.otpCode.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess()) {
                    Messages.snack(this, getString(R.string.auth_failed_connection));
                } else if (Boolean.TRUE.equals(r.data)) {
                    AuthFlow.alert(this, R.string.auth_confirm_otp_action, getString(R.string.auth_otp_success),
                            R.string.auth_sign_in, () -> AuthFlow.finish(this), 0, null);
                } else {
                    AuthFlow.alert(this, R.string.auth_confirm_otp_action, getString(R.string.auth_otp_failed),
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
