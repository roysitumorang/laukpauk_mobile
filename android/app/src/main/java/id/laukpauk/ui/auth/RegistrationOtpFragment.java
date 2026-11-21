package id.laukpauk.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

/**
 * Confirms the buyer's mobile phone with the 6-digit code the backend sends by SMS at sign-up
 * ({@code POST buyer/users/mobile_phone/confirm}); shown after registering, and after a login while the phone is
 * not confirmed yet. "Lewati" keeps the buyer signed in.
 */
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
        // "Lewati": optional, stay signed in without confirming now (checkout goes on); the next login asks again.
        binding.skipButton.setOnClickListener(v -> {
            AppContainer.from(requireContext()).session.skipRegistrationOtp();
            AuthFlow.finish(this);
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
                    String reason = r.message == null ? "" : r.message.trim();
                    Messages.snack(this, reason.isEmpty() ? getString(R.string.auth_failed_connection) : reason);
                } else if (Boolean.TRUE.equals(r.data)) {
                    // confirmed: close this screen and go on with what the buyer was doing (e.g. the next
                    // checkout step, the delivery address)
                    Toast.makeText(requireContext(), R.string.auth_otp_success, Toast.LENGTH_SHORT).show();
                    AuthFlow.finish(this);
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
