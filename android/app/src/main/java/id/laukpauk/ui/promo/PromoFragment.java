package id.laukpauk.ui.promo;

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

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.databinding.FragmentPromoBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;
import id.laukpauk.ui.product.ProductActions;

/** Promo.dart: the promo products around the chosen delivery address (a bottom navigation tab). */
public class PromoFragment extends Fragment {

    private FragmentPromoBinding binding;
    private PromoViewModel viewModel;
    private PromoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPromoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new PromoViewModel(c.promoRepository)))
                .get(PromoViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.emptyImage, "tidak-ada-promo.png");

        adapter = new PromoAdapter(new PromoAdapter.Listener() {
            @Override
            public void onOpen(id.laukpauk.data.model.promo.ProdukPromo item) {
                Bundle args = new Bundle();
                args.putString("idPromoItem", item.idPromoItem);
                NavHostFragment.findNavController(PromoFragment.this).navigate(R.id.detailPromoItemFragment, args);
            }

            @Override
            public void onBeli(id.laukpauk.data.model.promo.ProdukPromo item) {
                ProductActions.beli(PromoFragment.this, item.idProduk, item.namaProduk, item.idPromo);
            }
        });
        binding.list.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.list.setAdapter(adapter);

        SearchBox.bind(binding.search, getString(R.string.promo_search_hint), viewModel.getQuery(),
                viewModel::search, () -> viewModel.search(""));
        binding.refresh.setOnRefreshListener(viewModel::refresh);

        viewModel.getSelection().observe(getViewLifecycleOwner(), viewModel::onSelection);
        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);
        viewModel.getVisible().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            Resource<?> res = viewModel.getState().getValue();
            boolean loading = res == null || res.isLoading();
            binding.empty.setVisibility(!loading && list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void renderState(Resource<?> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (!loading) {
            binding.refresh.setRefreshing(false);
        }
        java.util.List<?> list = viewModel.getVisible().getValue();
        binding.empty.setVisibility(!loading && (list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
