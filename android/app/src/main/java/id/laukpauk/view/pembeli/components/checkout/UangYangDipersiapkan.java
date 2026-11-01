package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Checkout/UangYangDipersiapkan.dart: cash (COD) amount step. */
public class UangYangDipersiapkan extends ScrollView {
    private final long totalBelanja;
    private final JsonObject metodePembayaranTerpilihMap;
    private final JsonObject checkoutData;
    private final String channelCodeTerpilihLC;
    private final Async.Done<JsonObject> checkoutCallback;

    private Long uang;
    private final LinearLayout buttonHolder;
    private CustomButton.Btn button;

    public UangYangDipersiapkan(Context ctx, long totalBelanja, JsonObject metodePembayaranTerpilihMap,
                                JsonObject checkoutData, String channelCodeTerpilihLC,
                                Async.Done<JsonObject> checkoutCallback, final Runnable backToChannelPayment) {
        super(ctx);
        this.totalBelanja = totalBelanja;
        this.metodePembayaranTerpilihMap = metodePembayaranTerpilihMap;
        this.checkoutData = checkoutData;
        this.channelCodeTerpilihLC = channelCodeTerpilihLC;
        this.checkoutCallback = checkoutCallback;
        float hb = Ui.hb(ctx);

        LinearLayout col = Ui.column(ctx);
        addView(col, new LayoutParams(Ui.MATCH, Ui.WRAP));

        col.addView(Ui.spaceH(ctx, 10));
        LinearLayout head = Ui.row(ctx);
        View back = Ui.icon(ctx, R.drawable.ic_arrow_back, 24, 0xDD000000);
        back.setOnClickListener(v -> backToChannelPayment.run());
        head.addView(back, new LinearLayout.LayoutParams(Ui.dp(ctx, 24), Ui.dp(ctx, 24)));
        head.addView(Ui.spaceW(ctx, 15));
        head.addView(CheckoutUi.text(ctx, "Bayar Tunai", hb * 3.5f, CheckoutUi.TEXT, true));
        col.addView(head, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, 10));

        LinearLayout box = Ui.row(ctx);
        Ui.background(box, 0x3366CC99, 10);
        TextView lbl = CheckoutUi.plain(ctx, "Rp.");
        lbl.setGravity(Gravity.CENTER);
        Ui.padXY(lbl, hb * 3.5f, 0);
        box.addView(lbl);
        final EditText input = new EditText(ctx);
        input.setBackground(null);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, hb * 3.5f);
        input.setTextColor(CustomColors.green);
        input.setHint("Uang yang dipersiapkan untuk membayar");
        input.setHintTextColor(CustomColors.green);
        input.setTypeface(Ui.font(ctx));
        input.setPadding(Ui.dp(ctx, 15), Ui.dp(ctx, hb * 5), Ui.dp(ctx, 15), Ui.dp(ctx, hb * 5));
        CheckoutUi.onTextChanged(input, () -> {
            String value = input.getText().toString();
            if (value.length() >= 1) {
                try {
                    uang = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    uang = null;
                }
            } else {
                uang = null;
            }
            renderButton();
        });
        box.addView(input, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        col.addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        col.addView(Ui.spaceH(ctx, 10));
        col.addView(CheckoutUi.text(ctx,
                "Masukkan uang yang dipersiapkan untuk membayar agar memudahkan kurir mempersiapkan kembalian.",
                hb * 3.25f, CheckoutUi.TEXT, false));
        col.addView(Ui.spaceH(ctx, 10));
        col.addView(CheckoutUi.text(ctx, "Note : Uang yang dipersiapkan tidak boleh kurang dari "
                + Currency.formatRupiah(totalBelanja, 0), hb * 2.75f, CheckoutUi.TEXT, false));
        col.addView(Ui.spaceH(ctx, 15));
        buttonHolder = Ui.column(ctx);
        col.addView(buttonHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, 10));
    }

    private void renderButton() {
        boolean show = uang != null && uang >= totalBelanja;
        if (!show) {
            buttonHolder.removeAllViews();
            button = null;
            return;
        }
        if (button != null) {
            return;
        }
        button = CustomButton.button(getContext(), "Buat Orderan", this::buatOrder,
                new CustomButton.Opts().block().color(CustomButton.themeColor).pill());
        buttonHolder.addView(button, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void buatOrder() {
        BaseActivity a = CheckoutUi.activity(getContext());
        if (a == null || uang == null) {
            return;
        }
        String moreData = String.valueOf(uang);
        CheckoutUi.buatOrder(a, channelCodeTerpilihLC, metodePembayaranTerpilihMap, checkoutData, moreData,
                checkoutCallback, busy -> {
                    if (button != null) {
                        button.setBusy(busy);
                    }
                });
    }
}
