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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.databinding.FragmentRecipeBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.SearchBox;

/** Resep.dart: the recipe list ({@code GET buyer/recipes}; a bottom navigation tab). */
public class RecipeFragment extends Fragment {

    private FragmentRecipeBinding binding;
    private RecipeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new RecipeViewModel(c.recipeRepository)))
                .get(RecipeViewModel.class);

        binding.header.bind(this);
        binding.empty.emptyText.setText(R.string.recipe_empty);

        RecipeAdapter adapter = new RecipeAdapter(new RecipeAdapter.Listener() {
            @Override
            public void onOpen(id.laukpauk.data.model.recipe.BuyerRecipe recipe) {
                Bundle args = new Bundle();
                args.putString("idResep", recipe.id);
                NavHostFragment.findNavController(RecipeFragment.this).navigate(R.id.recipeDetailFragment, args);
            }

            @Override
            public void onLink(String link) {
                LinkOpener.open(RecipeFragment.this, link, R.string.recipe_url_invalid);
            }
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0 && !rv.canScrollVertically(1)) {
                    viewModel.loadMore();
                }
            }
        });

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
            List<id.laukpauk.data.model.recipe.BuyerRecipe> list = res.isSuccess() && res.data != null
                    ? res.data : new java.util.ArrayList<>();
            if (!loading) {
                adapter.submitList(list);
            }
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
