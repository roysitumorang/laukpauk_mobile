package id.laukpauk.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import id.laukpauk.core.CustomColors;

/**
 * Port of lib/libraries/CustomText.dart: named text styles sized in "horizontal blocks"
 * (1 block = 1% of the screen width).
 *
 *   TextView t = Ui.text(ctx, "Halo", Txt.headline3(ctx).bold().color(CustomColors.green));
 *
 * Sizes are in SP. Default colour is black (Flutter's default text colour).
 */
public final class Txt {
    private Txt() {}

    public static final class Style {
        public float sizeSp;
        public int color = 0xFF000000;
        public boolean bold;
        public boolean italic;
        public boolean strike;
        public boolean underline;
        public float letterSpacingEm = 0f;

        public Style(float sizeSp) {
            this.sizeSp = sizeSp;
        }

        public Style size(float sp) {
            this.sizeSp = sp;
            return this;
        }

        public Style color(int argb) {
            this.color = argb;
            return this;
        }

        public Style bold() {
            this.bold = true;
            return this;
        }

        public Style bold(boolean b) {
            this.bold = b;
            return this;
        }

        public Style italic() {
            this.italic = true;
            return this;
        }

        public Style italic(boolean i) {
            this.italic = i;
            return this;
        }

        public Style strike() {
            this.strike = true;
            return this;
        }

        public Style underline() {
            this.underline = true;
            return this;
        }

        public Style letterSpacing(float em) {
            this.letterSpacingEm = em;
            return this;
        }

        public Style copy() {
            Style s = new Style(sizeSp);
            s.color = color;
            s.bold = bold;
            s.italic = italic;
            s.strike = strike;
            s.underline = underline;
            s.letterSpacingEm = letterSpacingEm;
            return s;
        }

        /** Applies font, size, colour and decorations to a TextView. */
        public void apply(TextView tv) {
            int face = Typeface.NORMAL;
            if (bold && italic) {
                face = Typeface.BOLD_ITALIC;
            } else if (bold) {
                face = Typeface.BOLD;
            } else if (italic) {
                face = Typeface.ITALIC;
            }
            tv.setTypeface(Ui.font(tv.getContext()), face);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
            tv.setTextColor(color);
            int flags = tv.getPaintFlags();
            flags = strike ? (flags | Paint.STRIKE_THRU_TEXT_FLAG) : (flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            flags = underline ? (flags | Paint.UNDERLINE_TEXT_FLAG) : (flags & ~Paint.UNDERLINE_TEXT_FLAG);
            tv.setPaintFlags(flags);
            if (letterSpacingEm != 0f) {
                tv.setLetterSpacing(letterSpacingEm);
            }
        }
    }

    public static Style headline1(Context c) { return new Style(Ui.hb(c) * 5.50f); }
    public static Style headline2(Context c) { return new Style(Ui.hb(c) * 5.0f); }
    public static Style headline3(Context c) { return new Style(Ui.hb(c) * 4.75f); }
    public static Style headline4(Context c) { return new Style(Ui.hb(c) * 4.50f); }
    public static Style headline5(Context c) { return new Style(Ui.hb(c) * 4.25f); }
    public static Style headline6(Context c) { return new Style(Ui.hb(c) * 4.0f); }
    public static Style contentSize(Context c) { return new Style(Ui.hb(c) * 3.25f); }

    /** Struck-through old price. */
    public static Style hargaCoret(Context c) {
        return new Style(Ui.hb(c) * 3.0f).color(CustomColors.textLightMuted).bold().strike();
    }

    /** Current price. */
    public static Style hargaBersih(Context c) {
        return new Style(Ui.hb(c) * 3.75f).color(CustomColors.green).bold();
    }

    public static Style textXS(Context c) { return new Style(Ui.hb(c) * 3.0f).color(CustomColors.nearlyBlack); }
    public static Style textSM(Context c) { return new Style(Ui.hb(c) * 3.75f); }
    public static Style textMD(Context c) { return new Style(Ui.hb(c) * 4.25f); }
    public static Style textLG(Context c) { return new Style(Ui.hb(c) * 5.5f); }
}
