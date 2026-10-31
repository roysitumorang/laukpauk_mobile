package id.laukpauk.view.pembeli.components.notifikasi;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.NotifikasiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/**
 * Port of lib/views/pembeli/components/Notifikasi/NotifikasiGlobal.dart ("Info" tab).
 * The host gives it weight 1 (Dart Expanded).
 */
public class NotifikasiGlobal extends LinearLayout {
    private final Runnable reloadJumlahNotifikasi;
    private int requestId = 0;
    private SwipeRefreshLayout currentRefresh;

    public NotifikasiGlobal(Context ctx, String idUser, Runnable reloadJumlahNotifikasi) {
        super(ctx);
        setOrientation(VERTICAL);
        this.reloadJumlahNotifikasi = reloadJumlahNotifikasi;
        load();
    }

    private void load() {
        requestId++;
        final int thisRequest = requestId;
        final BaseActivity a = (BaseActivity) getContext();

        removeAllViews();
        currentRefresh = null;
        addView(Loading.spinner(a), new LayoutParams(Ui.MATCH, Ui.MATCH));

        a.async(NotifikasiModel::getUpdateNotifikasi, notifikasi -> {
            if (thisRequest != requestId || notifikasi == null) {
                return;
            }
            render(a, notifikasi);
        });
    }

    private void refresh() {
        final BaseActivity a = (BaseActivity) getContext();
        reloadJumlahNotifikasi.run();
        requestId++;
        final int thisRequest = requestId;
        final SwipeRefreshLayout old = currentRefresh;
        a.async(NotifikasiModel::getUpdateNotifikasi, notifikasi -> {
            if (thisRequest != requestId) {
                return;
            }
            if (notifikasi == null) {
                if (old != null) {
                    old.setRefreshing(false);
                }
                return;
            }
            render(a, notifikasi);
        });
    }

    private void render(BaseActivity a, JsonArray notifikasi) {
        removeAllViews();
        float hb = Ui.hb(a);
        Runnable onRefresh = this::refresh;
        if (notifikasi.size() >= 1) {
            LinearLayout list = Ui.column(a);
            for (int i = 0; i < notifikasi.size(); i++) {
                list.addView(new NotifikasiGlobalItem(a, Json.objAt(notifikasi, i)),
                        new LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            currentRefresh = DetailUi.refreshable(a, list, onRefresh, false);
        } else {
            LinearLayout column = Ui.column(a);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(a, "empty.png", hb * 30f));
            column.addView(Ui.spaceH(a, 10));
            column.addView(Ui.text(a, "Mak, belum ada info terbaru nih...", hb * 3.5f, CustomColors.textMuted));
            currentRefresh = DetailUi.refreshable(a, column, onRefresh, true);
        }
        addView(currentRefresh, new LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
