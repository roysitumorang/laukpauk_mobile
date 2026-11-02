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

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.Konten;
import id.laukpauk.data.model.home.KontenItem;
import id.laukpauk.data.model.search.HistoriPencarian;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.databinding.FragmentSearchBinding;
import id.laukpauk.databinding.ItemSearchHistoriBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.StatusBar;
import id.laukpauk.ui.home.HomeNav;
import id.laukpauk.ui.home.HomeViewModel;
import id.laukpauk.ui.home.KontenSection;
import id.laukpauk.ui.home.PasarViewModel;
import id.laukpauk.ui.home.ProdukListener;
import id.laukpauk.ui.home.ProdukPasarAdapter;

/**
 * HeaderSearch.dart + SearchResult.dart + HistoriPencarian.dart: the full screen search. Unfocused and empty it
 * browses (nearest market's products, "headersearch" konten, search history); focused or with text it shows the
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
        PasarViewModel pasarVm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new PasarViewModel(c.homeRepository, c.deliveryRepository, 1, true)))
                .get(PasarViewModel.class);
        HomeViewModel kontenVm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new HomeViewModel(c.homeRepository, "headersearch", null))).get(HomeViewModel.class);

        binding.back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        setupField();
        setupBrowse(c, pasarVm, kontenVm);
        setupResults();
    }

    private void setupField() {
        binding.field.setOnEditorActionListener((v, actionId, event) -> {
            // onSubmitted: the keyboard closes and the field loses focus
            String text = binding.field.getText().toString();
            binding.field.clearFocus();
            hideKeyboard();
            viewModel.addHistori(text);
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

    private void setupBrowse(AppContainer c, PasarViewModel pasarVm, HomeViewModel kontenVm) {
        ProdukPasarAdapter produk = new ProdukPasarAdapter(c.homeRepository::getLowestPrice,
                ProdukListener.navigating(this), pasar -> HomeNav.openPasar(this, pasar));
        binding.produkPasarList.setAdapter(produk);
        pasarVm.getPasar().observe(getViewLifecycleOwner(), res ->
                binding.pasarProgress.setVisibility(res != null && res.isLoading() ? View.VISIBLE : View.GONE));
        pasarVm.getSections().observe(getViewLifecycleOwner(), list -> {
            produk.submitList(list);
            binding.pasarTitle.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        });
        pasarVm.start();

        SearchKontenAdapter konten = new SearchKontenAdapter(new SearchKontenAdapter.Listener() {
            @Override
            public void onProduk(HomeProduk p) {
                replaceWith(R.id.detailProdukFragment, "idProduk", p.id);
            }

            @Override
            public void onBanner(Konten k, KontenItem item) {
                HomeNav.openBanner(SearchFragment.this, k, item);
            }
        });
        binding.kontenList.setAdapter(konten);
        kontenVm.getKonten().observe(getViewLifecycleOwner(), res -> {
            if (res != null && !res.isLoading()) {
                List<KontenSection> list = res.data;
                konten.submitList(list);
            }
        });
        kontenVm.startKonten();

        viewModel.getHistori().observe(getViewLifecycleOwner(), this::showHistori);
    }

    private void showHistori(@Nullable List<HistoriPencarian> list) {
        binding.historiList.removeAllViews();
        binding.histori.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        if (list == null) {
            return;
        }
        for (HistoriPencarian h : list) {
            ItemSearchHistoriBinding row = ItemSearchHistoriBinding.inflate(LayoutInflater.from(requireContext()), binding.historiList, false);
            row.title.setText(h.title);
            row.onTime.setText(h.onTime);
            row.getRoot().setOnClickListener(v -> {
                binding.field.setText(h.title);
                binding.field.setSelection(binding.field.getText().length());
            });
            binding.historiList.addView(row.getRoot());
        }
    }

    private void setupResults() {
        SearchSuggestionAdapter adapter = new SearchSuggestionAdapter(this::openSuggestion);
        binding.resultList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.resultList.setAdapter(adapter);
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
                binding.messageText.setText(getString(R.string.search_tidak_ditemukan, state.term));
            }
        });
    }

    private void openSuggestion(SearchSuggestion s) {
        if (s.isPenjual()) {
            replaceWith(R.id.penjualFragment, "idPenjual", s.id);
        } else {
            replaceWith(R.id.detailProdukFragment, "idProduk", s.id);
        }
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
