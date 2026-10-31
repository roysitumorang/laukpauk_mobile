package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.pembeli.components.otpverifikasiuser.ListPasar;

/**
 * lib/views/pembeli/OTPVerifikasiUser.dart. Args: routeToClass / routeToArgs (Dart `Widget? routeTo`, see AuthRoute).
 */
public class OTPVerifikasiUserActivity extends BaseActivity {
    private String pasarSelected = "0";
    private boolean isSellerState = false;

    private EditText verificationCodeField;
    private LinearLayout column;
    private View sellerBox;
    private TextView dan;
    private FrameLayout buttonHolder;
    private ListPasar listPasar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float hb = Ui.hb(this);

        column = Ui.column(this);
        column.setGravity(Gravity.CENTER);
        Ui.pad(column, 10, 15, 10, 15);

        column.addView(AuthUi.logo(this));
        column.addView(Ui.spaceH(this, 50));

        verificationCodeField = AuthUi.input(this, "Kode Verifikasi", AuthUi.numberInput(), 0f, 14f);
        verificationCodeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateForm();
            }
        });
        AuthUi.addPill(column, AuthUi.pill(this, verificationCodeField, null), 25);
        column.addView(Ui.spaceH(this, 15));

        sellerBox = buildSellerBox(hb);
        sellerBox.setVisibility(View.GONE);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        slp.leftMargin = Ui.dp(this, 10);
        slp.rightMargin = Ui.dp(this, 10);
        column.addView(sellerBox, slp);

        dan = Ui.text(this, "", 16f, 0xFF000000);
        dan.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        dlp.setMargins(Ui.dp(this, 25), Ui.dp(this, 10), Ui.dp(this, 25), Ui.dp(this, 10));
        column.addView(dan, dlp);

        buttonHolder = new FrameLayout(this);
        Ui.pad(buttonHolder, 25, 15, 25, 0);
        buttonHolder.addView(CustomButton.button(this, "Verifikasi", this::konfirmasiOTP,
                new CustomButton.Opts().color(CustomButton.themeColor).pill()),
                new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER_HORIZONTAL));
        column.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        setRoot(AuthUi.page(this, column, AuthUi.PAGE_BG));
        updateForm();
        initialization();
    }

    private View buildSellerBox(float hb) {
        LinearLayout box = Ui.column(this);
        Ui.pad(box, 15, 10, 15, 10);
        box.setBackground(Ui.shape(this, 0x2266CC99, 10f, CustomColors.green, 0.5f));

        LinearLayout header = Ui.row(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(Ui.text(this, "Pasar Tempat Berjualan", new Txt.Style(hb * 3.75f).color(0xFF000000)),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        TextView lokasi = Ui.text(this, "Lokasi Tersendiri",
                new Txt.Style(hb * 3.5f).color(CustomColors.textPrimary).bold());
        lokasi.setOnClickListener(v -> Nav.push(this, PinGMAPSActivity.class, null));
        header.addView(lokasi);
        box.addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        box.addView(Ui.spaceH(this, 5));

        listPasar = new ListPasar(this, pasarSelected, idPasar -> {
            pasarSelected = idPasar;
            listPasar.setPasarSelected(idPasar);
            updateForm();
        });
        box.addView(listPasar);
        box.addView(Ui.spaceH(this, 10));

        TextView note = Ui.text(this,
                "Anda adalah penjual di aplikasi sebelumnya, oleh karena itu anda diminta untuk menentukan lokasi berjualan agar pembeli mudah menemukan anda",
                new Txt.Style(hb * 2.5f).color(0xFF000000));
        note.setGravity(Gravity.CENTER);
        box.addView(note);
        return box;
    }

    /** The parts of build() that depend on the state (isSellerState / the code / pasarSelected). */
    private void updateForm() {
        String danText = isSellerState
                ? " dan tentukan lokasi anda berjualan untuk dapat berjualan."
                : ".";
        dan.setText("Silahkan masukkan kode OTP Verifikasi User yang dikirim ke nomor yang anda daftarkan" + danText);
        sellerBox.setVisibility(isSellerState ? View.VISIBLE : View.GONE);

        int len = verificationCodeField.getText().length();
        boolean isFormComplete = isSellerState ? (len >= 4 && !pasarSelected.equals("0")) : len >= 4;
        buttonHolder.setVisibility(isFormComplete ? View.VISIBLE : View.GONE);
    }

    private void initialization() {
        async(() -> {
            String params = HTTPRequest.getQueryString("getUserEntities=isSeller", true);
            Http.Response httpResponse = Http.getApi("/User/getDetailUser?" + params);
            if (httpResponse.statusCode == 200) {
                String isSeller = Json.str(Json.object(httpResponse.jsonObject(), "detailUser"), "isSeller");
                return Boolean.valueOf("1".equals(isSeller));
            }
            return null;
        }, (Boolean seller) -> {
            if (seller != null) {
                isSellerState = seller;
                updateForm();
            }
        });
    }

    private void konfirmasiOTP() {
        final String code = verificationCodeField.getText().toString();
        final boolean seller = isSellerState;
        final String idPasar = pasarSelected;
        async(() -> {
            String idUser = LocalStorage.getCurrentUserID();
            Map<String, String> otpData = new LinkedHashMap<>();
            otpData.put("idUser", idUser);
            otpData.put("verificationCode", code);
            otpData.put("isSeller", seller ? "yes" : "no");
            if (seller) {
                otpData.put("idPasar", idPasar);
            }
            return Http.postApi("/Auth/verifikasiUser", HTTPRequest.postBody(otpData));
        }, httpResponse -> {
            if (httpResponse.statusCode != 200) {
                return;
            }
            boolean isOTPSuccess = Json.bool(httpResponse.jsonObject(), "statusVerifikasiUser");
            if (isOTPSuccess) {
                AuthRoute.pushReplacement(this, AuthRoute.routeClass(args()), AuthRoute.routeArgs(args()),
                        SplashScreenActivity.class);
            } else {
                String title = "Verifikasi User";
                String subTitle = "Verifikasi User gagal, silahkan ulangi kembali !";
                Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(this,
                        CustomAlert.textButton(this, "OK", () -> handle.close()));
                CustomAlert.<Object>textComponent(this, null, title, subTitle, actions, null, false, null);
            }
        });
    }
}
