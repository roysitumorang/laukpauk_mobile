package id.laukpauk.ui.product;

import android.graphics.Paint;
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

import java.util.List;
import java.util.Locale;
import android.widget.ImageView;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.model.product.BuyerProductVariant;
import id.laukpauk.databinding.FragmentProductDetailBinding;
import id.laukpauk.databinding.ItemProductVariantBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** Product detail from {@code GET buyer/products/:id}: photo, name, category, description, variants. Arg: idProduk. */
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

    private void render(Resource<BuyerProduct> res) {
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
        BuyerProduct p = res.isSuccess() ? res.data : null;
        binding.notFound.setVisibility(p == null ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(p == null ? View.GONE : View.VISIBLE);
        if (p == null) {
            return;
        }
        boolean isDefault = p.photo == null || p.photo.trim().isEmpty()
                || p.photo.toLowerCase(Locale.ROOT).endsWith("-default.png");
        binding.image.setScaleType(isDefault ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(binding.image, Images.product(p.photo));
        binding.name.setText(p.name == null ? "" : p.name.toUpperCase(Locale.ROOT));
        boolean hasCategory = p.categoryName != null && !p.categoryName.trim().isEmpty();
        binding.category.setText((hasCategory ? p.categoryName : getString(R.string.product_category_unknown))
                .toUpperCase(Locale.ROOT));
        binding.category.setTypeface(null, hasCategory ? Typeface.NORMAL : Typeface.ITALIC);
        boolean has = p.description != null && !p.description.trim().isEmpty();
        binding.description.setText(has ? p.description : getString(R.string.product_description_empty));
        binding.description.setTypeface(null, has ? Typeface.NORMAL : Typeface.ITALIC);

        renderVariants(p.variants);

        binding.outOfStock.setVisibility(p.readyStock ? View.GONE : View.VISIBLE);
        binding.buy.setVisibility(p.readyStock ? View.VISIBLE : View.GONE);
        binding.buy.setText(R.string.product_buy);
        binding.buy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.green)));
        binding.buy.setOnClickListener(v -> {
            if (p.isPack()) {
                ProductActions.buyPack(this, p.id, p.name);
            } else {
                ProductActions.buy(this, p.id, p.name);
            }
        });
    }

    /** One row per variant: unit, sale price (selling price struck through when discounted), discount, note. */
    private void renderVariants(@Nullable List<BuyerProductVariant> variants) {
        binding.variants.removeAllViews();
        int shown = 0;
        if (variants != null) {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            for (BuyerProductVariant v : variants) {
                if (v == null) {
                    continue;
                }
                ItemProductVariantBinding row = ItemProductVariantBinding.inflate(inflater, binding.variants, false);
                row.unit.setText(v.stockUnit == null ? "" : v.stockUnit);
                row.price.setText(Currency.formatRupiah(v.price(), 0));
                boolean discounted = v.hasDiscount() && v.price() < v.sellingPrice;
                row.strikePrice.setVisibility(discounted ? View.VISIBLE : View.GONE);
                row.strikePrice.setText(Currency.formatRupiah(v.sellingPrice, 0));
                row.strikePrice.setPaintFlags(row.strikePrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                String discount = discountText(v);
                row.discount.setVisibility(discount == null ? View.GONE : View.VISIBLE);
                row.discount.setText(discount);
                boolean hasNote = v.description != null && !v.description.trim().isEmpty();
                row.note.setVisibility(hasNote ? View.VISIBLE : View.GONE);
                row.note.setText(hasNote ? v.description.trim() : null);
                binding.variants.addView(row.getRoot());
                shown++;
            }
        }
        binding.variantsEmpty.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
    }

    /** "Diskon 10 %" / "Diskon Rp. 2.000", plus "untuk pembelian min. 3" when it starts from a quantity. */
    @Nullable
    private String discountText(BuyerProductVariant v) {
        if (!v.hasDiscount()) {
            return null;
        }
        String text = v.discountType == BuyerProductVariant.DISCOUNT_PERCENTAGE
                ? getString(R.string.product_discount, String.valueOf(v.discountRate))
                : getString(R.string.product_discount_amount, Currency.formatRupiah(v.discountAmount, 0));
        if (v.discountMinimumQuantity > 1) {
            text = getString(R.string.product_discount_from_quantity, text, (int) v.discountMinimumQuantity);
        }
        return text;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
