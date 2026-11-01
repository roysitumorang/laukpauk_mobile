package id.laukpauk.view.pembeli.components.profilsaya;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.App;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.Prefs;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.AccountUi;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.UserAndDashboardActivity;

/** Port of lib/views/pembeli/components/ProfilSaya/GantiNomorHP.dart. */
public class GantiNomorHP extends LinearLayout {
    private static final String KEY_ID_PENGGANTIAN = "flutter.idPenggantianNomor";

    private final BaseActivity activity;
    private final JsonObject profilSaya;

    private long idPenggantianNomorState = 0;

    private EditText nomorHPController;
    private EditText kodeVerifikasiController;

    private LinearLayout modeA;
    private LinearLayout modeB;
    private View cekSpinner;
    private TextView batasText;
    private LinearLayout formBaru;
    private View nomorButtonWrap;
    private View kodeButtonWrap;

    public GantiNomorHP(Context ctx, JsonObject profilSaya) {
        super(ctx);
        setOrientation(VERTICAL);
        this.activity = (BaseActivity) ctx;
        this.profilSaya = profilSaya;

        idPenggantianNomorState = readId();
        build();
        updateState();
        if (idPenggantianNomorState == 0) {
            loadCek();
        }
    }

    // ------------------------------------------------------------------ SharedPreferences getInt / setInt / remove

    private static SharedPreferences sp() {
        return App.context().getSharedPreferences(Prefs.FILE_NAME, Context.MODE_PRIVATE);
    }

    private static long readId() {
        try {
            return sp().getLong(KEY_ID_PENGGANTIAN, 0L);
        } catch (ClassCastException e) {
            try {
                return sp().getInt(KEY_ID_PENGGANTIAN, 0);
            } catch (ClassCastException e2) {
                return 0L;
            }
        }
    }

    // ------------------------------------------------------------------ API

