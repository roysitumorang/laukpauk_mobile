package id.laukpauk.view.pembeli.components.produkdidalampasar;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/**
 * Shared body of PasarLainDalamCA and PasarLainLuarCA (the two Dart widgets are the same except
 * for the request, the texts and the list height). Package private.
 */
abstract class PasarLainSection extends LinearLayout {
    private final String title;
    private final String emptyText;
    private final float listHeightHb;
    private final boolean showDistance;

    private boolean isReadyToShow = false;
    private JsonArray state = new JsonArray();
    private JsonArray immutableState = new JsonArray();
    private String pencarianString = "";

    private final FrameLayout bodyHolder;

    PasarLainSection(Context ctx, String title, String emptyText, float listHeightHb,
                     boolean showDistance) {
        super(ctx);
        setOrientation(VERTICAL);
        this.title = title;
        this.emptyText = emptyText;
        this.listHeightHb = listHeightHb;
        this.showDistance = showDistance;

        float hb = Ui.hb(ctx);
        addView(Ui.text(ctx, title, hb * 4, 0xDD000000, true));
        addView(Ui.spaceH(ctx, 15));
        bodyHolder = new FrameLayout(ctx);
        addView(bodyHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        render();
    }

    /** BLOCKING: the `listPasar` of the request (empty when ADJD is not set / request failed). */
    abstract JsonArray fetchPasar();

    /** Starts the request; called by the subclass constructor once its own fields are set. */
    final void start() {
        Async.Work<JsonArray> work = this::fetchPasar;
        Async.Done<JsonArray> done = list -> {
            JsonArray result = list != null ? list : new JsonArray();
            state = result;
            immutableState = result;
            isReadyToShow = true;
            render();
        };
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).async(work, done);
        } else {
            Async.run(work, done);
        }
    }

    private void render() {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        bodyHolder.removeAllViews();
        if (!isReadyToShow) {
            LinearLayout box = Ui.column(ctx);
            Ui.pad(box, 0, 15, 0, 15);
            box.addView(DetailUi.smallSpinner(ctx, 15), new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));
            bodyHolder.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        LinearLayout column = Ui.column(ctx);
        column.addView(new id.laukpauk.ui.Pencarian(ctx, string -> {
            String stringLC = string.toLowerCase();
            JsonArray filtered = new JsonArray();
            for (JsonElement e : immutableState) {
                String namaPasarLC = Json.str(Json.obj(e), "nama").toLowerCase();
                if (namaPasarLC.indexOf(stringLC) >= 0) {
                    filtered.add(e);
                }
            }
            pencarianString = string;
            state = filtered;
            renderList();
        }, () -> {
            pencarianString = "";
            state = immutableState;
            renderList();
        }, "Cari Pasar dengan Nama", pencarianString), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(ctx, 15));
        listHolder = new FrameLayout(ctx);
        column.addView(listHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        renderList();
    }

    private FrameLayout listHolder;

    private void renderList() {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        listHolder.removeAllViews();
        if (state.size() >= 1) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(HORIZONTAL);
            for (int i = 0; i < state.size(); i++) {
                JsonObject item = Json.objAt(state, i);
                row.addView(new PasarLainItem(ctx, item, showDistance));
                row.addView(Ui.spaceW(ctx, 5));
            }
            listHolder.addView(Layouts.hscroll(ctx, row),
                    new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, hb * listHeightHb)));
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.pad(empty, 0, 15, 0, 15);
            empty.addView(Ui.asset(ctx, "empty.png", hb * 25));
            empty.addView(Ui.spaceH(ctx, 5));
            empty.addView(Ui.text(ctx, emptyText, hb * 3.5f, CustomColors.textMuted));
            listHolder.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
