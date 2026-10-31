package id.laukpauk.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;

/**
 * Port of lib/global-components/CustomForm.dart (static form-field builders).
 *
 * TextEditingController -> a plain {@link EditText} that the SCREEN owns (create it once with
 * {@link #controller(Context)} and keep it in a field, like the Dart State kept its controllers).
 * Every call of {@link #textField} restyles that EditText and moves it into a fresh container, so it
 * is safe to call it again on every render(): the text typed so far is kept. Read the text with
 * {@code controller.getText().toString()}, set it with {@code controller.setText(..)}. Passing
 * {@code null} as controller creates a new one (reachable with {@link TextFieldView#input()}).
 * Dart FocusNode -> the same EditText (requestFocus() / clearFocus()); there is no separate parameter.
 *
 * Dart named parameters -> either the full positional overload (all Dart parameters in declaration
 * order, nullable ones boxed) or the short overload with a fluent options object:
 *
 *   CustomForm.label(ctx, "Nama Produk");
 *   CustomForm.textField(ctx, "Contoh : Daun Bawang", namaProdukController,
 *           new CustomForm.TextFieldOpts().borderColor(c).borderWidth(w));
 *   CustomForm.selectBox(ctx, items, selected, v -> {...}, "Pilih Jenis Produk", "",
 *           new CustomForm.SelectBoxOpts().borderColor(c).borderWidth(w));
 *
 * Dart's `Color` -> ARGB int, `TextInputType` -> one of the KEYBOARD_* constants (android InputType),
 * `TextInputFormatter` list -> {@code InputFilter[]}, `TextStyle` -> {@link Txt.Style}.
 */
public final class CustomForm {
    private CustomForm() {}

    // ------------------------------------------------------------------ TextInputType

    public static final int KEYBOARD_TEXT = InputType.TYPE_CLASS_TEXT;
    public static final int KEYBOARD_MULTILINE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
    public static final int KEYBOARD_NUMBER = InputType.TYPE_CLASS_NUMBER;
    public static final int KEYBOARD_PHONE = InputType.TYPE_CLASS_PHONE;
    public static final int KEYBOARD_DATETIME = InputType.TYPE_CLASS_DATETIME;
    public static final int KEYBOARD_EMAIL = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    public static final int KEYBOARD_URL = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
    public static final int KEYBOARD_NAME = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME;

    // Flutter (Material 2, light theme) defaults that the Dart widgets inherited.
    private static final int TEXT_COLOR = 0xDD000000;      // textTheme.subtitle1 (black87)
    private static final int DISABLED_COLOR = 0x61000000;  // disabledColor (black38)
    private static final int HINT_COLOR = 0x99000000;      // hintColor
    private static final int PRIMARY_COLOR = 0xFF2196F3;   // focused floating label
    private static final int ICON_COLOR = 0xDD000000;      // IconTheme default
    private static final int DROPDOWN_ICON_COLOR = 0xFF616161; // grey.shade700
    private static final float DEFAULT_TEXT_SP = 16f;      // subtitle1

    /** SelectBoxItem(value, text) - the same class as the top-level {@code ui.SelectBoxItem}. */
    public static final class SelectBoxItem extends id.laukpauk.ui.SelectBoxItem {
        public SelectBoxItem(Object value, String text) {
            super(value, text);
        }
    }

    // ------------------------------------------------------------------ controller

    /** Dart: TextEditingController(). */
    public static EditText controller(Context ctx) {
        return new EditText(ctx);
    }

    /** Dart: TextEditingController(text: text). */
    public static EditText controller(Context ctx, String text) {
        EditText e = new EditText(ctx);
        if (text != null) {
            e.setText(text);
        }
        return e;
    }

    // ------------------------------------------------------------------ label

    /** Dart: CustomForm.label(context, label: label). */
    public static View label(Context ctx, String label) {
        return label(ctx, label, null, null, null, null, null);
    }

