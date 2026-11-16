package id.laukpauk.ui.promo;

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
import id.laukpauk.data.model.promo.PromoItemDetail;
import id.laukpauk.databinding.FragmentDetailPromoItemBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.product.ProductActions;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import id.laukpauk.util.ServerPath;

/** DetailPromoItem.dart. Argument: idPromoItem. */
public class DetailPromoItemFragment extends Fragment {

    private FragmentDetailPromoItemBinding binding;
    private DetailPromoItemViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailPromoItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        String idPromoItem = requireArguments().getString("idPromoItem", "");
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new DetailPromoItemViewModel(c.promoRepository, idPromoItem)))
                .get(DetailPromoItemViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.yuhu.yuhuImage, "icons/timbangan.png");
        binding.refresh.setOnRefreshListener(viewModel::load);

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::render);
        if (viewModel.getDetail().getValue() == null) {
            viewModel.load();
        }
    }

    private void render(Resource<PromoItemDetail> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        PromoItemDetail item = res.isSuccess() ? res.data : null;
        if (item == null) {
            binding.content.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        binding.notFound.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);

        PromoItemDetail.DetailPromo promo = item.detailPromo != null ? item.detailPromo : new PromoItemDetail.DetailPromo();
        PromoItemDetail.ProductDetail product = item.productDetail != null ? item.productDetail : new PromoItemDetail.ProductDetail();
        PromoItemDetail.UnitDetail unit = item.unitDetail != null ? item.unitDetail : new PromoItemDetail.UnitDetail();

        Images.url(binding.photo, ServerPath.baseURL + "/" + ServerPath.uploadImageProduct + "/" + nn(product.photo));

        binding.productName.setText(nn(product.name).toUpperCase(Locale.ROOT));
        binding.category.setText(nn(product.productCategoryName).toUpperCase(Locale.ROOT));
        bindAction(item, product);

        boolean promoActive = nn(promo.status).toLowerCase(Locale.ROOT).equals("aktif");
        binding.promoBox.setBackgroundResource(promoActive ? R.drawable.bg_promo_active : R.drawable.bg_promo_inactive);
        binding.promoName.setText(nn(promo.name));
        int netPrice = Json.parseInt(unit.price, 0)
                - (Json.parseInt(unit.discount, 0) + item.promoDiscount());
        binding.promoPrice.setText(Currency.formatRupiah(netPrice, 0));
        binding.special.setText(getString(R.string.promo_special, nn(unit.name)));
        binding.promoDescription.setText(nn(promo.description));
        binding.inactive.setVisibility(promoActive ? View.GONE : View.VISIBLE);

        setNote(binding.productDescription, nn(product.remarks));
        setNote(binding.promoNote, nn(item.note));
    }

    private void bindAction(PromoItemDetail item, PromoItemDetail.ProductDetail product) {
        boolean active = nn(item.statusPromo).toLowerCase(Locale.ROOT).equals("aktif");
        binding.action.setVisibility(active ? View.VISIBLE : View.GONE);
        if (!active) {
            return;
        }
        binding.action.setText(R.string.promo_buy);
        binding.action.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.green)));
        binding.action.setOnClickListener(v -> ProductActions.buy(this, nn(product.id), nn(product.name), nn(item.idPromo)));
    }

    /** Muted text, or an italic "Tidak ada deskripsi produk" placeholder. */
    private void setNote(android.widget.TextView view, String text) {
        boolean empty = text.isEmpty();
        view.setText(empty ? getString(R.string.promo_no_description) : text);
        view.setTypeface(null, empty ? android.graphics.Typeface.ITALIC : android.graphics.Typeface.NORMAL);
    }

    private static String nn(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
