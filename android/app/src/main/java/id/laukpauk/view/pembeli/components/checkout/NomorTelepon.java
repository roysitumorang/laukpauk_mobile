package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Checkout/NomorTelepon.dart: e-wallet phone number step. */
public class NomorTelepon extends LinearLayout {
    private final JsonObject metodePembayaranTerpilihMap;
    private final JsonObject checkoutData;
    private final String channelCodeTerpilihLC;
    private final Async.Done<JsonObject> checkoutCallback;

    private String nomorTelepon;
    private final LinearLayout buttonHolder;
    private CustomButton.Btn button;

    public NomorTelepon(Context ctx, JsonObject metodePembayaranTerpilihMap, JsonObject checkoutData,
                        String channelCodeTerpilihLC, Async.Done<JsonObject> checkoutCallback,
                        final Runnable backToChannelPayment) {
        super(ctx);
        this.metodePembayaranTerpilihMap = metodePembayaranTerpilihMap;
        this.checkoutData = checkoutData;
        this.channelCodeTerpilihLC = channelCodeTerpilihLC;
        this.checkoutCallback = checkoutCallback;
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        String paymentName = Json.str(metodePembayaranTerpilihMap, "name").toUpperCase();

        addView(Ui.spaceH(ctx, 10));
        LinearLayout head = Ui.row(ctx);
        View back = Ui.icon(ctx, R.drawable.ic_arrow_back, 24, 0xDD000000);
        back.setOnClickListener(v -> backToChannelPayment.run());
        head.addView(back, new LinearLayout.LayoutParams(Ui.dp(ctx, 24), Ui.dp(ctx, 24)));
        head.addView(Ui.spaceW(ctx, 15));
        head.addView(CheckoutUi.text(ctx, paymentName, hb * 3.5f, CheckoutUi.TEXT, true));
        addView(head, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));

        LinearLayout box = Ui.row(ctx);
        Ui.background(box, 0x3366CC99, 10);
        TextView lbl = CheckoutUi.plain(ctx, "Nomor Telepon");
        lbl.setGravity(Gravity.CENTER);
        Ui.padXY(lbl, hb * 3.5f, 0);
        box.addView(lbl);
        EditText input = new EditText(ctx);
        input.setBackground(null);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, hb * 3.5f);
        input.setTextColor(CustomColors.green);
        input.setHint("Nomor Yang Terhubung ke EWallet");
        input.setHintTextColor(CustomColors.green);
        input.setTypeface(Ui.font(ctx));
        input.setPadding(Ui.dp(ctx, 15), Ui.dp(ctx, hb * 5), Ui.dp(ctx, 15), Ui.dp(ctx, hb * 5));
        CheckoutUi.onTextChanged(input, () -> {
            String value = input.getText().toString();
            nomorTelepon = value.length() >= 1 ? value : null;
            renderButton();
        });
        box.addView(input, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        addView(Ui.spaceH(ctx, 10));
        addView(CheckoutUi.text(ctx,
                "Pastikan anda mengikuti format berikut ini 08XXXXXXXXXX tanpa menggunakan kode negara +62",
                hb * 3.25f, CheckoutUi.TEXT, false));
        addView(Ui.spaceH(ctx, 15));
        buttonHolder = Ui.column(ctx);
        addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));
    }

    private void renderButton() {
        Context ctx = getContext();
        boolean show = nomorTelepon != null && nomorTelepon.length() >= 10;
        if (!show) {
            buttonHolder.removeAllViews();
            button = null;
            return;
        }
        if (button != null) {
            return; // keep the button (and its busy state) while the number stays valid
        }
        button = CustomButton.button(ctx, "Buat Orderan", this::buatOrder,
                new CustomButton.Opts().block().color(CustomButton.themeColor).pill());
        buttonHolder.addView(button, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void buatOrder() {
        BaseActivity a = CheckoutUi.activity(getContext());
        if (a == null || nomorTelepon == null) {
            return;
        }
        String moreData = nomorTelepon;
        CheckoutUi.buatOrder(a, channelCodeTerpilihLC, metodePembayaranTerpilihMap, checkoutData, moreData,
                checkoutCallback, busy -> {
                    if (button != null) {
                        button.setBusy(busy);
                    }
                });
    }
}