    /**
     * Dart: CustomForm.label(context, label:, letterSpacing:, fontSize:, bottomPadding:, color:, fontWeight:).
     * Defaults when null: letterSpacing 1.5, fontSize horizontalBlock * 3.25, bottomPadding 10,
     * color CustomColors.green, fontWeight w700. {@code fontWeight} is the Dart weight number
     * (100..900; >= 600 renders bold).
     */
    public static View label(Context ctx, String label, Double letterSpacing, Double fontSize,
                             Double bottomPadding, Integer color, Integer fontWeight) {
        float hb = Ui.hb(ctx);
        float ls = letterSpacing != null ? letterSpacing.floatValue() : 1.5f;
        float fs = fontSize != null ? fontSize.floatValue() : hb * 3.25f;
        float bp = bottomPadding != null ? bottomPadding.floatValue() : 10f;
        int c = color != null ? color : CustomColors.green;
        int fw = fontWeight != null ? fontWeight : 700;

        Txt.Style style = new Txt.Style(fs).color(c).bold(fw >= 600).letterSpacing(fs > 0 ? ls / fs : 0f);
        TextView tv = Ui.text(ctx, (label == null ? "" : label).toUpperCase(Locale.ROOT), style);

        LinearLayout box = Ui.column(ctx);
        box.setPadding(0, 0, 0, Ui.dp(ctx, bp));
        box.addView(tv, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        return box;
    }

    // ------------------------------------------------------------------ readOnlyTextField

    /** Dart: CustomForm.readOnlyTextField(context, value: value). */
    public static View readOnlyTextField(Context ctx, Object value) {
        return readOnlyTextField(ctx, value, null, 0.0, null, 1.0, null, false);
    }

    /**
     * Dart: CustomForm.readOnlyTextField(context, value:, leading:, leadingAndHintTextSpacing:,
     * borderColor:, borderWidth:, trailing:, obsecureText:).
     */
    public static View readOnlyTextField(Context ctx, Object value, View leading,
                                         double leadingAndHintTextSpacing, Integer borderColor,
                                         double borderWidth, View trailing, boolean obsecureText) {
        float hb = Ui.hb(ctx);
        LinearLayout box = Ui.row(ctx);
        box.setBackground(shapeBox(ctx, CustomColors.grey, borderColor, (float) borderWidth));
        Ui.pad(box, hb * 3.75f, hb * 3.25f, hb * 3.75f, hb * 3.25f);

        addDetached(box, leading);
        if (leadingAndHintTextSpacing > 0) {
            box.addView(Ui.spaceW(ctx, (float) leadingAndHintTextSpacing));
        }

        EditText input = new EditText(ctx);
        input.setText(String.valueOf(value));
        styleInput(ctx, input, DEFAULT_TEXT_SP, KEYBOARD_TEXT, obsecureText, true, null, null);
        input.setKeyListener(null); // readOnly: true (still focusable / selectable)
        box.addView(input, Ui.weightW(1f));

        addDetached(box, trailing);
        return box;
    }

    // ------------------------------------------------------------------ textField

    /** The container returned by {@link #textField}: gives access to the EditText. */
    public static final class TextFieldView extends LinearLayout {
        private final EditText input;

        TextFieldView(Context ctx, EditText input) {
            super(ctx);
            this.input = input;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
        }

        /** The controller (the EditText that was passed in, or the one created for a null controller). */
        public EditText input() {
            return input;
        }

        public String getText() {
            return input.getText().toString();
        }

        public void setText(String text) {
            input.setText(text == null ? "" : text);
        }
    }

    /** Named parameters of CustomForm.textField (every one optional; Dart defaults are the field defaults). */
    public static final class TextFieldOpts {
        Txt.Style hintStyle;
        View leading;
        double leadingAndHintTextSpacing = 0.0;
        int keyboardType = KEYBOARD_TEXT;
        boolean obsecureText = false;
        View trailing;
        Integer borderColor;
        double borderWidth = 1.0;
        boolean enabled = true;
        Integer maxLength;
        String counterText = "";
        Runnable onEditingComplete;
        Async.Done<String> onChange;
        InputFilter[] inputFormatters;
        Integer backgroundColor;
        boolean useLabel = false;

        public TextFieldOpts hintStyle(Txt.Style v) {
            hintStyle = v;
            return this;
        }

        public TextFieldOpts leading(View v) {
            leading = v;
            return this;
        }

        public TextFieldOpts leadingAndHintTextSpacing(double v) {
            leadingAndHintTextSpacing = v;
            return this;
        }

        /** One of the KEYBOARD_* constants. */
        public TextFieldOpts keyboardType(int v) {
            keyboardType = v;
            return this;
        }

        /** Dart's (misspelled) parameter name. */
        public TextFieldOpts obsecureText(boolean v) {
            obsecureText = v;
            return this;
        }

        public TextFieldOpts trailing(View v) {
            trailing = v;
            return this;
        }

        public TextFieldOpts borderColor(Integer v) {
            borderColor = v;
            return this;
        }

        public TextFieldOpts borderWidth(double v) {
            borderWidth = v;
            return this;
        }

        public TextFieldOpts enabled(boolean v) {
            enabled = v;
            return this;
        }

        public TextFieldOpts maxLength(Integer v) {
            maxLength = v;
            return this;
        }

        public TextFieldOpts counterText(String v) {
            counterText = v;
            return this;
        }

        public TextFieldOpts onEditingComplete(Runnable v) {
            onEditingComplete = v;
            return this;
        }

        /** Dart: onChange (called with the new text on every user edit). */
        public TextFieldOpts onChange(Async.Done<String> v) {
            onChange = v;
            return this;
        }

        public TextFieldOpts inputFormatters(InputFilter... v) {
            inputFormatters = v;
            return this;
        }

        public TextFieldOpts backgroundColor(Integer v) {
            backgroundColor = v;
            return this;
        }

        public TextFieldOpts useLabel(boolean v) {
            useLabel = v;
            return this;
        }
    }

    /** Dart: CustomForm.textField(context, hintText: hintText, controller: controller). */
    public static TextFieldView textField(Context ctx, String hintText, EditText controller) {
        return textField(ctx, hintText, controller, new TextFieldOpts());
    }

    public static TextFieldView textField(Context ctx, String hintText, EditText controller,
                                          TextFieldOpts o) {
        if (o == null) {
            o = new TextFieldOpts();
        }
        return textField(ctx, hintText, controller, o.hintStyle, o.leading, o.leadingAndHintTextSpacing,
                o.keyboardType, o.obsecureText, o.trailing, o.borderColor, o.borderWidth, o.enabled,
                o.maxLength, o.counterText, o.onEditingComplete, o.onChange, o.inputFormatters,
                o.backgroundColor, o.useLabel);
    }

    /**
     * Dart: CustomForm.textField(context, hintText:, controller:, hintStyle:, leading:,
     * leadingAndHintTextSpacing:, keyboardType:, obsecureText:, trailing:, borderColor:, borderWidth:,
     * enabled:, maxLength:, counterText:, onEditingComplete:, onChange:, inputFormatters:, focusNode:,
     * backgroundColor:, useLabel:)  - without focusNode (see class comment).
     */
    public static TextFieldView textField(Context ctx, String hintText, EditText controller,
                                          Txt.Style hintStyle, View leading,
                                          double leadingAndHintTextSpacing, int keyboardType,
                                          boolean obsecureText, View trailing, Integer borderColor,
                                          double borderWidth, boolean enabled, Integer maxLength,
                                          String counterText, Runnable onEditingComplete,
                                          Async.Done<String> onChange, InputFilter[] inputFormatters,
                                          Integer backgroundColor, boolean useLabel) {
        final float hb = Ui.hb(ctx);
        final String hint = hintText == null ? "" : hintText;
        final EditText input = controller != null ? controller : new EditText(ctx);

        float hintSize = hintStyle != null ? hintStyle.sizeSp : hb * 3.5f;
        int hintColor = (hintStyle != null && hintStyle.color != 0xFF000000) ? hintStyle.color : HINT_COLOR;

        final TextFieldView box = new TextFieldView(ctx, input);
        int fill = enabled
                ? (backgroundColor != null ? backgroundColor : CustomColors.nearlyWhite)
                : CustomColors.grey;
        box.setBackground(shapeBox(ctx, fill, borderColor, (float) borderWidth));
        Ui.pad(box, hb * 3.75f, hb * 3.25f, hb * 3.75f, hb * 3.25f);

        addDetached(box, leading);
        if (leadingAndHintTextSpacing > 0) {
            box.addView(Ui.spaceW(ctx, (float) leadingAndHintTextSpacing));
        }

        styleInput(ctx, input, hb * 3.5f, keyboardType, obsecureText, enabled, maxLength, inputFormatters);
        input.setHintTextColor(hintColor);

        LinearLayout column = Ui.column(ctx);
        final TextView floating;
        if (useLabel) {
            // InputDecoration(labelText: hintText, labelStyle: hintStyle): the label sits in the input
            // line while empty + unfocused (shown as the hint) and floats above it otherwise.
            floating = Ui.text(ctx, hint, hintSize * 0.75f, hintColor);
            floating.setSingleLine(true);
            column.addView(floating, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            floating = null;
        }
        column.addView(input, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        final CharSequence hintText2 = sizedHint(ctx, hint, hintSize, hb * 3.5f);
        final int labelColor = hintColor;
        final boolean isEnabled = enabled;
        final Runnable refreshLabel = new Runnable() {
            @Override
            public void run() {
                if (floating == null) {
                    return;
                }
                boolean up = input.hasFocus() || input.length() > 0;
                floating.setVisibility(up ? View.VISIBLE : View.INVISIBLE);
                floating.setTextColor(input.hasFocus() && isEnabled ? PRIMARY_COLOR : labelColor);
                input.setHint(up ? "" : hintText2);
            }
        };
        if (useLabel) {
            input.setHint(hintText2);
            input.setOnFocusChangeListener((v, hasFocus) -> refreshLabel.run());
            refreshLabel.run();
        } else {
            input.setHint(hintText2);
        }

        if (counterText != null && !counterText.isEmpty()) {
            TextView counter = Ui.text(ctx, counterText, 12f, HINT_COLOR);
            counter.setGravity(Gravity.END);
            column.addView(counter, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        box.addView(column, Ui.weightW(1f));

        Binding binding = bindingOf(input);
        binding.onText = useLabel ? refreshLabel : null;
        binding.onChange = onChange;

        if (onEditingComplete != null) {
            final Runnable complete = onEditingComplete;
            input.setOnEditorActionListener((v, actionId, event) -> {
                complete.run();
                return true;
            });
        } else {
            input.setOnEditorActionListener(null);
        }

        addDetached(box, trailing);
        return box;
    }

    // ------------------------------------------------------------------ selectBox

    /** Named parameters of CustomForm.selectBox other than the required five. */
    public static final class SelectBoxOpts {
        View leading;
        double leadingAndHintTextSpacing = 0.0;
        int keyboardType = KEYBOARD_TEXT;
        boolean obsecureText = false;
        View trailing;
        Integer borderColor;
        double borderWidth = 1.0;
        boolean enabled = true;
        Txt.Style itemTextStyle;

        public SelectBoxOpts leading(View v) {
            leading = v;
            return this;
        }

        public SelectBoxOpts leadingAndHintTextSpacing(double v) {
            leadingAndHintTextSpacing = v;
            return this;
        }

        public SelectBoxOpts keyboardType(int v) {
            keyboardType = v;
            return this;
        }

        public SelectBoxOpts obsecureText(boolean v) {
            obsecureText = v;
            return this;
        }

        public SelectBoxOpts trailing(View v) {
            trailing = v;
            return this;
        }

        public SelectBoxOpts borderColor(Integer v) {
            borderColor = v;
            return this;
        }

        public SelectBoxOpts borderWidth(double v) {
            borderWidth = v;
            return this;
        }

        public SelectBoxOpts enabled(boolean v) {
            enabled = v;
            return this;
        }

        public SelectBoxOpts itemTextStyle(Txt.Style v) {
            itemTextStyle = v;
            return this;
        }
    }

    private static final class Entry {
        final Object value;
        final String text;

        Entry(Object value, String text) {
            this.value = value;
            this.text = text;
        }
    }

    /** The drop-down returned by {@link #selectBox}. */
    public static final class SelectBoxView extends LinearLayout {
        private Object value;
        private TextView shown;
        private List<Entry> entries = new ArrayList<>();

        SelectBoxView(Context ctx) {
            super(ctx);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
        }

        public Object getValue() {
            return value;
        }

        /** Selects the entry whose value equals {@code v} (updates the displayed text). */
        public void setValue(Object v) {
            value = v;
            if (shown != null) {
                shown.setText(textFor(entries, v));
            }
        }
    }

    /** Dart: CustomForm.selectBox(context, items:, value:, onChanged:, defaultItemText:, defaultItemValue:). */
    public static SelectBoxView selectBox(Context ctx,
                                          List<? extends id.laukpauk.ui.SelectBoxItem> items,
                                          Object value, Async.Done<Object> onChanged,
                                          String defaultItemText, Object defaultItemValue) {
        return selectBox(ctx, items, value, onChanged, defaultItemText, defaultItemValue,
                new SelectBoxOpts());
    }

    public static SelectBoxView selectBox(Context ctx,
                                          List<? extends id.laukpauk.ui.SelectBoxItem> items,
                                          Object value, Async.Done<Object> onChanged,
                                          String defaultItemText, Object defaultItemValue,
                                          SelectBoxOpts o) {
        if (o == null) {
            o = new SelectBoxOpts();
        }
        return selectBox(ctx, items, value, onChanged, defaultItemText, defaultItemValue, o.leading,
                o.leadingAndHintTextSpacing, o.keyboardType, o.obsecureText, o.trailing, o.borderColor,
                o.borderWidth, o.enabled, o.itemTextStyle);
    }

    /**
     * Dart: CustomForm.selectBox(context, items:, value:, onChanged:, defaultItemText:, defaultItemValue:,
     * leading:, leadingAndHintTextSpacing:, keyboardType:, obsecureText:, trailing:, borderColor:,
     * borderWidth:, enabled:, itemTextStyle:).  `onChanged` receives the selected item's value.
     * A disabled select box shows {@code value.toString()} in a read-only field with a drop-down
     * arrow (the Dart code overwrites `trailing` in that case).
     */
    public static SelectBoxView selectBox(final Context ctx,
                                          List<? extends id.laukpauk.ui.SelectBoxItem> items,
                                          Object value, final Async.Done<Object> onChanged,
                                          String defaultItemText, Object defaultItemValue,
                                          View leading, double leadingAndHintTextSpacing,
                                          int keyboardType, boolean obsecureText, View trailing,
                                          Integer borderColor, double borderWidth, boolean enabled,
                                          Txt.Style itemTextStyle) {
        float hb = Ui.hb(ctx);
        final SelectBoxView box = new SelectBoxView(ctx);
        box.value = value;
        box.setBackground(shapeBox(ctx, enabled ? CustomColors.nearlyWhite : CustomColors.grey,
                borderColor, (float) borderWidth));
        Ui.pad(box, hb * 3.75f, hb * 3.0f, hb * 3.75f, hb * 3.0f);

        if (!enabled) {
            // khusus untuk selectBox yang disabled, nilai trailing ditimpa
            trailing = Ui.icon(ctx, R.drawable.ic_arrow_drop_down, 24, ICON_COLOR);
        }

        final Txt.Style itemStyle = itemTextStyle != null ? itemTextStyle : new Txt.Style(hb * 3.5f);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(defaultItemValue, defaultItemText));
        if (items != null && items.size() >= 1) {
            for (id.laukpauk.ui.SelectBoxItem item : items) {
                entries.add(new Entry(item.value, item.text));
            }
        }
        box.entries = entries;

        addDetached(box, leading);
        if (leadingAndHintTextSpacing > 0) {
            box.addView(Ui.spaceW(ctx, (float) leadingAndHintTextSpacing));
        }

        if (enabled) {
            final LinearLayout field = Ui.row(ctx);
            final TextView shown = Ui.text(ctx, textFor(entries, value),
                    new Txt.Style(itemStyle.sizeSp).color(TEXT_COLOR).bold(itemStyle.bold)
                            .italic(itemStyle.italic));
            shown.setSingleLine(true);
            shown.setEllipsize(TextUtils.TruncateAt.END);
            box.shown = shown;
            field.addView(shown, Ui.weightW(1f));
            field.addView(Ui.icon(ctx, R.drawable.ic_arrow_drop_down, 24, DROPDOWN_ICON_COLOR));
            field.setClickable(true);
            final List<Entry> entryList = entries;
            field.setOnClickListener(v -> showMenu(ctx, box, entryList, itemStyle, box.value, picked -> {
                box.setValue(picked);
                if (onChanged != null) {
                    onChanged.run(picked);
                }
            }));
            box.addView(field, Ui.weightW(1f));
        } else {
            EditText disabled = new EditText(ctx);
            disabled.setText(String.valueOf(value));
            styleInput(ctx, disabled, DEFAULT_TEXT_SP, keyboardType, obsecureText, false, null, null);
            box.addView(disabled, Ui.weightW(1f));
        }

        addDetached(box, trailing);
        return box;
    }

    // ------------------------------------------------------------------ internals

    private static GradientDrawable shapeBox(Context ctx, int fill, Integer borderColor, float borderWidth) {
        GradientDrawable g = Ui.shape(ctx, fill, 8f);
        if (borderColor != null) {
            g.setStroke(Math.max(1, Ui.dp(ctx, borderWidth)), borderColor);
        }
        return g;
    }

    /** Adds `child` (if any) to a horizontal box after detaching it from a previous parent. */
    private static void addDetached(LinearLayout box, View child) {
        if (child == null) {
            return;
        }
        if (child.getParent() instanceof ViewGroup) {
            ((ViewGroup) child.getParent()).removeView(child);
        }
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            box.addView(child, lp);
        } else {
            box.addView(child, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
    }

    /** TextField style: borderless, no padding, 16sp-ish text, input type, length + formatters. */
    private static void styleInput(Context ctx, EditText e, float sizeSp, int keyboardType,
                                   boolean obscure, boolean enabled, Integer maxLength,
                                   InputFilter[] formatters) {
        if (e.getParent() instanceof ViewGroup) {
            ((ViewGroup) e.getParent()).removeView(e);
        }
        e.setBackground(null);
        e.setPadding(0, 0, 0, 0);
        e.setIncludeFontPadding(true);
        e.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        int type = keyboardType;
        if (obscure && (type & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
            type |= InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        }
        e.setInputType(type);
        e.setSingleLine(true);
        e.setTransformationMethod(obscure
                ? PasswordTransformationMethod.getInstance()
                : SingleLineTransformationMethod.getInstance());
        e.setImeOptions(EditorInfo.IME_ACTION_DONE);

        e.setTypeface(Ui.font(ctx));
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        e.setTextColor(enabled ? TEXT_COLOR : DISABLED_COLOR);
        e.setHintTextColor(HINT_COLOR);
        e.setEnabled(enabled);

        List<InputFilter> filters = new ArrayList<>();
        if (maxLength != null) {
            filters.add(new InputFilter.LengthFilter(maxLength)); // MaxLengthEnforcement.enforced
        }
        if (formatters != null) {
            for (InputFilter f : formatters) {
                if (f != null) {
                    filters.add(f);
                }
            }
        }
        e.setFilters(filters.toArray(new InputFilter[0]));
    }

    private static CharSequence sizedHint(Context ctx, String hint, float hintSizeSp, float textSizeSp) {
        if (hint.isEmpty() || Math.abs(hintSizeSp - textSizeSp) < 0.01f) {
            return hint;
        }
        int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, hintSizeSp,
                ctx.getResources().getDisplayMetrics()));
        SpannableString s = new SpannableString(hint);
        s.setSpan(new AbsoluteSizeSpan(px, false), 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    /**
     * One TextWatcher per EditText (kept in its tag): Dart's `onChanged` fires only for edits made by
     * the user, never for `controller.text = ...`, so the callback is skipped while the field has no focus.
     */
    private static final class Binding implements TextWatcher {
        final EditText owner;
        Async.Done<String> onChange;
        Runnable onText;

        Binding(EditText owner) {
            this.owner = owner;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // not needed
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (onText != null) {
                onText.run();
            }
            if (onChange != null && owner.hasFocus()) {
                onChange.run(s.toString());
            }
        }
    }

    private static Binding bindingOf(EditText e) {
        Object tag = e.getTag();
        if (tag instanceof Binding) {
            return (Binding) tag;
        }
        Binding b = new Binding(e);
        e.addTextChangedListener(b);
        if (tag == null) {
            e.setTag(b);
        }
        return b;
    }

    private static boolean sameValue(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.equals(b) || String.valueOf(a).equals(String.valueOf(b));
    }

    private static String textFor(List<Entry> entries, Object value) {
        for (Entry en : entries) {
            if (sameValue(en.value, value)) {
                return en.text == null ? "" : en.text;
            }
        }
        // DropdownButton would assert here; fall back to the default item.
        return entries.isEmpty() || entries.get(0).text == null ? "" : entries.get(0).text;
    }

    /** The DropdownButton menu: one 48dp row per entry, as wide as the select box. */
    private static void showMenu(final Context ctx, final View anchor, final List<Entry> entries,
                                 final Txt.Style itemStyle, Object current,
                                 final Async.Done<Object> onPick) {
        final ListPopupWindow popup = new ListPopupWindow(ctx);
        popup.setAnchorView(anchor);
        popup.setModal(true);
        popup.setWidth(anchor.getWidth() > 0 ? anchor.getWidth() : Ui.WRAP);
        popup.setBackgroundDrawable(Ui.shape(ctx, CustomColors.nearlyWhite, 4f, 0x22000000, 1f));
        popup.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return entries.size();
            }

            @Override
            public Object getItem(int position) {
                return entries.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Entry en = entries.get(position);
                TextView tv = Ui.text(ctx, en.text == null ? "" : en.text,
                        new Txt.Style(itemStyle.sizeSp).color(TEXT_COLOR).bold(itemStyle.bold)
                                .italic(itemStyle.italic));
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setMinHeight(Ui.dp(ctx, 48));
                Ui.padXY(tv, 16, 0);
                return tv;
            }
        });
        popup.setOnItemClickListener((parent, view, position, id) -> {
            popup.dismiss();
            onPick.run(entries.get(position).value);
        });
        popup.show();
    }
}
