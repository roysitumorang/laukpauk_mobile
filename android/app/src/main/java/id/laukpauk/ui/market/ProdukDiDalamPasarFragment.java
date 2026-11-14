package id.laukpauk.ui.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.databinding.FragmentProdukDiDalamPasarBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.ui.product.ProductActions;

/**
 * ProdukDiDalamPasar.dart: the products of a market (endless grid with search) and the other markets.
 * Args: idPasar, namaPasar (optional).
 */
public class ProdukDiDalamPasarFragment extends Fragment {

    private FragmentProdukDiDalamPasarBinding binding;
    private ProdukDiDalamPasarViewModel viewModel;
    private ProdukPasarAdapter adapter;
    private PasarLainSection sekitar;
    private PasarLainSection luar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProdukDiDalamPasarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String idPasar = requireArguments().getString("idPasar", "");
        String nama = requireArguments().getString("namaPasar");
        if (nama == null) {
            nama = getString(R.string.market_nama_tidak_dikenal);
        }
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ProdukDiDalamPasarViewModel(c.pasarRepository, c.produkRepository, idPasar)))
                .get(ProdukDiDalamPasarViewModel.class);

        binding.header.bind(this);
        binding.title.setText(getString(R.string.market_title, nama));
        binding.gridHolder.getLayoutParams().height = getResources().getDisplayMetrics().heightPixels / 2;
        Images.asset(binding.overlayImage, "loading.png");
        Images.asset(binding.pasarLain.adjdImage, "adjd-belum-ditentukan.png");

        adapter = new ProdukPasarAdapter(0.45f, new ProdukPasarAdapter.Listener() {
            @Override
            public void onOpen(Produk p) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                NavHostFragment.findNavController(ProdukDiDalamPasarFragment.this).navigate(R.id.detailProdukFragment, args);
            }

            @Override
            public void onBeli(Produk p) {
                ProductActions.beli(ProdukDiDalamPasarFragment.this, p.id, p.nama);
            }

            @Override
            public void onNeedPrice(Produk p) {
                viewModel.getPrices().request(p.id);
            }
        });
        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.list.setAdapter(adapter);
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0 && !rv.canScrollVertically(1)) {
                    viewModel.loadMore();
                }
            }
        });

        SearchBox.bind(binding.search, getString(R.string.market_cari_produk_di_sini), "",
                text -> {
                    hideKeyboard();
                    viewModel.search(text);
                }, viewModel::clearSearch);

        final PasarLainAdapter.Listener openPasar = pasar -> {
            Bundle args = new Bundle();
            args.putString("idPasar", pasar.id);
            args.putString("namaPasar", pasar.nama);
            NavController nav = NavHostFragment.findNavController(this);
            nav.navigate(R.id.produkDiDalamPasarFragment, args,
                    new NavOptions.Builder().setPopUpTo(R.id.produkDiDalamPasarFragment, true).build());
        };
        sekitar = new PasarLainSection(binding.pasarLain.dalamCa, R.string.market_pasar_sekitar,
                R.string.market_pasar_sekitar_kosong, true, openPasar);
        luar = new PasarLainSection(binding.pasarLain.luarCa, R.string.market_pasar_luar,
                R.string.market_pasar_luar_kosong, false, openPasar);
        binding.pasarLain.adjdButton.setOnClickListener(v -> DeliveryFlow.ask(this, null));

        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.refresh.setRefreshing(false);
            DeliverySelection sel = c.deliveryRepository.current();
            if (sel.isSet) {
                viewModel.loadPasarLain(key(sel));
            }
        });

        observe(c);
    }

    private static String key(DeliverySelection s) {
        return s.latitude + "|" + s.longitude + "|" + s.jadwalDeliveryCode;
    }

    private void observe(AppContainer c) {
        viewModel.getProducts().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            updateEmpty();
        });
        viewModel.getInitialLoaded().observe(getViewLifecycleOwner(), v -> updateEmpty());
        viewModel.getSearchTyped().observe(getViewLifecycleOwner(), v -> updateEmpty());
        viewModel.getFetchingMore().observe(getViewLifecycleOwner(),
                busy -> binding.overlay.setVisibility(Boolean.TRUE.equals(busy) ? View.VISIBLE : View.GONE));
        viewModel.getPrices().getPrices().observe(getViewLifecycleOwner(), adapter::setPrices);
        viewModel.getAround().observe(getViewLifecycleOwner(), sekitar::render);
        viewModel.getOver().observe(getViewLifecycleOwner(), luar::render);
        c.deliveryRepository.selection().observe(getViewLifecycleOwner(), sel -> {
            boolean set = sel != null && sel.isSet;
            binding.pasarLain.sections.setVisibility(set ? View.VISIBLE : View.GONE);
            binding.pasarLain.adjdBox.setVisibility(set ? View.GONE : View.VISIBLE);
            if (set) {
                viewModel.loadPasarLain(key(sel));
            }
        });
    }

    private void updateEmpty() {
        if (binding == null) {
            return;
        }
        List<Produk> list = viewModel.getProducts().getValue();
        boolean empty = list == null || list.isEmpty();
        binding.list.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!empty) {
            return;
        }
        boolean searched = Boolean.TRUE.equals(viewModel.getSearchTyped().getValue());
        boolean loaded = Boolean.TRUE.equals(viewModel.getInitialLoaded().getValue());
        boolean waiting = !loaded && !searched;
        binding.emptyProgress.setVisibility(waiting ? View.VISIBLE : View.GONE);
        binding.emptyImage.setVisibility(waiting ? View.GONE : View.VISIBLE);
        if (!waiting) {
            Images.asset(binding.emptyImage, "empty.png");
        }
        binding.emptyText.setText(waiting ? R.string.market_tunggu_sebentar_mak
                : searched ? R.string.market_produk_tidak_ditemukan : R.string.market_belum_ada_produk);
    }

    private void hideKeyboard() {
        View v = requireActivity().getCurrentFocus();
        if (v != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
