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

import id.laukpauk.databinding.FragmentResendOtpBinding;

public class ResendOtpFragment extends Fragment {

    private FragmentResendOtpBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResendOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        viewModel.loadPhoneNumber();
        viewModel.getPhoneNumber().observe(getViewLifecycleOwner(), n -> {
            if (binding.telephoneNumber.getText().length() == 0) {
                binding.telephoneNumber.setText(n);
            }
        });
        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.sendButton, binding.sendSpinner, busy));
        binding.sendButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.resendOTP(binding.telephoneNumber.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess() || r.data == null) {
                    Messages.snack(this, getString(R.string.auth_failed_connection));
                } else if (r.data.statusResendOTP) {
                    AuthFlow.replaceWith(this, R.id.registrationOtpFragment);
                } else {
                    Messages.snack(this, r.data.messageResendOTP == null ? "" : r.data.messageResendOTP);
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
