package id.laukpauk.ui.home;

import android.view.View;

import androidx.fragment.app.Fragment;

import id.laukpauk.R;
import id.laukpauk.databinding.ViewHomeProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.delivery.DeliveryFlow;

/** The "Produk" block of the home page and the search screen: the first products and "Lihat semua". */
final class ProdukSection {

    private boolean loaded;

    ProdukSection(Fragment f, ViewHomeProdukBinding b, ProdukSectionViewModel vm, boolean showPilihLokasi) {
        AppContainer c = AppContainer.from(f.requireContext());
        ProdukTileAdapter adapter = new ProdukTileAdapter(c.homeRepository::getLowestPrice,
                ProdukListener.navigating(f));
        b.produkList.setAdapter(adapter);

        b.lihatSemua.setOnClickListener(v -> HomeNav.openSemuaProduk(f));
        b.pilihLokasi.setVisibility(showPilihLokasi ? View.VISIBLE : View.GONE);
        b.pilihLokasi.setOnClickListener(v -> {
            c.deliveryRepository.clear();
            DeliveryFlow.ask(f, null);
        });
        b.empty.emptyText.setText(R.string.home_produk_kosong);

        vm.getProduk().observe(f.getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading() && !loaded) {
                b.loadingBox.setVisibility(View.VISIBLE);
                b.produkList.setVisibility(View.GONE);
                b.empty.getRoot().setVisibility(View.GONE);
                return;
            }
            if (res.isLoading()) {
                return;
            }
            loaded = true;
            boolean ada = res.isSuccess() && res.data != null && !res.data.isEmpty();
            adapter.submitList(ada ? res.data : null);
            b.loadingBox.setVisibility(View.GONE);
            b.produkList.setVisibility(ada ? View.VISIBLE : View.GONE);
            b.empty.getRoot().setVisibility(ada ? View.GONE : View.VISIBLE);
        });
        vm.start();
    }
}
