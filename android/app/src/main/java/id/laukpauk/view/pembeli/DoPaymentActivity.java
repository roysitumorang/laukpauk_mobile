package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.CustomColors;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/** Port of lib/views/pembeli/DoPayment.dart (a static instruction screen: no WebView, no arguments). */
public class DoPaymentActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TransaksiModel.removeTransactionSession(this, null);

        float hb = Ui.hb(this);
        setStatusBarScrimColor(CustomColors.aliceBlue);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.setGravity(Gravity.CENTER);

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(column, 15, 25, 15, 25);
        root.addView(column, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        column.addView(Ui.asset(this, "ovo.png", hb * 25));
        column.addView(Ui.spaceH(this, 15));
        column.addView(Ui.text(this, "Lakukan Pembayaran", new Txt.Style(hb * 4.25f).bold()));
        TextView sub = Ui.text(this,
                "Silahkan buka aplikasi OVO yang terinstal di perangkat anda untuk melakukan pembayaran",
                new Txt.Style(hb * 3.25f));
        sub.setGravity(Gravity.CENTER);
        Ui.pad(sub, 0, 5, 0, 0);
        column.addView(sub, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, 15));
        column.addView(CustomButton.button(this, "Belanja Lagi",
                () -> Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null),
                new CustomButton.Opts().color(CustomColors.green).pill()));

        setRoot(root);
    }
}
