package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.HomepageModel;
import id.laukpauk.model.KontenModel;
import id.laukpauk.core.AppState;
import id.laukpauk.view.pembeli.components.homepage.KontenItemBanner;
import id.laukpauk.view.pembeli.DetailProdukActivity;
import id.laukpauk.view.pembeli.components.homepage.ProdukPasarSekitar;

/**
 * lib/global-components/HeaderSearch.dart: the full screen search dialog opened from the Header
 * (showDialog(useSafeArea: false)). Use {@link #show(Activity)}.
 */
public class HeaderSearch extends LinearLayout {
    private final Runnable onBack;
    private final EditText pencarianField;
    private final ImageView cancelButton;
    private final FrameLayout body;
    private boolean onFocus = false;
    private SearchResult searchResult;
    private View browseView;

    /** Dart: showDialog(useSafeArea: false, context: context, builder: (_) => HeaderSearch()). */
    public static void show(Activity activity) {
        Modal.<Object>dialog(activity, true, true, handle -> new HeaderSearch(activity, () -> handle.close()), null);
    }

    /** @param onBack Navigator.pop(context): closes the dialog. */
    public HeaderSearch(final Context ctx, Runnable onBack) {
        super(ctx);
        this.onBack = onBack;
        setOrientation(VERTICAL);
        setBackgroundColor(CustomColors.nearlyWhite);

        // the dialog window may be edge to edge: keep the content out of the system bars / keyboard
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        // AppBar(backgroundColor: transparent, elevation: 0, leading: back arrow, title: TextField, actions: [cancel])
        LinearLayout bar = Ui.row(ctx);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout back = new FrameLayout(ctx);
        back.addView(Ui.icon(ctx, R.drawable.ic_arrow_back_ios, 20, CustomColors.grey),
                new FrameLayout.LayoutParams(Ui.dp(ctx, 20), Ui.dp(ctx, 20), Gravity.CENTER));
        back.setOnClickListener(v -> this.onBack.run());
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(ctx, 56), Ui.dp(ctx, 56)));

        pencarianField = new EditText(ctx);
        pencarianField.setBackground(new ColorDrawable(Color.TRANSPARENT));
        pencarianField.setTypeface(Ui.font(ctx));
        pencarianField.setSingleLine(true);
        pencarianField.setInputType(InputType.TYPE_CLASS_TEXT);
        pencarianField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        pencarianField.setHint("Cari Produk dan Toko di sini");
        pencarianField.setHintTextColor(CustomColors.grey);
        pencarianField.setTextColor(0xFF000000);
        pencarianField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        // onSubmitted: the keyboard closes and the field loses focus (the focus listener does the work)
        pencarianField.setOnEditorActionListener((v, actionId, event) -> {
            pencarianField.clearFocus();
            hideKeyboard();
            return true;
        });
        pencarianField.setOnFocusChangeListener((v, hasFocus) -> refreshFocus());
        pencarianField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        bar.addView(pencarianField, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        cancelButton = Ui.icon(ctx, R.drawable.ic_cancel, 24, CustomColors.textDanger);
        FrameLayout cancel = new FrameLayout(ctx);
        cancel.addView(cancelButton, new FrameLayout.LayoutParams(Ui.dp(ctx, 24), Ui.dp(ctx, 24), Gravity.CENTER));
        cancel.setVisibility(GONE);
        cancel.setOnClickListener(v -> {
            // _focusNode.unfocus(); _pencarianController.clear(); setState(() => _onFocus = false);
            pencarianField.setText("");
            pencarianField.clearFocus();
            hideKeyboard();
            onFocus = false;
            update();
        });
        cancelHolder = cancel;
        bar.addView(cancel, new LinearLayout.LayoutParams(Ui.dp(ctx, 48), Ui.dp(ctx, 56)));
        addView(bar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 56)));

        body = new FrameLayout(ctx);
        addView(body, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        update();
    }

    private View cancelHolder;

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(pencarianField.getWindowToken(), 0);
        }
    }

    /** _focusNode listener: _onFocus = hasFocus || text.length >= 1. */
    private void refreshFocus() {
        onFocus = pencarianField.hasFocus() || pencarianField.getText().length() >= 1;
        update();
    }

    /** build(): the actions and the body depend on _onFocus and on the text at this moment. */
    private void update() {
        cancelHolder.setVisibility(onFocus ? VISIBLE : GONE);
        String search = pencarianField.getText().toString();
        if (onFocus) {
            if (searchResult == null) {
                body.removeAllViews();
                browseView = null;
                searchResult = new SearchResult(getContext(), search, onBack);
                body.addView(searchResult, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            } else {
                searchResult.setSearchString(search);
            }
        } else {
            if (browseView == null) {
                body.removeAllViews();
                searchResult = null;
                browseView = buildBrowse();
                body.addView(browseView, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            }
        }
    }

    private View buildBrowse() {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        ScrollView scroll = new ScrollView(ctx);
        LinearLayout column = Ui.column(ctx);
        Ui.pad(column, 15, 0, 15, 15);
        scroll.addView(column, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));

        // Consumer<HeaderViewModel> + FutureBuilder(HomepageModel.getDataPasar(limitPasar: 1))
        final FrameLayout pasarHolder = new FrameLayout(ctx);
        pasarHolder.addView(Loading.view(ctx, null, null, 25f, 25f), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(pasarHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        final boolean isADJDSet = AppState.get().isADJDSet();
        final JsonObject savedADJD = AppState.get().getSavedADJD();
        Async.run(() -> HomepageModel.getDataPasar(isADJDSet, false, 1, null, savedADJD), (JsonArray dataPasar) -> {
            pasarHolder.removeAllViews();
            if (dataPasar != null) {
                pasarHolder.addView(new ProdukPasarSekitar(ctx, dataPasar),
                        new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });

        // FutureBuilder(KontenModel.getKontenAplikasi(page: 'headersearch'))
        final FrameLayout kontenHolder = new FrameLayout(ctx);
        kontenHolder.addView(Loading.view(ctx), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(kontenHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        Async.run(() -> KontenModel.getKontenAplikasi("headersearch"), (JsonArray dataKonten) -> {
            kontenHolder.removeAllViews();
            if (dataKonten == null || dataKonten.size() < 1) {
                return;
            }
            LinearLayout list = Ui.column(ctx);
            for (int i = 0; i < dataKonten.size(); i++) {
                final JsonObject konten = Json.objAt(dataKonten, i);
                final String idKonten = Json.str(konten, "id");
                final String namaDivisi = Json.str(konten, "namaDivisi");
                final String jenisItemLC = Json.str(konten, "jenisItem").toLowerCase();

                LinearLayout item = Ui.column(ctx);
                Ui.pad(item, 0, 0, 0, 15);
                item.addView(Ui.text(ctx, namaDivisi, hb * 4.0f, 0xFF000000));
                item.addView(Ui.spaceH(ctx, 10));

                final FrameLayout itemHolder = new FrameLayout(ctx);
                itemHolder.addView(Loading.view(ctx), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                item.addView(itemHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                Async.run(() -> KontenModel.getProdukKonten(idKonten), (JsonArray kontenItem) -> {
                    itemHolder.removeAllViews();
                    if (kontenItem == null || kontenItem.size() < 1) {
                        return;
                    }
                    if (jenisItemLC.equals("produk")) {
                        LinearLayout rows = Ui.column(ctx);
                        for (int j = 0; j < kontenItem.size(); j++) {
                            rows.addView(produkRow(ctx, Json.objAt(kontenItem, j)),
                                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                        }
                        itemHolder.addView(rows, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                    } else {
                        itemHolder.addView(new KontenItemBanner(ctx, konten, kontenItem),
                                new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                    }
                });
                list.addView(item, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            kontenHolder.addView(list, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        });
        return scroll;
    }

    /** ListTile(dense, contentPadding: zero, leading: round photo, title: nama, subtitle: kategori). */
    private View produkRow(final Context ctx, final JsonObject produk) {
        LinearLayout tile = Ui.row(ctx);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(tile, 0, 8, 0, 8);

        ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        Ui.loadNetworkCircle(iv, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/"
                + Json.str(produk, "foto"));
        tile.addView(iv, new LinearLayout.LayoutParams(Ui.dp(ctx, 50), Ui.dp(ctx, 50)));

        LinearLayout texts = Ui.column(ctx);
        texts.addView(Ui.text(ctx, Json.str(produk, "nama"), 14f, 0xDE000000));
        texts.addView(Ui.text(ctx, Json.str(produk, "namaKategoriProduk"), 12f, 0x99000000));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
        tlp.leftMargin = Ui.dp(ctx, 16);
        tile.addView(texts, tlp);

        Layouts.ripple(tile, 0x22000000, 0f);
        tile.setOnClickListener(v -> {
            // Navigator.pushReplacement: the dialog route is replaced by DetailProduk
            onBack.run();
            Nav.push(ctx, DetailProdukActivity.class, Json.of("id", Json.str(produk, "id")));
        });
        return tile;
    }
}
