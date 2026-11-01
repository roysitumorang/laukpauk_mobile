package id.laukpauk.view.penjual;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ImagePickerModel;
import id.laukpauk.model.KategoriProdukModel;
import id.laukpauk.model.PaketModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.model.TambahProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/**
 * Port of lib/views/penjual/TambahProduk.dart.
 * Args: idProduk (String, absent/null = new product), jenisProduk ("satuan" | "paket").
 * Result: true / false (saved or not); null when left without saving.
 */
public class TambahProdukActivity extends BaseActivity {
    private String idProduk;
    private String jenisProduk;

    private EditText namaProdukController;
    private EditText keteranganProdukController;
    private EditText hargaProdukController;
    private EditText diskonProdukController;

    private static final double BORDER_WIDTH = 1.0;
    private static final float SIZED_BOX_HEIGHT = 15f;
    private static final int BORDER_COLOR = CustomColors.green;

    private String kategoriProdukSelected = "";
    private String jenisProdukSelected = "";
    private String imageProdukData; // picked file path
    private JsonObject detailProdukState; // null = false
    private boolean isButtonSimpanBusy = false;
    private JsonArray kategoriProdukState = new JsonArray();

    private LinearLayout root;
    private LinearLayout buttonHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idProduk = Json.strOrNull(args(), "idProduk");
        jenisProduk = Json.str(args(), "jenisProduk");

        namaProdukController = new EditText(this);
        keteranganProdukController = new EditText(this);
        hargaProdukController = new EditText(this);
        diskonProdukController = new EditText(this);
        Runnable r = () -> refreshButton();
        PenjualUi.watch(namaProdukController, r);
        PenjualUi.watch(hargaProdukController, r);
        PenjualUi.watch(diskonProdukController, r);

