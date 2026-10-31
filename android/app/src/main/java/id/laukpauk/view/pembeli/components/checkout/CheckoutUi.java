package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.model.PembayaranModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Ui;

/** Small view helpers shared by the Checkout screen and its components. */
public final class CheckoutUi {
    private CheckoutUi() {}

    /** Flutter's default Text color (black87). */
    public static final int TEXT = 0xDD000000;
    /** Color(0x3366CC99): the Checkout text field fill. */
    public static final int FIELD_FILL = 0x3366CC99;

    /** The hosting BaseActivity of a (possibly dialog-wrapped) context, or null. */
    public static BaseActivity activity(Context c) {
        while (c instanceof ContextWrapper) {
            if (c instanceof BaseActivity) {
                return (BaseActivity) c;
            }
            c = ((ContextWrapper) c).getBaseContext();
        }
        return null;
    }

    /** Dart `SizeConfig.horizontalBlock * n` in dp. */
    public static float hb(Context ctx, float n) {
        return Ui.hb(ctx) * n;
    }

    public static TextView text(Context ctx, CharSequence s, float sizeSp, int color, boolean bold) {
        return Ui.text(ctx, s, sizeSp, color, bold);
    }

    public static TextView plain(Context ctx, CharSequence s) {
        return Ui.text(ctx, s, 14f, TEXT);
    }

    public static LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(l, t, r, b);
        return p;
    }

    /** Row(mainAxisAlignment: spaceBetween): left text, right view (right view keeps its size). */
    public static LinearLayout spaceBetween(Context ctx, View left, View right) {
        LinearLayout row = Ui.row(ctx);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.addView(left, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        row.addView(right, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        return row;
    }

    /** Center(SizedBox(w, h, CircularProgressIndicator(strokeWidth: 1.5))). */
    public static View spinner(Context ctx, float sizeDp, float verticalPaddingDp) {
        FrameLayout center = new FrameLayout(ctx);
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        bar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(
                id.laukpauk.core.CustomColors.green));
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(Ui.dp(ctx, sizeDp), Ui.dp(ctx, sizeDp),
                android.view.Gravity.CENTER);
        center.addView(bar, p);
        Ui.padXY(center, 0, verticalPaddingDp);
        return center;
    }

    /** Flutter TextButton: coloured label with ripple. */
    public static TextView textButton(Context ctx, String label, int color, final Runnable onTap) {
        TextView tv = Ui.text(ctx, label.toUpperCase(), Ui.hb(ctx) * 3.5f, color, true);
        Ui.padXY(tv, 16, 10);
        tv.setGravity(android.view.Gravity.CENTER);
        Layouts.ripple(tv, 0x22000000 | (color & 0x00FFFFFF), 4f);
        tv.setOnClickListener(v -> onTap.run());
        return tv;
    }

    public static void detach(View v) {
        if (v != null && v.getParent() instanceof ViewGroup) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
    }

    /** Runs `r` after every text change of the EditText. */
    public static void onTextChanged(EditText e, final Runnable r) {
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                r.run();
            }
        });
    }

    /**
     * The shared "Buat Orderan" flow of PilihanMetodePembayaran / NomorTelepon / UangYangDipersiapkan /
     * CreditCardData: busy(true); PembayaranModel.buatOrder(...); busy(false); PembayaranModel.buatOrderNextStep(...).
     */
    public static void buatOrder(final BaseActivity a, final String channelCodeTerpilihLC,
                                 final JsonObject metodePembayaranTerpilihMap, final JsonObject checkoutData,
                                 final String moreData, final Async.Done<JsonObject> checkoutCallback,
                                 final Async.Done<Boolean> busy) {
        busy.run(true);
        a.async(() -> PembayaranModel.buatOrder(metodePembayaranTerpilihMap, checkoutData, moreData),
                (JsonObject returnedData) -> {
                    busy.run(false);
                    PembayaranModel.buatOrderNextStep(a, channelCodeTerpilihLC, returnedData, checkoutData,
                            metodePembayaranTerpilihMap, moreData, checkoutCallback, null);
                }, error -> busy.run(false));
    }
}
