package id.laukpauk.ui.product;

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.databinding.FragmentSemuaProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;

/** Every product: an endless grid with a name search. */
public class SemuaProdukFragment extends Fragment {

    private FragmentSemuaProdukBinding binding;
    private SemuaProdukViewModel viewModel;
    private ProdukGridAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSemuaProdukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new SemuaProdukViewModel(c.produkRepository)))
                .get(SemuaProdukViewModel.class);

        binding.header.bind(this);
        binding.title.setText(R.string.product_list_title);
        binding.gridHolder.getLayoutParams().height = getResources().getDisplayMetrics().heightPixels * 7 / 10;
        Images.asset(binding.overlayImage, "loading.png");

        adapter = new ProdukGridAdapter(0.45f, new ProdukGridAdapter.Listener() {
            @Override
            public void onOpen(Produk p) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                NavHostFragment.findNavController(SemuaProdukFragment.this).navigate(R.id.detailProdukFragment, args);
            }

            @Override
            public void onBeli(Produk p) {
                ProductActions.beli(SemuaProdukFragment.this, p.id, p.nama);
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

        SearchBox.bind(binding.search, getString(R.string.product_list_cari_produk_di_sini), "",
                text -> {
                    hideKeyboard();
                    viewModel.search(text);
                }, viewModel::clearSearch);

        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.refresh.setRefreshing(false);
        });

        observe();
    }

    private void observe() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            updateEmpty();
        });
        viewModel.getInitialLoaded().observe(getViewLifecycleOwner(), v -> updateEmpty());
        viewModel.getSearchTyped().observe(getViewLifecycleOwner(), v -> updateEmpty());
        viewModel.getFetchingMore().observe(getViewLifecycleOwner(),
                busy -> binding.overlay.setVisibility(Boolean.TRUE.equals(busy) ? View.VISIBLE : View.GONE));
        viewModel.getPrices().getPrices().observe(getViewLifecycleOwner(), adapter::setPrices);
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
        binding.emptyText.setText(waiting ? R.string.product_list_tunggu_sebentar_mak
                : searched ? R.string.product_list_produk_tidak_ditemukan : R.string.product_list_belum_ada_produk);
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