    private JsonObject simpanNomorBaru() {
        Map<String, String> httpBody = new LinkedHashMap<>();
        httpBody.put("nomorBaru", nomorHPController.getText().toString());

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> body = HTTPRequest.postBody(httpBody);

        Http.Response httpResponse = Http.postApi("/Auth/gantiNomor?" + params, body);
        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }
        return Json.of("statusSimpanNomorBaru", false);
    }

    private boolean verifikasi() {
        String idPenggantianNomor = sp().contains(KEY_ID_PENGGANTIAN)
                ? String.valueOf(readId()) : "null";

        Map<String, String> httpBody = new LinkedHashMap<>();
        httpBody.put("idPenggantianNomor", idPenggantianNomor);
        httpBody.put("kodeVerifikasi", kodeVerifikasiController.getText().toString());

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> body = HTTPRequest.postBody(httpBody);

        Http.Response httpResponse = Http.postApi("/Auth/verifikasiNomor?" + params, body);
        if (httpResponse.statusCode == 200) {
            return Json.bool(httpResponse.jsonObject(), "statusVerifikasiNomor");
        }
        return false;
    }

    private int cekJumlahGantiNomorHPForToday() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Auth/jumlahGantiNomorHPForToday?" + params);
        if (httpResponse.statusCode == 200) {
            return Json.intVal(httpResponse.jsonObject(), "jumlahGantiNomorHPForToday");
        }
        return 0;
    }

    // ------------------------------------------------------------------ UI

    private CustomForm.TextFieldOpts fieldOpts() {
        return new CustomForm.TextFieldOpts()
                .borderColor(CustomColors.green)
                .borderWidth(1.0)
                .onChange(value -> updateState());
    }

    private void build() {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);

        nomorHPController = CustomForm.controller(ctx);
        kodeVerifikasiController = CustomForm.controller(ctx);

        addView(divider(ctx));

        // ---- idPenggantianNomorState == 0
        modeA = Ui.column(ctx);
        Ui.padXY(modeA, 15, 15);
        modeA.addView(Ui.text(ctx, "Nomor HP", hb * 4.0f, 0xDD000000, true));
        modeA.addView(Ui.spaceH(ctx, 5));
        modeA.addView(Ui.text(ctx, "Anda dapat mengubah nomor telepon anda dengan mengisi nomor lama",
                Txt.textXS(ctx)));
        modeA.addView(Ui.spaceH(ctx, 15));

        TextView current = Ui.text(ctx, "Nomor telepon anda saat ini : " + Json.str(profilSaya, "nomorHP"),
                hb * 3f, CustomColors.nearlyWhite);
        Ui.padXY(current, 10, 5);
        current.setBackground(Ui.shape(ctx, CustomColors.textPrimary, 15f));
        modeA.addView(current, new LayoutParams(Ui.WRAP, Ui.WRAP));
        modeA.addView(Ui.spaceH(ctx, 15));

        FrameLayout cekHolder = new FrameLayout(ctx);
        cekSpinner = DetailUi.smallSpinner(ctx, 20f);
        Ui.padXY(cekSpinner, 0, 25);
        cekHolder.addView(cekSpinner, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        batasText = Ui.text(ctx,
                "Mohon maaf, anda sudah mencapai batas maksimal ganti nomor telepon untuk hari ini. Silahkan ulangi lagi keesokan harinya.",
                hb * 3.25f, CustomColors.textDanger);
        batasText.setGravity(Gravity.CENTER);
        cekHolder.addView(batasText, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER));

        formBaru = Ui.column(ctx);
        formBaru.addView(CustomForm.label(ctx, "Nomor HP Baru"));
        formBaru.addView(CustomForm.textField(ctx, "Contoh : 0812XXXXXXXX", nomorHPController, fieldOpts()));
        LinearLayout nomorWrap = Ui.column(ctx);
        Ui.pad(nomorWrap, 0, 10, 0, 0);
        nomorWrap.addView(CustomButton.button(ctx, "Simpan Nomor Baru", this::onSimpanNomorBaru,
                new CustomButton.Opts().block().outline().color(CustomButton.themeColor)));
        nomorButtonWrap = nomorWrap;
        formBaru.addView(nomorWrap, new LayoutParams(Ui.MATCH, Ui.WRAP));
        cekHolder.addView(formBaru, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        modeA.addView(cekHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
        modeA.addView(Ui.spaceH(ctx, 10));
        addView(modeA, new LayoutParams(Ui.MATCH, Ui.WRAP));

        // ---- idPenggantianNomorState != 0
        modeB = Ui.column(ctx);
        Ui.padXY(modeB, 15, 15);
        LinearLayout titleRow = Ui.row(ctx);
        titleRow.addView(Ui.text(ctx, "Verifikasi Nomor Telepon", hb * 4.0f, 0xDD000000, true), Ui.weightW(1f));
        TextView batal = Ui.text(ctx, "Batal", hb * 3.5f, CustomColors.textDanger, true);
        batal.setOnClickListener(v -> {
            Prefs.remove("idPenggantianNomor");
            idPenggantianNomorState = 0;
            updateState();
            loadCek();
        });
        titleRow.addView(batal, new LayoutParams(Ui.WRAP, Ui.WRAP));
        modeB.addView(titleRow, new LayoutParams(Ui.MATCH, Ui.WRAP));
        modeB.addView(Ui.spaceH(ctx, 5));
        modeB.addView(Ui.text(ctx,
                "Silahkan masukkan kode verifikasi yang kami kirim ke nomor baru anda, atau jika anda dapat membatalkan proses penggantian nomor baru.",
                hb * 3.5f, 0xDD000000));
        modeB.addView(Ui.spaceH(ctx, 15));
        modeB.addView(CustomForm.label(ctx, "Kode Verifikasi"));
        modeB.addView(CustomForm.textField(ctx, "Kode Verifikasi", kodeVerifikasiController, fieldOpts()));
        LinearLayout kodeWrap = Ui.column(ctx);
        Ui.pad(kodeWrap, 0, 10, 0, 0);
        kodeWrap.addView(CustomButton.button(ctx, "Verifikasi Nomor Baru", this::onVerifikasi,
                new CustomButton.Opts().color(CustomButton.themeColor).pill().block()));
        kodeButtonWrap = kodeWrap;
        modeB.addView(kodeWrap, new LayoutParams(Ui.MATCH, Ui.WRAP));
        modeB.addView(Ui.spaceH(ctx, 10));
        addView(modeB, new LayoutParams(Ui.MATCH, Ui.WRAP));

        addView(divider(ctx));
    }

    private View divider(Context ctx) {
        View d = new View(ctx);
        d.setBackgroundColor(CustomColors.textLightMuted);
        LayoutParams lp = new LayoutParams(Ui.MATCH, Ui.dp(ctx, 1));
        int m = Ui.dp(ctx, 15);
        lp.setMargins(m, m, m, m);
        d.setLayoutParams(lp);
        return d;
    }

    /** Dart: the rebuild after setState (isFormCompleted and the idPenggantianNomorState branches). */
    private void updateState() {
        boolean isFormCompleted = (idPenggantianNomorState == 0)
                ? (nomorHPController.getText().length() >= 12)
                : (kodeVerifikasiController.getText().length() >= 6);
        modeA.setVisibility(idPenggantianNomorState == 0 ? VISIBLE : GONE);
        modeB.setVisibility(idPenggantianNomorState != 0 ? VISIBLE : GONE);
        nomorButtonWrap.setVisibility(isFormCompleted ? VISIBLE : GONE);
        kodeButtonWrap.setVisibility(isFormCompleted ? VISIBLE : GONE);
    }

    private void loadCek() {
        cekSpinner.setVisibility(VISIBLE);
        batasText.setVisibility(GONE);
        formBaru.setVisibility(GONE);
        activity.async(this::cekJumlahGantiNomorHPForToday, jumlah -> {
            if (jumlah == null) {
                return;
            }
            cekSpinner.setVisibility(GONE);
            if (jumlah >= 1) {
                batasText.setVisibility(VISIBLE);
            } else {
                formBaru.setVisibility(VISIBLE);
            }
        });
    }

    private void onSimpanNomorBaru() {
        activity.async(this::simpanNomorBaru, response -> {
            if (response == null) {
                return;
            }
            boolean statusSimpanNomorBaru = Json.bool(response, "statusSimpanNomorBaru");
            long idPenggantianNomor = Json.asLong(Json.get(response, "idPenggantianNomor"), 0);

            if (statusSimpanNomorBaru) {
                sp().edit().putLong(KEY_ID_PENGGANTIAN, idPenggantianNomor).apply();

                idPenggantianNomorState = idPenggantianNomor;
                updateState();
            }
        });
    }

    private void onVerifikasi() {
        activity.async(this::verifikasi, statusVerifikasiNomorTelepon -> {
            if (statusVerifikasiNomorTelepon != null && statusVerifikasiNomorTelepon) {
                Prefs.remove("idPenggantianNomor");
                Nav.pushReplacement(activity, UserAndDashboardActivity.class, null);
            } else {
                AccountUi.alert(activity, "Verifikasi Nomor Telepon",
                        "Mohon maaf, verifikasi nomor telepon gagal! Silahkan coba lagi.",
                        Ui.hb(activity) * 3.75f, Ui.hb(activity) * 3.25f, "Coba Lagi", null);
            }
        });
    }
}
