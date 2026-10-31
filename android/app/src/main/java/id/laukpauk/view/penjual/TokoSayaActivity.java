package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.TokoSayaModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.LoginActivity;
import id.laukpauk.view.pembeli.components.profilsaya.RMP;
import id.laukpauk.view.penjual.components.tokosaya.JamDelivery;
import id.laukpauk.view.penjual.components.tokosaya.PenjualanDanTransferDana;
import id.laukpauk.view.penjual.components.tokosaya.ProdukPenjual;
import id.laukpauk.view.penjual.components.tokosaya.TagihanToko;
import id.laukpauk.view.penjual.components.tokosaya.TransaksiPenjual;

/** Port of lib/views/penjual/TokoSaya.dart (seller dashboard, no arguments). */
public class TokoSayaActivity extends BaseActivity {
    private FrameLayout content;
    private boolean pageShown = false;

    /** Dart getDetailToko(): the `detailPenjual` entry (an object) or null (= false). BLOCKING. */
    private JsonObject getDetailToko() {
        String params = HTTPRequest.getQueryString(null, true);

        int idPenjualInt = LocalStorage.getIdPenjualFromActiveUser();
        String idPenjual = String.valueOf(idPenjualInt);

        Http.Response httpResponse = Http.getApi("/Penjual/getDetailPenjual/" + idPenjual + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            JsonElement detailPenjual = dataObject.get("detailPenjual");
            if (Json.isObject(detailPenjual)) {
                return Json.obj(detailPenjual);
            }
        }
        return null;
    }

    /** Result of the background work: LOGIN_REQUIRED or the (possibly null) detail. */
    private static final class Loaded {
        boolean isLogin;
        JsonObject dataPenjual;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(PesananScreenUi.scaffoldBackground);
        content = new FrameLayout(this);
        root.addView(content, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        content.addView(Loading.view(this));
        load();
    }

    private void load() {
        async(() -> {
            Loaded loaded = new Loaded();
            loaded.isLogin = AuthModel.isLogin();
            if (loaded.isLogin) {
                loaded.dataPenjual = getDetailToko();
            }
            return loaded;
        }, loaded -> {
            if (!loaded.isLogin) {
                Nav.pushReplacement(this, LoginActivity.class, null);
                return;
            }
            render(loaded.dataPenjual);
        });
    }

    private void reload() {
        load();
    }

    private void render(JsonObject dataPenjual) {
        content.removeAllViews();
        pageShown = true;

        if (dataPenjual == null) {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER);
            col.addView(Ui.asset(this, "empty.png", Ui.hb(this) * 25));
            col.addView(Ui.spaceH(this, 10));
            col.addView(PenjualUi.text(this, "Data Akun Anda tidak Ditemukan", 4f, CustomColors.textDanger, false));
            content.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
            return;
        }

