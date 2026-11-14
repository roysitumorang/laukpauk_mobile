package id.laukpauk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import id.laukpauk.databinding.ActivityMainBinding;
import id.laukpauk.ui.startup.MainRouter;

/**
 * The app's single Activity. Every screen is a Fragment destination of {@code res/navigation/nav_graph.xml},
 * hosted in a NavHostFragment; the five top-level screens are reachable from the bottom navigation.
 *
 * Android 15/16: the window is always edge-to-edge (the opt-out is ignored for targetSdk 36+). This
 * Activity applies the system-bar, display-cutout and keyboard insets once for every screen, and paints the
 * area behind the status bar ({@link #setStatusBarColor(int)}: green under the green headers, white elsewhere).
 * Back navigation, including the predictive back gesture, is handled by the NavController.
 */
public class MainActivity extends AppCompatActivity {

    /** Screens that showed the Flutter app's Footer; the bottom navigation is visible only on these. */
    private static final Set<Integer> BOTTOM_NAV_DESTINATIONS = new HashSet<>(Arrays.asList(
            R.id.homePageFragment, R.id.pesananSayaFragment, R.id.resepFragment, R.id.promoFragment,
            R.id.userAndDashboardFragment, R.id.listProdukFragment, R.id.produkDiDalamPasarFragment,
            R.id.detailProdukFragment, R.id.detailPaketFragment, R.id.detailPromoItemFragment,
            R.id.detailResepFragment, R.id.repeatOrderFragment, R.id.notifikasiFragment,
            R.id.detailNotifikasiGlobalFragment));

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme); // the manifest starts with LaunchTheme (launch background)
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            ViewGroup.LayoutParams lp = binding.statusScrim.getLayoutParams();
            if (lp.height != bars.top) {
                lp.height = bars.top;
                binding.statusScrim.setLayoutParams(lp);
            }
            v.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHost);
        navController = host.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            binding.bottomNav.setVisibility(
                    BOTTOM_NAV_DESTINATIONS.contains(destination.getId()) ? View.VISIBLE : View.GONE);
            // each screen starts with a white status bar; screens with the green header tint it again
            setStatusBarColor(Color.WHITE);
        });

        if (savedInstanceState == null) {
            MainRouter.handle(this, navController, getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        MainRouter.handle(this, navController, intent);
    }

    /**
     * Colour behind the status bar (Window#setStatusBarColor is a no-op on Android 15+). Status-bar icons
     * turn dark on light colours and light on dark ones.
     */
    public void setStatusBarColor(int color) {
        binding.statusScrim.setBackgroundColor(color);
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255.0;
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        c.setAppearanceLightStatusBars(luminance > 0.6);
        c.setAppearanceLightNavigationBars(true);
    }

    public NavController getNavController() {
        return navController;
    }

    /** The fragment of the current destination (for app-wide dialogs such as the expired-schedule alert). */
    @androidx.annotation.Nullable
    public androidx.fragment.app.Fragment currentFragment() {
        androidx.fragment.app.Fragment host = getSupportFragmentManager().findFragmentById(R.id.navHost);
        if (host == null) {
            return null;
        }
        return host.getChildFragmentManager().getPrimaryNavigationFragment();
    }
}
