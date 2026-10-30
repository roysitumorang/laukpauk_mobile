package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.UserModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.homepage.WalletAndVoucher;
import id.laukpauk.view.pembeli.components.useranddashboard.Transaksi;
import id.laukpauk.view.pembeli.components.useranddashboard.UserMenu;

/** Port of lib/views/pembeli/UserAndDashboard.dart (the "Akun" tab of the Footer). */
public class UserAndDashboardActivity extends BaseActivity {
    private FrameLayout bodyHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        bodyHolder = new FrameLayout(this);
        root.addView(bodyHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        showLoading(false);
        load();
    }

    private void showLoading(boolean withImage) {
        bodyHolder.removeAllViews();
        bodyHolder.addView(withImage ? Loading.view(this, null, null, 50f, 50f) : Loading.view(this),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private void load() {
        async(AuthModel::isLogin, isLogin -> {
            if (isLogin == null || !isLogin) {
                // Dart: return Login();
                Nav.pushReplacement(this, LoginActivity.class, null);
                return;
            }
            showLoading(true);
            async(UserModel::getDetailActiveUser, dataUser -> {
                if (dataUser == null) {
                    return; // Dart: snapshot without data keeps the loader
                }
                render(dataUser);
            });
        });
    }

    private void render(JsonElement dataUserElement) {
        bodyHolder.removeAllViews();
        float hb = Ui.hb(this);

        if (Json.isFalse(dataUserElement)) {
            LinearLayout column = Ui.column(this);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(this, "empty.png", hb * 30f));
            column.addView(Ui.spaceH(this, 10));
            column.addView(Ui.text(this, "Data Akun Anda tidak Ditemukan", hb * 3.5f, CustomColors.textDanger));
            bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        JsonObject dataUser = Json.obj(dataUserElement);
        float profilePicWidthAndHeight = hb * 37.5f;

        LinearLayout content = Ui.column(this);
        Ui.pad(content, 0, 25, 0, 25);

        LinearLayout top = Ui.column(this);
        top.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(top, 15, 0, 15, 0);

        ImageView pic = Ui.network(this, ServerPath.baseURL + "/" + ServerPath.uploadGambarUser + "/"
                + Json.str(dataUser, "foto"), profilePicWidthAndHeight, profilePicWidthAndHeight);
        Ui.clipRounded(pic, profilePicWidthAndHeight);
        top.addView(pic);
        top.addView(Ui.spaceH(this, 15));
        top.addView(Ui.text(this, Json.str(dataUser, "nama"), Txt.textLG(this).color(CustomColors.green).bold()));
        top.addView(Ui.text(this, Json.str(dataUser, "kodeReferal"),
                new Txt.Style(hb * 3.5f).color(CustomColors.green).bold()));
        top.addView(Ui.spaceH(this, 10));
        String alamat = Json.str(dataUser, "alamat");
        if (alamat.length() >= 1) {
            TextView t = Ui.text(this, alamat, hb * 3.35f, CustomColors.textMuted);
            t.setGravity(Gravity.CENTER);
            Ui.padXY(t, 10, 0);
            top.addView(t, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        } else {
            top.addView(Ui.text(this, "Alamat belum ditentukan",
                    new Txt.Style(hb * 3.35f).italic().color(CustomColors.textMuted)));
        }
        top.addView(Ui.spaceH(this, 15));
        top.addView(new WalletAndVoucher(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(top, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        content.addView(Ui.spaceH(this, 15));
        FrameLayout transaksiPad = new FrameLayout(this);
        Ui.padXY(transaksiPad, 5, 0);
        transaksiPad.addView(new Transaksi(this, dataUser), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(transaksiPad, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        FrameLayout menuPad = new FrameLayout(this);
        Ui.padXY(menuPad, 5, 0);
        menuPad.addView(new UserMenu(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(menuPad, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout column = Ui.column(this);
        column.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        SwipeRefreshLayout refresh = DetailUi.refreshable(this, content, new Runnable() {
            @Override
            public void run() {
                // onRefresh: setState(() {}) -> everything is fetched again
                load();
            }
        }, false);
        column.addView(refresh, Ui.weightH(1f));
        bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
