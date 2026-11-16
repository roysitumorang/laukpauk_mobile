package id.laukpauk.ui.home;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

import id.laukpauk.data.model.home.HomeProduct;

/** DiffUtil callback shared by the product adapters. */
final class HomeProductDiff extends DiffUtil.ItemCallback<HomeProduct> {
    static final HomeProductDiff INSTANCE = new HomeProductDiff();

    @Override
    public boolean areItemsTheSame(@NonNull HomeProduct a, @NonNull HomeProduct b) {
        return Objects.equals(a.id, b.id);
    }

    @Override
    public boolean areContentsTheSame(@NonNull HomeProduct a, @NonNull HomeProduct b) {
        return Objects.equals(a.name, b.name) && Objects.equals(a.photo, b.photo)
                && a.isStockAvailable() == b.isStockAvailable()
                && a.isInActivePromo() == b.isInActivePromo();
    }
}
