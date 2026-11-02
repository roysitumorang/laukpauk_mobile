package id.laukpauk.ui.home;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.ui.product.ProductActions;

/** Taps on a product tile: open the product, open its seller, or "Beli". */
public interface ProdukListener {
    void onProduk(HomeProduk produk);

    void onToko(HomeProduk produk);

    void onBeli(HomeProduk produk);

    /** The standard behaviour: detailProdukFragment, penjualFragment, ProductActions.beli. */
    static ProdukListener navigating(final Fragment fragment) {
        return new ProdukListener() {
            @Override
            public void onProduk(HomeProduk p) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                NavHostFragment.findNavController(fragment).navigate(R.id.detailProdukFragment, args);
            }

            @Override
            public void onToko(HomeProduk p) {
                if (p.idPenjual == null) {
                    return;
                }
                Bundle args = new Bundle();
                args.putString("idPenjual", p.idPenjual);
                NavHostFragment.findNavController(fragment).navigate(R.id.penjualFragment, args);
            }

            @Override
            public void onBeli(HomeProduk p) {
                ProductActions.beli(fragment, p.id, p.nama);
            }
        };
    }
}
