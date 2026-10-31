package id.laukpauk.view.penjual;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.model.ShopModelUi;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Ui;

/**
 * Small Flutter-widget equivalents shared by the seller storefront / store dashboard / product management
 * screens: SnackBar, Material AlertDialog with an OK button, the pill-shaped TextField, TextButton,
 * DropdownButton and the "empty.png + message" placeholder.
 */
public final class PenjualUi {
    private PenjualUi() {}

    /** Dart: Colors.amber. */
    public static final int amber = 0xFFFFC107;

    /** ThemeData() default primary / accent colour (TextButton, Checkbox). */
    public static final int defaultBlue = 0xFF2196F3;

    /** SizeConfig.horizontalBlock * n, in dp / sp. */
    public static float hb(Context ctx, float n) {
        return Ui.hb(ctx) * n;
    }

    /** Text sized with SizeConfig.horizontalBlock * n. */
    public static TextView text(Context ctx, CharSequence s, float hbN, int color, boolean bold) {
        return Ui.text(ctx, s, hb(ctx, hbN), color, bold);
    }

    /** The default (unstyled) Flutter Text: 14sp, black87. */
    public static TextView plain(Context ctx, CharSequence s) {
        return Ui.text(ctx, s, 14f, 0xDD000000);
    }

    /** Runs blocking work off the main thread and delivers the result while the hosting screen is alive. */
    public static <T> void async(Context ctx, Async.Work<T> work, Async.Done<T> done) {
        PesananScreenUi.run(ctx, work, done);
    }

    // ------------------------------------------------------------------ LinearLayout.LayoutParams shortcuts

    public static LinearLayout.LayoutParams lp(Context ctx, int w, int h, float l, float t, float r, float b) {
        return PesananScreenUi.lp(ctx, w, h, l, t, r, b);
    }

    public static LinearLayout.LayoutParams wrapLp() {
        return new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
    }

    public static LinearLayout.LayoutParams matchWrapLp() {
        return new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
    }

    // ------------------------------------------------------------------ cards

    /** Card(elevation: 5, shape: RoundedRectangleBorder(circular(10))) with its 4dp margin. */
    public static LinearLayout card(Context ctx) {
        return PesananScreenUi.card(ctx);
    }

    /** Card + Padding(symmetric(horizontal, vertical)); the padded content column is returned via `inner`. */
    public static LinearLayout cardPadded(Context ctx, float horizontal, float vertical) {
        LinearLayout card = card(ctx);
        Ui.padXY(card, horizontal, vertical);
        return card;
    }

    // ------------------------------------------------------------------ snackbar

    /** ScaffoldMessenger.of(context).showSnackBar(SnackBar(duration: seconds, content: Text(text, fontSize: hb * hbN))). */
    public static void snack(Activity a, String text, int seconds, float hbN) {
        if (a == null || a.isFinishing() || a.isDestroyed()) {
            return;
        }
        View anchor = a.findViewById(android.R.id.content);
        if (anchor == null) {
            return;
        }
        Snackbar bar = Snackbar.make(anchor, text, seconds * 1000);
        styleTexts(bar.getView(), hb(a, hbN), a);
        bar.show();
    }

    /** SnackBar(content: Text(text), duration: 2s) with the default 14sp text. */
    public static void snackDefault(Activity a, String text, int seconds) {
        if (a == null || a.isFinishing() || a.isDestroyed()) {
            return;
        }
        View anchor = a.findViewById(android.R.id.content);
        if (anchor == null) {
            return;
        }
        Snackbar bar = Snackbar.make(anchor, text, seconds * 1000);
        styleTexts(bar.getView(), 14f, a);
        bar.show();
    }

