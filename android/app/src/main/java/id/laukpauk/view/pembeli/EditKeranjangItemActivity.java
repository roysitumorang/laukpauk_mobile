package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.editkeranjangitem.KeranjangItemInput;
import id.laukpauk.view.pembeli.components.keranjang.CartUi;

/** Port of lib/views/pembeli/EditKeranjangItem.dart. Args: dataKeranjangItem (object). Result: bool. */
public class EditKeranjangItemActivity extends BaseActivity {
    private String satuanSelected = "0";
    private String quantity = "";
    private String catatanTambahan = "";
    private boolean saving = false;
    private JsonObject dataKeranjangItem;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        dataKeranjangItem = Json.object(args(), "dataKeranjangItem");
        satuanSelected = Json.str(dataKeranjangItem, "idSatuan");
        quantity = Json.str(dataKeranjangItem, "quantity");
        catatanTambahan = Json.str(dataKeranjangItem, "catatan");
        render();
    }

    /** Dart: simpanPerubahan(). Blocking. */
    private boolean simpanPerubahan() {
        String idItem = Json.str(dataKeranjangItem, "idCart");
        Map<String, String> httpBody = new HashMap<String, String>();
        httpBody.put("quantity", quantity);
        httpBody.put("catatan", catatanTambahan);
        Map<String, String> body = HTTPRequest.postBody(httpBody);

        Http.Response httpResponse = Http.postApi("/Keranjang/saveCart/" + idItem, body);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusAddCart");
        }
        return false;
    }

    private void render() {
        final float hb = Ui.hb(this);

        String jenisSatuan = Json.str(dataKeranjangItem, "jenisSatuan");
        String idPromo = Json.strOrNull(dataKeranjangItem, "idPromo");

        final List<String> quantityItems = new ArrayList<String>();
        for (int i = 1; i <= 10; i++) {
            quantityItems.add(String.valueOf(i));
        }
        if (!quantityItems.contains(quantity)) {
            quantityItems.add(quantity);
        }

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(CartUi.appBar(this, "Edit Keranjang Item", true), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout padded = Ui.column(this);
        Ui.pad(padded, 5, 5, 5, 5);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(padded, new ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));

        LinearLayout card = CartUi.card(this, CustomColors.white, 10f, 8f, false);
        Ui.pad(card, 15, 25, 15, 25);
        padded.addView(card);

        card.addView(Ui.text(this, Json.str(dataKeranjangItem, "namaProduk"), hb * 4, CustomColors.black, true));
        card.addView(Ui.spaceH(this, 5));
        card.addView(Ui.text(this, "Silahkan sesuaikan dengan kebutuhan belanja anda", hb * 3.5f, CustomColors.grey));

        if (jenisSatuan.toLowerCase().equals("tidak pas") && idPromo == null) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setGravity(android.view.Gravity.CENTER_VERTICAL);
            box.setBackground(Ui.shape(this, 0xFFF0F8FF, 10f));
            Ui.pad(box, 25, 10, 25, 10);
            LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            boxLp.topMargin = Ui.dp(this, 25);
            card.addView(box, boxLp);

            box.addView(Ui.text(this, "Quantity", hb * 3.5f, CustomColors.black), Ui.weightW(1f));

            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                    quantityItems) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView v = (TextView) super.getView(position, convertView, parent);
                    v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, hb * 3.25f);
                    v.setTextColor(CustomColors.black);
                    return v;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView v = (TextView) super.getDropDownView(position, convertView, parent);
                    v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, hb * 3.25f);
                    v.setTextColor(CustomColors.black);
                    return v;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(quantityItems.indexOf(quantity));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newQuantity = quantityItems.get(position);
                    if (newQuantity != null) {
                        quantity = newQuantity;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            box.addView(spinner, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        if (jenisSatuan.equals("tidak pas")) {
            card.addView(Ui.spaceH(this, 10));
        }

        card.addView(new KeranjangItemInput(this, "Catatan Tambahan", Json.str(dataKeranjangItem, "catatan"),
                InputType.TYPE_CLASS_TEXT, (String data) -> catatanTambahan = data, hb * 25, 5));
        card.addView(Ui.divider(this, CartUi.GREY_300, 1));
        card.addView(Ui.spaceH(this, 20));
        card.addView(CustomButton.button(this, "Simpan Perubahan", () -> {
            if (saving) {
                return;
            }
            saving = true;
            async(() -> Boolean.valueOf(simpanPerubahan()), (Boolean statusSaveCart) -> {
                saving = false;
                finishWithResult(statusSaveCart != null && statusSaveCart);
            });
        }, new CustomButton.Opts().color(CustomButton.themeColor).block()));

        setRoot(root);
    }
}
