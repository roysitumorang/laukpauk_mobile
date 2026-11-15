package id.laukpauk.ui.common;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.di.AppContainer;

/**
 * Screens that need a signed-in user (the Dart app showed {@code Login(routeTo: ThisScreen())} instead).
 * Implements Android's conditional-navigation pattern:
 *
 * <pre>
 * // in the gated screen's onViewCreated:
 * AuthGate.require(this, this::loadContent);
 * </pre>
 * Signed in: {@code onAuthenticated} runs immediately. Otherwise the user is taken to Login; when Login
 * succeeds it publishes {@code LOGIN_RESULT = true} and pops back, and {@code onAuthenticated} runs. If the user
 * leaves Login without signing in (Login publishes {@code false} as soon as it opens), the gated screen
 * closes too. That pending {@code false} is read as soon as the gated screen comes back, before anything else:
 * re-opening Login there is what used to make Back on the Login page appear to do nothing.
 *
 * Contract for LoginFragment: in onViewCreated call {@code NavResults.setResult(this, AuthGate.LOGIN_RESULT, false)};
 * after a successful login call {@code NavResults.setResult(this, AuthGate.LOGIN_RESULT, true)} and
 * {@code popBackStack()}.
 */
public final class AuthGate {
    private AuthGate() {}

    public static final String LOGIN_RESULT = "login_successful";

    public static boolean isSignedIn(Fragment fragment) {
        return !AppContainer.from(fragment.requireContext()).session.getAuthToken().isEmpty();
    }

    public static void require(Fragment fragment, Runnable onAuthenticated) {
        if (isSignedIn(fragment)) {
            onAuthenticated.run();
            return;
        }
        final NavController nav = NavHostFragment.findNavController(fragment);
        NavBackStackEntry entry = nav.getCurrentBackStackEntry();
        if (entry != null && Boolean.FALSE.equals(entry.getSavedStateHandle().get(LOGIN_RESULT))) {
            // Back from Login without signing in: this screen was just re-created. Leave it too.
            entry.getSavedStateHandle().remove(LOGIN_RESULT);
            close(nav);
            return;
        }
        NavResults.<Boolean>observe(fragment, LOGIN_RESULT, success -> {
            if (Boolean.TRUE.equals(success)) {
                onAuthenticated.run();
            } else if (!isSignedIn(fragment)) {
                close(nav);
            }
        });
        nav.navigate(R.id.loginFragment);
    }

    /** Closes the gated screen: back to whatever is below it, or to Home when it is the first screen. */
    private static void close(NavController nav) {
        if (nav.popBackStack()) {
            return;
        }
        NavDestination current = nav.getCurrentDestination();
        NavOptions.Builder options = new NavOptions.Builder();
        if (current != null) {
            options.setPopUpTo(current.getId(), true);
        }
        nav.navigate(R.id.homePageFragment, null, options.build());
    }
}
