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

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.model.UserModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * lib/views/pembeli/KirimUlangOTP.dart. Args: routeToClass / routeToArgs (Dart `Widget? routeTo`; the Dart
 * screen ignores it and always continues to OTPRegistrasi(routeTo: Checkout()) - kept).
 */
public class KirimUlangOTPActivity extends BaseActivity {
    private EditText nomorTeleponField;
    private CustomButton.Btn kirimButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, 10, 15, 10, 15);

        column.addView(AuthUi.logo(this));
        column.addView(Ui.spaceH(this, 50));

        nomorTeleponField = AuthUi.input(this, "Nomor Telepon", AuthUi.numberInput(), 0f, 14f);
        AuthUi.addPill(column, AuthUi.pill(this, nomorTeleponField, null), 25);
        column.addView(Ui.spaceH(this, 15));

        kirimButton = CustomButton.button(this, "Kirim OTP", this::kirimOtpTapped,
                new CustomButton.Opts().color(CustomButton.themeColor).pill());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        blp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(kirimButton, blp);
        column.addView(Ui.spaceH(this, hb * 10f));

        LinearLayout note = Ui.column(this);
        Ui.pad(note, hb * 7.5f, 0, hb * 7.5f, 0);
        note.addView(Ui.text(this, "Note", new Txt.Style(hb * 3.75f).color(0xFF000000).bold()));
        note.addView(Ui.spaceH(this, 10));
        TextView isi = Ui.text(this, "Verifikasi kode OTP hanya bisa dilakukan sekali dalam sehari",
                hb * 3.25f, 0xFF000000);
        isi.setGravity(Gravity.START);
        note.addView(isi);
        column.addView(note, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, hb * 3f));

        setRoot(AuthUi.page(this, column, AuthUi.PAGE_BG));

        // _initialization(): fill in the phone number of the active user.
        async(() -> UserModel.getDetailActiveUser(), (JsonElement detailUser) -> {
            if (detailUser != null && !Json.isFalse(detailUser) && detailUser.isJsonObject()) {
                nomorTeleponField.setText(Json.str(detailUser.getAsJsonObject(), "nomorHP"));
            }
        });
    }

    /** Future&lt;Map?&gt; _kirimOTP(). */
    private JsonObject kirimOTP(String nomorTelepon) {
        Map<String, String> httpBody = new LinkedHashMap<>();
        httpBody.put("nomorTelepon", nomorTelepon);

        String params = HTTPRequest.getQueryString(null, true);
        Http.Response response = Http.postApi("/Auth/resendOTP?" + params, HTTPRequest.postBody(httpBody));
        if (response.statusCode == 200) {
            return response.jsonObject();
        }
        return null;
    }

    private void kirimOtpTapped() {
        if (kirimButton.isBusy()) {
            return;
        }
        kirimButton.setBusy(true);
        final String nomorTelepon = nomorTeleponField.getText().toString();

        async(() -> {
            JsonObject r = kirimOTP(nomorTelepon);
            return r == null ? new JsonObject[0] : new JsonObject[] {r};
        }, (JsonObject[] wrapper) -> {
            if (wrapper.length > 0) {
                JsonObject responseResendOTP = wrapper[0];
                boolean statusResendOTP = Json.bool(responseResendOTP, "statusResendOTP");

                if (statusResendOTP) {
                    // OTPRegistrasi(routeTo: Checkout())
                    Nav.pushAndRemoveUntil(this, OTPRegistrasiActivity.class,
                            AuthRoute.carry(CheckoutActivity.class.getName(), null));
                } else {
                    String messageResendOTP = Json.str(responseResendOTP, "messageResendOTP");
                    AuthUi.snack(this, messageResendOTP, 3, hb3());
                }
            } else {
                AuthUi.snack(this, "Sistem gagal melakukan operasi. Silahkan ulangi lagi!", 3, hb3());
            }
            kirimButton.setBusy(false);
        }, error -> kirimButton.setBusy(false));
    }

    private float hb3() {
        return Ui.hb(this) * 3.25f;
    }
}
