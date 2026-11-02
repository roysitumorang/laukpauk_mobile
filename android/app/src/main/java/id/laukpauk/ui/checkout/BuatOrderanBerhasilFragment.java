package id.laukpauk.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.data.model.payment.ReturnedDataFromCheckout;
import id.laukpauk.databinding.FragmentBuatOrderanBerhasilBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;

/**
 * BuatOrderanBerhasil.dart. Arguments: {@code returnedDataFromCheckout} (JSON text), {@code removeTransactionSession}.
 */
public class BuatOrderanBerhasilFragment extends Fragment {

    private FragmentBuatOrderanBerhasilBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBuatOrderanBerhasilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        ReturnedDataFromCheckout data = c.apiClient.fromJson(
                requireArguments().getString("returnedDataFromCheckout"), ReturnedDataFromCheckout.class);
        if (savedInstanceState == null && requireArguments().getBoolean("removeTransactionSession", false)) {
            c.cartRepository.clearTransactionSession(res -> { });
        }
        Images.asset(binding.image, "buatOrderanBerhasil.png");
        binding.btnBelanjaLagi.setOnClickListener(v -> CheckoutNav.goHome(this, false));
        binding.btnDetail.setOnClickListener(v -> {
            String id = data != null && data.responseCheckout != null ? data.responseCheckout.idTransaksi : null;
            Bundle args = new Bundle();
            args.putString("idTransaksi", id == null ? "" : id);
            NavHostFragment.findNavController(this).navigate(R.id.detailTransaksiFragment, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
