package id.laukpauk.view.pembeli.components.profilsaya;

import android.content.Context;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.AccountUi;
import id.laukpauk.view.pembeli.UserAndDashboardActivity;

/** Port of lib/views/pembeli/components/ProfilSaya/GantiPassword.dart. */
public class GantiPassword extends LinearLayout {
    private final BaseActivity activity;

    private EditText passwordLamaController;
    private EditText passwordBaruController;
    private EditText ulangiPasswordBaruController;

    private View buttonWrap;

    public GantiPassword(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        this.activity = (BaseActivity) ctx;
        build();
        updateButton();
    }

    private void simpanPasswordBaru() {
        Map<String, String> dataUser = new LinkedHashMap<>();
        dataUser.put("passwordLama", passwordLamaController.getText().toString());
        dataUser.put("passwordBaru", passwordBaruController.getText().toString());
        dataUser.put("konfirmasiPasswordBaru", ulangiPasswordBaruController.getText().toString());

        activity.async(() -> {
            String params = HTTPRequest.getQueryString(null, true);
            Map<String, String> body = HTTPRequest.postBody(dataUser);
            Http.Response httpResponse = Http.postApi("/User/gantiPassword?" + params, body);
            if (httpResponse.statusCode == 200) {
                return Boolean.valueOf(Json.bool(httpResponse.jsonObject(), "statusGantiPassword"));
            }
            return null;
        }, statusGantiPassword -> {
            if (statusGantiPassword == null) {
                return;
            }
            String text = "Pembaharuan password gagal diproses !";
            if (statusGantiPassword) {
                text = "Berhasil memperbaharui password anda !";
            }
            final boolean ok = statusGantiPassword;
            AccountUi.alert(activity, "Password", text, 20f, Ui.hb(activity) * 3.5f, "OK", () -> {
                if (ok) {
                    Nav.push(activity, UserAndDashboardActivity.class, null);
                }
            });
        });
    }

    private EditText passwordField(String label, String hint, final boolean[] shown) {
        final Context ctx = getContext();
        final EditText controller = CustomForm.controller(ctx);
        final ImageView eye = Ui.icon(ctx, R.drawable.ic_visibility, 24, CustomColors.textMuted);
        eye.setOnClickListener(v -> {
            shown[0] = !shown[0];
            eye.setImageResource(!shown[0] ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
            controller.setTransformationMethod(shown[0]
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            controller.setSelection(controller.getText().length());
        });
        addView(CustomForm.label(ctx, label));
        addView(CustomForm.textField(ctx, hint, controller, new CustomForm.TextFieldOpts()
                .obsecureText(true)
                .trailing(eye)
                .borderColor(CustomColors.green)
                .borderWidth(1.0)
                .onChange(value -> updateButton())));
        return controller;
    }

    private void build() {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        Ui.padXY(this, 15, 15);

        addView(Ui.text(ctx, "Password", hb * 4.0f, 0xDD000000, true));
        addView(Ui.spaceH(ctx, 5));
        addView(Ui.text(ctx, "Anda dapat mengubah password anda dengan mengisi form di bawah ini",
                Txt.textXS(ctx)));
        addView(Ui.spaceH(ctx, 15));

        passwordLamaController = passwordField("Password Lama", "Password Lama Anda", new boolean[]{false});
        addView(Ui.spaceH(ctx, 10));
        passwordBaruController = passwordField("Password Baru", "Minimal 8 Karakter", new boolean[]{false});
        addView(Ui.spaceH(ctx, 10));
        ulangiPasswordBaruController = passwordField("Konfirmasi Password Baru", "Ulangi Password Baru",
                new boolean[]{false});

        LinearLayout wrap = Ui.column(ctx);
        Ui.pad(wrap, 0, 15, 0, 0);
        wrap.addView(CustomButton.button(ctx, "Simpan Password Baru", this::simpanPasswordBaru,
                new CustomButton.Opts().block().outline().color(CustomButton.themeColor)));
        buttonWrap = wrap;
        addView(wrap, new LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));
    }

    private void updateButton() {
        String baru = passwordBaruController.getText().toString();
        String ulangi = ulangiPasswordBaruController.getText().toString();
        boolean isFormCompleted = (baru.length() >= 8 && ulangi.length() >= 8 && baru.equals(ulangi));
        buttonWrap.setVisibility(isFormCompleted ? VISIBLE : GONE);
    }
}
