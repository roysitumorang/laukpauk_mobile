package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.UserAndDashboardActivity;

/** Port of lib/views/penjual/RequestPencairanDana.dart (no arguments). */
public class RequestPencairanDanaActivity extends BaseActivity {
    private EditText nomorRekeningController;
    private EditText namaPenerimaController;
    private EditText nominalController;
    private EditText keteranganController;

    private int saldoSaatIni = 0;

    private TextView saldoText;
    private TextView saldoTidakCukup;
    private LinearLayout buttonHolder;

    /** Dart getDetailCurrentSeller(): fills the account number / receiver name. BLOCKING part first. */
    private void getDetailCurrentSeller() {
        async(() -> {
            String getPenjualEntities = "nomorRekening, namaUser";
            String params = HTTPRequest.getQueryString("getPenjualEntities=" + getPenjualEntities);
            String idPenjual = LocalStorage.getCurrentUserID();

            Http.Response httpResponse = Http.getApi("/Penjual/getDetailPenjual/" + idPenjual + "?" + params);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.object(dataObject, "detailPenjual");
            }
            return null;
        }, detailCurrentSeller -> {
            if (detailCurrentSeller != null) {
                nomorRekeningController.setText(Json.str(detailCurrentSeller, "nomorRekening"));
                namaPenerimaController.setText(Json.str(detailCurrentSeller, "namaUser"));
            }
        });
    }

    private void ajukanRequest() {
        final Map<String, String> dataRequest = new LinkedHashMap<>();
        dataRequest.put("nomorRekening", nomorRekeningController.getText().toString());
        dataRequest.put("namaPenerima", namaPenerimaController.getText().toString());
        dataRequest.put("nominal", nominalController.getText().toString());
        dataRequest.put("keterangan", keteranganController.getText().toString());

        async(() -> {
            String params = HTTPRequest.getQueryString(null, true);
            Map<String, String> body = HTTPRequest.postBody(dataRequest);

            Http.Response httpResponse = Http.postApi("/Penjual/ajukanRPD?" + params, body);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Boolean.valueOf(Json.bool(dataObject, "statusRequest"));
            }
            return null;
        }, statusRequest -> {
            if (statusRequest == null) {
                return;
            }
            String text = "Pengajuan pencairan dana gagal dilakukan !";
            Runnable onOk = null;
            if (statusRequest) {
                text = "Pengajuan request berhasil dilakukan ! Tunggu hingga admin mentransfer dana yang diperlukan ke rekening yang anda masukkan.";
                onOk = () -> Nav.push(this, UserAndDashboardActivity.class);
            }
            PenjualUi.alertOk(this, "Pengajuan Pencairan Dana", text, 4f, 3.5f, onOk);
        });
    }

    /** Dart getTotalSaldo(). BLOCKING. */
    private int getTotalSaldo() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Penjual/getTotalSaldo?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalSaldo", 0);
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        nomorRekeningController = new EditText(this);
        namaPenerimaController = new EditText(this);
        nominalController = new EditText(this);
        keteranganController = new EditText(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, "Request Pencairan Dana"));

        LinearLayout card = PenjualUi.cardPadded(this, 15, 20);
        card.addView(PenjualUi.text(this, "Request Pencairan Dana", 4f, 0xDD000000, true));
        card.addView(Ui.spaceH(this, 3));
        card.addView(PenjualUi.text(this,
                "Lakukan request pencairan dana untuk mendapatkan uang hasil berdagang anda", 3.25f, 0xDD000000,
                false));
        card.addView(Ui.spaceH(this, 10));
        saldoText = PenjualUi.text(this, "", 4f, CustomColors.green, true);
        card.addView(saldoText);
        card.addView(PenjualUi.text(this, "Request dengan nominal 0 Rupiah tidak akan dapat diproses", 3f,
                CustomColors.textMuted, false));
        card.addView(Ui.spaceH(this, 20));

        card.addView(PenjualUi.pillField(this, nomorRekeningController, "Nomor Rekening", InputType.TYPE_CLASS_TEXT,
                2.5f));
        card.addView(Ui.spaceH(this, 10));
        card.addView(PenjualUi.pillField(this, namaPenerimaController, "Nama Penerima", InputType.TYPE_CLASS_TEXT,
                2.5f));
        card.addView(Ui.spaceH(this, 10));
        card.addView(PenjualUi.pillField(this, nominalController, "Nominal (Rupiah)", InputType.TYPE_CLASS_NUMBER,
                2.5f));
        saldoTidakCukup = PenjualUi.text(this, "Saldo anda tidak mencukupi", 3f, CustomColors.textDanger, false);
        Ui.pad(saldoTidakCukup, 15, 5, 0, 0);
        card.addView(saldoTidakCukup);
        card.addView(Ui.spaceH(this, 10));
        card.addView(PenjualUi.pillField(this, keteranganController, "Keterangan Tambahan (Opsional)",
                InputType.TYPE_CLASS_TEXT, 2.5f));
        card.addView(Ui.spaceH(this, 20));
        buttonHolder = Ui.column(this);
        card.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout wrap = Ui.column(this);
        Ui.padXY(wrap, 5, 5);
        wrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(wrap, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);

        Runnable refresh = () -> refreshForm();
        PenjualUi.watch(nomorRekeningController, refresh);
        PenjualUi.watch(namaPenerimaController, refresh);
        PenjualUi.watch(nominalController, refresh);
        refreshForm();

        getDetailCurrentSeller();
        async(() -> getTotalSaldo(), totalSaldo -> {
            saldoSaatIni = totalSaldo;
            refreshForm();
        });
    }

    /** The part of Dart build() that depends on the state / the text controllers. */
    private void refreshForm() {
        String nominalText = nominalController.getText().toString();
        long intNominal = 0;
        if (nominalText.length() >= 1) {
            try {
                intNominal = Long.parseLong(nominalText);
            } catch (NumberFormatException e) {
                intNominal = 0;
            }
        }

        boolean nominalIsLessThanSaldo = intNominal <= saldoSaatIni;
        boolean isFormCompleted = nomorRekeningController.getText().length() >= 1
                && namaPenerimaController.getText().length() >= 1
                && nominalText.length() >= 1
                && nominalIsLessThanSaldo
                && intNominal >= 1;

        saldoText.setText("Saldo anda saat ini " + Currency.formatRupiah(saldoSaatIni, 0));
        saldoTidakCukup.setVisibility(nominalIsLessThanSaldo ? View.GONE : View.VISIBLE);

        boolean showing = buttonHolder.getChildCount() > 0;
        if (isFormCompleted != showing) {
            buttonHolder.removeAllViews();
            if (isFormCompleted) {
                buttonHolder.addView(CustomButton.button(this, "Ajukan Request", () -> ajukanRequest(),
                        new CustomButton.Opts().color(CustomButton.themeColor).outline().block().pill()),
                        new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        }
    }
}
