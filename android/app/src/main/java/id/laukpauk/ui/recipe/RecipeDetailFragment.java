package id.laukpauk.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.Recipe;
import id.laukpauk.databinding.FragmentRecipeDetailBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** DetailResep.dart. Argument: recipeId. */
public class RecipeDetailFragment extends Fragment {

    private FragmentRecipeDetailBinding binding;
    private RecipeDetailViewModel viewModel;
    private AppContainer container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        container = AppContainer.from(requireContext());
        String recipeId = requireArguments().getString("idResep", "");
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new RecipeDetailViewModel(container.recipeRepository, recipeId)))
                .get(RecipeDetailViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.ingredientEmptyImage, "empty.png");
        binding.refresh.setOnRefreshListener(viewModel::load);

        IngredientAdapter adapter = new IngredientAdapter(new IngredientAdapter.Listener() {
            @Override
            public void onPickUnit(IngredientRow row, int unitIndex) {
                viewModel.pickUnit(row, unitIndex);
            }

            @Override
            public void onPickQuantity(IngredientRow row, int quantity) {
                viewModel.pickQuantity(row, quantity);
            }

            @Override
            public void onCart(IngredientRow row) {
                viewModel.toggle(row);
            }
        });
        binding.ingredient.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.ingredient.setAdapter(adapter);

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::renderDetail);
        viewModel.getItems().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.ingredientProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            boolean empty = res.isSuccess() && (res.data == null || res.data.isEmpty());
            binding.ingredientEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getRows().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            boolean any = total != null && total >= 0;
            binding.summary.setVisibility(any ? View.VISIBLE : View.GONE);
            if (any) {
                binding.total.setText(Currency.formatRupiah(total, 0));
            }
        });
        viewModel.getBusy().observe(getViewLifecycleOwner(), busy -> binding.shopping.setEnabled(!busy));
        binding.shopping.setOnClickListener(v -> viewModel.shopping(result -> {
            if (!isAdded()) {
                return;
            }
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                container.cartRepository.refreshCartCount();
                Messages.cartSnack(this, getString(R.string.recipe_cart_success));
            }
        }));

        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    private void renderDetail(Resource<Recipe> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        Recipe r = res.isSuccess() ? res.data : null;
        if (r == null) {
            binding.content.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        binding.notFound.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);

        Images.url(binding.photo, ServerPath.baseURL + "/" + ServerPath.uploadImageRecipe + "/" + r.photo);
        binding.name.setText(r.name);
        binding.remarks.setText(r.remarks);
        binding.fb.setOnClickListener(v -> LinkOpener.open(this, r.fbLink, R.string.recipe_url_invalid_detail));
        binding.ig.setOnClickListener(v -> LinkOpener.open(this, r.igLink, R.string.recipe_url_invalid_detail));
        binding.cookingMethod.setOnClickListener(v ->
                LinkOpener.open(this, r.igLink, R.string.recipe_url_invalid_detail));
        binding.share.setOnClickListener(v -> share(r.name == null ? "" : r.name));
    }

    private void share(String recipeName) {
        String name = recipeName.toUpperCase(Locale.ROOT);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.recipe_share_text, name));
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.recipe_share_subject, name));
        startActivity(Intent.createChooser(send, null));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
