package id.laukpauk.ui.common;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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
 * closes too.
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
        NavResults.<Boolean>observe(fragment, LOGIN_RESULT, success -> {
            if (Boolean.TRUE.equals(success)) {
                onAuthenticated.run();
            } else if (!isSignedIn(fragment)) {
                nav.popBackStack();
            }
        });
        nav.navigate(R.id.loginFragment);
    }
}
