package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.Prefs;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/**
 * lib/views/pembeli/OTPRegistrasi.dart. Args: routeToClass / routeToArgs (Dart `Widget? routeTo`, see AuthRoute).
 */
public class OTPRegistrasiActivity extends BaseActivity {
    private EditText otpCodeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, 10, 15, 10, 15);

        column.addView(AuthUi.logo(this));
        column.addView(Ui.spaceH(this, 50));

        otpCodeField = AuthUi.input(this, "Kode OTP", AuthUi.numberInput(), 0f, 14f);
        AuthUi.addPill(column, AuthUi.pill(this, otpCodeField, null), 25);

        TextView note = Ui.text(this, "Silahkan masukkan kode OTP yang dikirim ke nomor yang anda daftarkan",
                16f, 0xFF000000);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        nlp.setMargins(Ui.dp(this, 25), Ui.dp(this, 10), Ui.dp(this, 25), Ui.dp(this, 10));
        column.addView(note, nlp);
        column.addView(Ui.spaceH(this, 15));

        LinearLayout buttons = Ui.row(this);
        buttons.setGravity(Gravity.CENTER);
        Ui.pad(buttons, hb * 10f, 0, hb * 10f, 0);
        buttons.addView(CustomButton.button(this, "Konfirmasi OTP", this::konfirmasiOTP,
                new CustomButton.Opts().color(CustomButton.themeColor).pill()));
        buttons.addView(Ui.spaceW(this, hb * 1.5f));
        buttons.addView(CustomButton.button(this, "Lewati", () -> {
            LocalStorage.lewatiOTPRegistrasi();
            Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null);
        }, new CustomButton.Opts().color(CustomButton.themeColor).pill().outline()));
        column.addView(buttons, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        setRoot(AuthUi.page(this, column, AuthUi.PAGE_BG));
    }

    private void konfirmasiOTP() {
        final String otp = otpCodeField.getText().toString();
        async(() -> {
            String idUser = LocalStorage.getCurrentUserID();
            Map<String, String> otpData = new LinkedHashMap<>();
            otpData.put("idUser", idUser);
            otpData.put("otp", otp);
            return Http.postApi("/Auth/konfirmasiOTP", HTTPRequest.postBody(otpData));
        }, httpResponse -> {
            if (httpResponse.statusCode != 200) {
                return;
            }
            JsonObject dataObject = httpResponse.jsonObject();
            final boolean isOTPSuccess = Json.bool(dataObject, "statusKonfirmasiOTP");
            if (isOTPSuccess) {
                Prefs.setBool("otpHasConfirmed", true);

                String authToken = Json.str(Json.object(dataObject, "returnData"), "authToken");
                Prefs.setString("authToken", authToken);
            }
            String title = "Konfirmasi OTP";
            String subTitle = isOTPSuccess
                    ? "Konfirmasi OTP Berhasil ! Anda sudah terautentikasi, jadi anda tidak perlu login kembali."
                    : "Konfirmasi OTP gagal, silahkan ulangi kembali !";
            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                    isOTPSuccess
                            ? CustomAlert.textButton(this, "Masuk", () -> {
                                handle.close();
                                AuthRoute.pushAndRemoveUntil(this, AuthRoute.routeClass(args()),
                                        AuthRoute.routeArgs(args()), SplashScreenActivity.class);
                            })
                            : CustomAlert.textButton(this, "OK", () -> handle.close()));
            CustomAlert.<Object>textComponent(this, null, title, subTitle, actions, null, false, null);
        });
    }
}
