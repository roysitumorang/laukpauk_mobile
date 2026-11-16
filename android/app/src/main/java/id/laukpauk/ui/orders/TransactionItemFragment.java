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
import id.laukpauk.databinding.FragmentTransactionItemBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;

/** TransactionItem as a route (Daftar Belanjaan). Args: transactionId, cartCode?. */
public class TransactionItemFragment extends Fragment {

    private FragmentTransactionItemBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        String transactionId = requireArguments().getString("idTransaksi", "");
        String cartCode = requireArguments().getString("cartCode");
        AppBars.setup(this, binding.appBar, getString(R.string.orders_shopping_list));
        TransactionItemViewModel vm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new TransactionItemViewModel(c.transactionRepository, transactionId)))
                .get(TransactionItemViewModel.class);
        TransactionItemBinder.bind(this, binding.items, vm);
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
