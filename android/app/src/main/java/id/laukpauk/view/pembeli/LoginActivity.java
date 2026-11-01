package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.Prefs;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/**
 * lib/views/pembeli/Login.dart. Args: routeToClass / routeToArgs (Dart `Widget? routeTo`, see AuthRoute).
 */
public class LoginActivity extends BaseActivity {
    private EditText nomorHPField;
    private EditText passwordField;
    private CustomButton.Btn loginButton;
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, hb * 10f, 0, hb * 10f, 0);

        column.addView(AuthUi.logo(this));
        column.addView(Ui.spaceH(this, 50));

        nomorHPField = AuthUi.input(this, "Nomor HP", AuthUi.numberInput(), hb * 3.5f, Ui.vb(this) * 2f);
        column.addView(AuthUi.pill(this, nomorHPField, null), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, 10));

        passwordField = AuthUi.input(this, "Password", AuthUi.passwordInput(), hb * 3.5f, Ui.vb(this) * 2f);
        column.addView(AuthUi.pill(this, passwordField, AuthUi.passwordToggle(this, passwordField)),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, 20));

        loginButton = CustomButton.button(this, "Login", this::cekLogin,
                new CustomButton.Opts().color(CustomButton.themeColor).pill());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        blp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(loginButton, blp);
        column.addView(Ui.spaceH(this, 25));

        column.addView(AuthUi.promptRow(this, "Belum punya akun? Registrasi ", "disini", () -> {
            JsonObject carry = AuthRoute.carry(args());
            Nav.pushReplacement(this, RegisterActivity.class, carry);
        }));
        column.addView(Ui.spaceH(this, 10));
        TextView lupa = AuthUi.link(this, "Lupa Password",
                () -> Nav.pushReplacement(this, LupaPasswordActivity.class, null));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(lupa, llp);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        android.widget.ScrollView sv = AuthUi.page(this, column, CustomColors.nearlyWhite);
        root.addView(sv, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(0xBBFFFFFF);
        overlay.setClickable(true);
        overlay.addView(Loading.view(this, "Loading", "Mohon sebentar, aplikasi sedang memuat konten ..."),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        overlay.setVisibility(View.GONE);
        loadingOverlay = overlay;
        root.addView(overlay, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        setRoot(root);
    }

    private void cekLogin() {
        if (loginButton.isBusy()) {
            return;
        }
        loginButton.setBusy(true);
        final String nomorHP = nomorHPField.getText().toString();
        final String password = passwordField.getText().toString();

        async(() -> {
            String cartCode = LocalStorage.getCartCode();
            String params = HTTPRequest.getQueryString("nomorHP=" + Http.enc(nomorHP) + "&password="
                    + Http.enc(password) + "&cartCode=" + cartCode);
            return Http.getApi("/Auth/loginAuthentication?" + params);
        }, httpResponse -> {
            if (httpResponse.statusCode != 200) {
                loginButton.setBusy(false);
                return;
            }
            final JsonObject response = httpResponse.jsonObject();
            if (Json.get(response, "loginAuthentication") != null
                    && Json.asBool(Json.get(response, "loginAuthentication"), false)) {
                loadingOverlay.setVisibility(View.VISIBLE);
                async(() -> {
                    String authToken = Json.str(response, "authToken");
                    String idUser = Json.str(response, "idUser");
                    Prefs.setString("authToken", authToken);
                    Prefs.setString("idUser", idUser);

                    OneSignal.login(idUser);
                    LocalStorage.setExternalUserID(idUser);

                    int jumlahSeluruhNotifikasi = HeaderModel.getJumlahNotifikasi();
                    int jumlahCart = HeaderModel.getJumlahCart();

                    AppState.get().setJumlahCart(jumlahCart);
                    AppState.get().setJumlahNotifikasi(jumlahSeluruhNotifikasi);
                    return Json.bool(response, "isVerified");
                }, isVerified -> {
                    loginButton.setBusy(false);
                    loadingOverlay.setVisibility(View.GONE);

                    if (!isVerified) {
                        Nav.pushReplacement(this, OTPVerifikasiUserActivity.class, AuthRoute.carry(args()));
                    } else {
                        AuthRoute.pushReplacement(this, AuthRoute.routeClass(args()), AuthRoute.routeArgs(args()),
                                SplashScreenActivity.class);
                    }
                }, error -> {
                    loginButton.setBusy(false);
                    loadingOverlay.setVisibility(View.GONE);
                });
            } else {
                loginButton.setBusy(false);

                String title = "Autentikasi User";
                String subTitle = "Kombinasi nomor telepon dan password tidak sesuai !";
                TextView another = Ui.text(this,
                        "Silahkan ke halaman lupa password untuk memulihkan password anda !",
                        Ui.hb(this) * 2.75f, CustomColors.textMuted);
                another.setGravity(Gravity.CENTER);

                Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                        CustomAlert.textButton(this, "Tutup", () -> handle.close()),
                        CustomAlert.textButton(this, "Lupa Password", () -> {
                            handle.close();
                            Nav.push(this, LupaPasswordActivity.class, null);
                        }));
                CustomAlert.<Object>textComponent(this, null, title, subTitle, actions, another, false, null);
            }
        }, error -> loginButton.setBusy(false));
    }
}
