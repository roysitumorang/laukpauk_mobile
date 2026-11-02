package id.laukpauk.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.laukpauk.databinding.FragmentDoPaymentBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;

/** DoPayment.dart: static instruction to finish the OVO payment in the OVO app. */
public class DoPaymentFragment extends Fragment {

    private FragmentDoPaymentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDoPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            AppContainer.from(requireContext()).cartRepository.clearTransactionSession(res -> { });
        }
        Images.asset(binding.image, "ovo.png");
        binding.btnBelanjaLagi.setOnClickListener(v -> CheckoutNav.goHome(this, false));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
