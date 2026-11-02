package id.laukpauk.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.databinding.FragmentResepBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.SearchBox;

/** Resep.dart: the recipe list (a bottom navigation tab). */
public class ResepFragment extends Fragment {

    private FragmentResepBinding binding;
    private ResepViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResepBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new ResepViewModel(c.resepRepository)))
                .get(ResepViewModel.class);

        binding.header.bind(this);
        binding.empty.emptyText.setText(R.string.recipe_kosong);

        ResepAdapter adapter = new ResepAdapter(new ResepAdapter.Listener() {
            @Override
            public void onOpen(id.laukpauk.data.model.recipe.Resep resep) {
                Bundle args = new Bundle();
                args.putString("idResep", resep.id);
                NavHostFragment.findNavController(ResepFragment.this).navigate(R.id.detailResepFragment, args);
            }

            @Override
            public void onLink(String link) {
                LinkOpener.open(ResepFragment.this, link, R.string.recipe_url_tidak_valid);
            }
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        SearchBox.bind(binding.search, getString(R.string.recipe_search_hint), viewModel.getQuery(),
                viewModel::search, () -> viewModel.search(""));
        binding.refresh.setOnRefreshListener(viewModel::load);

        viewModel.getState().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
            if (!loading) {
                binding.refresh.setRefreshing(false);
            }
            if (res.status == Resource.Status.ERROR) {
                // Legacy: the "Exception Occured" dialog, then an empty list.
                Messages.info(this, getString(R.string.recipe_error_title), res.message, null);
            }
        });
        viewModel.getVisible().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            Resource<?> res = viewModel.getState().getValue();
            boolean loading = res == null || res.isLoading();
            binding.empty.getRoot().setVisibility(!loading && list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
