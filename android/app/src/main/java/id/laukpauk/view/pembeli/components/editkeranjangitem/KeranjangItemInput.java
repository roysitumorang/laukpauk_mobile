package id.laukpauk.view.pembeli.components.editkeranjangitem;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.keranjang.CartUi;

/**
 * Port of lib/views/pembeli/components/EditKeranjangItem/KeranjangItemInput.dart: a rounded
 * alice-blue text field with a floating label. Constructor parameters in Dart order:
 * (label, value, inputType, callBack, {height = 55.0, minLines = 1}); `inputType` is an
 * android.text.InputType flag set (Dart: TextInputType.text -> InputType.TYPE_CLASS_TEXT).
 */
public class KeranjangItemInput extends LinearLayout {
    private final String label;
    private final Async.Done<String> callBack;
    private final int minLines;

    private TextView labelView;
    private EditText editText;

    public KeranjangItemInput(Context ctx, String label, String value, int inputType,
                              Async.Done<String> callBack, double height, int minLines) {
        super(ctx);
        this.label = label;
        this.callBack = callBack;
        this.minLines = minLines;
        setOrientation(VERTICAL);

        float hb = Ui.hb(ctx);

        // Container(margin: EdgeInsets.only(bottom: 13), height: height, decoration: ...)
        Ui.pad(this, 0, 0, 0, 13);

        FrameLayout box = new FrameLayout(ctx);
        box.setBackground(Ui.shape(ctx, 0xFFF0F8FF, 10f));
        // inner Container(padding: EdgeInsets.only(right: 15, left: 25))
        Ui.pad(box, 25, 0, 15, 0);
        addView(box, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, (float) height)));

        editText = new EditText(ctx);
        new Txt.Style(hb * 3.5f).color(CartUi.BLACK87).apply(editText);
        editText.setBackground(null);
        editText.setHintTextColor(CartUi.GREY_500);
        editText.setPadding(0, Ui.dp(ctx, 22), 0, Ui.dp(ctx, 12));
        editText.setGravity(Gravity.TOP | Gravity.START);
        editText.setMinLines(minLines);
        editText.setMaxLines(minLines + 1);
        int type = inputType;
        if (minLines > 1 || minLines + 1 > 1) {
            // maxLines > 1: Flutter turns the keyboard into a multiline one
            type = type | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        }
        editText.setInputType(type);
        editText.setText(value == null ? "" : value); // textController.text = widget.value (no onChanged)
        box.addView(editText, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        // labelText with labelStyle(WorkSans, w500, Colors.grey): floats above the text when focused / filled
        labelView = Ui.text(ctx, label, 12f, CartUi.GREY_500);
        FrameLayout.LayoutParams labelLp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.TOP | Gravity.START);
        labelLp.topMargin = Ui.dp(ctx, 8);
        box.addView(labelView, labelLp);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLabel();
                if (KeranjangItemInput.this.callBack != null) {
                    KeranjangItemInput.this.callBack.run(s.toString());
                }
            }
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> updateLabel());
        // the field sits inside a ScrollView: let it scroll its own text when it overflows
        editText.setOnTouchListener((v, event) -> {
            ViewParent p = v.getParent();
            if (p != null) {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN
                        && (v.canScrollVertically(1) || v.canScrollVertically(-1))) {
                    p.requestDisallowInterceptTouchEvent(true);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    p.requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
        updateLabel();
    }

    private void updateLabel() {
        boolean floating = editText.hasFocus() || editText.getText().length() > 0;
        labelView.setVisibility(floating ? View.VISIBLE : View.INVISIBLE);
        editText.setHint(floating ? null : label);
    }

    /** The current text (Dart: textController.text). */
    public String getText() {
        return editText.getText().toString();
    }

    public int getMinLines() {
        return minLines;
    }
}
