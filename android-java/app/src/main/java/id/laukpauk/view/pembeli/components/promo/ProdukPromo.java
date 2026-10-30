package id.laukpauk.view.pembeli.components.promo;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.core.Async;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.model.PromoModel;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/Promo/ProdukPromo.dart. */
public class ProdukPromo extends LinearLayout {
    private JsonArray produkPromoState = new JsonArray();
    private JsonArray produkPromoImmutableState = new JsonArray();
    private String pencarianString = "";

    private final FrameLayout listHolder;
    private SwipeRefreshLayout refresh;

    public ProdukPromo(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);

        LinearLayout searchHolder = Ui.column(ctx);
        Ui.pad(searchHolder, 10, 10, 10, 0);
        searchHolder.addView(new Pencarian(ctx, string -> {
            String stringLC = string.toLowerCase(Locale.ROOT);
            JsonArray filtered = new JsonArray();
            for (JsonElement e : produkPromoImmutableState) {
                String namaProduk = Json.str(Json.obj(e), "namaProduk");
                if (namaProduk.toLowerCase(Locale.ROOT).indexOf(stringLC) >= 0) {
                    filtered.add(e);
                }
            }
            pencarianString = string;
            produkPromoState = filtered;
            render();
        }, () -> {
            pencarianString = "";
            produkPromoState = produkPromoImmutableState;
            render();
        }, "Cari Produk Promo Di Sini", pencarianString), new LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(searchHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));

        listHolder = new FrameLayout(ctx);
        addView(listHolder, Ui.weightH(1f));

        render();
        initialization();
    }

    /** Dart: initialization() (initState and didUpdateWidget). */
    public void initialization() {
        final BaseActivity activity = (BaseActivity) getContext();
        activity.async(() -> PromoModel.getProdukPromo(), produkPromo -> {
            if (produkPromo == null) {
                produkPromo = new JsonArray();
            }
            produkPromoState = produkPromo;
            produkPromoImmutableState = produkPromo;
            render();
        });
    }

    /** Dart: didUpdateWidget -> initialization(). */
    public void refresh() {
        initialization();
    }

    private void render() {
        Context ctx = getContext();
        listHolder.removeAllViews();

        View content;
        boolean fill;
        if (produkPromoState.size() >= 1) {
            List<View> items = new ArrayList<>();
            for (JsonElement e : produkPromoState) {
                items.add(new PromoItem(ctx, Json.obj(e), (double) (Ui.hb(ctx) * 25)));
            }
            LinearLayout grid = Layouts.grid(ctx, items, 3, 0, 0, 0.45f);
            LinearLayout holder = Ui.column(ctx);
            Ui.pad(holder, 10, 10, 10, 0);
            holder.addView(grid, new LayoutParams(Ui.MATCH, Ui.WRAP));
            content = holder;
            fill = false;
        } else {
            LinearLayout holder = Ui.column(ctx);
            holder.setGravity(Gravity.CENTER);
            Ui.padXY(holder, Ui.hb(ctx) * 20f, 0);
            holder.addView(Ui.asset(ctx, "tidak-ada-promo.png", Ui.hb(ctx) * 60f));
            content = holder;
            fill = true;
        }

        refresh = DetailUi.refreshable(ctx, content, () -> {
            final BaseActivity activity = (BaseActivity) getContext();
            activity.async(() -> PromoModel.getProdukPromo(), produkPromo -> {
                if (produkPromo == null) {
                    produkPromo = new JsonArray();
                }
                produkPromoState = produkPromo;
                produkPromoImmutableState = produkPromo;
                render();
            });
        }, fill);
        listHolder.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
