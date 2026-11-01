package id.laukpauk.view.pembeli;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** lib/views/pembeli/KodeReferal.dart. No arguments (shows Login(routeTo: KodeReferal) when logged out). */
public class KodeReferalActivity extends BaseActivity {
    private FrameLayout host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        host = new FrameLayout(this);
        host.setBackgroundColor(CustomColors.nearlyWhite);
        setRoot(host);
        showOnly(Loading.view(this));
        load();
    }

    private void showOnly(View v) {
        host.removeAllViews();
        host.addView(v, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    /** The FutureBuilder chain: isLogin -> getReferralConfiguration. */
    private void load() {
        async(() -> AuthModel.isLogin(), (Boolean isUserLoggedIn) -> {
            if (!isUserLoggedIn) {
                // return Login(routeTo: KodeReferal());
                Nav.pushReplacement(this, LoginActivity.class,
                        AuthRoute.carry(KodeReferalActivity.class.getName(), null));
                return;
            }
            async(() -> getReferralConfiguration(), (JsonElement referralConfiguration) -> {
                if (referralConfiguration != null && !Json.isFalse(referralConfiguration)) {
                    int referralWallet = Json.intVal(Json.obj(referralConfiguration), "referralWallet");
                    showOnly(buildPage(Currency.formatRupiah(referralWallet, 0)));
                } else {
                    showOnly(buildNoConfiguration());
                }
            });
        });
    }

    private JsonElement getReferralConfiguration() {
        String params = HTTPRequest.getQueryString(null, true);
        Http.Response httpResponse = Http.getApi("/Pengaturan/getReferralConfiguration?" + params);
        if (httpResponse.statusCode == 200) {
            JsonElement v = Json.get(httpResponse.jsonObject(), "referralConfiguration");
            return v == null || Json.isNull(v) ? new JsonPrimitive(false) : v;
        }
        return new JsonPrimitive(false);
    }

    private JsonElement getDetailUser() {
        String params = HTTPRequest.getQueryString(null, true);
        Http.Response httpResponse = Http.getApi("/User/getDetailUser?" + params);
        if (httpResponse.statusCode == 200) {
            JsonElement v = Json.get(httpResponse.jsonObject(), "detailUser");
            return v == null || Json.isNull(v) ? new JsonPrimitive(false) : v;
        }
        return new JsonPrimitive(false);
    }

    private int getReferralUserWallet() {
        int referralUserWallet = 0;
        String params = HTTPRequest.getQueryString(null, true);
        Http.Response httpResponse = Http.getApi("/Pengaturan/getSettingValue/referralUserWallet?" + params);
        if (httpResponse.statusCode == 200) {
            referralUserWallet = Json.parseInt(Json.str(httpResponse.jsonObject(), "settingValue"), 0);
        }
        return referralUserWallet;
    }

    private void bagikan(final JsonObject detailActiveUser) {
        final String nama = Json.str(detailActiveUser, "nama");
        final String kodeReferal = Json.str(detailActiveUser, "kodeReferal");
        async(() -> getReferralUserWallet(), (Integer nilaiDompet) -> {
            String nilaiDompetString = Currency.formatRupiah(nilaiDompet, 0);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, "Kode Referal " + kodeReferal);
            send.putExtra(Intent.EXTRA_TEXT, "Temanmu *" + nama + "* mengirimkan Kode Referal *" + kodeReferal
                    + "* senilai *" + nilaiDompetString + "* untuk pembelian kebutuhan dapur pertamamu di Laukpauk. \n\nLangkah mudah belanja di Laukpauk ke Pedagang Pasar Tradisional : \n*Pilih Pasar - Belanja - Tunggu dirumah*\n\nYukk, teman-teman di Area Medan sekitarnya kalau apakali, belanja di https://laukpauk.id/app aja!");
            startActivity(Intent.createChooser(send, null));
        });
    }

    private View buildNoConfiguration() {
        float hb = Ui.hb(this);
        FrameLayout center = new FrameLayout(this);
        center.setBackgroundColor(CustomColors.nearlyWhite);
        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(column, 50, 10, 50, 10);
        ImageView img = Ui.asset(this, "empty.png", hb * 25f);
        img.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, hb * 25f), Ui.WRAP));
        column.addView(img);
        column.addView(Ui.spaceH(this, 5));
        TextView t = Ui.text(this, "Pengaturan referral belum dibuat (Nilai Wallet dan User Wallet) dsb",
                hb * 3.5f, CustomColors.textDanger);
        t.setGravity(Gravity.CENTER);
        column.addView(t);
        column.addView(Ui.spaceH(this, 15));
        center.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER));
        return center;
    }

    private View buildPage(String formattedReferralWallet) {
        final float hb = Ui.hb(this);
        LinearLayout page = Ui.column(this);
        page.setBackgroundColor(CustomColors.nearlyWhite);
        page.addView(DetailUi.appBar(this, "Kode Referal"));

        LinearLayout content = Ui.column(this);
        Ui.pad(content, hb * 5f, 5, hb * 5f, 5);

        LinearLayout header = Ui.column(this);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(header, 0, 35, 0, 35);
        header.addView(Ui.text(this, "Kode Referal Anda",
                new Txt.Style(hb * 5f).color(CustomColors.nearlyBlack).bold()));
        header.addView(Ui.spaceH(this, 5));

        final FrameLayout detailHolder = new FrameLayout(this);
        detailHolder.addView(Loading.view(this, null, null, 20f, 20f),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        header.addView(detailHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        content.addView(Ui.text(this,
                "Silahkan bagikan kode ini ke keluarga, saudara, teman atau kepada siapa saja untuk mendapatkan uang belanja gratis sebesar ",
                14f, 0xFF000000));
        content.addView(Ui.text(this, formattedReferralWallet, 14f, 0xFF000000, true));
        content.addView(Ui.spaceH(this, 15));
        content.addView(Ui.text(this,
                "Uang belanja gratis akan otomatis masuk ke dompet anda ketika si penerima referral melakukan belanja, dan sudah selesai ",
                14f, 0xFF000000));
        content.addView(Ui.text(this, "(COMPLETED)", 14f, 0xFF000000, true));

        final SwipeHolder holder = new SwipeHolder();
        holder.refresh = DetailUi.refreshable(this, content, () -> {
            // onRefresh: setState(() {}) rebuilds the whole chain
            load();
        }, false);
        page.addView(holder.refresh, Ui.weightH(1f));

        async(() -> getDetailUser(), (JsonElement detailUser) -> {
            detailHolder.removeAllViews();
            if (detailUser != null && !Json.isFalse(detailUser)) {
                final JsonObject detail = Json.obj(detailUser);
                final String kodeReferalString = Json.str(detail, "kodeReferal");
                LinearLayout col = Ui.column(this);
                col.setGravity(Gravity.CENTER_HORIZONTAL);
                col.addView(Ui.text(this, kodeReferalString,
                        new Txt.Style(hb * 4.5f).color(CustomColors.green).bold().letterSpacing(0.08f)));
                col.addView(Ui.spaceH(this, 15));
                LinearLayout row = Ui.row(this);
                row.setGravity(Gravity.CENTER);
                row.addView(CustomButton.button(this, "Salin", () -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("Kode Referal", kodeReferalString));
                    }
                    AuthUi.snack(this, "Kode Referal Tersalin !", 4, hb * 3.25f);
                }, new CustomButton.Opts().outline().pill().color(CustomButton.themeColor)));
                row.addView(Ui.spaceW(this, 10));
                row.addView(CustomButton.button(this, "Bagikan", () -> bagikan(detail),
                        new CustomButton.Opts().color(CustomButton.themeColor).pill()));
                col.addView(row);
                detailHolder.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            } else {
                TextView t = Ui.text(this, "Akun Anda tidak Dikenal",
                        new Txt.Style(18f).color(CustomColors.textDanger).bold().letterSpacing(0.08f));
                t.setGravity(Gravity.CENTER);
                detailHolder.addView(t, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });
        return page;
    }

    private static final class SwipeHolder {
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout refresh;
    }
}
