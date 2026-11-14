package id.laukpauk.ui.home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.laukpauk.R;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.Konten;
import id.laukpauk.data.model.home.KontenItem;
import id.laukpauk.data.repository.HomeRepository;
import id.laukpauk.databinding.ViewHomeProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.startup.MainRouter;

/** Navigation shared by the home page and the search screen. */
public final class HomeNav {
    private HomeNav() {}

    /** The "Produk" block without the "pilih lokasi lain" button (search screen). */
    public static void bindProdukSection(Fragment f, ViewHomeProdukBinding b, ProdukSectionViewModel vm) {
        new ProdukSection(f, b, vm, false);
    }

    /** The page with every product. */
    public static void openSemuaProduk(Fragment f) {
        NavHostFragment.findNavController(f).navigate(R.id.semuaProdukFragment);
    }

    /** listProdukFragment with the products as JSON text. */
    public static void openListProduk(Fragment f, String title, String subTitle, List<? extends HomeProduk> produk) {
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("subTitle", subTitle);
        args.putString("dataProduk", AppContainer.from(f.requireContext()).apiClient.toJson(produk,
                com.squareup.moshi.Types.newParameterizedType(List.class, HomeProduk.class)));
        args.putBoolean("isReadyToShow", true);
        NavHostFragment.findNavController(f).navigate(R.id.listProdukFragment, args);
    }

    /** A konten block's "Lihat Semua": fetch every item, then the list screen. */
    public static void openKontenSemua(Fragment f, Konten konten) {
        HomeRepository home = AppContainer.from(f.requireContext()).homeRepository;
        home.getKontenItem(konten.id, null, result -> {
            if (!f.isAdded()) {
                return;
            }
            String nama = konten.namaDivisi == null ? "" : konten.namaDivisi.toUpperCase(Locale.getDefault());
            List<KontenItem> items = result.data;
            if (items == null) {
                Toast.makeText(f.requireContext(), R.string.home_konten_kosong, Toast.LENGTH_SHORT).show();
                return;
            }
            openListProduk(f, nama, f.getString(R.string.home_list_subtitle_konten, nama), items);
        });
    }

    /** Legacy KontenItemBanner tap: pushNamed("/" + route, {route: pilihan}). */
    public static void openBanner(Fragment f, Konten konten, KontenItem item) {
        if (konten == null || item == null || konten.route == null) {
            return;
        }
        Map<String, String> args = new HashMap<>();
        args.put(konten.route, item.pilihan);
        MainRouter.route(NavHostFragment.findNavController(f), "/" + konten.route, args);
    }
}
