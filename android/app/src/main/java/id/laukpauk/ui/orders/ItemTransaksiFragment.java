package id.laukpauk.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentItemTransaksiBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;

/** ItemTransaksi as a route (Daftar Belanjaan). Args: idTransaksi, cartCode?. */
public class ItemTransaksiFragment extends Fragment {

    private FragmentItemTransaksiBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentItemTransaksiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        String idTransaksi = requireArguments().getString("idTransaksi", "");
        String cartCode = requireArguments().getString("cartCode");
        AppBars.setup(this, binding.appBar, getString(R.string.orders_daftar_belanjaan));
        ItemTransaksiViewModel vm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ItemTransaksiViewModel(c.transaksiRepository, idTransaksi)))
                .get(ItemTransaksiViewModel.class);
        ItemTransaksiBinder.bind(this, binding.items, vm);
        if (vm.getItems().getValue() == null) {
            vm.load(cartCode);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
