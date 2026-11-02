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
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.model.shop.Penjual;
import id.laukpauk.databinding.FragmentPenjualBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.ui.common.SearchBox;
import id.laukpauk.ui.market.ProdukPasarAdapter;
import id.laukpauk.ui.product.DetailProdukFragment;
import id.laukpauk.ui.product.ProductActions;
import id.laukpauk.util.ServerPath;

/** Penjual.dart: a seller's public shop. Args: idPenjual, jenisProdukSelected ("satuan" | "paket", optional). */
public class PenjualFragment extends Fragment {

    private FragmentPenjualBinding binding;
    private PenjualViewModel viewModel;
    private ProdukPasarAdapter produkAdapter;
    private PenjualPaketAdapter paketAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPenjualBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String idPenjual = requireArguments().getString("idPenjual", "");
        String jenis = requireArguments().getString("jenisProdukSelected");
        final int initialTab = jenis == null || "satuan".equals(jenis) ? 0 : 1;
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new PenjualViewModel(c.shopRepository, c.produkRepository, idPenjual, initialTab)))
                .get(PenjualViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.emptyImage, "empty.png");
        binding.listHolder.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.69f);
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(viewModel::load);

        produkAdapter = new ProdukPasarAdapter(0.5f, new ProdukPasarAdapter.Listener() {
            @Override
            public void onOpen(Produk p) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                NavHostFragment.findNavController(PenjualFragment.this).navigate(R.id.detailProdukFragment, args);
            }

            @Override
            public void onToko(Produk p) {
                // already on this seller's shop
            }

            @Override
            public void onBeli(Produk p) {
                ProductActions.beli(PenjualFragment.this, p.id, p.nama);
            }

            @Override
            public void onNeedPrice(Produk p) {
                viewModel.getPrices().request(p.id);
            }
        });
        paketAdapter = new PenjualPaketAdapter(viewModel.currentUserId(), new PenjualPaketAdapter.Listener() {
            @Override
            public void onOpen(Paket p) {
                Bundle args = new Bundle();
                args.putString("idPaket", p.id);
                NavHostFragment.findNavController(PenjualFragment.this).navigate(R.id.detailPaketFragment, args);
            }

            @Override
            public void onButton(Paket p, boolean mine) {
                if (mine) {
                    Bundle args = new Bundle();
                    args.putString("idProduk", p.id);
                    args.putString("jenisProduk", "paket");
                    NavHostFragment.findNavController(PenjualFragment.this).navigate(R.id.tambahProdukFragment, args);
                } else {
                    ProductActions.beliPaket(PenjualFragment.this, p.id, p.nama);
                }
            }
        });
        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.seller_penjual_tab_produk)));
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.seller_penjual_tab_paket)));
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
        SearchBox.bind(binding.search, getString(R.string.seller_penjual_cari_produk), "",
                viewModel::setQuery, () -> viewModel.setQuery(""));

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::renderDetail);
        viewModel.getTab().observe(getViewLifecycleOwner(), tab -> {
            TabLayout.Tab t = binding.tabs.getTabAt(tab);
            if (t != null && binding.tabs.getSelectedTabPosition() != tab) {
                t.select();
            }
            binding.list.setAdapter(tab == 0 ? produkAdapter : paketAdapter);
            renderList();
        });
        viewModel.getProduk().observe(getViewLifecycleOwner(), r -> renderList());
        viewModel.getPaket().observe(getViewLifecycleOwner(), r -> renderList());
        viewModel.getQuery().observe(getViewLifecycleOwner(), q -> renderList());
        viewModel.getPrices().getPrices().observe(getViewLifecycleOwner(), produkAdapter::setPrices);
        NavResults.<Boolean>observe(this, DetailProdukFragment.EDIT_RESULT_KEY, ok -> {
            Messages.snack(this, getString(ok ? R.string.product_edit_paket_berhasil : R.string.product_edit_paket_gagal));
            viewModel.load();
        });
    }

    private void renderDetail(Resource<Penjual> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.content.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        binding.refresh.setRefreshing(false);
        Penjual p = res.isSuccess() ? res.data : null;
        binding.notFound.setVisibility(p == null ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(p == null ? View.GONE : View.VISIBLE);
        binding.header.setVisibility(p == null ? View.GONE : View.VISIBLE);
        if (p == null) {
            return;
        }
        Images.url(binding.banner, Images.server(ServerPath.uploadGambarBannerPenjual, p.banner == null ? "" : p.banner));
        binding.nama.setText(p.namaToko != null ? p.namaToko : getString(R.string.seller_penjual_toko_tidak_dikenal));
        binding.alamat.setText(p.alamatToko != null ? p.alamatToko : getString(R.string.seller_penjual_alamat_kosong));
        final String namaPasar = p.namaPasar == null ? getString(R.string.seller_penjual_pasar_kosong)
                : p.namaPasar.isEmpty() ? getString(R.string.seller_penjual_pasar_tanpa_nama) : p.namaPasar;
        binding.pasar.setText(namaPasar);
        binding.pasar.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idPasar", p.pasar == null ? "" : p.pasar);
            args.putString("namaPasar", namaPasar);
            NavHostFragment.findNavController(this).navigate(R.id.produkDiDalamPasarFragment, args);
        });
    }

    private void renderList() {
        if (binding == null) {
            return;
        }
        Integer tab = viewModel.getTab().getValue();
        boolean produkTab = tab == null || tab == 0;
        String q = viewModel.getQuery().getValue() == null ? "" : viewModel.getQuery().getValue().toLowerCase(Locale.ROOT);
        Resource<?> res = produkTab ? viewModel.getProduk().getValue() : viewModel.getPaket().getValue();
        if (res == null || res.isLoading()) {
            binding.listProgress.setVisibility(View.VISIBLE);
            binding.empty.setVisibility(View.GONE);
            return;
        }
        binding.listProgress.setVisibility(View.GONE);
        int count;
        if (produkTab) {
            List<Produk> shown = new ArrayList<>();
            List<Produk> all = viewModel.getProduk().getValue().data;
            if (all != null) {
                for (Produk p : all) {
                    if (p.nama != null && p.nama.toLowerCase(Locale.ROOT).contains(q)) {
                        shown.add(p);
                    }
                }
            }
            produkAdapter.submitList(shown);
            count = shown.size();
        } else {
            List<Paket> shown = new ArrayList<>();
            List<Paket> all = viewModel.getPaket().getValue().data;
            if (all != null) {
                for (Paket p : all) {
                    if (p.nama != null && p.nama.toLowerCase(Locale.ROOT).contains(q)) {
                        shown.add(p);
                    }
                }
            }
            paketAdapter.submitList(shown);
            count = shown.size();
        }
        binding.empty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        binding.list.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
