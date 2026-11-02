package id.laukpauk.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentOTPNotCompletedBinding;
import id.laukpauk.ui.common.Images;

/** OTPNotCompleted.dart: shown when an action needs a confirmed OTP. */
public class OTPNotCompletedFragment extends Fragment {

    private FragmentOTPNotCompletedBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOTPNotCompletedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Images.asset(binding.emptyImage, "empty.png");
        binding.verifikasiButton.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.oTPRegistrasiFragment));
        binding.kirimUlangButton.setOnClickListener(v -> AuthFlow.replaceWith(this, R.id.kirimUlangOTPFragment));
        binding.homeButton.setOnClickListener(v -> AuthFlow.goHome(this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
