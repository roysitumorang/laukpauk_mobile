package id.laukpauk.ui.checkout;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;

/** Navigation shared by the checkout screens (legacy Nav.pushAndRemoveUntil). */
final class CheckoutNav {
    private CheckoutNav() {}

    /** Opens a post-order screen and removes Checkout from the back stack, so Back never returns to it. */
    static void toResult(Fragment from, int destination, @Nullable Bundle args) {
        NavOptions options = new NavOptions.Builder().setPopUpTo(R.id.checkoutFragment, true).build();
        NavHostFragment.findNavController(from).navigate(destination, args, options);
    }

    /** Legacy "push SplashScreen and clear the stack": restarts the app flow at the splash screen. */
    static void goHome(Fragment from, boolean removeTransactionSession) {
        Bundle args = new Bundle();
        args.putBoolean("removeTransactionSession", removeTransactionSession);
        NavOptions options = new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build();
        NavHostFragment.findNavController(from).navigate(R.id.splashScreenFragment, args, options);
    }
}
