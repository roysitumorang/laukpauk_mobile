package id.laukpauk.ui.common;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.databinding.ViewAppBarBinding;

/** Sets up the shared green app bar ({@code @layout/view_app_bar}): title, back arrow, green status bar. */
public final class AppBars {
    private AppBars() {}

    public static void setup(Fragment fragment, ViewAppBarBinding appBar, CharSequence title) {
        appBar.toolbar.setTitle(title);
        appBar.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(fragment).navigateUp());
        StatusBar.green(fragment);
    }
}
