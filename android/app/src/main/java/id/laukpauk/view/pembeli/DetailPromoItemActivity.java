package id.laukpauk.view.pembeli;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.ui.Yuhu;
import id.laukpauk.view.pembeli.components.detailpromo.NamaPenjual;
import id.laukpauk.view.pembeli.components.detailpromo.NamaProduk;
import id.laukpauk.view.pembeli.components.detailpromo.NamaPromo;

/** Port of lib/views/pembeli/DetailPromoItem.dart. Args: idPromoItem (String). */
public class DetailPromoItemActivity extends BaseActivity {

    private static final class Data {
        boolean found;
        boolean isLogin;
        String currentUserID;
        JsonElement promoItem;
    }

    private String idPromoItem;
    private FrameLayout bodyHost;
    private FrameLayout contentHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idPromoItem = Json.str(args(), "idPromoItem");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        bodyHost = new FrameLayout(this);
        root.addView(bodyHost, Ui.weightH(1f));
        contentHolder = new FrameLayout(this);
        bodyHost.addView(contentHolder, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        load();
    }

    /** Dart: the FutureBuilder's getDetailPromoItem() (re-run on every setState). */
    private void load() {
        contentHolder.removeAllViews();
        contentHolder.addView(Loading.view(this));
        async(() -> getDetailPromoItem(), data -> {
            if (data == null) {
                data = new Data();
            }
            render(data);
        });
    }

    private Data getDetailPromoItem() {
        Data data = new Data();
        boolean isLogin = AuthModel.isLogin();
        int currentUserIDInt = LocalStorage.getIdPenjualFromActiveUser();
        String currentUserID = String.valueOf(currentUserIDInt);

        String select = "namaPromo, statusPromo, catatan, idPromo, diskon";
        String params = HTTPRequest.getQueryString("select=" + select);

        Http.Response httpResponse = Http.getApi("/Promo/getPromoItem/" + idPromoItem + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            data.found = true;
            data.isLogin = isLogin;
            data.currentUserID = currentUserID;
            data.promoItem = Json.get(dataObject, "promoItem");
        }
        return data;
    }

    private void render(Data data) {
        contentHolder.removeAllViews();
        if (!data.found || data.promoItem == null || !data.promoItem.isJsonObject()) {
            contentHolder.addView(DetailUi.refreshable(this, notFound(), this::load, true),
                    new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }
        float hb = Ui.hb(this);
        JsonObject detailPromoItem = data.promoItem.getAsJsonObject();

        JsonObject detailPromo = Json.object(detailPromoItem, "detailPromo");

        String idPromo = Json.str(detailPromoItem, "idPromo");
        String statusPromo = Json.str(detailPromoItem, "statusPromo");
        String currentUserIDString = data.currentUserID;

        String idPenjual = "", idProduk = "", keterangan = "", fotoProduk = "", namaToko = "",
                namaProduk = "", namaKategoriProduk = "", catatanPromoProduk = "";

        catatanPromoProduk = Json.str(detailPromoItem, "catatan");

        JsonElement detailProdukEl = Json.get(detailPromoItem, "detailProduk");
        if (detailProdukEl != null && detailProdukEl.isJsonObject()) {
            JsonObject detailProduk = detailProdukEl.getAsJsonObject();
            idProduk = Json.str(detailProduk, "id");
            namaProduk = Json.str(detailProduk, "nama");
            fotoProduk = Json.str(detailProduk, "foto");
            namaKategoriProduk = Json.str(detailProduk, "namaKategoriProduk");

            namaToko = Json.str(detailProduk, "namaToko");
            idPenjual = Json.str(detailProduk, "idPenjual");
            keterangan = Json.str(detailProduk, "keterangan");
        }

        JsonElement detailSatuanEl = Json.get(detailPromoItem, "detailSatuan");
        JsonObject detailSatuan = new JsonObject();
        String idSatuan = "0";
        if (detailSatuanEl != null && detailSatuanEl.isJsonObject()) {
            detailSatuan = detailSatuanEl.getAsJsonObject();
            idSatuan = Json.str(detailSatuan, "id");
        }

        JsonObject detailPenjual = Json.of("namaToko", namaToko, "idToko", idPenjual);

        JsonObject dataProduk = Json.of("namaProduk", namaProduk, "namaKategoriProduk",
                namaKategoriProduk, "idProduk", idProduk, "idPenjual", idPenjual, "idSatuan", idSatuan,
                "idPromo", idPromo, "statusPromo", statusPromo);

        LinearLayout list = Ui.column(this);

        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetwork(foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/" + fotoProduk);
        list.addView(foto, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, hb * 60)));

        LinearLayout padded = Ui.column(this);
        Ui.pad(padded, 17.5f, 20, 17.5f, 20);

        padded.addView(new NamaProduk(this, dataProduk, currentUserIDString, statusEditProduk -> {
            if (statusEditProduk != null) {
                String text = statusEditProduk ? "Berhasil mengubah data produk"
                        : "Gagal mengubah data produk !";
                DetailUi.snackbar(bodyHost, text, 14f, 2000, null, 0, null);
                if (statusEditProduk) {
                    load();
                }
            }
        }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(Ui.spaceH(this, 2.5f));
        padded.addView(new NamaPenjual(this, detailPenjual), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(Ui.spaceH(this, 15));
        padded.addView(new NamaPromo(this, detailPromo, detailSatuan, detailPromoItem),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(new Yuhu(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(Ui.spaceH(this, 15));

        padded.addView(heading("Deskripsi Produk", hb));
        padded.addView(Ui.spaceH(this, 5));
        padded.addView(muted(keterangan.length() >= 1 ? keterangan : "Tidak ada deskripsi produk",
                keterangan.length() < 1, hb));
        padded.addView(Ui.spaceH(this, 10));
        padded.addView(heading("Catatan Promo Produk", hb));
        padded.addView(Ui.spaceH(this, 5));
        padded.addView(muted(catatanPromoProduk.length() >= 1 ? catatanPromoProduk
                : "Tidak ada deskripsi produk", catatanPromoProduk.length() < 1, hb));

        list.addView(padded, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        contentHolder.addView(DetailUi.refreshable(this, list, this::load, false),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private TextView heading(String s, float hb) {
        return Ui.text(this, s, hb * 4, CustomColors.textMuted, true);
    }

    private TextView muted(String s, boolean italic, float hb) {
        TextView tv = Ui.text(this, s, hb * 3, CustomColors.textMuted);
        if (italic) {
            tv.setTypeface(tv.getTypeface(), Typeface.ITALIC);
        }
        return tv;
    }

    private View notFound() {
        float hb = Ui.hb(this);
        LinearLayout col = Ui.column(this);
        col.setGravity(Gravity.CENTER);
        col.addView(Ui.asset(this, "empty.png", hb * 35f));
        col.addView(Ui.spaceH(this, 20));
        col.addView(Ui.text(this, "Detail Produk Tidak Ditemukan", hb * 3.25f, CustomColors.textMuted));
        return col;
    }
}
