package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.Async;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/Checkout/CheckoutElement.dart: a rounded (0xFFF0F8FF) labelled text
 * field. `inputType` is an android.text.InputType value (Dart TextInputType); `initialValue` may be null.
 */
public class CheckoutElement extends LinearLayout {
    private final EditText controller;

    public CheckoutElement(Context ctx, String label, int inputType, final Async.Done<String> callBack,
                           String initialValue) {
        super(ctx);
        setOrientation(VERTICAL);

        controller = new EditText(ctx);
        if (initialValue != null) {
            controller.setText(initialValue);
        }

        LinearLayout box = Ui.row(ctx);
        Ui.background(box, 0xFFF0F8FF, 10);
        Ui.pad(box, 25, 0, 15, 0);

        controller.setBackground(null);
        controller.setInputType(inputType == 0 ? InputType.TYPE_CLASS_TEXT : inputType);
        controller.setTextSize(TypedValue.COMPLEX_UNIT_SP, Ui.hb(ctx) * 3.5f);
        controller.setTextColor(0xFF000000);
        controller.setTypeface(Ui.font(ctx));
        controller.setHint(label);
        controller.setHintTextColor(0xFF9E9E9E);
        controller.setTypeface(controller.getTypeface(), Typeface.NORMAL);
        controller.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        controller.setPadding(0, Ui.dp(ctx, Ui.hb(ctx) * 3.25f), 0, Ui.dp(ctx, Ui.hb(ctx) * 3.25f));
        CheckoutUi.onTextChanged(controller, () -> {
            if (callBack != null) {
                callBack.run(controller.getText().toString());
            }
        });
        box.addView(controller, new LinearLayout.LayoutParams(0, Ui.MATCH, 1f));

        addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, Ui.hb(ctx) * 15)));
        addView(Ui.spaceH(ctx, 7.5f)); // margin: EdgeInsets.only(bottom: 7.5)
    }

    public EditText controller() {
        return controller;
    }
}
