package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;

/**
 * lib/views/pembeli/OTPNotCompleted.dart. Args: routeToClass / routeToArgs (Dart `Widget? routeTo`,
 * default Checkout()).
 */
public class OTPNotCompletedActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);

        // Widget _routeTo = (routeTo == null) ? Checkout() : routeTo!;
        String givenClass = AuthRoute.routeClass(args());
        final String routeClass = givenClass != null ? givenClass : CheckoutActivity.class.getName();
        final JsonObject routeArgs = givenClass != null ? AuthRoute.routeArgs(args()) : null;

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, hb * 10f, 0, hb * 10f, 0);

        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(Ui.dp(this, hb * 30f), Ui.WRAP);
        ilp.gravity = Gravity.CENTER_HORIZONTAL;
        android.widget.ImageView empty = Ui.asset(this, "empty.png", hb * 30f);
        empty.setLayoutParams(ilp);
        column.addView(empty);
        column.addView(Ui.spaceH(this, hb * 5f));
        column.addView(Ui.text(this, "OTP Belum Terverifikasi", hb * 4f, 0xFF000000));
        column.addView(Ui.spaceH(this, 10));
        TextView sub = Ui.text(this, "Silahkan masukkan ulang kode otp yang dikirimkan ke HP anda",
                hb * 3.25f, 0xFF000000);
        sub.setGravity(Gravity.CENTER);
        column.addView(sub);
        column.addView(Ui.spaceH(this, hb * 10f));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER);
        row.addView(CustomButton.button(this, "Verifikasi OTP",
                () -> Nav.pushAndRemoveUntil(this, OTPRegistrasiActivity.class, AuthRoute.carry(routeClass, routeArgs)),
                new CustomButton.Opts().color(CustomColors.green).pill().block()),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        row.addView(Ui.spaceW(this, hb * 1.5f));
        row.addView(CustomButton.button(this, "Kirim Ulang ",
                () -> Nav.pushAndRemoveUntil(this, KirimUlangOTPActivity.class, AuthRoute.carry(routeClass, routeArgs)),
                new CustomButton.Opts().color(CustomColors.textInfo).pill().block()),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        column.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, hb * 2.5f));
        column.addView(CustomButton.button(this, "Home ",
                () -> Nav.pushAndRemoveUntil(this, HomePageActivity.class, null),
                new CustomButton.Opts().color(CustomColors.green).outline().pill().block()),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        android.widget.FrameLayout center = new android.widget.FrameLayout(this);
        center.setBackgroundColor(CustomColors.nearlyWhite);
        center.addView(column, new android.widget.FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER));
        setRoot(center);
    }
}
