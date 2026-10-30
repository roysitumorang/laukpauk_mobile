package id.laukpauk.view.pembeli.components.repeatorder;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ROModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.KeranjangActivity;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;

/** Port of lib/views/pembeli/components/RepeatOrder/RepeatOrderItemTransaksi.dart. */
public class RepeatOrderItemTransaksi extends FrameLayout {
    private final BaseActivity activity;
    private final String idTransaksi;
    private final Runnable onRefresh;

    private final List<String> selectedSellers = new ArrayList<>();
    private boolean isProcessingRO = false;
    private boolean hasItems = false;
    private FrameLayout fab;

    /** Dart: RepeatOrderItemTransaksi(idTransaksi, onRefresh). A new instance starts with no selection (didUpdateWidget). */
    public RepeatOrderItemTransaksi(Context ctx, String idTransaksi, Runnable onRefresh) {
        super(ctx);
        this.activity = (BaseActivity) ctx;
        this.idTransaksi = idTransaksi;
        this.onRefresh = onRefresh;

        // Center(SizedBox(15x15, CircularProgressIndicator(strokeWidth: 1.5)))
        addView(DetailUi.smallSpinner(ctx, 15f), new LayoutParams(Ui.MATCH, Ui.MATCH));
        activity.async(() -> ROModel.getItemTransaksiPerPenjual(idTransaksi), this::showItems);
    }

    private void showItems(JsonArray itemTransaksiState) {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        removeAllViews();
        hasItems = itemTransaksiState.size() >= 1;

        Runnable refreshAction = () -> {
            if (onRefresh != null) {
                onRefresh.run();
            }
        };

        if (hasItems) {
            LinearLayout list = Ui.column(ctx);
            Ui.pad(list, 5, 0, 5, 10);
            for (int index = 0; index < itemTransaksiState.size(); index++) {
                JsonObject item = Json.objAt(itemTransaksiState, index);
                boolean isAvailable = Json.bool(item, "isAvailable");
                String idTransaksiItem = Json.str(item, "idTransaksi");
                final String idPenjual = Json.str(item, "idPenjual");

                JsonObject detailPenjual = Json.of(
                        "idPenjual", idPenjual,
                        "namaPenjual", Json.str(item, "namaPenjual"),
                        "namaPasar", Json.str(item, "namaPasar"),
                        "bannerPenjual", Json.str(item, "bannerPenjual"),
                        "alamatToko", Json.str(item, "alamatToko"));

                boolean isSelected = selectedSellers.contains(idPenjual);

                list.addView(new RepeatOrderTransaksiItemPerPenjual(ctx, isSelected, isAvailable, idTransaksiItem,
                        detailPenjual, Json.array(item, "itemTransaksi"), isChecked -> {
                    if (selectedSellers.contains(idPenjual)) {
                        selectedSellers.remove(idPenjual);
                    } else {
                        selectedSellers.add(idPenjual);
                    }
                    updateFab();
                }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            addView(DetailUi.refreshable(ctx, list, refreshAction, false), new LayoutParams(Ui.MATCH, Ui.MATCH));

            fab = new FrameLayout(ctx);
            fab.setBackground(Ui.circle(CustomColors.green));
            fab.setElevation(Ui.dp(ctx, 6));
            fab.setOnClickListener(v -> onFab());
            LayoutParams flp = new LayoutParams(Ui.dp(ctx, 56), Ui.dp(ctx, 56), Gravity.BOTTOM | Gravity.END);
            flp.bottomMargin = Ui.dp(ctx, hb * 5.0f);
            flp.rightMargin = Ui.dp(ctx, hb * 5.0f);
            addView(fab, flp);
            updateFab();
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER);
            Ui.pad(empty, hb * 15.0f, 0, hb * 15.0f, 10);
            empty.addView(Ui.asset(ctx, "empty.png", hb * 25));
            empty.addView(Ui.spaceH(ctx, 10));
            android.widget.TextView t = TransaksiUi.hbText(ctx,
                    "Tidak ada belanjaan di dalam transaksi ini yang dapat dibelanja ulang.\nSilahkan belanja ulang seperti biasa!",
                    2.75f, CustomColors.textMuted, false);
            t.setGravity(Gravity.CENTER);
            empty.addView(t);
            addView(DetailUi.refreshable(ctx, empty, refreshAction, true), new LayoutParams(Ui.MATCH, Ui.MATCH));
        }
    }

    /** (selectedSellers.length >= 1) ? Positioned(FloatingActionButton) : SizedBox() */
    private void updateFab() {
        if (fab == null) {
            return;
        }
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        fab.setVisibility(selectedSellers.size() >= 1 ? View.VISIBLE : View.GONE);
        fab.removeAllViews();
        if (isProcessingRO) {
            ProgressBar bar = new ProgressBar(ctx);
            bar.setIndeterminate(true);
            bar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(CustomColors.nearlyWhite));
            fab.addView(bar, new LayoutParams(Ui.dp(ctx, hb * 4.5f), Ui.dp(ctx, hb * 4.5f), Gravity.CENTER));
        } else {
            ImageView icon = Ui.icon(ctx, R.drawable.ic_shopping_cart, hb * 6.0f, CustomColors.white);
            fab.addView(icon, new LayoutParams(Ui.dp(ctx, hb * 6.0f), Ui.dp(ctx, hb * 6.0f), Gravity.CENTER));
        }
    }

    private void onFab() {
        if (isProcessingRO) {
            return;
        }
        isProcessingRO = true;
        updateFab();
        final JsonArray sellers = new JsonArray();
        for (String s : selectedSellers) {
            sellers.add(s);
        }
        activity.async(() -> ROModel.addToCartThesePenjual(idTransaksi, sellers), addToCartResponse -> {
            String text = "Gagal menambahkan ke keranjang belanjaan, coba lagi!";

            final boolean statusAddToCart = Json.bool(addToCartResponse, "statusAddCart");
            if (statusAddToCart) {
                text = "Berhasil menambahkan ke keranjang!";
            }

            if (addToCartResponse.has("message")) {
                String message = Json.strOrNull(addToCartResponse, "message");
                if (message != null) {
                    text = text + " " + message;
                }
            }

            DetailUi.snackbar(this, text, Ui.hb(getContext()) * 3.5f, 3000,
                    statusAddToCart ? "Lihat Keranjang" : null, CustomColors.green,
                    () -> Nav.push(getContext(), KeranjangActivity.class, null));

            if (statusAddToCart) {
                activity.async(() -> HeaderModel.getJumlahCart(), jumlahCart -> {
                    AppState.get().setJumlahCart(jumlahCart);
                    isProcessingRO = false;
                    updateFab();
                });
            } else {
                isProcessingRO = false;
                updateFab();
            }
        });
    }
}
