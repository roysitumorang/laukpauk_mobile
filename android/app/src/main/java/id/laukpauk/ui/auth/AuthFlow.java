package id.laukpauk.ui.auth;

import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import id.laukpauk.R;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.startup.MainRouter;

/** Navigation and view helpers shared by the auth screens (legacy AuthRoute / AuthUi). */
final class AuthFlow {
    private AuthFlow() {}

    private static final Set<Integer> AUTH_SCREENS = new HashSet<>(Arrays.asList(
            R.id.loginFragment, R.id.registerFragment, R.id.oTPRegistrasiFragment, R.id.oTPVerifikasiUserFragment,
            R.id.oTPNotCompletedFragment, R.id.kirimUlangOTPFragment, R.id.lupaPasswordFragment));

    /** Legacy pushReplacement / pushAndRemoveUntil between auth screens. */
    static void replaceWith(Fragment f, int destination) {
        NavController nav = NavHostFragment.findNavController(f);
        NavDestination current = nav.getCurrentDestination();
        NavOptions.Builder options = new NavOptions.Builder();
        if (current != null) {
            options.setPopUpTo(current.getId(), true);
        }
        nav.navigate(destination, null, options.build());
    }

    /**
     * End of the login / registration / OTP chain: pops every auth screen and delivers LOGIN_RESULT=true to the
     * screen below (the one that required login), or opens Home when nothing is below.
     */
    static void finish(Fragment f) {
        NavController nav = NavHostFragment.findNavController(f);
        while (true) {
            NavBackStackEntry previous = nav.getPreviousBackStackEntry();
            if (previous == null) {
                NavDestination current = nav.getCurrentDestination();
                NavOptions.Builder options = new NavOptions.Builder();
                if (current != null) {
                    options.setPopUpTo(current.getId(), true);
                }
                nav.navigate(R.id.homePageFragment, null, options.build());
                return;
            }
            if (AUTH_SCREENS.contains(previous.getDestination().getId())) {
                nav.popBackStack();
                continue;
            }
            previous.getSavedStateHandle().set(AuthGate.LOGIN_RESULT, Boolean.TRUE);
            nav.popBackStack();
            return;
        }
    }

    static void goHome(Fragment f) {
        NavController nav = NavHostFragment.findNavController(f);
        if (!nav.popBackStack(R.id.homePageFragment, false)) {
            NavDestination current = nav.getCurrentDestination();
            NavOptions.Builder options = new NavOptions.Builder();
            if (current != null) {
                options.setPopUpTo(current.getId(), true);
            }
            nav.navigate(R.id.homePageFragment, null, options.build());
        }
    }

    /** Spinner replaces the label while busy. */
    static void setBusy(View button, View spinner, boolean busy) {
        button.setEnabled(!busy);
        button.setAlpha(busy ? 0f : 1f);
        spinner.setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    /** The eye icon of a password field. */
    static void bindPasswordToggle(EditText edit, ImageView icon) {
        final boolean[] shown = {false};
        icon.setOnClickListener(v -> {
            shown[0] = !shown[0];
            int sel = edit.getSelectionEnd();
            edit.setTransformationMethod(shown[0] ? null : PasswordTransformationMethod.getInstance());
            icon.setImageResource(shown[0] ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
            if (sel >= 0) {
                edit.setSelection(sel);
            }
        });
    }

    static void alert(Fragment f, int title, CharSequence message, int positive, Runnable onPositive,
                      int negative, Runnable onNegative) {
        if (!f.isAdded()) {
            return;
        }
        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(f.requireContext())
                .setTitle(title).setMessage(message).setCancelable(false)
                .setPositiveButton(positive, (d, w) -> {
                    if (onPositive != null) {
                        onPositive.run();
                    }
                });
        if (negative != 0) {
            b.setNegativeButton(negative, (d, w) -> {
                if (onNegative != null) {
                    onNegative.run();
                }
            });
        }
        b.show();
    }

    static void home(Fragment f) {
        MainRouter.goHome(NavHostFragment.findNavController(f));
    }
}
