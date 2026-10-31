package id.laukpauk.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;

/**
 * lib/global-components/Pencarian.dart: a pill shaped search field with a search and (when the given
 * text is not empty) a close button. Dart rebuilt the widget with the parent's new `text`; here the
 * parent calls {@link #setText(String)} instead.
 */
public class Pencarian extends LinearLayout {
    private final EditText field;
    private final FrameLayout searchButton;
    private final FrameLayout closeButton;
    private final float hb;

    public Pencarian(Context ctx, final Async.Done<String> onSearch, final Runnable onClose, String hint, String text) {
        super(ctx);
        hb = Ui.hb(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 50f, CustomColors.green, 1f));

        field = new EditText(ctx);
        field.setBackground(new ColorDrawable(Color.TRANSPARENT));
        field.setTypeface(Ui.font(ctx));
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        field.setHint(hint == null ? "Type Here" : hint);
        field.setHintTextColor(0xFF757575);
        field.setTextColor(0xFF000000);
        field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        field.setHintTextColor(0xFF757575);
        Ui.pad(field, 0, hb * 3.25f, 0, hb * 3.25f);
        field.setOnEditorActionListener((v, actionId, event) -> {
            if (onSearch != null) {
                onSearch.run(field.getText().toString());
            }
            return true;
        });
        LayoutParams flp = new LayoutParams(0, Ui.WRAP, 1f);
        flp.leftMargin = Ui.dp(ctx, hb * 5f);
        flp.rightMargin = Ui.dp(ctx, hb * 5f);
        addView(field, flp);

        searchButton = new FrameLayout(ctx);
        ImageView searchIcon = Ui.icon(ctx, R.drawable.ic_search, hb * 3.75f, CustomColors.green);
        searchButton.addView(searchIcon, new FrameLayout.LayoutParams(Ui.dp(ctx, hb * 3.75f), Ui.dp(ctx, hb * 3.75f), Gravity.CENTER));
        searchButton.setOnClickListener(v -> {
            if (onSearch != null) {
                onSearch.run(field.getText().toString());
            }
        });
        addView(searchButton, new LayoutParams(Ui.dp(ctx, hb * 12.5f), Ui.dp(ctx, 40)));

        closeButton = new FrameLayout(ctx);
        ImageView closeIcon = Ui.icon(ctx, R.drawable.ic_close, hb * 3.75f, CustomColors.textDanger);
        closeButton.addView(closeIcon, new FrameLayout.LayoutParams(Ui.dp(ctx, hb * 3.75f), Ui.dp(ctx, hb * 3.75f), Gravity.CENTER));
        closeButton.setOnClickListener(v -> {
            if (onClose != null) {
                onClose.run();
            }
        });
        addView(closeButton, new LayoutParams(Ui.dp(ctx, hb * 12.5f), Ui.dp(ctx, 40)));

        setText(text == null ? "" : text);
    }

    /** The Dart widget is rebuilt with the new `text`: set the field and update the icon widths. */
    public void setText(String text) {
        String t = text == null ? "" : text;
        field.setText(t);
        field.setSelection(t.length());
        boolean pencarianStringExist = t.length() >= 1;
        ViewGroupLp.width(searchButton, Ui.dp(getContext(), pencarianStringExist ? hb * 7.5f : hb * 12.5f));
        closeButton.setVisibility(pencarianStringExist ? VISIBLE : GONE);
    }

    private static final class ViewGroupLp {
        static void width(android.view.View v, int widthPx) {
            android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.width = widthPx;
            v.setLayoutParams(lp);
        }
    }
}
