package id.laukpauk.ui.account;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;

/** Navigation helpers of the account screens. */
final class AccountNav {
    private AccountNav() {}

    /** Legacy Nav.push(UserAndDashboardActivity): back to the account tab. */
    static void toDashboard(Fragment fragment) {
        NavController nav = NavHostFragment.findNavController(fragment);
        if (!nav.popBackStack(R.id.userAndDashboardFragment, false)) {
            nav.navigate(R.id.userAndDashboardFragment);
        }
    }

    /** Legacy pushAndRemoveUntil to the start screen: home with an empty back stack behind it. */
    static void toHome(Fragment fragment) {
        NavController nav = NavHostFragment.findNavController(fragment);
        nav.navigate(R.id.homePageFragment, null,
                new NavOptions.Builder().setPopUpTo(R.id.homePageFragment, true).build());
    }

    /** Legacy pushAndRemoveUntil(TokoSaya). */
    static void toTokoSaya(Fragment fragment) {
        NavController nav = NavHostFragment.findNavController(fragment);
        nav.navigate(R.id.tokoSayaFragment, null,
                new NavOptions.Builder().setPopUpTo(R.id.homePageFragment, false).build());
    }
}
