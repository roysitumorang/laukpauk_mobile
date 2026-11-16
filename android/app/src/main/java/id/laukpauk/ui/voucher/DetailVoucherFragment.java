package id.laukpauk.ui.voucher;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.databinding.FragmentDetailVoucherBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** DetailVoucher.dart. Argument: idVoucher. */
public class DetailVoucherFragment extends Fragment {

    private FragmentDetailVoucherBinding binding;
    private DetailVoucherViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailVoucherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String idVoucher = requireArguments().getString("idVoucher", "");
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new DetailVoucherViewModel(c.voucherRepository, idVoucher)))
                .get(DetailVoucherViewModel.class);
        AppBars.setup(this, binding.appBar, getString(R.string.voucher_detail_title));
        Images.asset(binding.notFoundImage, "empty.png");
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(viewModel::load);

        viewModel.getDetail().observe(getViewLifecycleOwner(), res -> render());
        viewModel.getUsedVoucherId().observe(getViewLifecycleOwner(), used -> render());
        viewModel.load();
    }

    private void render() {
        Resource<Voucher> res = viewModel.getDetail().getValue();
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        final Voucher v = res.data;
        if (v == null) {
            binding.content.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        binding.notFound.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);

        int usageCount = Voucher.toInt(v.usageCount);
        int maximumUsage = Voucher.toInt(v.maximumUsage);
        String type = v.type == null ? "" : v.type;
        boolean used = viewModel.isThisVoucherUsed();

        Images.url(binding.photo, Images.server(ServerPath.uploadImageVoucher, v.photo));
        binding.name.setText(v.name);
        binding.type.setText(type.toUpperCase(Locale.ROOT));
        binding.explanation.setText(type.toLowerCase(Locale.ROOT).equals("ongkir")
                ? R.string.voucher_shipping_type : R.string.voucher_shopping_type);
        binding.value.setText(Currency.formatRupiah(Voucher.toInt(v.value), 0));
        binding.code.setText(v.code == null ? "" : v.code.toUpperCase(Locale.ROOT));
        binding.minimum.setText(Currency.formatRupiah(Voucher.toInt(v.minimumShopping), 0));
        binding.maxUsage.setText(getString(R.string.voucher_times_used, Currency.formatUnit(maximumUsage)));
        binding.usage.setText(usageCount >= 1
                ? getString(R.string.voucher_times_used, Currency.formatUnit(usageCount))
                : getString(R.string.voucher_no_usage));
        binding.valid.setText(getString(R.string.voucher_valid_range, v.validFrom, v.validUntil));
        binding.description.setText(v.description);

        if (maximumUsage - usageCount >= 1) {
            binding.toggle.setVisibility(View.VISIBLE);
            binding.exhausted.setVisibility(View.GONE);
            binding.toggle.setText(used ? R.string.voucher_cancel_use : R.string.voucher_use);
            binding.toggle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    used ? R.color.text_danger : R.color.green)));
            binding.toggle.setOnClickListener(x -> {
                boolean nowUsed = viewModel.toggle();
                String name = v.name == null ? "" : v.name;
                Messages.snack(this, getString(nowUsed ? R.string.voucher_used_message
                        : R.string.voucher_cancelled_message, name));
            });
        } else {
            binding.toggle.setVisibility(View.GONE);
            binding.exhausted.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
