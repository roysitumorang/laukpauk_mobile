package id.laukpauk.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RoPenjual;
import id.laukpauk.databinding.FragmentRepeatOrderBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.delivery.DeliveryFlow;

/** RepeatOrder.dart. Args: idTransaksi. */
public class RepeatOrderFragment extends Fragment {

    private FragmentRepeatOrderBinding binding;
    private RepeatOrderViewModel viewModel;
    private RepeatOrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRepeatOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        String idTransaksi = requireArguments().getString("idTransaksi", "");
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new RepeatOrderViewModel(
                c.transaksiRepository, c.repeatOrderRepository, c.deliveryRepository, c.cartRepository,
                idTransaksi))).get(RepeatOrderViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.promptImage, "adjd-belum-ditentukan.png");
        adapter = new RepeatOrderAdapter(viewModel.getSelected(), viewModel::toggle);
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(() -> viewModel.refresh());
        binding.btnAdjd.setOnClickListener(v -> DeliveryFlow.ask(this, null));
        binding.fab.setOnClickListener(v -> viewModel.addToCart(result -> {
            if (!isAdded()) {
                return;
            }
            boolean ok = result.isSuccess() && result.data != null && result.data.statusAddCart;
            String text = getString(ok ? R.string.orders_ro_berhasil : R.string.orders_ro_gagal);
            if (result.data != null && result.data.message != null) {
                text = text + " " + result.data.message;
            }
            if (ok) {
                Messages.cartSnack(this, text);
            } else {
                Messages.snack(this, text);
            }
        }));

        viewModel.getDetail().observe(getViewLifecycleOwner(), res -> {
            if (res == null || res.isLoading()) {
                return;
            }
            binding.progress.setVisibility(View.GONE);
            binding.body.setVisibility(View.VISIBLE);
            String nomor = "#";
            if (res.isSuccess() && res.data != null) {
                nomor = "#" + OrderUi.nz(res.data.nomorTransaksi);
            }
            binding.nomorTransaksi.setText(getString(R.string.orders_nomor_transaksi_format, nomor));
        });
        viewModel.getSelection().observe(getViewLifecycleOwner(), this::onSelection);
        viewModel.getItems().observe(getViewLifecycleOwner(), this::onItems);
        viewModel.getSelectedCount().observe(getViewLifecycleOwner(), n ->
                binding.fab.setVisibility(n != null && n >= 1 ? View.VISIBLE : View.GONE));
        viewModel.getProcessing().observe(getViewLifecycleOwner(), busy -> {
            boolean b = Boolean.TRUE.equals(busy);
            binding.fabProgress.setVisibility(b ? View.VISIBLE : View.GONE);
            binding.fabIcon.setVisibility(b ? View.GONE : View.VISIBLE);
        });
    }

    private void onSelection(DeliverySelection s) {
        boolean set = s != null && s.isSet;
        binding.promptGroup.setVisibility(set ? View.GONE : View.VISIBLE);
        binding.refresh.setVisibility(set ? View.VISIBLE : View.GONE);
        if (!set) {
            binding.itemsProgress.setVisibility(View.GONE);
            binding.emptyGroup.setVisibility(View.GONE);
            binding.fab.setVisibility(View.GONE);
            return;
        }
        viewModel.loadItemsIfNeeded(s);
    }

    private void onItems(Resource<List<RoPenjual>> res) {
        DeliverySelection s = viewModel.getSelection().getValue();
        if (res == null || s == null || !s.isSet) {
            return;
        }
        boolean loading = res.isLoading();
        binding.itemsProgress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        List<RoPenjual> list = res.isSuccess() ? res.data : null;
        boolean empty = list == null || list.isEmpty();
        binding.emptyGroup.setVisibility(empty ? View.VISIBLE : View.GONE);
        adapter.submitList(list, () -> adapter.notifyDataSetChanged());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
