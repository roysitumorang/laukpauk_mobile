package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Environment;
import id.laukpauk.core.Json;
import id.laukpauk.model.XenditModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/Checkout/CreditCardData.dart: credit card form -> Xendit token ->
 * PembayaranModel.buatOrder. Card data is never logged; the fields are numeric, the CVV is masked, and
 * autofill / suggestions are disabled for all of them.
 */
public class CreditCardData extends ScrollView {
    private final long totalTagihan;
    private final JsonObject metodePembayaranTerpilihMap, checkoutData;
    private final String channelCodeTerpilihLC;
    private final Async.Done<JsonObject> checkoutCallback;

    private final EditText nomorAkunController, bulanExpiredController, tahunExpiredController, cvnController;
    private final LinearLayout buttonHolder;
    private final View processingOverlay;
    private CustomButton.Btn button;
    private boolean isProcessButtonBusy = false;

    public CreditCardData(Context ctx, String channelCodeTerpilihLC, long totalTagihan,
                          JsonObject metodePembayaranTerpilihMap, JsonObject checkoutData,
                          Async.Done<JsonObject> checkoutCallback, Runnable backToChannelPayment) {
        super(ctx);
        this.channelCodeTerpilihLC = channelCodeTerpilihLC;
        this.totalTagihan = totalTagihan;
        this.metodePembayaranTerpilihMap = metodePembayaranTerpilihMap;
        this.checkoutData = checkoutData;
        this.checkoutCallback = checkoutCallback;
        float hb = Ui.hb(ctx);

        nomorAkunController = CustomForm.controller(ctx);
        bulanExpiredController = CustomForm.controller(ctx);
        tahunExpiredController = CustomForm.controller(ctx);
        cvnController = CustomForm.controller(ctx);
        if (Environment.environment.equals("development")) {
            nomorAkunController.setText("4000000000000002");
            bulanExpiredController.setText("03");
            tahunExpiredController.setText("26");
            cvnController.setText("266");
        }
        for (EditText e : new EditText[]{nomorAkunController, bulanExpiredController, tahunExpiredController,
                cvnController}) {
            e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
            CheckoutUi.onTextChanged(e, this::renderButton);
        }

        float horizontalPadding = hb * 3.5f;
        float formTopPadding = hb * 5.0f;

        FrameLayout stack = new FrameLayout(ctx);
        addView(stack, new LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout col = Ui.column(ctx);
        Ui.pad(col, horizontalPadding, 0, horizontalPadding, 0);
        stack.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        col.addView(Ui.spaceH(ctx, 10));
        col.addView(CheckoutUi.text(ctx, "Kartu Kredit", hb * 3.5f, CheckoutUi.TEXT, true));
        col.addView(Ui.spaceH(ctx, 10));
        col.addView(CheckoutUi.text(ctx, "Silahkan input data kartu kredit yang anda gunakan untuk pembayaran",
                hb * 3.25f, CheckoutUi.TEXT, false));
        col.addView(Ui.spaceH(ctx, formTopPadding));
        col.addView(CustomForm.label(ctx, "Nomor Kartu Kredit"));
        col.addView(inputForm(ctx, "5100XXXXXXXXXXXX", nomorAkunController, false),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.asset(ctx, "payment/jcb-mastercard.png", 175f));
        col.addView(Ui.spaceH(ctx, formTopPadding));
        col.addView(CustomForm.label(ctx, "Masa Berlaku"));
        LinearLayout expiry = Ui.row(ctx);
        expiry.addView(inputForm(ctx, "Bulan", bulanExpiredController, false),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 2f));
        TextView slash = CheckoutUi.text(ctx, "/", 30f, CheckoutUi.TEXT, false);
        Ui.padXY(slash, hb * 2.5f, 0);
        expiry.addView(slash);
        expiry.addView(inputForm(ctx, "Tahun", tahunExpiredController, false),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 2f));
        expiry.addView(new View(ctx), new LinearLayout.LayoutParams(0, 0, 5f));
        col.addView(expiry, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, formTopPadding));
        col.addView(CustomForm.label(ctx, "CVV/CCV"));
        LinearLayout cvv = Ui.row(ctx);
        cvv.addView(inputForm(ctx, "3 Digit Angka Rahasia Kartu Kredit", cvnController, true),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        cvv.addView(Ui.spaceW(ctx, 7.5f));
        cvv.addView(Ui.asset(ctx, "payment/CVV.png", 50f));
        cvv.addView(new View(ctx), new LinearLayout.LayoutParams(0, 0, 4f));
        col.addView(cvv, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, formTopPadding));
        buttonHolder = Ui.column(ctx);
        col.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        processingOverlay = buildOverlay(ctx, hb);
        processingOverlay.setVisibility(View.GONE);
        stack.addView(processingOverlay, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        renderButton();
    }

    private View inputForm(Context ctx, String hintText, EditText controller, boolean cvv) {
        CustomForm.TextFieldOpts o = new CustomForm.TextFieldOpts().borderColor(CustomColors.green)
                .borderWidth(1.0).keyboardType(CustomForm.KEYBOARD_NUMBER).obsecureText(cvv);
        CustomForm.TextFieldView v = CustomForm.textField(ctx, hintText, controller, o);
        EditText input = v.input();
        input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        input.setLongClickable(!cvv);
        return v;
    }

    private View buildOverlay(Context ctx, float hb) {
        LinearLayout box = Ui.column(ctx);
        box.setBackgroundColor(0x9FFFFFFF);
        box.setGravity(Gravity.CENTER);
        Ui.pad(box, hb * 15f, 0, hb * 15f, 0);
        box.setClickable(true); // swallow touches while processing
        box.addView(Ui.asset(ctx, "loading.png", hb * 50f));
        box.addView(Ui.spaceH(ctx, hb * 2.5f));
        TextView t = CheckoutUi.text(ctx, "Sedang Memproses", hb * 4.0f, CheckoutUi.TEXT, true);
        t.setGravity(Gravity.CENTER);
        box.addView(t);
        box.addView(Ui.spaceH(ctx, 5));
        TextView s = CheckoutUi.text(ctx, "Tunggu sebentar ya, pembayaranmu sedang diproses", hb * 3.25f,
                CustomColors.textMuted, false);
        s.setGravity(Gravity.CENTER);
        box.addView(s);
        box.addView(Ui.spaceH(ctx, 35));
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        bar.setIndeterminateTintList(ColorStateList.valueOf(CustomColors.nearlyWhite));
        bar.setBackground(Ui.circle(CustomColors.green));
        Ui.padAll(bar, 4);
        box.addView(bar, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 7.5f), Ui.dp(ctx, hb * 7.5f)));
        return box;
    }

    private boolean formOkay() {
        return nomorAkunController.length() >= 16 && bulanExpiredController.length() >= 2
                && tahunExpiredController.length() >= 2 && cvnController.length() >= 3;
    }

    private void renderButton() {
        if (!formOkay()) {
            buttonHolder.removeAllViews();
            button = null;
            return;
        }
        if (button != null) {
            return;
        }
        button = CustomButton.button(getContext(), "Verifikasi Kartu Kredit", this::verifikasi,
                new CustomButton.Opts().block().color(CustomButton.themeColor).pill());
        button.setBusy(isProcessButtonBusy);
        buttonHolder.addView(button, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void setProcessButtonBusy(boolean busy) {
        isProcessButtonBusy = busy;
        if (button != null) {
            button.setBusy(busy);
        }
    }

    private void verifikasi() {
        final BaseActivity a = CheckoutUi.activity(getContext());
        if (a == null || !formOkay()) {
            return;
        }
        String accountNumber = nomorAkunController.getText().toString();
        String expiryMonth = bulanExpiredController.getText().toString();
        String expiryYear = "20" + tahunExpiredController.getText().toString();
        String cardCVN = cvnController.getText().toString();

        setProcessButtonBusy(true);

        JsonObject dataTransaksi = new JsonObject();
        dataTransaksi.addProperty("totalTagihan", totalTagihan);
        dataTransaksi.addProperty("cardNumber", accountNumber);
        dataTransaksi.addProperty("expiryMonth", expiryMonth);
        dataTransaksi.addProperty("expiryYear", expiryYear);
        dataTransaksi.addProperty("cardCVN", cardCVN);

        XenditModel.createCreditCardToken(a, dataTransaksi, (JsonObject tokenAndAuthentication) -> {
            setProcessButtonBusy(false);
            if (tokenAndAuthentication == null) {
                return; // Dart: token creation failed, nothing else happens
            }
            final String creditCardDataString = Json.toJson(tokenAndAuthentication);

            processingOverlay.setVisibility(View.VISIBLE);
            CheckoutUi.buatOrder(a, channelCodeTerpilihLC, metodePembayaranTerpilihMap, checkoutData,
                    creditCardDataString, checkoutCallback, busy ->
                            processingOverlay.setVisibility(busy ? View.VISIBLE : View.GONE));
        });
    }
}
