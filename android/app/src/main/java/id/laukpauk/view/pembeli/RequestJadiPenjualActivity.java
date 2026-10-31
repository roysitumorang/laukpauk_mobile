package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.otpverifikasiuser.ListPasar;

/** Port of lib/views/pembeli/RequestJadiPenjual.dart. */
public class RequestJadiPenjualActivity extends BaseActivity {
    private EditText nomorRekeningController;
    private EditText namaTokoController;
    private EditText alamatDetailTokoController;
    private EditText bioPenjualController;

    private String lokasiBerjualan = "pasarTersedia";
    private String pasarSelected = "0";
    private JsonObject lokasiTersendiriState;

    private boolean isButtonAjukanBusy = false;

    private TextView dropdownText;
    private LinearLayout lokasiSection;
    private LinearLayout ajukanWrap;
    private CustomButton.Btn ajukanButton;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        nomorRekeningController = new EditText(this);
        namaTokoController = new EditText(this);
        alamatDetailTokoController = new EditText(this);
        bioPenjualController = new EditText(this);
        render();
    }

    private void ajukanRequest() {
        isButtonAjukanBusy = true;
        ajukanButton.setBusy(true);

        final Map<String, String> dataRequest = new LinkedHashMap<>();
        dataRequest.put("namaToko", namaTokoController.getText().toString());
        dataRequest.put("alamatToko", alamatDetailTokoController.getText().toString());
        dataRequest.put("bio", bioPenjualController.getText().toString());
        dataRequest.put("lokasiBerjualan", lokasiBerjualan);
        dataRequest.put("pasar", pasarSelected);
        dataRequest.put("nomorRekening", nomorRekeningController.getText().toString());

        if (lokasiTersendiriState != null) {
            if (lokasiBerjualan.toLowerCase().equals("lokasitersendiri")) {
                dataRequest.put("lat", Json.str(lokasiTersendiriState, "lat"));
                dataRequest.put("long", Json.str(lokasiTersendiriState, "lng"));
            }
        }

        async(() -> {
            String params = HTTPRequest.getQueryString(null, true);
            Map<String, String> body = HTTPRequest.postBody(dataRequest);

            Http.Response httpResponse = Http.postApi("/Penjual/ajukanRequestPenjual?" + params, body);
            if (httpResponse.statusCode == 200) {
                return Boolean.valueOf(Json.bool(httpResponse.jsonObject(), "statusRequest"));
            }
            return null;
        }, statusRequest -> {
            if (statusRequest == null) {
                return; // Dart: nothing happens when the request does not answer 200
            }
            float hb = Ui.hb(this);
            String text = "Pengajuan request gagal dilakukan !";
            Runnable onOk = null;
            if (statusRequest) {
                text = "Pengajuan request berhasil dilakukan ! Tunggu hingga admin menyetujui request anda, dan anda dapat langsung berjualan.";
                onOk = () -> Nav.push(this, UserAndDashboardActivity.class);
            }

            isButtonAjukanBusy = false;
            ajukanButton.setBusy(false);

            AccountUi.alert(this, "Pengajuan Request", text, hb * 4f, hb * 3.25f, "OK", onOk);
        });
    }

    private void lokasiBerjualanChanged(final String lokasiBerjualanSelected, boolean pilihLokasiLain) {
        String lokasiBerjualanSelectedLC = lokasiBerjualanSelected.toLowerCase();
        if (lokasiBerjualanSelectedLC.equals("lokasitersendiri")) {
            if (lokasiTersendiriState == null || pilihLokasiLain) {
                Nav.pushForResult(this, PinGMAPSActivity.class,
                        Json.of("appBarText", "Pilih Lokasi Berjualan"), result -> {
                            if (result != null && result.isJsonObject()) {
                                lokasiBerjualan = lokasiBerjualanSelected;
                                lokasiTersendiriState = result.getAsJsonObject();
                            } else {
                                lokasiBerjualan = "pasarTersedia";
                            }
                            updateLokasi();
                        });
            } else {
                lokasiBerjualan = "lokasiTersendiri";
                updateLokasi();
            }
        } else {
            lokasiBerjualan = lokasiBerjualanSelected;
            updateLokasi();
        }
    }

    private TextWatcher completeWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateAjukan();
            }
        };
    }

    private void updateAjukan() {
        boolean isFormCompleted = (nomorRekeningController.getText().length() >= 1
                && namaTokoController.getText().length() >= 1
                && alamatDetailTokoController.getText().length() >= 1
                && bioPenjualController.getText().length() >= 1);
        ajukanWrap.setVisibility(isFormCompleted ? View.VISIBLE : View.GONE);
    }

    /**
     * Container(padding: horizontal 30, color: lightGreen, radius 50) > TextField(labelText, hintText).
     * The floating label is a small caption that shows while the field has focus or text.
     */
    private LinearLayout pillField(final String labelText, final String hintText, final EditText input) {
        float hb = Ui.hb(this);
        float topBotPadding = hb * 3.0f;
        LinearLayout box = Ui.column(this);
        Ui.padXY(box, 30, 0);
        box.setBackground(Ui.shape(this, CustomColors.lightGreen, 50f));

        final TextView caption = Ui.text(this, labelText == null ? "" : labelText, hb * 2.6f, CustomColors.darkText);
        caption.setVisibility(View.GONE);
        if (labelText != null) {
            Ui.pad(caption, 0, 6, 0, 0);
            box.addView(caption, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }

        input.setBackground(null);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextSize(hb * 3.5f);
        input.setTextColor(CustomColors.darkText);
        input.setHintTextColor(CustomColors.darkText);
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        int pad = Ui.dp(this, topBotPadding);
        input.setPadding(0, pad, 0, pad);
        input.setHint(labelText != null ? labelText : hintText);
        input.setOnFocusChangeListener((v, hasFocus) -> {
            boolean floating = labelText != null && (hasFocus || input.getText().length() > 0);
            caption.setVisibility(floating ? View.VISIBLE : View.GONE);
            if (labelText != null) {
                input.setHint(hasFocus ? hintText : labelText);
            }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (labelText != null) {
                    caption.setVisibility(input.hasFocus() || s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            }
        });
        input.addTextChangedListener(completeWatcher());
        box.addView(input, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        return box;
    }

    private String lokasiLabel(String value) {
        if (value.equals("pasarTersedia")) {
            return "Pasar yang sudah tersedia";
        }
        if (value.equals("lokasiTersendiri")) {
            return "Lokasi Tersendiri";
        }
        return "Lokasi Berjualan";
    }

    private void render() {
        float hb = Ui.hb(this);
        float topBotPadding = hb * 3.0f;

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, "Request Menjadi Penjual"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout page = Ui.column(this);
        Ui.padXY(page, 5, 5);
        LinearLayout card = AccountUi.card(this, page);
        Ui.pad(card, 15, 20, 15, 20);

        card.addView(Ui.text(this, "Request Menjadi Penjual", hb * 4f, 0xDD000000, true));
        card.addView(Ui.spaceH(this, 3));
        card.addView(Ui.text(this,
                "Anda dapat bergabung bersama kami menjadi penjual dan dapatkan beragam keuntungan. Silahkan lengkapi data anda dengan form di bawah ini.",
                hb * 3.25f, 0xDD000000));
        card.addView(Ui.spaceH(this, 20));

        card.addView(pillField("Nama Toko", "Misal : Toko Jaya Sejahtera", namaTokoController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 10));
        card.addView(pillField("Alamat Detail Toko", "Jl. Asrama No. 23 AB Medan", alamatDetailTokoController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 10));
        card.addView(pillField("Bio Penjual", "Penjelasan tentang toko atau produk yang anda jual",
                bioPenjualController), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 10));

        // DropdownButton<String>
        LinearLayout dropdown = Ui.row(this);
        Ui.padXY(dropdown, 30, topBotPadding);
        dropdown.setBackground(Ui.shape(this, CustomColors.lightGreen, 50f));
        dropdownText = Ui.text(this, lokasiLabel(lokasiBerjualan), hb * 3.5f, 0xDD000000);
        dropdown.addView(dropdownText, Ui.weightW(1f));
        dropdown.addView(Ui.icon(this, R.drawable.ic_arrow_drop_down, 24f, 0xDD000000));
        dropdown.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, dropdown);
            menu.getMenu().add(0, 0, 0, "Lokasi Berjualan");
            menu.getMenu().add(0, 1, 1, "Pasar yang sudah tersedia");
            menu.getMenu().add(0, 2, 2, "Lokasi Tersendiri");
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    lokasiBerjualanChanged("pasarTersedia", false);
                } else if (item.getItemId() == 2) {
                    lokasiBerjualanChanged("lokasiTersendiri", false);
                }
                // value: null -> onChanged(null) does nothing
                return true;
            });
            menu.show();
        });
        card.addView(dropdown, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 10));

        lokasiSection = Ui.column(this);
        card.addView(lokasiSection, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 10));

        card.addView(pillField(null, "Nomor Rekening", nomorRekeningController),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 20));

        ajukanWrap = Ui.column(this);
        ajukanButton = CustomButton.button(this, "Ajukan Request", this::ajukanRequest,
                new CustomButton.Opts().block().busy(isButtonAjukanBusy).outline()
                        .color(CustomButton.themeColor).pill());
        ajukanWrap.addView(ajukanButton, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(ajukanWrap, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);

        updateLokasi();
        updateAjukan();
    }

    private void updateLokasi() {
        float hb = Ui.hb(this);
        dropdownText.setText(lokasiLabel(lokasiBerjualan));
        lokasiSection.removeAllViews();
        if (lokasiBerjualan.equals("lokasiTersendiri")) {
            LinearLayout box = Ui.column(this);
            Ui.padXY(box, 10, 0);
            box.addView(Ui.text(this, "Lokasi pasar anda akan sesuai dengan lokasi yang anda pilih :",
                    hb * 3.5f, 0xDD000000));
            box.addView(Ui.spaceH(this, 5));
            box.addView(Ui.text(this, "Latitude : " + Json.str(lokasiTersendiriState, "lat")
                    + ", Longitude : " + Json.str(lokasiTersendiriState, "lng"), hb * 2.5f, 0xDD000000));
            box.addView(Ui.spaceH(this, 10));
            TextView lain = Ui.text(this, "Pilih Lokasi Lain", hb * 3.5f, CustomColors.green);
            lain.setOnClickListener(v -> lokasiBerjualanChanged("lokasiTersendiri", true));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            box.addView(lain, lp);
            box.addView(Ui.spaceH(this, 10));
            lokasiSection.addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            lokasiSection.addView(new ListPasar(this, pasarSelected, idPasar -> {
                pasarSelected = idPasar;
                updateLokasi();
            }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
