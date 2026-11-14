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

import java.util.Collections;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.databinding.FragmentRepeatOrderBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Messages;

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
        adapter = new RepeatOrderAdapter();
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(() -> viewModel.refresh());
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
        viewModel.getProcessing().observe(getViewLifecycleOwner(), busy -> {
            boolean b = Boolean.TRUE.equals(busy);
            binding.fabProgress.setVisibility(b ? View.VISIBLE : View.GONE);
            binding.fabIcon.setVisibility(b ? View.GONE : View.VISIBLE);
        });
    }

    /** The lines load with or without a delivery selection; a chosen one only refines their availability. */
    private void onSelection(@Nullable DeliverySelection s) {
        viewModel.loadItemsIfNeeded(s);
    }

    private void onItems(Resource<RepeatOrderResponses.Items> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.itemsProgress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            binding.fab.setVisibility(View.GONE);
            return;
        }
        binding.refresh.setRefreshing(false);
        RepeatOrderResponses.Items items = res.isSuccess() ? res.data : null;
        boolean empty = items == null || items.itemTransaksi == null || items.itemTransaksi.isEmpty();
        binding.emptyGroup.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.fab.setVisibility(!empty && items.isAvailable ? View.VISIBLE : View.GONE);
        adapter.submitList(empty ? null : Collections.singletonList(items), () -> adapter.notifyDataSetChanged());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
