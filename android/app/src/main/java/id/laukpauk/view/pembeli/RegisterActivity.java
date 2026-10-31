package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.Prefs;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;

/**
 * lib/views/pembeli/Register.dart. Args: routeToClass / routeToArgs (Dart `Widget? routeTo`, see AuthRoute).
 */
public class RegisterActivity extends BaseActivity {
    private EditText namaField;
    private EditText nomorHPField;
    private EditText passwordField;
    private EditText konfirmasiPasswordField;
    private EditText kodeReferalField;
    private CustomButton.Btn registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);
        float vPad = Ui.vb(this) * 2f;

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, 10, 15, 10, 15);

        column.addView(AuthUi.logo(this));
        column.addView(Ui.spaceH(this, 50));

        namaField = AuthUi.input(this, "Nama Lengkap", AuthUi.textInput(), hb * 3.5f, vPad);
        AuthUi.addPill(column, AuthUi.pill(this, namaField, null), 25);
        column.addView(Ui.spaceH(this, 10));

        nomorHPField = AuthUi.input(this, "Nomor HP", AuthUi.numberInput(), hb * 3.5f, vPad);
        AuthUi.addPill(column, AuthUi.pill(this, nomorHPField, null), 25);
        column.addView(Ui.spaceH(this, 10));

        passwordField = AuthUi.input(this, "Password", AuthUi.passwordInput(), hb * 3.5f, vPad);
        AuthUi.addPill(column, AuthUi.pill(this, passwordField, AuthUi.passwordToggle(this, passwordField)), 25);
        column.addView(Ui.spaceH(this, 10));

        konfirmasiPasswordField = AuthUi.input(this, "Konfirmasi Password", AuthUi.passwordInput(), hb * 3.5f, vPad);
        AuthUi.addPill(column, AuthUi.pill(this, konfirmasiPasswordField,
                AuthUi.passwordToggle(this, konfirmasiPasswordField)), 25);
        column.addView(Ui.spaceH(this, 10));

        LinearLayout referal = Ui.column(this);
        kodeReferalField = AuthUi.input(this, "Kode Referal (Opsional)", AuthUi.textInput(), hb * 3.5f, vPad);
        referal.addView(AuthUi.pill(this, kodeReferalField, null), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        referal.addView(Ui.spaceH(this, 10));
        TextView info = Ui.text(this,
                "Dapatkan uang belanja GRATIS ketika anda menggunakan Kode Referral dari Teman/Keluarga/Sahabat.",
                new id.laukpauk.ui.Txt.Style(hb * 3f).color(0xFF000000).italic());
        referal.addView(info, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        AuthUi.addPill(column, referal, 25);
        column.addView(Ui.spaceH(this, 20));

        registerButton = CustomButton.button(this, "Registrasi", this::doRegistrasi,
                new CustomButton.Opts().color(CustomButton.themeColor).pill());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        blp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(registerButton, blp);
        column.addView(Ui.spaceH(this, 25));

        column.addView(AuthUi.promptRow(this, "Sudah punya akun? Login ", "disini",
                () -> Nav.pushReplacement(this, LoginActivity.class, null)));
        column.addView(Ui.spaceH(this, 10));
        TextView lupa = AuthUi.link(this, "Lupa Password",
                () -> Nav.pushReplacement(this, LupaPasswordActivity.class, null));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(lupa, llp);

        setRoot(AuthUi.page(this, column, AuthUi.PAGE_BG));
    }

    private void doRegistrasi() {
        if (registerButton.isBusy()) {
            return;
        }
        registerButton.setBusy(true);

        final Map<String, String> registrasiData = new LinkedHashMap<>();
        registrasiData.put("namaLengkap", namaField.getText().toString());
        registrasiData.put("nomorHP", nomorHPField.getText().toString());
        registrasiData.put("password", passwordField.getText().toString());
        registrasiData.put("konfirmasiPassword", konfirmasiPasswordField.getText().toString());
        registrasiData.put("kodeReferal", kodeReferalField.getText().toString());

        async(() -> Http.postApi("/Auth/register", HTTPRequest.postBody(registrasiData)), httpResponse -> {
            registerButton.setBusy(false);

            if (httpResponse.statusCode != 200) {
                return;
            }
            JsonObject dataObject = httpResponse.jsonObject();

            String alertMessage;
            JsonObject returnData = Json.object(dataObject, "returnData");
            JsonElement statusRegister = Json.get(dataObject, "statusRegister");
            boolean statusTrue = Json.asBool(statusRegister, false);
            boolean statusFalse = Json.isFalse(statusRegister);
            if (statusTrue) {
                alertMessage = "Registrasi berhasil ! Silahkan konfirmasikan kode OTP yang kami kirim ke nomor yang anda gunakan untuk mendaftar!";

                Prefs.setBool("otpHasSent", true);
                Prefs.setBool("otpHasConfirmed", false);
                Prefs.setString("idUser", Json.str(returnData, "idUser"));
            } else {
                alertMessage = "Gagal Registrasi ! " + Json.str(returnData, "message");
            }

            String title = "Registrasi";
            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                    CustomAlert.textButton(this, statusFalse ? "OK" : "Konfirmasi OTP", () -> {
                        handle.close();
                        if (!statusFalse) {
                            Nav.push(this, OTPRegistrasiActivity.class, AuthRoute.carry(args()));
                        }
                    }));
            CustomAlert.<Object>textComponent(this, null, title, alertMessage, actions, null, false, null);
        }, error -> registerButton.setBusy(false));
    }
}
