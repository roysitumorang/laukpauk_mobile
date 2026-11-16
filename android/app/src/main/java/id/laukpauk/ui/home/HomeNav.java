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
import id.laukpauk.data.model.home.HomeProduct;
import id.laukpauk.data.model.home.Content;
import id.laukpauk.data.model.home.ContentItem;
import id.laukpauk.data.repository.HomeRepository;
import id.laukpauk.databinding.ViewHomeProductBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.startup.MainRouter;

/** Navigation shared by the home page and the search screen. */
public final class HomeNav {
    private HomeNav() {}

    /** The "Produk" block (search screen). */
    public static void bindProductSection(Fragment f, ViewHomeProductBinding b, ProductSectionViewModel vm) {
        new ProductSection(f, b, vm);
    }

    /** The page with every product. */
    public static void openAllProducts(Fragment f) {
        NavHostFragment.findNavController(f).navigate(R.id.allProductsFragment);
    }

    /** productListFragment with the products as JSON text. */
    public static void openProductList(Fragment f, String title, String subTitle, List<? extends HomeProduct> product) {
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("subTitle", subTitle);
        args.putString("dataProduk", AppContainer.from(f.requireContext()).apiClient.toJson(product,
                com.squareup.moshi.Types.newParameterizedType(List.class, HomeProduct.class)));
        args.putBoolean("isReadyToShow", true);
        NavHostFragment.findNavController(f).navigate(R.id.productListFragment, args);
    }

    /** A konten block's "Lihat Semua": fetch every item, then the list screen. */
    public static void openContentAll(Fragment f, Content content) {
        HomeRepository home = AppContainer.from(f.requireContext()).homeRepository;
        home.getContentItem(content.id, null, result -> {
            if (!f.isAdded()) {
                return;
            }
            String name = content.divisionName == null ? "" : content.divisionName.toUpperCase(Locale.getDefault());
            List<ContentItem> items = result.data;
            if (items == null) {
                Toast.makeText(f.requireContext(), R.string.home_content_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            openProductList(f, name, f.getString(R.string.home_list_subtitle_content, name), items);
        });
    }

    /** Legacy KontenItemBanner tap: pushNamed("/" + route, {route: pilihan}). */
    public static void openBanner(Fragment f, Content content, ContentItem item) {
        if (content == null || item == null || content.route == null) {
            return;
        }
        Map<String, String> args = new HashMap<>();
        args.put(content.route, item.choice);
        MainRouter.route(NavHostFragment.findNavController(f), "/" + content.route, args);
    }
}
