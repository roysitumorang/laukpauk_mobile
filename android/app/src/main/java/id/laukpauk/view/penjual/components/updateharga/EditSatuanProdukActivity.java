package id.laukpauk.view.penjual.components.updateharga;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.UpdateHargaModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.PesananScreenUi;

/**
 * Port of lib/views/penjual/components/UpdateHarga/EditSatuanProduk.dart (pushed as a route).
 * Args: idProduk (String, required), dataSatuan (object, optional), appBarTitle (String, default "Edit Satuan Produk"),
 * isUpdate (boolean, default true). Result: true / false (saved or not).
 */
public class EditSatuanProdukActivity extends BaseActivity {
    private EditText namaController;
    private EditText hargaController;
    private EditText diskonController;
    private EditText keteranganController;
    private String jenisHarga = "";

    private TextView noteJenisHarga;
    private LinearLayout buttonHolder;
    private CustomButton.Btn simpan;

    private JsonObject dataSatuan;
    private String idProduk;
    private boolean isUpdate;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idProduk = Json.str(args(), "idProduk");
        dataSatuan = Json.objectOrNull(args(), "dataSatuan");
        String appBarTitle = Json.str(args(), "appBarTitle", "Edit Satuan Produk");
        isUpdate = Json.bool(args(), "isUpdate", true);

        namaController = new EditText(this);
        hargaController = new EditText(this);
        diskonController = new EditText(this);
        keteranganController = new EditText(this);

        // initData()
        if (dataSatuan != null) {
            namaController.setText(Json.str(dataSatuan, "nama"));
            hargaController.setText(Json.str(dataSatuan, "harga"));
            diskonController.setText(Json.str(dataSatuan, "diskon"));
            jenisHarga = Json.str(dataSatuan, "jenisHarga");
            keteranganController.setText(Json.str(dataSatuan, "keterangan"));
        } else {
            hargaController.setText("0");
            diskonController.setText("0");
        }

        float hb = Ui.hb(this);
        String actions = isUpdate ? "edit (perbaharui)" : "tambah";

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(PesananScreenUi.scaffoldBackground);
        root.addView(DetailUi.appBar(this, appBarTitle));

        LinearLayout card = PenjualUi.cardPadded(this, 15, 15);
        card.addView(PenjualUi.text(this, isUpdate ? "Edit Satuan Produk" : "Satuan Produk Baru", 4f, 0xDD000000,
                true));
        card.addView(PenjualUi.text(this, "Silahkan " + actions + " data harga produk dengan mengisi form berikut ini :",
                3.25f, 0xDD000000, false));
        card.addView(Ui.spaceH(this, 15));

        card.addView(field(namaController, "Nama Satuan (Harga)", InputType.TYPE_CLASS_TEXT),
                PenjualUi.lp(this, Ui.MATCH, Ui.dp(this, hb * 15), 0, 0, 0, 7.5f));
        card.addView(Ui.spaceH(this, 5));