        String statusApprove = Json.str(dataPenjual, "status");
        if (!statusApprove.toLowerCase().equals("approved")) {
            content.addView(new RMP(this, dataPenjual), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        content.addView(buildPage(dataPenjual), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private View buildPage(final JsonObject dataPenjual) {
        final String idPenjual = Json.str(dataPenjual, "id");
        String namaPasar = Json.str(dataPenjual, "pasarUmum").equals("0") ? "Lokasi Tersendiri"
                : Json.str(dataPenjual, "namaPasar");
        String alamatToko = Json.str(dataPenjual, "alamatToko");
        float hb = Ui.hb(this);

        LinearLayout column = Ui.column(this);
        column.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout list = Ui.column(this);

        // Padding(top 5, right 5, left 5) > Card(elevation 5, radius 10) > Padding(top 10, bottom 20, left 15, right 15)
        LinearLayout cardWrap = Ui.column(this);
        Ui.pad(cardWrap, 5, 5, 5, 0);
        LinearLayout card = PenjualUi.card(this);
        Ui.pad(card, 15, 10, 15, 20);
        cardWrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(cardWrap, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetworkCircle(avatar, ServerPath.baseURL + "/" + ServerPath.uploadGambarBannerPenjual + "/compress/"
                + Json.str(dataPenjual, "banner"));
        row.addView(avatar, new LinearLayout.LayoutParams(Ui.dp(this, hb * 17.5f), Ui.dp(this, hb * 17.5f)));
        row.addView(Ui.spaceW(this, 15));

        LinearLayout info = Ui.column(this);
        row.addView(info, Ui.weightW(1f));

        TextView nama = PenjualUi.text(this, Json.str(dataPenjual, "namaToko"), 4.25f, CustomColors.green, true);
        Ui.onTap(nama, () -> Nav.push(this, PenjualActivity.class, Json.of("id", Json.str(dataPenjual, "id"))));
        info.addView(nama);
        info.addView(Ui.spaceH(this, 3));

        TextView alamat = PenjualUi.text(this, (alamatToko.length() >= 1) ? alamatToko : "Alamat Toko Belum Ditentukan",
                3f, CustomColors.textMuted, false);
        if (alamatToko.length() < 1) {
            alamat.setTypeface(alamat.getTypeface(), android.graphics.Typeface.ITALIC);
        }
        info.addView(alamat);
        info.addView(Ui.spaceH(this, 5));

        LinearLayout lokasi = Ui.row(this);
        if (!Json.isNull(Json.get(dataPenjual, "namaPasar"))) {
            lokasi.addView(PenjualUi.text(this, "Berjualan di " + namaPasar, 3f, CustomColors.textMuted, false),
                    Ui.weightW(1f));
        }
        lokasi.addView(Ui.spaceW(this, 5));
        ImageView pin = Ui.icon(this, R.drawable.ic_pin_drop, hb * 4.5f, CustomColors.textPrimary);
        Ui.onTap(pin, () -> TokoSayaModel.showModalUbahLokasiBerjualan(this, dataPenjual,
                statusUpdateLokasiBerjualan -> {
                    if (statusUpdateLokasiBerjualan != null) {
                        String message = statusUpdateLokasiBerjualan ? "Berhasil mengubah lokasi berjualan."
                                : "Gagal mengubah lokasi berjualan.";
                        PenjualUi.snack(this, message, 1, 3.25f);
                        if (statusUpdateLokasiBerjualan) {
                            reload();
                        }
                    }
                }));
        lokasi.addView(pin);
        info.addView(lokasi, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        info.addView(Ui.spaceH(this, 10));

        TextView lihat = PenjualUi.text(this, "Lihat Toko Saya", 3f, CustomColors.textPrimary, false);
        Ui.onTap(lihat, () -> Nav.push(this, PenjualActivity.class, Json.of("id", Json.str(dataPenjual, "id"))));
        info.addView(lihat);

        TextView updateHarga = PenjualUi.text(this, "Update Harga", 3.25f, CustomColors.textPrimary, false);
        Ui.onTap(updateHarga, () -> Nav.push(this, UpdateHargaActivity.class));
        row.addView(updateHarga);

        card.addView(Ui.spaceH(this, 20));
        card.addView(new PenjualanDanTransferDana(this));

        addSection(list, new TransaksiPenjual(this));
        addSection(list, new TagihanToko(this));
        addSection(list, new ProdukPenjual(this, statusSaveProduk -> {
            if (statusSaveProduk != null) {
                String message = statusSaveProduk ? "Produk baru berhasil ditambahkan ke toko kamu."
                        : "Produk baru gagal disimpan !";
                PenjualUi.snack(this, message, 1, 3.25f);

                if (statusSaveProduk) {
                    reload();
                }
            }
        }));
        addSection(list, new JamDelivery(this));
        addSection(list, new UbahNamaToko(this, statusUbahNamaToko -> {
            if (statusUbahNamaToko != null) {
                String message = statusUbahNamaToko ? "Nama Toko berhasil diubah" : "Nama Toko gagal diubah !";
                PenjualUi.snack(this, message, 1, 3.25f);

                if (statusUbahNamaToko) {
                    reload();
                }
            }
        }));

        SwipeRefreshLayout refresh = DetailUi.refreshable(this, list, () -> reload(), false);
        refresh.setLayoutParams(Ui.weightH(1f));
        column.addView(refresh);
        return column;
    }

    /** Padding(horizontal: 5, child: view). */
    private void addSection(LinearLayout list, View view) {
        LinearLayout holder = Ui.column(this);
        Ui.pad(holder, 5, 0, 5, 0);
        holder.addView(view, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(holder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
