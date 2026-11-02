package id.laukpauk.ui.home;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import id.laukpauk.R;
import id.laukpauk.data.model.home.Pasar;
import id.laukpauk.databinding.ViewHomePasarBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.SearchBox;
import id.laukpauk.ui.delivery.DeliveryFlow;

/**
 * Legacy PasarSekitarSaya bound to {@code view_home_pasar.xml}: market grid (with distance), optional title row,
 * name search, "Pilih Lokasi Lain" button and the "Pasar Terdekat" product rows.
 */
final class PasarSection {
    private boolean loaded;

    PasarSection(Fragment f, ViewHomePasarBinding b, PasarViewModel vm, boolean showTitle, boolean showPencarian,
                 boolean showPilihLokasi, boolean showProduk) {
        AppContainer c = AppContainer.from(f.requireContext());
        PasarAdapter adapter = new PasarAdapter(pasar -> HomeNav.openPasar(f, pasar));
        b.pasarList.setLayoutManager(new GridLayoutManager(f.requireContext(), 3));
        b.pasarList.setAdapter(adapter);

        b.titleRow.setVisibility(showTitle ? View.VISIBLE : View.GONE);
        b.lihatSemua.setOnClickListener(v ->
                NavHostFragment.findNavController(f).navigate(R.id.semuaPasarFragment));
        b.pilihLokasi.setVisibility(showPilihLokasi ? View.VISIBLE : View.GONE);
        b.pilihLokasi.setOnClickListener(v -> {
            c.deliveryRepository.clear();
            DeliveryFlow.ask(f, null);
        });
        b.empty.emptyText.setText(R.string.home_pasar_kosong);
        if (showPencarian) {
            b.search.getRoot().setVisibility(View.VISIBLE);
            SearchBox.bind(b.search, f.getString(R.string.home_cari_pasar_hint), vm.getQuery(), query -> {
                String q = query.toLowerCase();
                SearchBox.setText(b.search, q);
                vm.setQuery(q);
            }, () -> vm.setQuery(""));
        }

        vm.getShowJarak().observe(f.getViewLifecycleOwner(), show -> adapter.setShowJarak(Boolean.TRUE.equals(show)));
        vm.getPasar().observe(f.getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            if (res.isLoading() && !loaded) {
                b.loadingBox.setVisibility(View.VISIBLE);
                b.pasarList.setVisibility(View.GONE);
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
            b.pasarList.setVisibility(ada ? View.VISIBLE : View.GONE);
            b.empty.getRoot().setVisibility(ada ? View.GONE : View.VISIBLE);
        });

        if (showProduk) {
            ProdukPasarAdapter produk = new ProdukPasarAdapter(c.homeRepository::getLowestPrice,
                    ProdukListener.navigating(f), (Pasar pasar) -> HomeNav.openPasar(f, pasar));
            b.produkList.setLayoutManager(new LinearLayoutManager(f.requireContext()));
            b.produkList.setAdapter(produk);
            vm.getSections().observe(f.getViewLifecycleOwner(), list -> {
                boolean ada = list != null && !list.isEmpty();
                produk.submitList(list);
                b.produkTitle.setVisibility(ada ? View.VISIBLE : View.GONE);
                b.produkList.setVisibility(ada ? View.VISIBLE : View.GONE);
            });
        }
            vm.start();
    }
}
