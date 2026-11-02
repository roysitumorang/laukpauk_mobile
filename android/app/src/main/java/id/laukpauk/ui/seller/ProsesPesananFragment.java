package id.laukpauk.ui.seller;

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

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.orders.PesananMasukItem;
import id.laukpauk.data.model.seller.orders.TransaksiPenjual;
import id.laukpauk.databinding.FragmentProsesPesananBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.ui.common.StatusBar;

/** ProsesPesanan.dart: process an incoming order and mark unavailable items. Argument: idTransaksi. */
public class ProsesPesananFragment extends Fragment {

    private FragmentProsesPesananBinding binding;
    private ProsesPesananViewModel viewModel;
    private ProsesPesananAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProsesPesananBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StatusBar.green(this);
        binding.header.bind(this);
        final String idTransaksi = requireArguments().getString("idTransaksi");
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ProsesPesananViewModel(c.pesananMasukRepository, idTransaksi)))
                .get(ProsesPesananViewModel.class);

        adapter = new ProsesPesananAdapter((item, rejected) -> {
            if (rejected) {
                viewModel.pembatalanKlaim(item.id, r -> klaimResult(r.isSuccess() && Boolean.TRUE.equals(r.data)));
            } else {
                Bundle args = new Bundle();
                args.putString("pesananItem", c.apiClient.gson().toJson(item));
                NavHostFragment.findNavController(this).navigate(R.id.pesananItemTidakTersediaFragment, args);
            }
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(this::reload);
        binding.kembali.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.proses.setOnClickListener(v -> {
            binding.proses.setEnabled(false);
            viewModel.prosesPesanan(r -> {
                if (!isAdded()) {
                    return;
                }
                if (r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                    NavHostFragment.findNavController(this).navigateUp();
                } else {
                    binding.proses.setEnabled(true);
                    Messages.snack(this, getString(R.string.seller_pesanan_gagal_proses));
                }
            });
        });
        NavResults.<Boolean>observe(this, PesananItemTidakTersediaFragment.RESULT_KEY,
                ok -> klaimResult(Boolean.TRUE.equals(ok)));
        AuthGate.require(this, this::observe);
    }

    private void klaimResult(boolean ok) {
        if (!isAdded()) {
            return;
        }
        if (ok) {
            viewModel.loadItems();
        } else {
            Messages.snack(this, getString(R.string.seller_pesanan_gagal_klaim));
        }
    }

    private void reload() {
        viewModel.loadDetail();
        viewModel.loadItems();
    }

    private void observe() {
        viewModel.getDetail().observe(getViewLifecycleOwner(), this::renderDetail);
        viewModel.getItems().observe(getViewLifecycleOwner(), this::renderItems);
        if (!viewModel.isLoaded()) {
            viewModel.loadDetail();
        }
    }

    private void renderDetail(Resource<TransaksiPenjual> res) {
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.found.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        TransaksiPenjual t = res.isSuccess() ? res.data : null;
        if (t == null) {
            if (res.isError()) {
                return; // the legacy indicator stays when the request failed
            }
            binding.found.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        binding.notFound.setVisibility(View.GONE);
        binding.found.setVisibility(View.VISIBLE);
        String status = t.status == null ? "" : t.status.toLowerCase();
        boolean processing = status.equals("processing");
        binding.banner.setVisibility(processing ? View.VISIBLE : View.GONE);
        binding.itemsBox.setVisibility(processing ? View.VISIBLE : View.GONE);
        binding.selesai.setVisibility(processing ? View.GONE : View.VISIBLE);
        if (processing) {
            if (!viewModel.itemsLoaded()) {
                viewModel.loadItems();
            }
        } else {
            boolean otw = status.equals("otw");
            Images.asset(binding.selesaiImage, "pesanan-diterima.png");
            binding.selesaiTitle.setText(otw ? R.string.seller_pesanan_sedang_dijemput
                    : R.string.seller_pesanan_selesai);
            binding.selesaiDesc.setText(otw ? R.string.seller_pesanan_dijemput_desc
                    : R.string.seller_pesanan_selesai_desc);
        }
    }

    private void renderItems(Resource<List<PesananMasukItem>> res) {
        boolean loading = res.isLoading();
        binding.itemsProgress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        List<PesananMasukItem> items = res.isSuccess() ? res.data : null;
        adapter.submitList(items);
        boolean empty = items != null && items.isEmpty();
        binding.itemsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.refresh.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
