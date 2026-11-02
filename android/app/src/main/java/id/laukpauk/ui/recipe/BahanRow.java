package id.laukpauk.ui.recipe;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.recipe.ResepItem;
import id.laukpauk.data.model.recipe.SatuanProduk;

/** Immutable UI state of one ingredient row (legacy ResepItemItem). */
final class BahanRow {
    final int index;
    final ResepItem item;
    final boolean satuanLoaded;
    final List<SatuanProduk> satuan;
    @Nullable final SatuanProduk selectedSatuan;
    final int quantity;
    final boolean selected;

    BahanRow(int index, ResepItem item, boolean satuanLoaded, List<SatuanProduk> satuan,
             @Nullable SatuanProduk selectedSatuan, int quantity, boolean selected) {
        this.index = index;
        this.item = item;
        this.satuanLoaded = satuanLoaded;
        this.satuan = satuan;
        this.selectedSatuan = selectedSatuan;
        this.quantity = quantity;
        this.selected = selected;
    }

    int hargaBersih() {
        return selectedSatuan == null ? 0 : selectedSatuan.hargaBersih();
    }

    boolean sameAs(BahanRow o) {
        return index == o.index && satuanLoaded == o.satuanLoaded && satuan == o.satuan
                && selectedSatuan == o.selectedSatuan && quantity == o.quantity && selected == o.selected;
    }
}
