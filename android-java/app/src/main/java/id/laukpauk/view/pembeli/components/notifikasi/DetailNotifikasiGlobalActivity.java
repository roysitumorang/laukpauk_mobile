package id.laukpauk.view.pembeli.components.notifikasi;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.NotifikasiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/Notifikasi/DetailNotifikasiGlobal.dart. Args: idNotifikasi. */
public class DetailNotifikasiGlobalActivity extends BaseActivity {
    private JsonElement detailNotifikasiGlobalState; // null = still loading, JsonPrimitive false = not found

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        render();

        String idNotifikasi = Json.str(args(), "idNotifikasi");
        async(() -> NotifikasiModel.getDetailNotifikasiGlobal(idNotifikasi), detail -> {
            detailNotifikasiGlobalState = detail;
            render();
        });
    }

    private void render() {
        float hb = Ui.hb(this);
        JsonElement dngs = detailNotifikasiGlobalState;
        boolean isDNGSNull = (dngs == null);
        boolean isFalse = !isDNGSNull && Json.isFalse(dngs);
        JsonObject data = (!isDNGSNull && !isFalse) ? Json.obj(dngs) : null;

        boolean isFotoOkay = false;
        if (data != null) {
            String foto = Json.strOrNull(data, "foto");
            if (foto != null && foto.length() >= 1) {
                isFotoOkay = true;
            }
        }

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);

        if (!isDNGSNull) {
            View bar = DetailUi.appBar(this, data != null ? Json.str(data, "judul") : "");
            bar.setElevation(0f);
            root.addView(bar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        View body;
        if (!isDNGSNull) {
            if (data != null) {
                LinearLayout column = Ui.column(this);

                // Container(width: 100hb, height: 60hb, image: DecorationImage(scale: 2.0, fit: cover | none))
                FrameLayout imageBox = new FrameLayout(this);
                imageBox.setClipChildren(true);
                if (isFotoOkay) {
                    ImageView iv = Ui.network(this, ServerPath.baseURL + "/"
                            + ServerPath.uploadGambarPushNotifikasi + "/" + Json.str(data, "foto"),
                            Ui.screenWidthDp(this), hb * 60f);
                    imageBox.addView(iv, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                } else {
                    Bitmap bmp = Ui.assetBitmap(this, "laukpauk-icon.png");
                    ImageView iv = new ImageView(this);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    iv.setImageBitmap(bmp);
                    // scale 2.0: logical size = pixel size / 2
                    int w = bmp != null ? Ui.dp(this, bmp.getWidth() / 2f) : Ui.dp(this, 100);
                    int h = bmp != null ? Ui.dp(this, bmp.getHeight() / 2f) : Ui.dp(this, 100);
                    imageBox.addView(iv, new FrameLayout.LayoutParams(w, h, Gravity.CENTER));
                }
                LinearLayout.LayoutParams imageLp = new LinearLayout.LayoutParams(Ui.MATCH,
                        Ui.dp(this, hb * 60f));
                if (!isFotoOkay) {
                    imageLp.topMargin = Ui.dp(this, 5);
                    imageLp.bottomMargin = Ui.dp(this, 5);
                }
                column.addView(imageBox, imageLp);

                column.addView(Ui.spaceH(this, 15));
                View judul = Ui.text(this, Json.str(data, "judul"), hb * 4.0f, CustomColors.green);
                Ui.padXY(judul, 15, 0);
                column.addView(judul, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                column.addView(Ui.spaceH(this, 15));
                View pesan = Ui.text(this, Json.str(data, "pesan"), hb * 3.25f, CustomColors.textMuted);
                Ui.padXY(pesan, 15, 0);
                column.addView(pesan, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                column.addView(Ui.spaceH(this, 15));
                View pada = Ui.text(this, "Pada " + Json.str(data, "createdAt"), hb * 3.0f,
                        CustomColors.textMuted);
                Ui.padXY(pada, 15, 0);
                column.addView(pada, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

                ScrollView scroll = new ScrollView(this);
                scroll.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                body = scroll;
            } else {
                FrameLayout center = new FrameLayout(this);
                center.addView(DetailUi.defaultText(this, "Notifikasi Tidak Terdaftar !"),
                        new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
                body = center;
            }
        } else {
            body = Loading.view(this);
        }
        root.addView(body, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
    }
}
