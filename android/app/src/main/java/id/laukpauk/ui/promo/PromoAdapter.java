package id.laukpauk.ui.promo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.promo.PromoProduct;
import id.laukpauk.databinding.ItemPromoProductBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** The promo product cards (legacy PromoItem). */
public class PromoAdapter extends ListAdapter<PromoProduct, PromoAdapter.Holder> {

    public interface Listener {
        void onOpen(PromoProduct item);

        void onBuy(PromoProduct item);
    }

    private final Listener listener;

    public PromoAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<PromoProduct>() {
            @Override
            public boolean areItemsTheSame(@NonNull PromoProduct a, @NonNull PromoProduct b) {
                return String.valueOf(a.idPromoItem).equals(String.valueOf(b.idPromoItem));
            }

            @Override
            public boolean areContentsTheSame(@NonNull PromoProduct a, @NonNull PromoProduct b) {
                return a.promoNetPrice() == b.promoNetPrice() && a.grossPrice() == b.grossPrice()
                        && String.valueOf(a.productName).equals(String.valueOf(b.productName))
                        && String.valueOf(a.productPhoto).equals(String.valueOf(b.productPhoto))
                        && String.valueOf(a.shortDescription).equals(String.valueOf(b.shortDescription))
                        && String.valueOf(a.maxOrder).equals(String.valueOf(b.maxOrder));
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPromoProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position), listener);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemPromoProductBinding b;

        Holder(ItemPromoProductBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(PromoProduct item, Listener listener) {
            String photo = item.productPhoto == null ? "" : item.productPhoto;
            boolean isDefaultImage = photo.equals(DefaultImage.defaultProduct);
            b.photo.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
            Images.url(b.photo, ServerPath.baseURL + "/" + ServerPath.uploadImageProduct + "/compress/" + photo);

            int grossPrice = item.grossPrice();
            int discount = item.discount();
            double percentage = discount >= 1 && grossPrice >= 1 ? (double) discount / grossPrice * 100 : 0;
            if (percentage >= 0.1) {
                b.saving.setVisibility(View.VISIBLE);
                b.saving.setText(b.getRoot().getContext().getString(R.string.promo_saving,
                        String.format(Locale.US, "%.1f", percentage)));
            } else {
                b.saving.setVisibility(View.GONE);
            }
            b.max.setText(b.getRoot().getContext().getString(R.string.promo_max,
                    item.maxOrder == null ? "" : item.maxOrder));

            b.name.setText(item.productName);
            boolean hasDesc = item.shortDescription != null && !item.shortDescription.isEmpty();
            b.descriptionScroll.setVisibility(hasDesc ? View.VISIBLE : View.GONE);
            b.description.setText(item.shortDescription);

            b.netPrice.setText(Currency.formatRupiah(item.promoNetPrice(), 0));
            b.grossPrice.setText(Currency.formatRupiah(grossPrice, 0));
            b.grossPrice.setPaintFlags(b.grossPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

            b.card.setOnClickListener(v -> listener.onOpen(item));
            b.buy.setOnClickListener(v -> listener.onBuy(item));
        }
    }
}
