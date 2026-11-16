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
import id.laukpauk.data.model.product.Product;
import id.laukpauk.databinding.FragmentAllProductsBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;

/** Every product: an endless grid with a name search. */
public class AllProductsFragment extends Fragment {

    private FragmentAllProductsBinding binding;
    private AllProductsViewModel viewModel;
    private ProductGridAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAllProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new AllProductsViewModel(c.productRepository)))
                .get(AllProductsViewModel.class);

        binding.header.bind(this);
        binding.title.setText(R.string.product_list_title);
        binding.gridHolder.getLayoutParams().height = getResources().getDisplayMetrics().heightPixels * 7 / 10;
        Images.asset(binding.overlayImage, "loading.png");

        adapter = new ProductGridAdapter(0.45f, new ProductGridAdapter.Listener() {
            @Override
            public void onOpen(Product p) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                NavHostFragment.findNavController(AllProductsFragment.this).navigate(R.id.productDetailFragment, args);
            }

            @Override
            public void onBuy(Product p) {
                ProductActions.buy(AllProductsFragment.this, p.id, p.name);
            }

            @Override
            public void onNeedPrice(Product p) {
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

        SearchBox.bind(binding.search, getString(R.string.product_list_search_product_here), "",
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
        List<Product> list = viewModel.getProducts().getValue();
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
        binding.emptyText.setText(waiting ? R.string.product_list_please_wait_mak
                : searched ? R.string.product_product_list_not_found : R.string.product_list_no_product_yet);
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
