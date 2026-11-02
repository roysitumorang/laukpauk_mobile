package id.laukpauk.ui.seller;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;

/** Navigation shortcuts shared by the shop screens. */
final class SellerShopNav {
    private SellerShopNav() {}

    /** Legacy Nav.push(UserAndDashboardActivity) / pushAndRemoveUntil: the account tab on top of home. */
    static void toAkunSaya(Fragment fragment) {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homePageFragment, false)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(fragment).navigate(R.id.userAndDashboardFragment, null, options);
    }
}
