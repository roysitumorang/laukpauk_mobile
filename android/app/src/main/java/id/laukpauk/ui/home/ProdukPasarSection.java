package id.laukpauk.ui.home;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.Pasar;

/** One "Produk Pasar X" row of the home page: a market and its first products ({@code produk} null = loading). */
public final class ProdukPasarSection {
    public final Pasar pasar;
    @Nullable
    public final List<HomeProduk> produk;

    public ProdukPasarSection(Pasar pasar, @Nullable List<HomeProduk> produk) {
        this.pasar = pasar;
        this.produk = produk;
    }
}
