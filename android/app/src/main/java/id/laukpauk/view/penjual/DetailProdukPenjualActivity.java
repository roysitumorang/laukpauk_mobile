package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ProdukPenjualModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.components.updateharga.SatuanProduk;

/**
 * Port of lib/views/penjual/DetailProdukPenjual.dart.
 * Args: id (String), jenisProduk (String: "satuan" | "paket").
 */
public class DetailProdukPenjualActivity extends BaseActivity {
    private String id;
    private String jenisProduk;

    private EditText deskripsiProdukController;
    private boolean isUpdateDeskripsiProduk = false;

    private FrameLayout content;
    private JsonObject detailProduk;
    private boolean loaded = false;

    private boolean isSatuan() {
        return jenisProduk.toLowerCase(Locale.ROOT).equals("satuan");
    }

    /** Dart getDetailProduk(): the detail object or null (= false). BLOCKING. */
    private JsonObject getDetailProduk() {
        if (isSatuan()) {
            String params = HTTPRequest.getQueryString("idProduk=" + id);

            Http.Response httpResponse = Http.getApi("/Produk/getHargaProduk?" + params);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                JsonElement detail = dataObject.get("detailProduk");
                return Json.isObject(detail) ? Json.obj(detail) : null;
            }
        } else {
            String params = HTTPRequest.getQueryString("idPaket=" + id);

            Http.Response httpResponse = Http.getApi("/Produk/detailPaket?" + params);
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                JsonElement detail = dataObject.get("detailPaket");
                return Json.isObject(detail) ? Json.obj(detail) : null;
            }
        }

        return null;
    }

    /** Dart getKategoriProduk(). BLOCKING. */
    private JsonArray getKategoriProduk() {
        Http.Response httpResponse = Http.getApi("/KategoriProduk/getKategoriProduk");

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "kategoriProduk");
        }
        return new JsonArray();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        id = Json.str(args(), "id");
        jenisProduk = Json.str(args(), "jenisProduk");
        deskripsiProdukController = new EditText(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content = new FrameLayout(this);
        root.addView(content, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        showSpinner();
        load();
    }

    private void showSpinner() {
        content.removeAllViews();
        content.addView(PenjualUi.spinner(this, 15), new FrameLayout.LayoutParams(Ui.dp(this, 15), Ui.dp(this, 15),
                Gravity.CENTER));
    }

    /** setState(() {}): the FutureBuilder gets a new future; the old page stays until the new data arrived. */
    private void load() {
        async(() -> getDetailProduk(), result -> {
            detailProduk = result;
            loaded = true;
            render();
        });
    }

    private void render() {
        content.removeAllViews();
        if (detailProduk == null) {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER);
            col.addView(Ui.asset(this, "empty.png", 200));
            col.addView(Ui.spaceH(this, 20));
            col.addView(Ui.text(this, "Detail Produk Tidak Ditemukan", 16f, CustomColors.textDanger, true));
            content.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
            return;
        }

        final JsonObject detail = detailProduk;
        float hb = Ui.hb(this);
        String uploadPath = isSatuan() ? ServerPath.uploadGambarProduk : ServerPath.uploadGambarPaket;
        boolean isSuperior = Json.str(detail, "superior").equals("1");
        boolean namaKategoriNull = Json.isNull(Json.get(detail, "namaKategoriProduk"));
        String namaKategoriProduk = !namaKategoriNull ? Json.str(detail, "namaKategoriProduk")
                : "Kategori Produk Tidak Diketahui";

        LinearLayout list = Ui.column(this);

        android.widget.ImageView foto = Ui.network(this, ServerPath.baseURL + "/" + uploadPath + "/"
                + Json.str(detail, "foto"), 100 * hb, 60 * hb);
        foto.setLayoutParams(new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 60 * hb)));
        list.addView(foto);

        LinearLayout cardWrap = Ui.column(this);
        Ui.padXY(cardWrap, 2.5f, 5);
        LinearLayout card = PenjualUi.card(this);
        Ui.padXY(card, 15, 15);
        cardWrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(cardWrap, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        // Row(start, spaceBetween)
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout namaCol = Ui.column(this);
        namaCol.addView(PenjualUi.text(this, Json.str(detail, "nama").toUpperCase(Locale.ROOT), 4.5f,
                CustomColors.green, true));
        if (isSatuan()) {
            namaCol.addView(Ui.spaceH(this, 5));
            TextView kategori = PenjualUi.text(this, namaKategoriProduk.toUpperCase(Locale.ROOT), 3.5f,
                    CustomColors.textMuted, false);
            if (namaKategoriNull) {
                kategori.setTypeface(kategori.getTypeface(), android.graphics.Typeface.ITALIC);
            }
            namaCol.addView(kategori);
        }
        top.addView(namaCol, Ui.weightW(1f));
        top.addView(Ui.spaceW(this, 10));

        LinearLayout superiorCol = Ui.column(this);
        superiorCol.setGravity(Gravity.END);
        final Switch toggle = new Switch(this);
        toggle.setChecked(isSuperior);
        toggle.setThumbTintList(PenjualUi.tint(CustomColors.green));
        toggle.setTrackTintList(PenjualUi.tint(CustomColors.lightGreen));
        toggle.setOnClickListener(v -> {
            // The Flutter Switch only changes with its `value`: undo the local flip until the server agreed.
            toggle.setChecked(!toggle.isChecked());
            async(() -> ProdukPenjualModel.toggleProdukPalingLaris(id, jenisProduk), statusToggleProdukPalingLaris -> {
                if (statusToggleProdukPalingLaris) {
                    load();
                }
            });
        });
        superiorCol.addView(toggle, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        superiorCol.addView(PenjualUi.text(this, "Produk Paling Laris", 3f, 0xDD000000, false));
        top.addView(superiorCol, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        card.addView(top, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (isSatuan()) {
            final FrameLayout kategoriHolder = new FrameLayout(this);
            kategoriHolder.addView(PenjualUi.paddedSpinner(this, 15, 15));
            card.addView(kategoriHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            async(() -> getKategoriProduk(), kategoriProduk -> renderKategori(kategoriHolder, detail, kategoriProduk));
        }

        card.addView(Ui.spaceH(this, isSatuan() ? 10 : 0));

        if (isSatuan()) {
            LinearLayout satuanBox = Ui.column(this);
            Ui.padXY(satuanBox, 0, 15);
            satuanBox.setBackground(Ui.shape(this, 0x00000000, 0f));
            satuanBox.addView(new SatuanProduk(this, Json.str(detail, "id"), true, false, true),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            card.addView(Ui.divider(this, CustomColors.textMuted, 0.5f));
            card.addView(satuanBox, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            card.addView(Ui.divider(this, CustomColors.textMuted, 0.5f));
        }

        card.addView(Ui.spaceH(this, 10));

        // Deskripsi Produk
        LinearLayout deskripsi = Ui.column(this);
        LinearLayout deskripsiHeader = Ui.row(this);
        deskripsiHeader.addView(PenjualUi.text(this, "Deskripsi Produk", 4f, 0xDD000000, true));
        deskripsiHeader.addView(Ui.spacer(this));
        TextView ubah = PenjualUi.text(this, !isUpdateDeskripsiProduk ? "Ubah" : "Batal", 3.25f,
                !isUpdateDeskripsiProduk ? CustomColors.textPrimary : CustomColors.textDanger, false);
        Ui.onTap(ubah, () -> {
            isUpdateDeskripsiProduk = !isUpdateDeskripsiProduk;
            render();
        });
        deskripsiHeader.addView(ubah);
        deskripsi.addView(deskripsiHeader, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        deskripsi.addView(Ui.spaceH(this, 5));

        if (!isUpdateDeskripsiProduk) {
            deskripsi.addView(PenjualUi.text(this, Json.str(detail, "keterangan"), 3.5f, CustomColors.textMuted,
                    false));
        } else {
            FrameLayout box = new FrameLayout(this);
            box.setBackground(Ui.shape(this, CustomColors.lightGrey, 10f, CustomColors.grey, 0.25f));
            Ui.pad(box, 15, 0, 15, 0);
            if (deskripsiProdukController.getParent() instanceof ViewGroup) {
                ((ViewGroup) deskripsiProdukController.getParent()).removeView(deskripsiProdukController);
            }
            PenjualUi.styleEdit(this, deskripsiProdukController, "Deskripsi Produk",
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, hb * 3.25f, 0xDD000000,
                    0x99000000);
            deskripsiProdukController.setMinLines(4);
            deskripsiProdukController.setMaxLines(Integer.MAX_VALUE);
            deskripsiProdukController.setGravity(Gravity.TOP | Gravity.START);
            int v = Ui.dp(this, Ui.vb(this) * 2);
            deskripsiProdukController.setPadding(0, v, 0, v);
            box.addView(deskripsiProdukController, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            deskripsi.addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            TextView simpan = PenjualUi.textButton(this, "Simpan Perubahan Deskripsi Produk", hb * 3f,
                    CustomColors.green, false, () -> {
                        final String text = deskripsiProdukController.getText().toString();
                        async(() -> ProdukPenjualModel.ubahDeskripsiProduk(id, jenisProduk, text),
                                statusUbahDeskripsiProduk -> {
                                    if (statusUbahDeskripsiProduk) {
                                        isUpdateDeskripsiProduk = false;
                                        load();
                                    }
                                });
                    });
            LinearLayout.LayoutParams simpanLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            simpanLp.gravity = Gravity.CENTER_HORIZONTAL;
            deskripsi.addView(simpan, simpanLp);
        }
        card.addView(deskripsi, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        SwipeRefreshLayout refresh = DetailUi.refreshable(this, list, () -> load(), false);
        content.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private void renderKategori(FrameLayout holder, final JsonObject detail, JsonArray kategoriProduk) {
        holder.removeAllViews();
        float hb = Ui.hb(this);

        if (kategoriProduk.size() >= 1) {
            String kategoriProdukSelected = !Json.isNull(Json.get(detail, "idKategoriProduk"))
                    ? Json.str(detail, "idKategoriProduk") : "0";
            float dropdownSp = hb * 3f;

            final List<String> values = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            values.add("0");
            labels.add("Pilih Kategori Produk");
            for (JsonElement e : kategoriProduk) {
                JsonObject kategori = Json.obj(e);
                values.add(Json.str(kategori, "id"));
                labels.add(Json.str(kategori, "nama"));
            }

            LinearLayout col = Ui.column(this);
            Ui.padXY(col, 0, 10);
            col.addView(PenjualUi.text(this, "Kategori Produk", 4f, 0xDD000000, true));
            col.addView(Ui.spaceH(this, 5));
            col.addView(PenjualUi.dropdown(this, values, labels, kategoriProdukSelected, dropdownSp, selected -> {
                final String idProduk = Json.str(detail, "id");
                async(() -> ProdukPenjualModel.ubahKategoriProduk(idProduk, selected), statusUbahKategoriProduk -> {
                    if (statusUbahKategoriProduk) {
                        load();
                    } else {
                        render();
                    }
                });
            }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            holder.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.padXY(col, 0, 20);
            col.addView(Ui.asset(this, "empty.png", hb * 27.5f));
            col.addView(Ui.spaceH(this, 5));
            TextView t = PenjualUi.text(this, "Tidak ada kategori produk yang bisa dipilih.", 3.5f, 0xDD000000,
                    false);
            t.setGravity(Gravity.CENTER);
            col.addView(t, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            holder.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER));
        }
    }
}
