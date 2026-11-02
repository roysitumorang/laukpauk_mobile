package id.laukpauk.ui.common;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import id.laukpauk.MainActivity;
import id.laukpauk.R;

/**
 * Colours the area behind the status bar for the current screen. MainActivity resets it to white on every
 * navigation; screens whose top bar is green (the Header, green app bars) call {@link #green(Fragment)} in
 * {@code onViewCreated}.
 */
public final class StatusBar {
    private StatusBar() {}

    public static void tint(Fragment fragment, int color) {
        if (fragment.getActivity() instanceof MainActivity) {
            ((MainActivity) fragment.getActivity()).setStatusBarColor(color);
        }
    }

    public static void green(Fragment fragment) {
        tint(fragment, ContextCompat.getColor(fragment.requireContext(), R.color.green));
    }
}
