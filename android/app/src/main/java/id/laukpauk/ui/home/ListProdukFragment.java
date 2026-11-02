package id.laukpauk.ui.home;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.PenjualItem;
import id.laukpauk.databinding.FragmentListProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;
import id.laukpauk.R;

/**
 * ListProduk.dart: a title, a name search and a 2 column grid of products - or of sellers when the arguments
 * carry {@code dataPenjual} (a switch icon toggles between them).
 */
public class ListProdukFragment extends Fragment {

    private FragmentListProdukBinding binding;
    private ListProdukViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListProdukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        Bundle args = requireArguments();
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> {
            List<HomeProduk> produk = parse(c, args.getString("dataProduk"), HomeProduk.class);
            List<PenjualItem> penjual = parse(c, args.getString("dataPenjual"), PenjualItem.class);
            return new ListProdukViewModel(produk, penjual);
        })).get(ListProdukViewModel.class);

        binding.header.bind(this);
        binding.title.setText(args.getString("title", ""));
        boolean ready = args.getBoolean("isReadyToShow", true);
        binding.progress.setVisibility(ready ? View.GONE : View.VISIBLE);

        final ProdukListener produkListener = ProdukListener.navigating(this);
        ListProdukAdapter produkAdapter = new ListProdukAdapter(c.homeRepository::getLowestPrice, produkListener);
        ListPenjualAdapter penjualAdapter = new ListPenjualAdapter(p -> {
            Bundle b = new Bundle();
            b.putString("idPenjual", p.id);
            NavHostFragment.findNavController(this).navigate(R.id.penjualFragment, b);
        });
        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.list.setAdapter(produkAdapter);

        SearchBox.bind(binding.search, getString(R.string.home_cari_produk_hint), viewModel.getQuery(), q -> {
            String lower = q.toLowerCase(Locale.getDefault());
            SearchBox.setText(binding.search, lower);
            viewModel.setQuery(lower);
        }, () -> viewModel.setQuery(""));
        binding.switchView.setOnClickListener(v -> viewModel.toggleView());

        viewModel.getShowPenjual().observe(getViewLifecycleOwner(), penjualView -> {
            boolean showPenjual = Boolean.TRUE.equals(penjualView);
            binding.list.setAdapter(showPenjual ? penjualAdapter : produkAdapter);
            if (viewModel.hasPenjual()) {
                binding.switchView.setVisibility(View.VISIBLE);
                Images.asset(binding.switchView, showPenjual ? "produk.png" : "store.png");
            } else {
                binding.switchView.setVisibility(View.GONE);
            }
        });
        viewModel.getProduk().observe(getViewLifecycleOwner(), list -> {
            produkAdapter.submitList(list);
            updateEmpty(list);
        });
        viewModel.getPenjual().observe(getViewLifecycleOwner(), penjualAdapter::submitList);
    }

    // Quirk kept from the legacy screen: the empty state follows the product list, even in the seller view.
    private void updateEmpty(@Nullable List<HomeProduk> produk) {
        boolean empty = produk == null || produk.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.list.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private static <T> List<T> parse(AppContainer c, @Nullable String json, Class<T> element) {
        List<T> list = c.apiClient.fromJson(json, com.squareup.moshi.Types.newParameterizedType(List.class, element));
        return list == null ? new ArrayList<T>() : list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
