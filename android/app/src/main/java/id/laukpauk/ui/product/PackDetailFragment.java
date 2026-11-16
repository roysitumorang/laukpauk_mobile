package id.laukpauk.ui.product;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Pack;
import id.laukpauk.databinding.FragmentPackDetailBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** DetailPaket.dart. Arg: packId. */
public class PackDetailFragment extends Fragment {

    private FragmentPackDetailBinding binding;
    private PackDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPackDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String packId = requireArguments().getString("idPaket", "");
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new PackDetailViewModel(c.packRepository, packId))).get(PackDetailViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.yuhu.yuhuImage, "icons/timbangan.png");
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.othersEmptyImage, "empty.png");

        OthersAdapter othersAdapter = new OthersAdapter(ServerPath.uploadImagePack, true, 12f, 10,
                item -> {
                    Bundle args = new Bundle();
                    args.putString("idPaket", item.id);
                    NavHostFragment.findNavController(this).navigate(R.id.packDetailFragment, args,
                            new NavOptions.Builder().setPopUpTo(R.id.packDetailFragment, true).build());
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

    private void render(Resource<Pack> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.content.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        Pack p = res.isSuccess() ? res.data : null;
        binding.notFound.setVisibility(p == null ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(p == null ? View.GONE : View.VISIBLE);
        if (p == null) {
            return;
        }
        boolean isDefault = DefaultImage.defaultPack.equalsIgnoreCase(p.photo);
        binding.image.setScaleType(isDefault ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(binding.image, Images.server(ServerPath.uploadImagePack, p.photo == null ? "" : p.photo));
        binding.name.setText(p.name == null ? "" : p.name.toUpperCase());

        double percent = p.discount >= 1 && p.price >= 1 ? (double) p.discount / p.price * 100 : 0;
        binding.discountRow.setVisibility(percent >= 0.1 ? View.VISIBLE : View.GONE);
        binding.strikePrice.setText(Currency.formatRupiah(p.price, 0));
        binding.strikePrice.setPaintFlags(binding.strikePrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        binding.discount.setText(getString(R.string.product_discount, String.format(Locale.US, "%.1f", percent)));
        binding.price.setText(Currency.formatRupiah(p.price - p.discount, 0));

        boolean has = p.remarks != null && !p.remarks.isEmpty();
        binding.description.setText(has ? p.remarks : getString(R.string.product_pack_description_empty));
        binding.description.setTypeface(null, has ? Typeface.NORMAL : Typeface.ITALIC);

        binding.buy.setText(R.string.product_buy);
        binding.buy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.green)));
        binding.buy.setOnClickListener(v -> ProductActions.buyPack(this, p.id, p.name));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
