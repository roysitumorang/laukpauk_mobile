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

import com.google.android.material.tabs.TabLayout;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentHistoriPenjualanBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Images;

/** HistoriPenjualan.dart. Argument: idPenjual (optional). */
public class HistoriPenjualanFragment extends Fragment {

    private FragmentHistoriPenjualanBinding binding;
    private HistoriPenjualanViewModel viewModel;
    private HistoriPenjualanAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoriPenjualanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        String idPenjual = requireArguments().getString("idPenjual");
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new HistoriPenjualanViewModel(c.historiPenjualanRepository, idPenjual)))
                .get(HistoriPenjualanViewModel.class);
        AppBars.setup(this, binding.appBar, getString(R.string.seller_toko_histori_judul));

        adapter = new HistoriPenjualanAdapter(item -> viewModel.resolveIdPenjual(result -> {
            if (!isAdded()) {
                return;
            }
            Bundle args = new Bundle();
            args.putString("idTransaksi", item.id);
            args.putString("idPenjual", result.data == null ? "0" : result.data);
            NavHostFragment.findNavController(this).navigate(R.id.itemTransaksiFragment, args);
        }));
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        Images.asset(binding.empty.emptyImage, "empty.png");
        binding.empty.emptyText.setText(R.string.seller_toko_histori_kosong);

        int[] labels = {R.string.seller_toko_histori_hari_ini, R.string.seller_toko_histori_semua,
                R.string.seller_toko_histori_completed};
        for (int label : labels) {
            binding.tabs.addTab(binding.tabs.newTab().setText(getString(label)));
        }
        Integer selected = viewModel.getTab().getValue();
        TabLayout.Tab current = binding.tabs.getTabAt(selected == null ? 0 : selected);
        if (current != null) {
            current.select();
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.refresh.setOnRefreshListener(viewModel::load);
        AuthGate.require(this, this::observe);
    }

    private void observe() {
        viewModel.getList().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
            if (!loading) {
                binding.refresh.setRefreshing(false);
            }
            if (res.isSuccess()) {
                adapter.submitList(res.data);
                binding.empty.getRoot().setVisibility(res.data == null || res.data.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
