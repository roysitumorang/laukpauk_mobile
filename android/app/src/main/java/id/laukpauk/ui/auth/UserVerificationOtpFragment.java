package id.laukpauk.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentUserVerificationOtpBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;

/** OTPVerifikasiUser.dart: the verification code. */
public class UserVerificationOtpFragment extends Fragment {

    private FragmentUserVerificationOtpBinding binding;
    private UserVerificationOtpViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserVerificationOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new UserVerificationOtpViewModel(c.authRepository)))
                .get(UserVerificationOtpViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");

        binding.verificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateForm();
            }
        });

        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.verificationButton, binding.verificationSpinner, busy));
        binding.verificationButton.setOnClickListener(v -> viewModel.verify(
                binding.verificationCode.getText().toString(), r -> {
                    if (binding == null) {
                        return;
                    }
                    if (!r.isSuccess()) {
                        Messages.snack(this, getString(R.string.auth_failed_connection));
                    } else if (Boolean.TRUE.equals(r.data)) {
                        AuthFlow.finish(this);
                    } else {
                        AuthFlow.alert(this, R.string.auth_verification_heading, getString(R.string.auth_verification_failed),
                                R.string.auth_ok, null, 0, null);
                    }
                }));
        binding.and.setText(R.string.auth_verification_message);
        updateForm();
    }

    /** The parts of build() that depend on the state. */
    private void updateForm() {
        if (binding == null) {
            return;
        }
        boolean complete = viewModel.isFormComplete(binding.verificationCode.getText().length());
        binding.buttonHolder.setVisibility(complete ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