        render();
        initialization();
    }

    private void initialization() {
        async(() -> KategoriProdukModel.getKategoriProduk(), kategoriProduk -> {
            kategoriProdukState = kategoriProduk == null ? new JsonArray() : kategoriProduk;
            render();
            if (idProduk != null) {
                initEditProduk();
            } else {
                hargaProdukController.setText("0");
                diskonProdukController.setText("0");
            }
        });
    }

    private void initEditProduk() {
        final String id = idProduk;
        if (jenisProduk.toLowerCase(Locale.ROOT).equals("satuan")) {
            async(() -> ProdukModel.getDetailProduk(id), detail -> {
                if (detail != null && Json.isObject(detail)) {
                    JsonObject d = Json.obj(detail);
                    namaProdukController.setText(Json.str(d, "nama"));
                    keteranganProdukController.setText(Json.str(d, "keterangan"));

                    jenisProdukSelected = "satuan";
                    kategoriProdukSelected = Json.str(d, "idKategoriProduk");
                    detailProdukState = d;
                    render();
                }
            });
        } else {
            async(() -> PaketModel.detailPaket(id), detail -> {
                if (detail != null && Json.isObject(detail)) {
                    JsonObject d = Json.obj(detail);
                    namaProdukController.setText(Json.str(d, "nama"));
                    keteranganProdukController.setText(Json.str(d, "keterangan"));
                    hargaProdukController.setText(Json.str(d, "harga"));
                    diskonProdukController.setText(Json.str(d, "diskon"));

                    jenisProdukSelected = "paket";
                    detailProdukState = d;
                    render();
                }
            });
        }
    }

    private static void detach(View v) {
        if (v.getParent() instanceof ViewGroup) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
    }

    private boolean isFormCompleted() {
        String namaProduk = namaProdukController.getText().toString();
        String hargaProduk = hargaProdukController.getText().toString();
        String diskonProduk = diskonProdukController.getText().toString();
        boolean isJenisProdukNotEmpty = !jenisProdukSelected.equals("");

        boolean completed = false;
        if (jenisProdukSelected.equals("satuan")) {
            completed = namaProduk.length() >= 1 && !kategoriProdukSelected.equals("0") && isJenisProdukNotEmpty;
        }
        if (jenisProdukSelected.equals("paket")) {
            completed = namaProduk.length() >= 1 && hargaProduk.length() >= 1 && diskonProduk.length() >= 1
                    && isJenisProdukNotEmpty;
        }
        return completed;
    }

    private View uploadFotoWidget() {
        float hb = Ui.hb(this);
        String jenisProdukLC = jenisProduk.toLowerCase(Locale.ROOT);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        boolean custom = false;
        if (idProduk == null) {
            if (imageProdukData != null) {
                image.setImageURI(Uri.fromFile(new java.io.File(imageProdukData)));
                custom = true;
            }
        } else if (detailProdukState != null) {
            String fotoProdukLC = Json.str(detailProdukState, "foto").toLowerCase(Locale.ROOT);
            boolean satuan = jenisProdukLC.equals("satuan");
            if (satuan || jenisProdukLC.equals("paket")) {
                String defaultLC = (satuan ? DefaultImage.defaultProduk : DefaultImage.defaultPaket)
                        .toLowerCase(Locale.ROOT);
                boolean isDefaultImage = defaultLC.equals(fotoProdukLC);
                if (imageProdukData == null) {
                    String path = satuan ? ServerPath.uploadGambarProduk : ServerPath.uploadGambarPaket;
                    String file = satuan ? fotoProdukLC : Json.str(detailProdukState, "foto");
                    Ui.loadNetwork(image, ServerPath.baseURL + "/" + path + "/" + file);
                } else {
                    image.setImageURI(Uri.fromFile(new java.io.File(imageProdukData)));
                }
                image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
                custom = true;
            }
        }
        FrameLayout holder = new FrameLayout(this);
        holder.setLayoutParams(new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, hb * 50)));
        if (custom) {
            Ui.clipRounded(image, 10f);
            holder.addView(image, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        } else {
            ImageView icon = Ui.asset(this, "upload-icon.png", 10);
            icon.setAlpha(0.35f);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Ui.padAll(icon, hb * 10);
            holder.addView(icon, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
        Ui.onTap(holder, () -> ImagePickerModel.showImagePicker(this, source -> {
            if (source != null) {
                ImagePickerModel.takePicture(this, source, file -> {
                    if (file != null) {
                        imageProdukData = file.getPath();
                        render();
                    }
                });
            }
        }));
        return holder;
    }

    private CustomForm.TextFieldView field(String hint, EditText c, View leading) {
        detach(c);
        CustomForm.TextFieldOpts o = new CustomForm.TextFieldOpts().borderColor(BORDER_COLOR).borderWidth(BORDER_WIDTH);
        if (leading != null) {
            o.leading(leading).leadingAndHintTextSpacing(15.0);
        }
        return CustomForm.textField(this, hint, c, o);
    }

    private void render() {
        float hb = Ui.hb(this);
        root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(DetailUi.appBar(this, idProduk == null ? "Tambah Produk" : "Update Produk"));

        LinearLayout col = Ui.column(this);
        Ui.padXY(col, 15, 20);
        col.addView(PenjualUi.text(this, idProduk == null ? "Tambah Produk" : "Update Produk", 4f, 0xDD000000, true));
        col.addView(Ui.spaceH(this, 5));
        col.addView(PenjualUi.text(this, idProduk == null
                ? "Anda dapat menambahkan produk dengan mengisi data produk anda di bawah ini"
                : "Silahkan perbaharui data produk dengan mengisi form di bawah ini", 3f, 0xDD000000, false));
        col.addView(Ui.spaceH(this, SIZED_BOX_HEIGHT));
        col.addView(uploadFotoWidget());
        col.addView(Ui.spaceH(this, SIZED_BOX_HEIGHT));

        col.addView(CustomForm.label(this, "Nama Produk"));
        col.addView(field("Contoh : Daun Bawang", namaProdukController, null));
        col.addView(Ui.spaceH(this, SIZED_BOX_HEIGHT));

        col.addView(CustomForm.label(this, "Jenis Produk"));
        List<CustomForm.SelectBoxItem> jenisItems = new ArrayList<>();
        jenisItems.add(new CustomForm.SelectBoxItem("satuan", "Produk Satuan"));
        jenisItems.add(new CustomForm.SelectBoxItem("paket", "Produk Paketan"));
        col.addView(CustomForm.selectBox(this, jenisItems, jenisProdukSelected, selected -> {
            jenisProdukSelected = String.valueOf(selected);
            render();
        }, "Pilih Jenis Produk", "", new CustomForm.SelectBoxOpts().borderColor(BORDER_COLOR)
                .borderWidth(BORDER_WIDTH)));

        boolean isJenisProdukNotEmpty = !jenisProdukSelected.equals("");
        if (isJenisProdukNotEmpty && jenisProdukSelected.equals("satuan")) {
            LinearLayout k = Ui.column(this);
            k.addView(CustomForm.label(this, "Kategori Produk"));
            List<CustomForm.SelectBoxItem> kategoriItems = new ArrayList<>();
            for (JsonElement e : kategoriProdukState) {
                JsonObject kategori = Json.obj(e);
                kategoriItems.add(new CustomForm.SelectBoxItem(Json.str(kategori, "id"), Json.str(kategori, "nama")));
            }
            k.addView(CustomForm.selectBox(this, kategoriItems, kategoriProdukSelected, selected -> {
                kategoriProdukSelected = String.valueOf(selected);
                render();
            }, "Pilih Kategori Produk", "", new CustomForm.SelectBoxOpts().borderColor(BORDER_COLOR)
                    .borderWidth(BORDER_WIDTH)));
            col.addView(k, PenjualUi.lp(this, Ui.MATCH, Ui.WRAP, 0, 15, 0, 0));
        }
        col.addView(Ui.spaceH(this, 5));

        if (isJenisProdukNotEmpty) {
            if (jenisProdukSelected.equals("paket")) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout h = Ui.column(this);
                h.addView(CustomForm.label(this, "Harga"));
                h.addView(field("Rp.", hargaProdukController, PenjualUi.plain(this, "Rp.")));
                row.addView(h, Ui.weightW(1f));
                row.addView(Ui.spaceW(this, 10));
                LinearLayout d = Ui.column(this);
                d.addView(CustomForm.label(this, "Diskon"));
                d.addView(field("Rp.", diskonProdukController, PenjualUi.plain(this, "Rp.")));
                row.addView(d, Ui.weightW(1f));
                col.addView(row, PenjualUi.lp(this, Ui.MATCH, Ui.WRAP, 0, 10, 0, 0));
            } else {
                col.addView(PenjualUi.text(this,
                        "Anda akan dapat menentukan harga - harga produk setelah anda menyimpan produk ini", 3f,
                        CustomColors.textMuted, false));
            }
        }
        col.addView(Ui.spaceH(this, SIZED_BOX_HEIGHT));

        col.addView(CustomForm.label(this, "Keterangan (Opsional)"));
        col.addView(field("Contoh : Produk bagus, diambil langsung dari kebun dijual dengan harga murah",
                keteranganProdukController, null));
        col.addView(Ui.spaceH(this, 20));

        buttonHolder = Ui.column(this);
        col.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        refreshButton();

        ScrollView scroll = new ScrollView(this);
        scroll.addView(col, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);
    }

    private void refreshButton() {
        if (buttonHolder == null) {
            return;
        }
        boolean completed = isFormCompleted();
        boolean showing = buttonHolder.getChildCount() > 0;
        if (completed == showing) {
            return;
        }
        buttonHolder.removeAllViews();
        if (completed) {
            final CustomButton.Btn[] btn = new CustomButton.Btn[1];
            btn[0] = CustomButton.button(this, "Simpan Data Produk", () -> simpan(btn[0]),
                    new CustomButton.Opts().color(CustomButton.themeColor).block().busy(isButtonSimpanBusy));
            buttonHolder.addView(btn[0], new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    private void simpan(CustomButton.Btn btn) {
        String jenisProdukSelectedLC = jenisProdukSelected.toLowerCase(Locale.ROOT);

        JsonObject dataProdukMap = new JsonObject();
        dataProdukMap.addProperty("nama", namaProdukController.getText().toString());
        dataProdukMap.addProperty("jenis", jenisProdukSelectedLC);
        dataProdukMap.addProperty("keterangan", keteranganProdukController.getText().toString());
        if (imageProdukData != null) {
            dataProdukMap.addProperty("imageProdukData", imageProdukData);
        }

        if (jenisProdukSelectedLC.equals("satuan")) {
            dataProdukMap.addProperty("kategori", kategoriProdukSelected);
        } else {
            dataProdukMap.addProperty("harga", hargaProdukController.getText().toString());
            dataProdukMap.addProperty("diskon", diskonProdukController.getText().toString());
        }

        isButtonSimpanBusy = true;
        btn.setBusy(true);
        TambahProdukModel.saveProduk(this, idProduk == null ? "" : idProduk, dataProdukMap, statusSaveProduk -> {
            isButtonSimpanBusy = false;
            btn.setBusy(false);
            finishWithResult(statusSaveProduk != null && statusSaveProduk);
        });
    }
}
