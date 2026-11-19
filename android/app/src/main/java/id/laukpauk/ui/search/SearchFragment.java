package id.laukpauk.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;
import id.laukpauk.data.model.search.SearchHistory;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.databinding.FragmentSearchBinding;
import id.laukpauk.databinding.ItemSearchHistoryBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.StatusBar;
import id.laukpauk.ui.home.HomeNav;
import id.laukpauk.ui.home.HomeViewModel;
import id.laukpauk.ui.home.ContentSection;
import id.laukpauk.ui.home.ProductSectionViewModel;

/**
 * HeaderSearch.dart + SearchResult.dart + SearchHistory.dart: the full screen search. Unfocused and empty it
 * browses (the first products, "headersearch" konten, search history); focused or with text it shows the
 * live suggestions. Opening a suggestion replaces this screen.
 */
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final AppContainer c = AppContainer.from(requireContext());
        StatusBar.tint(this, requireContext().getColor(R.color.nearly_white));
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new SearchViewModel(c.searchRepository)))
                .get(SearchViewModel.class);
        ProductSectionViewModel productVm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ProductSectionViewModel(c.homeRepository, c.deliveryRepository)))
                .get(ProductSectionViewModel.class);
        HomeViewModel contentVm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new HomeViewModel(c.homeRepository, "headersearch", null))).get(HomeViewModel.class);

        binding.back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        setupField();
        setupBrowse(productVm, contentVm);
        setupResults();
    }

    private void setupField() {
        binding.field.setOnEditorActionListener((v, actionId, event) -> {
            // onSubmitted: the keyboard closes and the field loses focus
            String text = binding.field.getText().toString();
            binding.field.clearFocus();
            hideKeyboard();
            viewModel.addHistory(text);
            refreshFocus();
            return true;
        });
        binding.field.setOnFocusChangeListener((v, hasFocus) -> refreshFocus());
        binding.field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshFocus();
                viewModel.setTerm(s.toString());
            }
        });
        binding.cancel.setOnClickListener(v -> {
            binding.field.setText("");
            binding.field.clearFocus();
            hideKeyboard();
            refreshFocus();
        });
        refreshFocus();
    }

    /** _onFocus = hasFocus || text.length >= 1: switches between browsing and the suggestions. */
    private void refreshFocus() {
        if (binding == null) {
            return;
        }
        boolean onFocus = binding.field.hasFocus() || binding.field.getText().length() >= 1;
        binding.cancel.setVisibility(onFocus ? View.VISIBLE : View.GONE);
        binding.results.setVisibility(onFocus ? View.VISIBLE : View.GONE);
        binding.browse.setVisibility(onFocus ? View.GONE : View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.field.getWindowToken(), 0);
        }
    }

    private void setupBrowse(ProductSectionViewModel productVm, HomeViewModel contentVm) {
        HomeNav.bindProductSection(this, binding.product, productVm);

        SearchContentAdapter content = new SearchContentAdapter(new SearchContentAdapter.Listener() {
            @Override
            public void onProduct(HomeProduct p) {
                replaceWith(R.id.productDetailFragment, "idProduk", p.id);
            }

            @Override
            public void onBanner(Content k, ContentItem item) {
                HomeNav.openBanner(SearchFragment.this, k, item);
            }
        });
        binding.contentList.setAdapter(content);
        contentVm.getContent().observe(getViewLifecycleOwner(), res -> {
            if (res != null && !res.isLoading()) {
                List<ContentSection> list = res.data;
                content.submitList(list);
            }
        });
        contentVm.startContent();

        viewModel.getHistory().observe(getViewLifecycleOwner(), this::showHistory);
    }

    private void showHistory(@Nullable List<SearchHistory> list) {
        binding.historyList.removeAllViews();
        binding.history.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        if (list == null) {
            return;
        }
        for (SearchHistory h : list) {
            ItemSearchHistoryBinding row = ItemSearchHistoryBinding.inflate(LayoutInflater.from(requireContext()), binding.historyList, false);
            row.title.setText(h.title);
            row.onTime.setText(h.onTime);
            row.getRoot().setOnClickListener(v -> {
                binding.field.setText(h.title);
                binding.field.setSelection(binding.field.getText().length());
            });
            binding.historyList.addView(row.getRoot());
        }
    }

    private void setupResults() {
        SearchSuggestionAdapter adapter = new SearchSuggestionAdapter(this::openSuggestion);
        binding.resultList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.resultList.setAdapter(adapter);
        binding.resultList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0 && !rv.canScrollVertically(1)) {
                    viewModel.loadMore();
                }
            }
        });
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            boolean loading = state.kind == SearchState.Kind.LOADING;
            boolean results = state.kind == SearchState.Kind.RESULTS;
            binding.resultProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.resultList.setVisibility(results ? View.VISIBLE : View.GONE);
            binding.message.setVisibility(state.kind == SearchState.Kind.PROMPT
                    || state.kind == SearchState.Kind.EMPTY ? View.VISIBLE : View.GONE);
            if (results) {
                adapter.submitList(state.list);
            } else if (state.kind == SearchState.Kind.PROMPT) {
                Images.asset(binding.messageImage, "produk.png");
                ((TextView) binding.messageText).setText(R.string.search_prompt);
            } else if (state.kind == SearchState.Kind.EMPTY) {
                Images.asset(binding.messageImage, "empty.png");
                binding.messageText.setText(getString(R.string.search_not_found, state.term));
            }
        });
    }

    private void openSuggestion(SearchSuggestion s) {
        replaceWith(R.id.productDetailFragment, "idProduk", s.id);
    }

    /** Legacy pushReplacement: the search dialog closes, so back from the detail returns to the previous screen. */
    private void replaceWith(int destination, String argName, String id) {
        hideKeyboard();
        Bundle args = new Bundle();
        args.putString(argName, id);
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(destination, args, new NavOptions.Builder().setPopUpTo(R.id.searchFragment, true).build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
