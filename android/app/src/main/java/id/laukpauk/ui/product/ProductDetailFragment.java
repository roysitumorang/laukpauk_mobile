package id.laukpauk.ui.product;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.ImageView;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Product;
import id.laukpauk.databinding.FragmentProductDetailBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** ProductDetail.dart. Arg: productId. */
public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private ProductDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String productId = requireArguments().getString("idProduk", "");
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ProductDetailViewModel(c.productRepository, productId))).get(ProductDetailViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.yuhu.yuhuImage, "icons/timbangan.png");
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.othersEmptyImage, "empty.png");
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(viewModel::load);

        OthersAdapter othersAdapter = new OthersAdapter(ServerPath.uploadImageProduct, false, 10f, 7,
                item -> {
                    Bundle args = new Bundle();
                    args.putString("idProduk", item.id);
                    NavHostFragment.findNavController(this).navigate(R.id.productDetailFragment, args,
                            new NavOptions.Builder().setPopUpTo(R.id.productDetailFragment, true).build());
                });
        binding.others.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.others.setAdapter(othersAdapter);

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::render);
        viewModel.getOthers().observe(getViewLifecycleOwner(), res -> {
            binding.othersProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                boolean empty = !res.isSuccess() || res.data == null || res.data.isEmpty();
                othersAdapter.submitList(empty ? null : res.data);
                binding.othersEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void render(Resource<Product> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.refresh.isRefreshing() || binding.content.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        binding.refresh.setRefreshing(false);
        Product p = res.isSuccess() ? res.data : null;
        binding.notFound.setVisibility(p == null ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(p == null ? View.GONE : View.VISIBLE);
        if (p == null) {
            return;
        }
        boolean isDefault = DefaultImage.defaultProduct.equalsIgnoreCase(p.photo);
        binding.image.setScaleType(isDefault ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(binding.image, Images.server(ServerPath.uploadImageProduct, p.photo == null ? "" : p.photo));
        binding.name.setText(p.name == null ? "" : p.name.toUpperCase());
        boolean hasCategory = p.productCategoryName != null;
        binding.category.setText((hasCategory ? p.productCategoryName : getString(R.string.product_category_unknown)).toUpperCase());
        binding.category.setTypeface(null, hasCategory ? Typeface.NORMAL : Typeface.ITALIC);
        boolean has = p.remarks != null && !p.remarks.isEmpty();
        binding.description.setText(has ? p.remarks : getString(R.string.product_description_empty));
        binding.description.setTypeface(null, has ? Typeface.NORMAL : Typeface.ITALIC);

        binding.buy.setText(R.string.product_buy);
        binding.buy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.green)));
        binding.buy.setOnClickListener(v -> ProductActions.buy(this, p.id, p.name));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