    private static void styleTexts(View v, float sp, Activity a) {
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                styleTexts(g.getChildAt(i), sp, a);
            }
        } else if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setTypeface(Ui.font(a));
            if (!(v instanceof Button)) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
                tv.setMaxLines(4);
            }
        }
    }

    // ------------------------------------------------------------------ dialogs

    /** TextButton(child: Text(label)) used in AlertDialog actions (default blue text button). */
    public static TextView dialogButton(Activity a, String label, Runnable onTap) {
        return CustomAlert.textButton(a, label, onTap);
    }

    /**
     * showDialog(builder: AlertDialog(title: Text(title, fontSize: hb*titleHb [w700 when titleBold]),
     * content: Text(content, fontSize: hb*contentHb), actions: [TextButton('OK')])). `contentHb` &lt;= 0 means
     * the default 14sp text, `titleHb` &lt;= 0 the default 20sp title. `onOk` runs after the OK button closed the dialog (not when it was dismissed by tapping outside).
     */
    public static void alertOk(final Activity a, String title, String content, float titleHb, float contentHb,
                               final Runnable onOk) {
        final float titleSp = titleHb > 0 ? hb(a, titleHb) : 20f;
        final float contentSp = contentHb > 0 ? hb(a, contentHb) : 14f;
        final String t = title;
        final String c = content;
        Modal.<Boolean>dialog(a, true, handle -> {
            View actions = ShopModelUi.alertActions(a, dialogButton(a, "OK", () -> handle.close(Boolean.TRUE)));
            return ShopModelUi.alertDialogCard(a, t, c, titleSp, contentSp, actions);
        }, r -> {
            if (r != null && onOk != null) {
                onOk.run();
            }
        });
    }

    /** AlertDialog without actions (dismissed by tapping outside). */
    public static void alertPlain(final Activity a, String title, String content, float titleHb) {
        final float titleSp = titleHb > 0 ? hb(a, titleHb) : 20f;
        final String t = title;
        final String c = content;
        Modal.<Object>dialog(a, true, handle -> ShopModelUi.alertDialogCard(a, t, c, titleSp, 14f, null), null);
    }

    // ------------------------------------------------------------------ text fields

    /** Re-evaluates `onChange` after every edit of the field (the Dart screens rebuild on keyboard changes). */
    public static void watch(EditText e, final Runnable onChange) {
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onChange.run();
            }
        });
    }

    /**
     * Container(padding: horizontal 30, decoration: BoxDecoration(color: CustomColors.lightGreen, borderRadius: 50)) with
     * TextField(isDense, border none, contentPadding vertical = verticalBlock * vbN, hint in darkText, text darkText hb*3.5).
     */
    public static View pillField(Context ctx, EditText e, String hint, int inputType, float vbN) {
        FrameLayout box = new FrameLayout(ctx);
        box.setBackground(Ui.shape(ctx, CustomColors.lightGreen, 50f));
        Ui.pad(box, 30, 0, 30, 0);
        styleEdit(ctx, e, hint, inputType, hb(ctx, 3.5f), CustomColors.darkText, CustomColors.darkText);
        int v = Ui.dp(ctx, Ui.vb(ctx) * vbN);
        e.setPadding(0, v, 0, v);
        box.addView(e, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        return box;
    }

    /** Borderless single-style EditText. */
    public static void styleEdit(Context ctx, EditText e, String hint, int inputType, float textSp, int textColor,
                                 int hintColor) {
        e.setBackground(null);
        e.setHint(hint);
        e.setHintTextColor(hintColor);
        e.setTextColor(textColor);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp);
        e.setTypeface(Ui.font(ctx));
        e.setInputType(inputType);
        e.setSingleLine((inputType & android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        e.setIncludeFontPadding(false);
        e.setHighlightColor(CustomColors.lightGreen);
        e.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
    }

    /** Row(mainAxisAlignment: spaceAround): equal slots, every child centred in its slot. */
    public static LinearLayout spaceAround(Context ctx, View... children) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (View c : children) {
            FrameLayout slot = new FrameLayout(ctx);
            if (c.getParent() instanceof ViewGroup) {
                ((ViewGroup) c.getParent()).removeView(c);
            }
            slot.addView(c, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER_HORIZONTAL));
            row.addView(slot, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        }
        return row;
    }

    // ------------------------------------------------------------------ text button

    /** TextButton(child: Text(label, style: ...)) - 8dp horizontal padding, ripple. */
    public static TextView textButton(Context ctx, String label, float sp, int color, boolean bold,
                                      final Runnable onTap) {
        TextView tv = Ui.text(ctx, label, sp, color, bold);
        tv.setGravity(Gravity.CENTER);
        Ui.padXY(tv, 8, 8);
        tv.setMinHeight(Ui.dp(ctx, 36));
        Layouts.ripple(tv, 0x22000000, 4f);
        Ui.onTap(tv, onTap);
        return tv;
    }

    // ------------------------------------------------------------------ placeholders

    /** Column(center) { Image.asset(assets/img/empty.png, width: hb*imageHb), SizedBox(gap), Text(...) }. */
    public static LinearLayout emptyState(Context ctx, float imageHb, float gapDp, CharSequence message, float textHb,
                                          int color) {
        LinearLayout col = Ui.column(ctx);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(Ui.dp(ctx, hb(ctx, imageHb)), Ui.WRAP);
        col.addView(Ui.asset(ctx, "empty.png", hb(ctx, imageHb)), imgLp);
        col.addView(Ui.spaceH(ctx, gapDp));
        TextView tv = text(ctx, message, textHb, color, false);
        tv.setGravity(Gravity.CENTER);
        col.addView(tv, wrapLp());
        return col;
    }

    /** Center(child: SizedBox(width: size, height: size, child: CircularProgressIndicator(strokeWidth: 1.5))). */
    public static View spinner(Context ctx, float sizeDp) {
        return id.laukpauk.view.pembeli.DetailUi.smallSpinner(ctx, sizeDp);
    }

    /** Padding(vertical) { Center(spinner) } . */
    public static View paddedSpinner(Context ctx, float vertical, float sizeDp) {
        FrameLayout box = new FrameLayout(ctx);
        Ui.padXY(box, 0, vertical);
        box.addView(spinner(ctx, sizeDp), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, sizeDp)));
        return box;
    }

    // ------------------------------------------------------------------ dropdown

    /**
     * DropdownButton&lt;String&gt;(isExpanded, isDense, underline hidden) over (value, label) pairs. `onChanged`
     * is called only when the user picks an entry.
     */
    public static View dropdown(final Context ctx, final List<String> values, final List<String> labels,
                                String selected, final float textSp, final Async.Done<String> onChanged) {
        final Spinner spinner = new Spinner(ctx);
        spinner.setBackground(null);
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return values.size();
            }

            @Override
            public Object getItem(int position) {
                return values.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = Ui.text(ctx, labels.get(position), textSp, 0xDD000000);
                Ui.padXY(tv, 0, 6);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = Ui.text(ctx, labels.get(position), textSp, 0xDD000000);
                Ui.padXY(tv, 16, 12);
                return tv;
            }
        };
        spinner.setAdapter(adapter);
        int index = Math.max(0, values.indexOf(selected));
        spinner.setSelection(index, false);
        final int[] current = {index};
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == current[0]) {
                    return;
                }
                current[0] = position;
                if (onChanged != null) {
                    onChanged.run(values.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    /** A ScrollView that keeps its touches when it sits inside another vertical scroller (fixed-height grids). */
    @android.annotation.SuppressLint("ClickableViewAccessibility")
    public static android.widget.ScrollView innerScroll(Context ctx) {
        android.widget.ScrollView s = new android.widget.ScrollView(ctx);
        s.setOnTouchListener((v, event) -> {
            if (v.getParent() != null) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });
        return s;
    }

    // ------------------------------------------------------------------ tint

    public static ColorStateList tint(int color) {
        return ColorStateList.valueOf(color);
    }

    public static int transparent() {
        return Color.TRANSPARENT;
    }

    /** BaseActivity helper: the hosting Activity of a component. */
    public static BaseActivity activity(Context ctx) {
        return (BaseActivity) ctx;
    }
}
