package id.laukpauk.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.home.ProductCategory;
import id.laukpauk.databinding.FragmentAllProductCategoriesBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.SearchBox;

/** SemuaKategoriProduk.dart: category grid with a name filter; a tap lists the category's products. */
public class AllProductCategoriesFragment extends Fragment {

    private FragmentAllProductCategoriesBinding binding;
    private AllProductCategoriesViewModel viewModel;
    private boolean opening;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAllProductCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        AppBars.setup(this, binding.appBar, getString(R.string.home_all_categories_title));
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new AllProductCategoriesViewModel(c.homeRepository))).get(AllProductCategoriesViewModel.class);

        CategoryAdapter adapter = new CategoryAdapter(k -> open(c, k));
        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.list.setAdapter(adapter);
        binding.empty.emptyText.setText(R.string.home_category_empty);
        binding.refresh.setColorSchemeColors(requireContext().getColor(R.color.green));
        binding.refresh.setOnRefreshListener(viewModel::load);
        SearchBox.bind(binding.search, getString(R.string.home_search_category_hint), viewModel.getQuery(), q -> {
            String lower = q.toLowerCase(Locale.getDefault());
            SearchBox.setText(binding.search, lower);
            viewModel.setQuery(lower);
        }, () -> viewModel.setQuery(""));

        viewModel.getState().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (!res.isLoading()) {
                binding.refresh.setRefreshing(false);
                binding.progress.setVisibility(View.GONE);
                binding.body.setVisibility(View.VISIBLE);
            }
        });
        viewModel.getFiltered().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            boolean has = list != null && !list.isEmpty();
            binding.empty.getRoot().setVisibility(has ? View.GONE : View.VISIBLE);
        });
        if (viewModel.getState().getValue() == null) {
            viewModel.load();
        }
    }

    private void open(AppContainer c, ProductCategory category) {
        if (opening) {
            return;
        }
        opening = true;
        c.homeRepository.getProductInThisCategory(category.id, result -> {
            opening = false;
            if (!isAdded()) {
                return;
            }
            if (result.data == null) {
                Messages.snack(this, getString(R.string.home_category_empty));
                return;
            }
            String name = category.name == null ? "" : category.name.toUpperCase(Locale.getDefault());
            HomeNav.openProductList(this, getString(R.string.home_list_title),
                    getString(R.string.home_list_subtitle_category, name), result.data);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
