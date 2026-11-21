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

import id.laukpauk.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private final PhoneNumberHint phoneHint = new PhoneNumberHint(this);
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        phoneHint.attach(binding.phoneNumber);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        AuthFlow.bindPasswordToggle(binding.password, binding.togglePassword);
        binding.registerLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.registerFragment));
        binding.forgotLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.forgotPasswordFragment));
        viewModel.getBusy().observe(getViewLifecycleOwner(), busy -> {
            AuthFlow.setBusy(binding.loginButton, binding.loginSpinner, busy);
            binding.loadingOverlay.setVisibility(busy ? View.VISIBLE : View.GONE);
        });
        binding.loginButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.login(binding.phoneNumber.getText().toString(), binding.password.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess() || r.data == null) {
                    Messages.snack(this, getString(R.string.auth_failed_connection));
                } else if (!r.data.authenticated) {
                    AuthFlow.alert(this, R.string.auth_login_heading, loginFailedText(r.data.message),
                            R.string.auth_forgot_password,
                            () -> NavHostFragment.findNavController(this).navigate(R.id.forgotPasswordFragment),
                            R.string.auth_close, null);
                } else if (!r.data.phoneConfirmed) {
                    // the SMS code of sign-up still has to be entered (it can be skipped there)
                    AuthFlow.replaceWith(this, R.id.registrationOtpFragment);
                } else {
                    AuthFlow.finish(this);
                }
            });
        });
    }

    /** The wrong-credentials text; a locked account says so instead (the backend's message). */
    private String loginFailedText(String message) {
        if (message != null && message.toLowerCase(java.util.Locale.ROOT).contains("lock")) {
            return getString(R.string.auth_login_locked);
        }
        return getString(R.string.auth_login_failed) + "\n\n" + getString(R.string.auth_login_failed_suggestion);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
