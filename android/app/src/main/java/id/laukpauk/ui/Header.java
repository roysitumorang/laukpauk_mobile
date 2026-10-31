package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.view.pembeli.KeranjangActivity;
import id.laukpauk.view.pembeli.NotifikasiActivity;

/**
 * lib/global-components/Header.dart: the green app header (delivery address strip, search entry,
 * cart and notification buttons with badges). It does NOT pad for the status bar: BaseActivity does,
 * and this header only tints the area behind it green.
 *
 * Dart: Header({adjdSetCallback, askADJD = false, askADJDListener}). When `askADJD` is true the
 * address / schedule flow (HeaderModel.adjd) runs on creation; {@link #askADJD()} runs it again
 * (Dart: didUpdateWidget with a changed askADJDListener).
 */
public class Header extends LinearLayout {
    private final boolean askADJDOnCreate;
    private final Async.Done<JsonObject> adjdSetCallback;
    private boolean askedOnce = false;

    private final LinearLayout adjdRow;
    private final TextView alamatText;
    private final TextView cartBadge;
    private final TextView notifBadge;

    private final AppState.Listener listener = new AppState.Listener() {
        @Override
        public void onChanged() {
            bindState();
        }
    };

    public Header(Context ctx) {
        this(ctx, false, null, null);
    }

