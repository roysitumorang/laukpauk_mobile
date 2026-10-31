package id.laukpauk.view;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Async;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.MainModel;
import id.laukpauk.model.SplashScreenModel;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.AuthRoute;
import id.laukpauk.view.pembeli.HomePageActivity;
import id.laukpauk.view.pembeli.OTPRegistrasiActivity;

/**
 * lib/views/SplashScreen.dart: the launcher and deep-link target.
 * Args: removeTransactionSession (boolean), routeToClass (String, fully-qualified Activity class) and
 * routeToArgs (JsonObject) replacing Dart's `Widget? routeTo`.
 */
public class SplashScreenActivity extends BaseActivity {
    private static final int GO_INTRO = 0;
    private static final int GO_HOME = 1;
    private static final int GO_OTP_REGISTRASI = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainModel.handleDeepLink(this, getIntent())) {
            return;
        }

        float hb = Ui.hb(this);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView logo = Ui.asset(this, "laukpauk-icon.png", hb * 30f);
        logo.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, hb * 30f), Ui.WRAP));
        column.addView(logo);
        column.addView(Ui.spaceH(this, 20));
        column.addView(Ui.text(this, "Kalau Apakali, Laukpauk Aja", 12.5f, 0xFF000000));
        root.addView(column, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
        setRoot(root);

        // WidgetsBinding.instance.addPostFrameCallback((_) => _initialization());
        post(this::initialization);
    }

    private void initialization() {
        final String routeToClass = AuthRoute.routeClass(args());
        final JsonObject routeToArgs = AuthRoute.routeArgs(args());
        boolean removeTransactionSession = id.laukpauk.core.Json.bool(args(), "removeTransactionSession");

        if (removeTransactionSession) {
            TransaksiModel.removeTransactionSession(this, null);
        } else {
            if (routeToClass != null && AuthRoute.resolve(routeToClass) != null) {
                postDelayed(() -> AuthRoute.pushAndRemoveUntil(this, routeToClass, routeToArgs,
                        HomePageActivity.class), 3000);
            } else {
                postDelayed(() -> async(() -> {
                    boolean hasAppStarted = SplashScreenModel.checkHasAppStarted();

                    if (hasAppStarted) {
                        boolean otpHasSent = SplashScreenModel.checkHasOTPSent();
                        boolean otpHasConfirmed = SplashScreenModel.checkHasOTPConfirmed();
                        boolean isOTPRegistrasiPassed = LocalStorage.isOTPRegistrasiPassed();

                        if (otpHasSent && (!otpHasConfirmed || isOTPRegistrasiPassed)) {
                            return isOTPRegistrasiPassed ? GO_HOME : GO_OTP_REGISTRASI;
                        }
                        return GO_HOME;
                    }
                    return GO_INTRO;
                }, (Integer destination) -> {
                    if (destination == GO_HOME) {
                        Nav.pushAndRemoveUntil(this, HomePageActivity.class, null);
                    } else if (destination == GO_OTP_REGISTRASI) {
                        Nav.pushAndRemoveUntil(this, OTPRegistrasiActivity.class, null);
                    } else {
                        Nav.pushAndRemoveUntil(this, IntroPageActivity.class, null);
                    }
                }), 1500);
            }
        }
    }
}
