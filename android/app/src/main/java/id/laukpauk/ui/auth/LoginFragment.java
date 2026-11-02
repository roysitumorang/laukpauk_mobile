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

import id.laukpauk.databinding.FragmentLoginBinding;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.NavResults;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
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
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new AuthViewModel(c.authRepository))).get(AuthViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        NavResults.setResult(this, AuthGate.LOGIN_RESULT, false);
        AuthFlow.bindPasswordToggle(binding.password, binding.togglePassword);
        binding.registerLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.registerFragment));
        binding.lupaLink.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.lupaPasswordFragment));
        viewModel.getBusy().observe(getViewLifecycleOwner(), busy -> {
            AuthFlow.setBusy(binding.loginButton, binding.loginSpinner, busy);
            binding.loadingOverlay.setVisibility(busy ? View.VISIBLE : View.GONE);
        });
        binding.loginButton.setOnClickListener(v -> {
            if (viewModel.isBusy()) {
                return;
            }
            viewModel.login(binding.nomorHP.getText().toString(), binding.password.getText().toString(), r -> {
                if (binding == null) {
                    return;
                }
                if (!r.isSuccess() || r.data == null) {
                    Messages.snack(this, getString(R.string.auth_gagal_koneksi));
                } else if (!r.data.authenticated) {
                    AuthFlow.alert(this, R.string.auth_login_judul,
                            getString(R.string.auth_login_gagal) + "\n\n" + getString(R.string.auth_login_gagal_saran),
                            R.string.auth_lupa_password,
                            () -> NavHostFragment.findNavController(this).navigate(R.id.lupaPasswordFragment),
                            R.string.auth_tutup, null);
                } else if (!r.data.verified) {
                    AuthFlow.replaceWith(this, R.id.oTPVerifikasiUserFragment);
                } else {
                    AuthFlow.finish(this);
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
