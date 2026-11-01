package id.laukpauk.view.pembeli;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * Small view helpers shared by the auth screens (Login, Register, OTPRegistrasi, OTPVerifikasiUser,
 * KirimUlangOTP, LupaPassword): the pill shaped text field on a light-green background, the logo,
 * the centred scrolling page and the "link" text.
 */
public final class AuthUi {
    private AuthUi() {}

    /** Color(0xFFF9F9F9), the page background of the OTP / register / reset screens. */
    public static final int PAGE_BG = 0xFFF9F9F9;

    /** The logo `Image.asset('/laukpauk.png', width: MediaQuery.size.width - 100)`. */
    public static ImageView logo(Context ctx) {
        ImageView iv = Ui.asset(ctx, "laukpauk.png", Ui.hb(ctx) * 100f - 100f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(ctx, Ui.hb(ctx) * 100f - 100f), Ui.WRAP);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        iv.setLayoutParams(lp);
        iv.setAdjustViewBounds(true);
        return iv;
    }

    /** A TextField without border; `sizeSp` <= 0 keeps the default 16. */
    public static EditText input(Context ctx, String hint, int inputType, float sizeSp, float verticalPaddingDp) {
        EditText e = new EditText(ctx);
        e.setBackground(new ColorDrawable(Color.TRANSPARENT));
        e.setTypeface(Ui.font(ctx));
        e.setSingleLine(true);
        e.setInputType(inputType);
        e.setHint(hint);
        e.setHintTextColor(CustomColors.darkText);
        e.setTextColor(CustomColors.darkText);
        e.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sizeSp > 0 ? sizeSp : 16f);
        e.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        Ui.pad(e, 0, verticalPaddingDp, 0, verticalPaddingDp);
        return e;
    }

    public static int numberInput() {
        return InputType.TYPE_CLASS_NUMBER;
    }

    public static int textInput() {
        return InputType.TYPE_CLASS_TEXT;
    }

    public static int passwordInput() {
        return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
    }

    /** Container(padding: horizontal 30, color: lightGreen, radius 50, child: TextField + optional suffix). */
    public static LinearLayout pill(Context ctx, EditText edit, View suffix) {
        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(row, 30, 0, 30, 0);
        row.setBackground(Ui.shape(ctx, CustomColors.lightGreen, 50f));
        row.addView(edit, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        if (suffix != null) {
            row.addView(suffix, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        return row;
    }

    /** The eye suffix of a password field; tapping toggles the visibility of `edit`. */
    public static ImageView passwordToggle(Context ctx, final EditText edit) {
        final ImageView icon = Ui.icon(ctx, R.drawable.ic_visibility, Ui.hb(ctx) * 3.5f, CustomColors.darkText);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, Ui.hb(ctx) * 3.5f), Ui.dp(ctx, Ui.hb(ctx) * 3.5f)));
        final boolean[] shown = {false};
        edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        icon.setOnClickListener(v -> {
            shown[0] = !shown[0];
            int sel = edit.getSelectionEnd();
            edit.setTransformationMethod(shown[0] ? null : PasswordTransformationMethod.getInstance());
            icon.setImageResource(shown[0] ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
            if (sel >= 0) {
                edit.setSelection(sel);
            }
        });
        return icon;
    }

    /** A scrolling page whose column fills the viewport and centres its children. */
    public static ScrollView page(Context ctx, LinearLayout column, int background) {
        ScrollView sv = new ScrollView(ctx);
        sv.setFillViewport(true);
        sv.setBackgroundColor(background);
        column.setGravity(Gravity.CENTER);
        sv.addView(column, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        return sv;
    }

    /** Bold green link text (fontWeight w700, CustomColors.green). */
    public static TextView link(Context ctx, String text, final Runnable onTap) {
        TextView t = Ui.text(ctx, text, Ui.hb(ctx) * 3.5f, CustomColors.green, true);
        t.setOnClickListener(v -> onTap.run());
        return t;
    }

    /** A Column child with the given top gap and full-width pill, horizontally padded by `sidePaddingDp`. */
    public static void addPill(LinearLayout column, View pill, float sidePaddingDp) {
        Context ctx = column.getContext();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        lp.leftMargin = Ui.dp(ctx, sidePaddingDp);
        lp.rightMargin = Ui.dp(ctx, sidePaddingDp);
        column.addView(pill, lp);
    }

    /** SnackBar(duration, content: Text(text)). */
    public static void snack(BaseActivity activity, String text, int seconds, float sizeSp) {
        View anchor = activity.findViewById(android.R.id.content);
        Snackbar s = Snackbar.make(anchor, text, seconds * 1000);
        s.show();
    }

    /** Centered row of two links: "Belum punya akun? Registrasi " + link. */
    public static LinearLayout promptRow(Context ctx, String prompt, String linkText, Runnable onTap) {
        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.CENTER);
        row.addView(Ui.text(ctx, prompt, Ui.hb(ctx) * 3.5f, 0xFF000000));
        row.addView(Ui.spaceW(ctx, 2));
        row.addView(link(ctx, linkText, onTap));
        return row;
    }

    public static Txt.Style black(Context ctx, float blocks) {
        return new Txt.Style(Ui.hb(ctx) * blocks).color(0xFF000000);
    }
}
