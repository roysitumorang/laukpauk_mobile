package id.laukpauk.view.pembeli;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** lib/views/pembeli/HubungiKami.dart. No arguments. */
public class HubungiKamiActivity extends BaseActivity {
    private FrameLayout body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(DetailUi.appBar(this, "Hubungi Kami"));

        body = new FrameLayout(this);
        Ui.pad(body, 5, 5, 5, 5);
        body.addView(Loading.spinner(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        root.addView(body, Ui.weightH(1f));
        setRoot(root);

        async(() -> getContactUsWhatsappConfiguration(), (JsonElement config) -> {
            body.removeAllViews();
            if (config != null && !Json.isFalse(config)) {
                body.addView(contactView(), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            } else {
                body.addView(failedView(), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });
    }

    /** Future&lt;dynamic&gt; getContactUsWhatsappConfiguration(): the value, or JSON false. */
    private JsonElement getContactUsWhatsappConfiguration() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Pengaturan/getContactUsWhatsappConfiguration?" + params);

        if (httpResponse.statusCode == 200) {
            JsonElement value = Json.get(httpResponse.jsonObject(), "contactUsWhatsappConfiguration");
            // snapshot.hasData is false for a null value: keep the loading state off but show the failure view
            return value == null || Json.isNull(value) ? new com.google.gson.JsonPrimitive(false) : value;
        }
        return new com.google.gson.JsonPrimitive(false);
    }

    private LinearLayout contactView() {
        float hb = Ui.hb(this);
        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER);

        ImageView img = Ui.asset(this, "hubungi-kami.png", hb * 30f);
        img.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, hb * 30f), Ui.WRAP));
        column.addView(img);
        column.addView(Ui.spaceH(this, 5));
        column.addView(Ui.text(this, "Ada yang bisa kami bantu Mak ?",
                new Txt.Style(hb * 3.5f).color(CustomColors.green).bold()));
        column.addView(Ui.spaceH(this, 25));
        column.addView(CustomButton.button(this, "Chat Kami dari Whatsapp", () -> {
            Uri uri = Uri.parse("https://wa.me/62618367205");
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (RuntimeException e) {
                CustomAlert.urlCantBeLaunched(this, uri.toString());
            }
        }, new CustomButton.Opts().pill().outline().color(CustomButton.themeColor)));
        return column;
    }

    private LinearLayout failedView() {
        float hb = Ui.hb(this);
        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView img = Ui.asset(this, "empty.png", hb * 30f);
        img.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, hb * 30f), Ui.WRAP));
        column.addView(img);
        column.addView(Ui.spaceH(this, 10));
        TextView t = Ui.text(this,
                "Mohon maaf, server kami gagal mengambil data nomor telepon yang dapat anda gunakan untuk menghubungi Laukpauk. Silahkan cari informasi di laman web kami.",
                hb * 3.5f, CustomColors.textDanger);
        column.addView(t);
        column.addView(Ui.spaceH(this, 10));
        return column;
    }
}
