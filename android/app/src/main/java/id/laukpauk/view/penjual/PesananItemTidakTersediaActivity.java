package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.editkeranjangitem.KeranjangItemInput;

/** Port of lib/views/penjual/PesananItemTidakTersedia.dart. Args: pesananItem (object). Result: bool. */
public class PesananItemTidakTersediaActivity extends BaseActivity {
    private JsonObject pesananItem;
    private String keterangan = "";
    private boolean sending = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        pesananItem = Json.object(args(), "pesananItem");
        float hb = Ui.hb(this);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(PesananScreenUi.scaffoldBackground);
        root.addView(PesananScreenUi.appBar(this, "Pesanan Tidak Tersedia"),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout content = Ui.column(this);
        Ui.pad(content, 15, 15, 15, 15); // Padding(5) + Padding(10)
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content, new ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));

        content.addView(Ui.text(this, "Beri Alasan", hb * 4, CustomColors.black, true));
        content.addView(Ui.spaceH(this, 2.5f));
        content.addView(Ui.text(this, "Silahkan berikan alasan mengapa produk ini tidak tersedia saat ini", 14f,
                CustomColors.black));
        content.addView(Ui.spaceH(this, 15));

        LinearLayout field = Ui.column(this);
        Ui.pad(field, 0, 0, 0, 7.5f);
        content.addView(field, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        final LinearLayout buttonHolder = Ui.column(this);
        field.addView(new KeranjangItemInput(this, "Keterangan", "", InputType.TYPE_CLASS_TEXT, (String data) -> {
            keterangan = data;
            // the Klaim button is shown while the text is not empty
            buttonHolder.setVisibility((keterangan.length() >= 1) ? android.view.View.VISIBLE
                    : android.view.View.GONE);
        }, hb * 25, 5));

        buttonHolder.setVisibility(android.view.View.GONE);
        Ui.pad(buttonHolder, 0, 10, 0, 0);
        buttonHolder.addView(CustomButton.button(this, "Klaim Pesanan Tidak Tersedia", () -> {
            if (sending) {
                return;
            }
            sending = true;
            async(() -> Boolean.valueOf(klaim()), (Boolean klaimProdukTidakTersedia) -> {
                sending = false;
                finishWithResult(klaimProdukTidakTersedia != null && klaimProdukTidakTersedia);
            });
        }, new CustomButton.Opts().color(CustomButton.themeColor).pill().block()));
        field.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        setRoot(root);
    }

    /** Dart: klaim(). Blocking. */
    private boolean klaim() {
        Map<String, String> httpBody = new HashMap<String, String>();
        httpBody.put("idItem", Json.str(pesananItem, "id"));
        httpBody.put("keterangan", keterangan);

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> body = HTTPRequest.postBody(httpBody);

        Http.Response httpResponse = Http.postApi("/Transaksi/klaimProdukTidakTersedia?" + params, body);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusKlaim");
        }
        return false;
    }
}
