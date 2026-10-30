package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.voucher.VoucherItem;

/**
 * Port of lib/views/pembeli/Voucher.dart.
 * Args: appBarTitle (String, default 'Voucher Saya').
 */
public class VoucherActivity extends BaseActivity {
    private String appBarTitle;
    private FrameLayout bodyHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        appBarTitle = Json.has(args(), "appBarTitle") ? Json.str(args(), "appBarTitle") : "Voucher Saya";

        // FutureBuilder(AuthModel.isLogin()) waiting: Scaffold(body: Loading(...))
        FrameLayout waiting = new FrameLayout(this);
        waiting.setBackgroundColor(CustomColors.nearlyWhite);
        waiting.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        setRoot(waiting);

        async(() -> AuthModel.isLogin(), isLogin -> {
            if (isLogin != null && isLogin) {
                buildScreen();
                loadVoucher();
            } else {
                // Login(routeTo: Voucher()) takes the place of this screen
                JsonObject routeArgs = Json.of("appBarTitle", appBarTitle);
                Nav.pushReplacement(this, id.laukpauk.view.pembeli.LoginActivity.class,
                        Json.of(AuthRoute.KEY_CLASS, VoucherActivity.class.getName(), AuthRoute.KEY_ARGS, routeArgs));
            }
        });
    }

    private void buildScreen() {
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, appBarTitle), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder = new FrameLayout(this);
        bodyHolder.addView(Loading.view(this, null, null, 20f, 20f), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        root.addView(bodyHolder, Ui.weightH(1f));
        setRoot(root);
    }

    private JsonArray getVoucher() {
        String entitasVoucher = "id, foto, minimumBelanja, berlakuMulai, berlakuSampai, maximumUsage";

        String params = HTTPRequest.getQueryString("entitasVoucher=" + entitasVoucher.replace(" ", "%20"), true);

        Http.Response httpResponse = Http.getApi("/User/getVoucher?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "listVoucher");
        }
        return new JsonArray();
    }

    private void loadVoucher() {
        async(this::getVoucher, dataVoucher -> render(dataVoucher != null ? dataVoucher : new JsonArray()));
    }

    private void render(JsonArray dataVoucher) {
        final float hb = Ui.hb(this);
        bodyHolder.removeAllViews();

        LinearLayout content = Ui.column(this);
        content.setClipChildren(false);
        if (dataVoucher.size() >= 1) {
            LinearLayout list = Ui.column(this);
            list.setClipChildren(false);
            Ui.padXY(list, 5, 5);
            for (int i = 0; i < dataVoucher.size(); i++) {
                list.addView(new VoucherItem(this, Json.objAt(dataVoucher, i)),
                        new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            content.addView(list, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            content.setGravity(Gravity.CENTER);
            content.addView(Ui.asset(this, "empty.png", hb * 30));
            content.addView(Ui.spaceH(this, 10));
            content.addView(Ui.text(this, "Anda tidak memiliki voucher aktif apapun", hb * 3.5f, CustomColors.textMuted));
            content.addView(Ui.spaceH(this, 10));
        }
        final androidx.swiperefreshlayout.widget.SwipeRefreshLayout[] ref =
                new androidx.swiperefreshlayout.widget.SwipeRefreshLayout[1];
        ref[0] = DetailUi.refreshable(this, content, () -> {
            loadVoucher(); // setState(() {}) -> the future runs again
            ref[0].setRefreshing(false);
        }, true);
        bodyHolder.addView(ref[0], new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
