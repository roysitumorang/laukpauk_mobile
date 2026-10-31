package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.Prefs;
import id.laukpauk.model.ProfilSayaModel;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.pembeli.components.profilsaya.GantiFotoProfil;
import id.laukpauk.view.pembeli.components.profilsaya.GantiNomorHP;
import id.laukpauk.view.pembeli.components.profilsaya.GantiPassword;

/** Port of lib/views/pembeli/ProfilSaya.dart. */
public class ProfilSayaActivity extends BaseActivity {
    private boolean isSimpanPerubahanDataBusy = false;

    private EditText namaLengkapController;
    private EditText alamatController;
    private EditText usernameController;

    private FrameLayout bodyHolder;
    private LinearLayout photoHolder;
    private JsonElement profilSaya;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        namaLengkapController = CustomForm.controller(this);
        alamatController = CustomForm.controller(this);
        usernameController = CustomForm.controller(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);

        View bar = DetailUi.appBar(this, "Profil Saya");
        // AppBar actions: TextButton('Keluar')
        FrameLayout toolbar = (FrameLayout) ((ViewGroup) bar).getChildAt(0);
        TextView keluar = Ui.text(this, "Keluar", 14f, CustomColors.nearlyWhite, true);
        Ui.padXY(keluar, 12, 10);
        keluar.setOnClickListener(v -> logout());
        toolbar.addView(keluar, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.END | Gravity.CENTER_VERTICAL));
        root.addView(bar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        bodyHolder = new FrameLayout(this);
        root.addView(bodyHolder, Ui.weightH(1f));
        setRoot(root);

        showSpinner();
        async(ProfilSayaModel::getDetailActiveUser, detail -> {
            if (detail == null) {
                return; // Dart: snapshot without data keeps the spinner
            }
            profilSaya = detail;
            if (!Json.isFalse(detail)) {
                fillTextFields(detail);
            }
            render();
        });
    }

    private void showSpinner() {
        bodyHolder.removeAllViews();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(this, 35), Ui.dp(this, 35), Gravity.CENTER);
        bodyHolder.addView(DetailUi.smallSpinner(this, 35f), lp);
    }

    private void fillTextFields(JsonElement detailUser) {
        JsonObject d = Json.obj(detailUser);
        namaLengkapController.setText(Json.str(d, "nama"));
        alamatController.setText(Json.str(d, "alamat"));
        usernameController.setText(Json.str(d, "username"));
    }

    /** Dart: initializeTextFieldValue(). */
    private void initializeTextFieldValue() {
        async(ProfilSayaModel::getDetailActiveUser, detailUser -> {
            if (detailUser != null && !Json.isFalse(detailUser)) {
                fillTextFields(detailUser);
            }
        });
    }

    private void logout() {
        async(() -> {
            String cartCode = LocalStorage.getCartCode();

            String params = HTTPRequest.getQueryString("cartCode=" + cartCode, true);

            Http.Response httpResponse = Http.getApi("/Auth/removeToken?" + params);
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                if (Json.bool(dataObject, "removeToken")) {
                    Prefs.remove("authToken");

                    com.onesignal.OneSignal.logout();
                    LocalStorage.removeExternalUserID();
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }, removed -> {
            if (removed != null && removed) {
                TransaksiModel.removeTransactionSession(this, done -> {
                    AppState.get().setJumlahNotifikasi(0);
                    Nav.pushReplacement(this, SplashScreenActivity.class, null);
                });
            }
        });
    }

    private void render() {
        bodyHolder.removeAllViews();
        float hb = Ui.hb(this);
        final int borderColor = CustomColors.green;

        if (profilSaya != null && Json.isFalse(profilSaya)) {
            LinearLayout column = Ui.column(this);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(this, "empty.png", 150f));
            column.addView(Ui.spaceH(this, 10));
            column.addView(Ui.text(this, "Data Akun Anda tidak Ditemukan", 14f, CustomColors.textDanger));
            bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
            return;
        }

        JsonObject profil = Json.obj(profilSaya);

        LinearLayout page = Ui.column(this);
        Ui.padXY(page, 5, 5);
        page.setClipChildren(false);

        photoHolder = Ui.column(this);
        Ui.padAll(photoHolder, 4);
        photoHolder.setClipChildren(false);
        photoHolder.addView(newFotoProfil(), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        page.addView(photoHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout content = Ui.column(this);

        LinearLayout form = Ui.column(this);
        Ui.padXY(form, 15, 15);
        form.addView(Ui.text(this, "Profil", hb * 4.0f, 0xDD000000, true));
        form.addView(Ui.spaceH(this, 5));
        form.addView(Ui.text(this, "Anda dapat mengubah data profil anda dengan mengisi form di bawah ini",
                Txt.textXS(this)));
        form.addView(Ui.spaceH(this, 15));
        form.addView(CustomForm.label(this, "Nama Lengkap"));
        form.addView(CustomForm.textField(this, "Nama Lengkap", namaLengkapController,
                new CustomForm.TextFieldOpts().borderColor(borderColor).borderWidth(1.0)));
        form.addView(Ui.spaceH(this, 15));
        form.addView(CustomForm.label(this, "Alamat Lengkap"));
        form.addView(CustomForm.textField(this, "Alamat Lengkap", alamatController,
                new CustomForm.TextFieldOpts().borderColor(borderColor).borderWidth(1.0)));
        form.addView(Ui.spaceH(this, 15));
        form.addView(CustomForm.label(this, "Username"));
        form.addView(CustomForm.textField(this, "Nama Pengguna (Username)", usernameController,
                new CustomForm.TextFieldOpts().borderColor(borderColor).borderWidth(1.0)));
        form.addView(Ui.spaceH(this, 15));

        final CustomButton.Btn[] ref = new CustomButton.Btn[1];
        final CustomButton.Btn simpan = CustomButton.button(this, "Simpan Perubahan Data Anda",
                () -> onSimpan(ref[0]),
                new CustomButton.Opts().busy(isSimpanPerubahanDataBusy).block().color(CustomButton.themeColor));
        ref[0] = simpan;
        simpan.setBusy(isSimpanPerubahanDataBusy);
        form.addView(simpan, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        form.addView(Ui.spaceH(this, 5));
        content.addView(form, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(new GantiNomorHP(this, profil), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(new GantiPassword(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        page.addView(scroll, Ui.weightH(1f));

        bodyHolder.addView(page, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private View newFotoProfil() {
        return new GantiFotoProfil(this, profilSaya, statusGantiFotoProfil -> {
            if (statusGantiFotoProfil != null && statusGantiFotoProfil) {
                // setState(() {}) -> the FutureBuilder fetches the profile again
                async(ProfilSayaModel::getDetailActiveUser, detail -> {
                    if (detail == null || Json.isFalse(detail)) {
                        return;
                    }
                    profilSaya = detail;
                    photoHolder.removeAllViews();
                    photoHolder.addView(newFotoProfil(), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                });
            }
        });
    }

    private void onSimpan(final CustomButton.Btn simpan) {
        isSimpanPerubahanDataBusy = true;
        simpan.setBusy(true);
        final JsonObject dataUser = Json.of(
                "namaLengkap", namaLengkapController.getText().toString(),
                "alamat", alamatController.getText().toString(),
                "username", usernameController.getText().toString());

        async(() -> ProfilSayaModel.simpanPerubahanDataProfil(dataUser), statusUpdateProfil -> {
            if (statusUpdateProfil != null && statusUpdateProfil) {
                initializeTextFieldValue();
            }
            isSimpanPerubahanDataBusy = false;
            simpan.setBusy(false);
        }, error -> {
            isSimpanPerubahanDataBusy = false;
            simpan.setBusy(false);
        });
    }
}
