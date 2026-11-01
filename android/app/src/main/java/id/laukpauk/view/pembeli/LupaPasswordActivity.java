package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;

/** lib/views/pembeli/LupaPassword.dart. No arguments. */
public class LupaPasswordActivity extends BaseActivity {
    private EditText nomorHPField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, 10, 15, 10, 15);

        column.addView(AuthUi.logo(this));
        column.addView(Ui.spaceH(this, 50));

        nomorHPField = AuthUi.input(this, "Nomor HP", AuthUi.numberInput(), hb * 3.5f, Ui.vb(this) * 2f);
        AuthUi.addPill(column, AuthUi.pill(this, nomorHPField, null), 25);
        column.addView(Ui.spaceH(this, 15));

        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        blp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(CustomButton.button(this, "Reset Password", this::recoveryPassword,
                new CustomButton.Opts().pill().color(CustomButton.themeColor)), blp);
        column.addView(Ui.spaceH(this, 25));

        column.addView(AuthUi.promptRow(this, "Belum punya akun? Registrasi ", "disini",
                () -> Nav.pushReplacement(this, RegisterActivity.class, null)));
        column.addView(Ui.spaceH(this, 10));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(AuthUi.link(this, "Login",
                () -> Nav.pushReplacement(this, LoginActivity.class, null)), llp);

        setRoot(AuthUi.page(this, column, AuthUi.PAGE_BG));
    }

    private void recoveryPassword() {
        final String nomorHP = nomorHPField.getText().toString();
        async(() -> {
            Map<String, String> lupaPasswordData = new LinkedHashMap<>();
            lupaPasswordData.put("nomorHP", nomorHP);
            return Http.postApi("/Auth/lupaPassword", HTTPRequest.postBody(lupaPasswordData));
        }, httpResponse -> {
            if (httpResponse.statusCode != 200) {
                return;
            }
            JsonObject recoveryPassword = httpResponse.jsonObject();

            final boolean isRecoveryPasswordSuccess = Json.asBool(Json.get(recoveryPassword, "statusRecoveryPassword"), false);

            String title = "Recovery Password";
            String subTitle = isRecoveryPasswordSuccess
                    ? "Password baru telah dikirimkan melalui SMS. Silahkan login ulang menggunakan password baru Anda"
                    : Json.str(Json.object(recoveryPassword, "returnData"), "message");
            Modal.Content<Object> actions = handle -> {
                if (isRecoveryPasswordSuccess) {
                    return CustomAlert.actionsRow(this,
                            CustomAlert.textButton(this, "OK", () -> handle.close()),
                            CustomAlert.textButton(this, "Login", () -> {
                                handle.close();
                                Nav.pushReplacement(this, LoginActivity.class, null);
                            }));
                }
                return CustomAlert.actionsRow(this, CustomAlert.textButton(this, "OK", () -> handle.close()));
            };
            CustomAlert.<Object>textComponent(this, null, title, subTitle, actions, null, false, null);
        });
    }
}
