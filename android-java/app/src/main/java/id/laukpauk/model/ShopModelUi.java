package id.laukpauk.model;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.KeranjangActivity;

/**
 * Small helpers shared by the shop / seller models (this package only; the Dart models had
 * these inline).
 *
 *  - {@link #keranjangSnackbar}: the "... / Lihat Keranjang" SnackBar of ProdukModel and PaketModel.
 *  - {@link #alertDialogCard}: Flutter's Material AlertDialog (title + content + actions).
 *  - {@link #form}: Map&lt;String, dynamic&gt; request body -&gt; Map&lt;String, String&gt;.
 */
public final class ShopModelUi {
    private ShopModelUi() {}

    /** Dart `false` returned from a `Future&lt;dynamic&gt;` (the models answer false on failure). */
    public static final JsonElement FALSE = new JsonPrimitive(Boolean.FALSE);

    /** Dart `dynamic` that may be null: Java null becomes JSON null (never a Java null). */
    public static JsonElement dyn(JsonElement e) {
        return e == null ? JsonNull.INSTANCE : e;
    }

    /**
     * Dart:
     * <pre>
     *   SnackBar(duration: Duration(seconds: n),
     *            action: SnackBarAction(label: 'Lihat Keranjang', textColor: CustomColors.green,
     *                                   onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => Keranjang()))),
     *            content: Text(text, style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)));
     *   ScaffoldMessenger.of(context).showSnackBar(snackBar);
     * </pre>
     * Shows the snack bar right away (main thread) and returns it.
     */
    public static Snackbar keranjangSnackbar(final BaseActivity activity, String text, int seconds) {
        View anchor = activity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(anchor, text, seconds * 1000);
        snackbar.setActionTextColor(CustomColors.green);
        snackbar.setAction("Lihat Keranjang", v -> Nav.push(activity, KeranjangActivity.class, null));
        styleSnackbarTexts(snackbar.getView(), Ui.hb(activity) * 3.25f, activity);
        snackbar.show();
        return snackbar;
    }

    private static void styleSnackbarTexts(View v, float messageSizeSp, Activity a) {
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                styleSnackbarTexts(g.getChildAt(i), messageSizeSp, a);
            }
        } else if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setTypeface(Ui.font(a));
            if (!(v instanceof Button)) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageSizeSp);
                tv.setMaxLines(4);
            }
        }
    }

    /**
     * Flutter's Material {@code AlertDialog(title: Text(title, style: TextStyle(fontSize: titleSp)),
     * content: Text(content, style: TextStyle(fontSize: contentSp)), actions: actions)}.
     * Pass the result to Modal.dialog; {@code actions} (a row of text buttons) may be null.
     */
    public static View alertDialogCard(Activity a, String title, String content, float titleSp,
                                       float contentSp, View actions) {
        LinearLayout box = Ui.column(a);
        box.setBackground(Ui.shape(a, CustomColors.nearlyWhite, 4f));

        TextView titleView = Ui.text(a, title, titleSp, 0xDD000000);
        Ui.pad(titleView, 24, 24, 24, 0);
        box.addView(titleView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        TextView contentView = Ui.text(a, content, contentSp, 0xDD000000);
        Ui.pad(contentView, 24, 20, 24, 24);
        box.addView(contentView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (actions != null) {
            if (actions.getParent() instanceof ViewGroup) {
                ((ViewGroup) actions.getParent()).removeView(actions);
            }
            box.addView(actions, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        return box;
    }

    /**
     * Wraps a bottom sheet's content like showModalBottomSheet(shape: RoundedRectangleBorder(top-left /
     * top-right radius 15)): white background with rounded top corners.
     */
    public static View roundedSheet(Activity a, View content) {
        LinearLayout holder = Ui.column(a);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CustomColors.nearlyWhite);
        float r = Ui.dp(a, 15);
        bg.setCornerRadii(new float[]{r, r, r, r, 0f, 0f, 0f, 0f});
        holder.setBackground(bg);
        holder.addView(content, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        return holder;
    }

    /** The ButtonBar of an AlertDialog: buttons packed to the end, 8 dp around. */
    public static LinearLayout alertActions(Activity a, View... buttons) {
        LinearLayout row = Ui.row(a);
        row.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        Ui.pad(row, 8, 8, 8, 8);
        for (View b : buttons) {
            if (b.getParent() instanceof ViewGroup) {
                ((ViewGroup) b.getParent()).removeView(b);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.leftMargin = Ui.dp(a, 4);
            lp.rightMargin = Ui.dp(a, 4);
            row.addView(b, lp);
        }
        return row;
    }

    /**
     * Dart `HTTPRequest.postBody(body: map)` takes a Map&lt;String, dynamic&gt;: convert a JSON
     * object into the String-to-String form map (insertion order kept).
     */
    public static Map<String, String> form(JsonObject o) {
        Map<String, String> m = new LinkedHashMap<>();
        if (o != null) {
            for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                JsonElement v = e.getValue();
                m.put(e.getKey(), (v != null && (v.isJsonObject() || v.isJsonArray()))
                        ? v.toString() : Json.asStr(v, ""));
            }
        }
        return m;
    }

    /** Percent-encodes one URL PATH segment (Http.enc turns spaces into '+', which a path does not decode). */
    public static String pathEnc(String s) {
        return Http.enc(s).replace("+", "%20");
    }
}
