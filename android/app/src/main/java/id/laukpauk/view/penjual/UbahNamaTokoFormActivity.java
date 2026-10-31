package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of class UbahNamaTokoForm in lib/views/penjual/UbahNamaToko.dart. Result: true / false (saved or not). */
public class UbahNamaTokoFormActivity extends BaseActivity {
    private EditText namaTokoController;
    private LinearLayout buttonHolder;

    /** Dart ubahNamaToko(). BLOCKING. */
    private boolean ubahNamaToko(String namaToko) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("namaToko", namaToko);

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> postBody = HTTPRequest.postBody(body);

        Http.Response httpResponse = Http.postApi("/Penjual/ubahNamaToko?" + params, postBody);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusUbahNamaToko");
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(PesananScreenUi.scaffoldBackground);
        root.addView(DetailUi.appBar(this, "Ubah Nama Toko"));

        LinearLayout card = PenjualUi.cardPadded(this, 15, 15);
        card.addView(PenjualUi.text(this, "Ubah Nama Toko", 4f, 0xDD000000, true));
        card.addView(Ui.spaceH(this, 10));

        namaTokoController = new EditText(this);
        card.addView(PenjualUi.pillField(this, namaTokoController, "Nama Toko Baru",
                android.text.InputType.TYPE_CLASS_TEXT, 2.25f));

        buttonHolder = Ui.column(this);
        card.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        PenjualUi.watch(namaTokoController, () -> renderButton());
        renderButton();

        LinearLayout wrap = Ui.column(this);
        Ui.padXY(wrap, 5, 5);
        wrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(wrap, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);
    }

    private void renderButton() {
        boolean hasText = namaTokoController.getText().length() >= 1;
        boolean showing = buttonHolder.getChildCount() > 0;
        if (hasText == showing) {
            return;
        }
        buttonHolder.removeAllViews();
        if (hasText) {
            buttonHolder.addView(Ui.spaceH(this, 10));
            final CustomButton.Btn[] btn = new CustomButton.Btn[1];
            btn[0] = CustomButton.button(this, "Ubah Nama Toko", () -> {
                final String namaToko = namaTokoController.getText().toString();
                btn[0].setBusy(true);
                async(() -> ubahNamaToko(namaToko), statusUbahNamaToko -> finishWithResult(statusUbahNamaToko));
            }, new CustomButton.Opts().color(CustomButton.themeColor).block());
            buttonHolder.addView(btn[0], new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
