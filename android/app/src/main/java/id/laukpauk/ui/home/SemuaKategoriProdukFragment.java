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
import id.laukpauk.data.model.home.KategoriProduk;
import id.laukpauk.databinding.FragmentSemuaKategoriProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.SearchBox;

/** SemuaKategoriProduk.dart: category grid with a name filter; a tap lists the category's products. */
public class SemuaKategoriProdukFragment extends Fragment {

    private FragmentSemuaKategoriProdukBinding binding;
    private SemuaKategoriProdukViewModel viewModel;
    private boolean opening;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSemuaKategoriProdukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        AppBars.setup(this, binding.appBar, getString(R.string.home_semua_kategori_title));
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new SemuaKategoriProdukViewModel(c.homeRepository))).get(SemuaKategoriProdukViewModel.class);

        KategoriAdapter adapter = new KategoriAdapter(k -> open(c, k));
        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.list.setAdapter(adapter);
        binding.empty.emptyText.setText(R.string.home_kategori_kosong);
        binding.refresh.setColorSchemeColors(requireContext().getColor(R.color.green));
        binding.refresh.setOnRefreshListener(viewModel::load);
        SearchBox.bind(binding.search, getString(R.string.home_cari_kategori_hint), viewModel.getQuery(), q -> {
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
            boolean ada = list != null && !list.isEmpty();
            binding.empty.getRoot().setVisibility(ada ? View.GONE : View.VISIBLE);
        });
        if (viewModel.getState().getValue() == null) {
            viewModel.load();
        }
    }

    private void open(AppContainer c, KategoriProduk kategori) {
        if (opening) {
            return;
        }
        opening = true;
        c.homeRepository.getProdukInThisCategory(kategori.id, result -> {
            opening = false;
            if (!isAdded()) {
                return;
            }
            if (result.data == null) {
                Messages.snack(this, getString(R.string.home_kategori_kosong));
                return;
            }
            String nama = kategori.nama == null ? "" : kategori.nama.toUpperCase(Locale.getDefault());
            HomeNav.openListProduk(this, getString(R.string.home_list_title),
                    getString(R.string.home_list_subtitle_kategori, nama), result.data);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
