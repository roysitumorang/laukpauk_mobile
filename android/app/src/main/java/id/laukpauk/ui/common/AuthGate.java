package id.laukpauk.ui.common;

import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.di.AppContainer;

/**
 * Screens that need a signed-in user (the Dart app showed {@code Login(routeTo: ThisScreen())} instead).
 *
 * <pre>
 * // in the gated screen's onViewCreated:
 * AuthGate.require(this, this::loadContent);
 * </pre>
 * Signed in: {@code onAuthenticated} runs immediately. Otherwise the gated screen remembers, on its own
 * back-stack entry, that it sent the user to Login, and opens Login. Whenever the gated screen comes back
 * (its view is re-created) {@code require} runs again:
 * <ul>
 *   <li>signed in by then: {@code onAuthenticated} runs;</li>
 *   <li>still signed out and Login was already opened from here: the user left Login with Back, so the
 *       gated screen closes too. It never opens Login a second time, which is what made Back on the Login
 *       page appear to do nothing.</li>
 * </ul>
 * Opening Login and closing the screen both wait until the gated screen is resumed and the current fragment
 * transaction has finished; navigating from inside {@code onViewCreated} while Back is still being handled
 * is not reliable.
 *
 * After a successful login AuthFlow.finish publishes {@code LOGIN_RESULT = true} to the screen below and pops
 * back; screens that open Login themselves (the cart, the account page) observe that result.
 */
public final class AuthGate {
    private AuthGate() {}

    public static final String LOGIN_RESULT = "login_successful";

    /** Set on the gated screen's back-stack entry once it has sent the user to Login. */
    private static final String SENT_TO_LOGIN = "auth_gate_sent_to_login";

    public static boolean isSignedIn(Fragment fragment) {
        return !AppContainer.from(fragment.requireContext()).session.getAuthToken().isEmpty();
    }

    public static void require(Fragment fragment, Runnable onAuthenticated) {
        final NavController nav = NavHostFragment.findNavController(fragment);
        NavBackStackEntry entry = nav.getCurrentBackStackEntry();
        SavedStateHandle handle = entry == null ? null : entry.getSavedStateHandle();
        if (isSignedIn(fragment)) {
            if (handle != null) {
                handle.remove(SENT_TO_LOGIN);
                handle.remove(LOGIN_RESULT);
            }
            onAuthenticated.run();
            return;
        }
        if (entry == null) {
            return;
        }
        final int gated = entry.getDestination().getId();
        final boolean cameBack = Boolean.TRUE.equals(handle.get(SENT_TO_LOGIN));
        if (cameBack) {
            handle.remove(SENT_TO_LOGIN);
        } else {
            handle.set(SENT_TO_LOGIN, Boolean.TRUE);
        }
        whenResumed(fragment, () -> {
            NavDestination current = nav.getCurrentDestination();
            if (current == null || current.getId() != gated) {
                return; // something else already navigated away
            }
            if (cameBack) {
                close(nav);
            } else {
                nav.navigate(R.id.loginFragment);
            }
        });
    }

    /** Runs {@code action} once, after the fragment's view is resumed and the running transaction is done. */
    private static void whenResumed(Fragment fragment, Runnable action) {
        final Handler main = new Handler(Looper.getMainLooper());
        final Lifecycle lifecycle = fragment.getViewLifecycleOwner().getLifecycle();
        lifecycle.addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(androidx.lifecycle.LifecycleOwner source, Lifecycle.Event event) {
                if (event != Lifecycle.Event.ON_RESUME) {
                    return;
                }
                lifecycle.removeObserver(this);
                main.post(() -> {
                    if (fragment.isAdded() && fragment.getView() != null
                            && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        action.run();
                    }
                });
            }
        });
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