        LinearLayout hargaRow = new LinearLayout(this);
        hargaRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams hargaLp = new LinearLayout.LayoutParams(0, Ui.dp(this, hb * 15), 1f);
        hargaLp.bottomMargin = Ui.dp(this, 7.5f);
        hargaRow.addView(field(hargaController, "Harga (Rupiah)", InputType.TYPE_CLASS_NUMBER), hargaLp);
        hargaRow.addView(Ui.spaceW(this, 5));
        LinearLayout.LayoutParams diskonLp = new LinearLayout.LayoutParams(0, Ui.dp(this, hb * 15), 1f);
        diskonLp.bottomMargin = Ui.dp(this, 7.5f);
        hargaRow.addView(field(diskonController, "Diskon (Rupiah)", InputType.TYPE_CLASS_NUMBER), diskonLp);
        card.addView(hargaRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 5));

        // Jenis harga dropdown
        FrameLayout dropBox = new FrameLayout(this);
        dropBox.setBackground(Ui.shape(this, 0xFFF0F8FF, 10f));
        Ui.pad(dropBox, 25, 0, 15, 0);
        List<String> values = new ArrayList<>(Arrays.asList("", "pas", "tidak pas"));
        List<String> labels = new ArrayList<>(Arrays.asList("Jenis Harga", "Harga Pas", "Harga Tidak Pas"));
        dropBox.addView(PenjualUi.dropdown(this, values, labels, jenisHarga, hb * 3.25f, selected -> {
            jenisHarga = selected;
            refreshForm();
        }), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER_VERTICAL));
        card.addView(dropBox, PenjualUi.lp(this, Ui.MATCH, Ui.dp(this, hb * 12), 0, 0, 0, 7.5f));

        noteJenisHarga = Ui.text(this, "", hb * 3.25f, CustomColors.textMuted);
        card.addView(noteJenisHarga, PenjualUi.lp(this, Ui.WRAP, Ui.WRAP, 0, 0, 0, 10));
        card.addView(Ui.spaceH(this, 5));

        card.addView(field(keteranganController, "Keterangan", InputType.TYPE_CLASS_TEXT),
                PenjualUi.lp(this, Ui.MATCH, Ui.dp(this, hb * 15), 0, 0, 0, 7.5f));

        buttonHolder = Ui.column(this);
        card.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout wrap = Ui.column(this);
        Ui.padXY(wrap, 5, 10);
        wrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(wrap, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);

        Runnable refresh = () -> refreshForm();
        PenjualUi.watch(namaController, refresh);
        PenjualUi.watch(hargaController, refresh);
        PenjualUi.watch(diskonController, refresh);
        refreshForm();
    }

    /** Container(height, padding right 15 left 25, color 0xFFF0F8FF, radius 10) > TextField(labelText). */
    private FrameLayout field(EditText e, String label, int inputType) {
        float hb = Ui.hb(this);
        FrameLayout box = new FrameLayout(this);
        box.setBackground(Ui.shape(this, 0xFFF0F8FF, 10f));
        Ui.pad(box, 25, 0, 15, 0);
        PenjualUi.styleEdit(this, e, label, inputType, hb * 3.5f, 0xDD000000, 0xFF9E9E9E);
        e.setPadding(0, 0, 0, 0);
        box.addView(e, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER_VERTICAL));
        return box;
    }

    /** The part of Dart build() that depends on the controllers / jenisHarga. */
    private void refreshForm() {
        float hb = Ui.hb(this);
        boolean isFormComplete = namaController.getText().length() >= 1
                && hargaController.getText().length() >= 1
                && diskonController.getText().length() >= 1
                && jenisHarga.length() >= 1;

        noteJenisHarga.setTextColor(jenisHarga.length() >= 1 ? CustomColors.textInfo : CustomColors.textMuted);
        String note = "Silahkan tentukan jenis harga pada satuan (harga) produk  ini.";
        if (jenisHarga.toLowerCase(Locale.ROOT).equals("pas")) {
            note = "Tipe ini adalah produk anda TIDAK memiliki quantity. Contoh jual cabe dengan harga Rp. 2.000, tidak ada quantity 1 s/d 10.";
        }
        if (jenisHarga.toLowerCase(Locale.ROOT).equals("tidak pas")) {
            note = "Tipe ini adalah produk anda memiliki quantity. Pembeli dapat memilih quantity 1 s/d 10.";
        }
        noteJenisHarga.setText(note);

        boolean showing = buttonHolder.getChildCount() > 0;
        if (isFormComplete != showing) {
            buttonHolder.removeAllViews();
            simpan = null;
            if (isFormComplete) {
                simpan = CustomButton.button(this, "Simpan", () -> simpanSatuan(),
                        new CustomButton.Opts().color(CustomButton.themeColor).pill().block());
                buttonHolder.addView(simpan, PenjualUi.lp(this, Ui.MATCH, Ui.WRAP, 0, 15, 0, 10));
            }
        }
    }

    private void simpanSatuan() {
        simpan.setBusy(true);

        // Dart: `String idSatuan = (dataSatuan != null) ? dataSatuan['id'] : null;` - a new unit has no id here.
        final String idSatuan = (dataSatuan != null) ? Json.str(dataSatuan, "id") : null;

        final JsonObject dataSatuanProduk = new JsonObject();
        dataSatuanProduk.addProperty("idProduk", idProduk);
        dataSatuanProduk.addProperty("nama", namaController.getText().toString());
        dataSatuanProduk.addProperty("harga", hargaController.getText().toString());
        dataSatuanProduk.addProperty("diskon", diskonController.getText().toString());
        dataSatuanProduk.addProperty("jenisHarga", jenisHarga);
        dataSatuanProduk.addProperty("keterangan", keteranganController.getText().toString());

        async(() -> UpdateHargaModel.editSatuanProduk(idSatuan, dataSatuanProduk),
                statusEditSatuan -> finishWithResult(statusEditSatuan));
    }
}
