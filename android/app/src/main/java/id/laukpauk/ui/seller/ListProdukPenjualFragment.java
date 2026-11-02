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

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.ProdukPenjual;
import id.laukpauk.data.repository.ProdukPenjualRepository;
import id.laukpauk.databinding.FragmentListProdukPenjualBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.ui.common.StatusBar;

/** ListProdukPenjual.dart: "Produk Anda" with the Satuan / Paketan tabs. Seller only (signed in). */
public class ListProdukPenjualFragment extends Fragment {

    private FragmentListProdukPenjualBinding binding;
    private ListProdukPenjualViewModel viewModel;
    private ListProdukPenjualAdapter satuanAdapter;
    private ListProdukPenjualAdapter paketAdapter;
    /** What the screen started via TambahProduk: picks the failure message. */
    private boolean pendingEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListProdukPenjualBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StatusBar.green(this);
        binding.header.bind(this);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new ListProdukPenjualViewModel(c.produkPenjualRepository)))
                .get(ListProdukPenjualViewModel.class);

        satuanAdapter = new ListProdukPenjualAdapter(false, listener(false));
        paketAdapter = new ListProdukPenjualAdapter(true, listener(true));
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.seller_produk_tab_satuan)));
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.seller_produk_tab_paket)));
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
        binding.tambah.setOnClickListener(v -> tambah());
        binding.empty.emptyAction.setOnClickListener(v -> tambah());

        NavResults.<Boolean>observe(this, TambahProdukFragment.RESULT_KEY, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                viewModel.load();
            } else if (pendingEdit) {
                Messages.snack(this, getString(R.string.seller_produk_gagal_edit));
            } else {
                Messages.snack(this, getString(R.string.seller_produk_gagal_tambah));
            }
        });

        AuthGate.require(this, this::observe);
    }

    private ListProdukPenjualAdapter.Listener listener(boolean paket) {
        return new ListProdukPenjualAdapter.Listener() {
            @Override
            public void onOpen(ProdukPenjual item) {
                Bundle args = new Bundle();
                args.putString("idProduk", item.id);
                args.putString("jenisProduk", paket ? "paket" : "satuan");
                NavHostFragment.findNavController(ListProdukPenjualFragment.this)
                        .navigate(R.id.detailProdukPenjualFragment, args);
            }

            @Override
            public void onEdit(ProdukPenjual item) {
                pendingEdit = true;
                Bundle args = new Bundle();
                args.putString("idProduk", item.id);
                args.putString("jenisProduk", paket ? "paket" : "satuan");
                NavHostFragment.findNavController(ListProdukPenjualFragment.this)
                        .navigate(R.id.tambahProdukFragment, args);
            }

            @Override
            public void onHapus(ProdukPenjual item) {
                Messages.confirm(ListProdukPenjualFragment.this,
                        getString(R.string.seller_produk_konfirmasi_hapus_title),
                        getString(R.string.seller_produk_konfirmasi_hapus_msg, item.nama),
                        getString(R.string.seller_produk_lanjutkan), () -> viewModel.hapus(item.id, paket, r -> {
                            if (!isAdded()) {
                                return;
                            }
                            if (r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                                viewModel.load();
                            } else {
                                Messages.snack(ListProdukPenjualFragment.this, getString(paket
                                        ? R.string.seller_produk_gagal_hapus_paket
                                        : R.string.seller_produk_gagal_hapus));
                            }
                        }));
            }
        };
    }

    private void tambah() {
        pendingEdit = false;
        Bundle args = new Bundle();
        args.putString("jenisProduk", "satuan");
        NavHostFragment.findNavController(this).navigate(R.id.tambahProdukFragment, args);
    }

    private void observe() {
        viewModel.getTab().observe(getViewLifecycleOwner(), tab -> {
            boolean paket = tab == 1;
            binding.list.setAdapter(paket ? paketAdapter : satuanAdapter);
            binding.empty.emptyText.setText(paket ? R.string.seller_produk_kosong_paket
                    : R.string.seller_produk_kosong_satuan);
            TabLayout.Tab t = binding.tabs.getTabAt(tab);
            if (t != null && binding.tabs.getSelectedTabPosition() != tab) {
                t.select();
            }
            Resource<List<ProdukPenjual>> res = paket ? viewModel.getPaket().getValue()
                    : viewModel.getSatuan().getValue();
            render(res, paket);
        });
        viewModel.getSatuan().observe(getViewLifecycleOwner(), res -> {
            if (!isPaketTab()) {
                render(res, false);
            }
        });
        viewModel.getPaket().observe(getViewLifecycleOwner(), res -> {
            if (isPaketTab()) {
                render(res, true);
            }
        });
        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    private boolean isPaketTab() {
        return viewModel.getTab().getValue() != null && viewModel.getTab().getValue() == 1;
    }

    private void render(@Nullable Resource<List<ProdukPenjual>> res, boolean paket) {
        if (binding == null || res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (!loading) {
            binding.refresh.setRefreshing(false);
        }
        List<ProdukPenjual> items = res.isSuccess() ? res.data : null;
        (paket ? paketAdapter : satuanAdapter).submitList(items);
        boolean empty = !loading && (items == null || items.isEmpty());
        binding.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
