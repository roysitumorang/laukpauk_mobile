package id.laukpauk.view.pembeli;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.resep.ResepItem;
import id.laukpauk.view.pembeli.components.resep.ResepUi;

/** Port of lib/views/pembeli/DetailResep.dart. Args: id (String). */
public class DetailResepActivity extends BaseActivity {
    private String idResepArg;
    private FrameLayout contentHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idResepArg = Json.str(args(), "id");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        contentHolder = new FrameLayout(this);
        root.addView(contentHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        load();
    }

    private void load() {
        contentHolder.removeAllViews();
        contentHolder.addView(Loading.view(this));
        async(() -> getDetailResep(), dataResep -> render(dataResep));
    }

    /** Dart: getDetailResep(). BLOCKING; null on failure (Dart returned a FractionalOffset). */
    private JsonElement getDetailResep() {
        String idResep = idResepArg;
        String entitasResep = "id, nama, foto, keterangan, igLink, fbLink";

        String params = HTTPRequest.getQueryString("entitasResep=" + entitasResep);

        Http.Response httpResponse = Http.getApi("/Resep/getResep/" + idResep + "?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.get(httpResponse.jsonObject(), "dataResep");
        }
        return null;
    }

    private void openLink(String link) {
        if (!ResepUi.launch(this, link)) {
            ResepUi.snackbar(this, "URL Tidak Valid !", 1000, null, null);
        }
    }

    /** Dart: bagikan(context, namaResep) -> Share.share(text, subject:). */
    private void bagikan(String namaResep) {
        String nama = namaResep.toUpperCase(Locale.ROOT);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, "Mak, pengen buat " + nama
                + ", terus gak pakek ribet belanja, sekali klik, semua bahan resep langsung masuk keranjang belanja, ya cuma di Laukpauk lah. Lagi ada promo tuh mak. Install-kan terus apps nya di bit.ly/laukpauk");
        send.putExtra(Intent.EXTRA_SUBJECT, "Belanja Resep " + nama);
        startActivity(Intent.createChooser(send, null));
    }

    private void render(JsonElement dataResepEl) {
        contentHolder.removeAllViews();
        float hb = Ui.hb(this);

        if (dataResepEl == null || !dataResepEl.isJsonObject()) {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER);
            col.addView(Ui.asset(this, "empty.png", hb * 30f));
            col.addView(Ui.spaceH(this, 10));
            col.addView(Ui.text(this, "Detail Resep tidak Ditemukan !", hb * 3.5f,
                    CustomColors.textMuted, true));
            contentHolder.addView(DetailUi.refreshable(this, col, this::load, true),
                    new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }
        final JsonObject dataResep = dataResepEl.getAsJsonObject();
        final String fbLink = Json.str(dataResep, "fbLink");
        final String igLink = Json.str(dataResep, "igLink");

        LinearLayout list = Ui.column(this);

        FrameLayout header = new FrameLayout(this);
        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetwork(foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarResep + "/"
                + Json.str(dataResep, "foto"));
        header.addView(foto, new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(this, Ui.vb(this) * 30)));
        header.addView(circleIcon(R.drawable.ic_send, 15, () -> openLink(fbLink)));
        header.addView(circleIcon(R.drawable.ic_instagram, 55, () -> openLink(igLink)));
        list.addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout body = Ui.column(this);
        Ui.padXY(body, 15, 0);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        Ui.pad(buttons, 0, 15, 0, 15);
        View caraMasak = CustomButton.button(this, "Cara Masak", () -> openLink(igLink),
                new CustomButton.Opts().color(CustomButton.themeColor).outline().pill());
        buttons.addView(caraMasak, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        buttons.addView(Ui.spaceW(this, 7.5f));
        View share = CustomButton.button(this, "Share Resep",
                () -> bagikan(Json.str(dataResep, "nama")),
                new CustomButton.Opts().pill().color(CustomButton.themeColor));
        buttons.addView(share, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        body.addView(buttons, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        body.addView(Ui.text(this, Json.str(dataResep, "nama"), hb * 4.5f, CustomColors.green, true));
        body.addView(Ui.spaceH(this, 5));
        LinearLayout ket = Ui.column(this);
        Ui.pad(ket, 0, 0, 10, 0);
        ket.addView(Ui.spaceH(this, 3));
        ket.addView(Ui.text(this, Json.str(dataResep, "keterangan"), hb * 3, CustomColors.textMuted));
        body.addView(ket, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        View line = new View(this);
        line.setBackgroundColor(CustomColors.green);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(Ui.MATCH, Math.max(1, Ui.dp(this, 0.5f)));
        lineLp.topMargin = Ui.dp(this, 15);
        lineLp.bottomMargin = Ui.dp(this, 15);
        body.addView(line, lineLp);

        LinearLayout itemHolder = Ui.column(this);
        Ui.pad(itemHolder, 0, 0, 0, 15);
        itemHolder.addView(new ResepItem(this, Json.str(dataResep, "id")),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        body.addView(itemHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        list.addView(body, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        contentHolder.addView(DetailUi.refreshable(this, list, this::load, false),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    /** Material(borderRadius 50, elevation 10) > Padding(5) > InkWell > Icon(size 20). */
    private View circleIcon(int drawable, int leftDp, final Runnable onTap) {
        FrameLayout box = new FrameLayout(this);
        box.setBackground(Ui.shape(this, CustomColors.nearlyWhite, 50));
        box.setElevation(Ui.dp(this, 10));
        box.addView(Ui.icon(this, drawable, 20, 0xDD000000),
                new FrameLayout.LayoutParams(Ui.dp(this, 20), Ui.dp(this, 20), Gravity.CENTER));
        Layouts.ripple(box, 0x33000000, 15f);
        box.setOnClickListener(v -> onTap.run());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(this, 30), Ui.dp(this, 30),
                Gravity.BOTTOM | Gravity.LEFT);
        lp.leftMargin = Ui.dp(this, leftDp);
        lp.bottomMargin = Ui.dp(this, 15);
        box.setLayoutParams(lp);
        return box;
    }
}
