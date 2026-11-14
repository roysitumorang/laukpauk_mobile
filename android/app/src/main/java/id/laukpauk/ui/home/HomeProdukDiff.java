package id.laukpauk.ui.home;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

import id.laukpauk.data.model.home.HomeProduk;

/** DiffUtil callback shared by the product adapters. */
final class HomeProdukDiff extends DiffUtil.ItemCallback<HomeProduk> {
    static final HomeProdukDiff INSTANCE = new HomeProdukDiff();

    @Override
    public boolean areItemsTheSame(@NonNull HomeProduk a, @NonNull HomeProduk b) {
        return Objects.equals(a.id, b.id);
    }

    @Override
    public boolean areContentsTheSame(@NonNull HomeProduk a, @NonNull HomeProduk b) {
        return Objects.equals(a.nama, b.nama) && Objects.equals(a.foto, b.foto)
                && a.isStokAvailable() == b.isStokAvailable()
                && a.isInActivePromo() == b.isInActivePromo();
    }
}