    public Header(Context ctx, boolean askADJD, Object askADJDListener, Async.Done<JsonObject> adjdSetCallback) {
        super(ctx);
        this.askADJDOnCreate = askADJD;
        this.adjdSetCallback = adjdSetCallback;
        final float hb = Ui.hb(ctx);

        setOrientation(VERTICAL);
        setBackgroundColor(CustomColors.green);
        Layouts.tintStatusBar(ctx, CustomColors.green);

        // Consumer<HeaderViewModel>: isADJDSet ? Row(spaceBetween, [title, address]) : SizedBox()
        adjdRow = Ui.row(ctx);
        adjdRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = Ui.text(ctx, "Diantarin ke mana ?",
                new Txt.Style(Ui.hb(ctx) * 3.25f).color(CustomColors.nearlyWhite).bold());
        Ui.pad(title, 10, 7.5f, 10, 7.5f);
        adjdRow.addView(title, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        alamatText = Ui.text(ctx, "", new Txt.Style(Ui.hb(ctx) * 3.25f).color(CustomColors.textMuted).bold());
        alamatText.setBackgroundColor(CustomColors.nearlyWhite);
        Ui.pad(alamatText, 15, 7.5f, 15, 7.5f);
        alamatText.setOnClickListener(v -> onAlamatTapped());
        adjdRow.addView(alamatText, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        addView(adjdRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        // Container(height: 50, child: Row([Expanded(search TextField), Container(white, [cart, bell])]))
        LinearLayout searchRow = Ui.row(ctx);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout searchField = Ui.row(ctx);
        searchField.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout prefix = new FrameLayout(ctx);
        prefix.addView(Ui.icon(ctx, R.drawable.ic_search, hb * 4f, CustomColors.nearlyWhite),
                new FrameLayout.LayoutParams(Ui.dp(ctx, hb * 4f), Ui.dp(ctx, hb * 4f), Gravity.CENTER));
        searchField.addView(prefix, new LinearLayout.LayoutParams(Ui.dp(ctx, 48), Ui.dp(ctx, 48)));
        TextView hint = Ui.text(ctx, "Cari Toko atau Sayuran Segar", hb * 4f, CustomColors.nearlyWhite);
        hint.setSingleLine(true);
        hint.setEllipsize(android.text.TextUtils.TruncateAt.END);
        searchField.addView(hint, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        searchField.setOnClickListener(v -> {
            if (getContext() instanceof Activity) {
                HeaderSearch.show((Activity) getContext());
            }
        });
        searchRow.addView(searchField, new LinearLayout.LayoutParams(0, Ui.MATCH, 1f));

        LinearLayout buttons = Ui.row(ctx);
        buttons.setBackgroundColor(CustomColors.nearlyWhite);
        buttons.setGravity(Gravity.CENTER_VERTICAL);

        cartBadge = badge(ctx);
        buttons.addView(iconButton(ctx, R.drawable.ic_shopping_cart, hb * 4.5f, cartBadge, 0f,
                () -> Nav.push(getContext(), KeranjangActivity.class, null)),
                new LinearLayout.LayoutParams(Ui.dp(ctx, 48), Ui.dp(ctx, 48)));
        notifBadge = badge(ctx);
        buttons.addView(iconButton(ctx, R.drawable.ic_notifications, hb * 4.5f, notifBadge, 5f,
                () -> Nav.push(getContext(), NotifikasiActivity.class, null)),
                new LinearLayout.LayoutParams(Ui.dp(ctx, 48), Ui.dp(ctx, 48)));
        searchRow.addView(buttons, new LinearLayout.LayoutParams(Ui.WRAP, Ui.dp(ctx, 50.5f)));
        addView(searchRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 50)));

        bindState();
    }

    private TextView badge(Context ctx) {
        float hb = Ui.hb(ctx);
        TextView t = Ui.text(ctx, "0", hb * 2.5f, CustomColors.nearlyWhite);
        t.setGravity(Gravity.CENTER);
        t.setMinWidth(Ui.dp(ctx, 17.5f));
        t.setMinHeight(Ui.dp(ctx, 17.5f));
        Ui.padAll(t, 3);
        t.setBackground(Ui.circle(0xFFF44336));
        t.setIncludeFontPadding(false);
        return t;
    }

    private View iconButton(Context ctx, int drawable, float sizeDp, TextView badge, float rightDp, Runnable onTap) {
        FrameLayout f = new FrameLayout(ctx);
        f.addView(Ui.icon(ctx, drawable, sizeDp, CustomColors.green),
                new FrameLayout.LayoutParams(Ui.dp(ctx, sizeDp), Ui.dp(ctx, sizeDp), Gravity.CENTER));
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.TOP | Gravity.END);
        blp.topMargin = Ui.dp(ctx, 5);
        blp.rightMargin = Ui.dp(ctx, rightDp);
        f.addView(badge, blp);
        Layouts.ripple(f, 0x22000000, 24f);
        f.setOnClickListener(v -> onTap.run());
        return f;
    }

    /** Applies the HeaderViewModel values (isADJDSet, alamatDelivery, jumlahCart, jumlahNotifikasi). */
    private void bindState() {
        AppState s = AppState.get();
        adjdRow.setVisibility(s.isADJDSet() ? VISIBLE : GONE);
        String alamatDelivery = s.getAlamatDelivery();
        if (alamatDelivery == null) {
            alamatDelivery = "";
        }
        alamatText.setText(alamatDelivery.length() >= 30 ? alamatDelivery.substring(0, 30) + " ..." : alamatDelivery);
        int cart = s.getJumlahCart();
        cartBadge.setText(cart >= 10 ? "9+" : String.valueOf(cart));
        int notif = s.getJumlahNotifikasi();
        notifBadge.setText(notif >= 10 ? "9+" : String.valueOf(notif));
    }

    private BaseActivity activity() {
        Context c = getContext();
        return c instanceof BaseActivity ? (BaseActivity) c : null;
    }

    private void onAlamatTapped() {
        final BaseActivity a = activity();
        if (a == null) {
            return;
        }
        HeaderModel.showAlamatPengantaran(a, AppState.get().getAlamatDelivery(), (Boolean ubahLokasi) -> {
            if (ubahLokasi != null && ubahLokasi) {
                ADJDModel.deleteAlamatDanJadwalDelivery();
                askADJD();
            }
        });
    }

    /** Dart headerADJD(): runs the address / schedule flow and hands the result to adjdSetCallback. */
    public void askADJD() {
        BaseActivity a = activity();
        if (a == null) {
            return;
        }
        HeaderModel.adjd(a, null, null, null, (JsonObject adjdFromHeader) -> {
            if (adjdSetCallback != null) {
                adjdSetCallback.run(adjdFromHeader);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AppState.get().addListener(listener);
        bindState();
        if (askADJDOnCreate && !askedOnce) {
            askedOnce = true;
            askADJD();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        AppState.get().removeListener(listener);
        super.onDetachedFromWindow();
    }
}
