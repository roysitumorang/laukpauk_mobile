package id.laukpauk.ui.voucher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentVoucherBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;

/** Voucher.dart: "Voucher Saya" (signed-in users only). Argument: appBarTitle (optional). */
public class VoucherFragment extends Fragment {

    private FragmentVoucherBinding binding;
    private VoucherViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVoucherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new VoucherViewModel(c.voucherRepository)))
                .get(VoucherViewModel.class);
        String title = requireArguments().getString("appBarTitle");
        AppBars.setup(this, binding.appBar, title != null ? title : getString(R.string.voucher_title_default));
        binding.empty.emptyText.setText(R.string.voucher_empty);

        final VoucherAdapter adapter = new VoucherAdapter(voucher -> {
            Bundle args = new Bundle();
            args.putString("idVoucher", voucher.id);
            NavHostFragment.findNavController(this).navigate(R.id.detailVoucherFragment, args);
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(viewModel::load);

        AuthGate.require(this, () -> {
            viewModel.getVouchers().observe(getViewLifecycleOwner(), res -> {
                if (res == null) {
                    return;
                }
                boolean loading = res.isLoading();
                binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
                if (loading) {
                    return;
                }
                binding.refresh.setRefreshing(false);
                adapter.submitList(res.data);
                boolean empty = res.data == null || res.data.isEmpty();
                binding.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
            });
            viewModel.load();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
