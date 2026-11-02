package id.laukpauk.view.pembeli.components.notifikasi;

import id.laukpauk.core.AppContainer;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;

import id.laukpauk.util.CustomColors;
import id.laukpauk.util.Json;
import id.laukpauk.model.NotifikasiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/**
 * Port of lib/views/pembeli/components/Notifikasi/NotifikasiKhusus.dart ("Notifikasi" tab).
 * The host gives it weight 1 (Dart Expanded).
 */
public class NotifikasiKhusus extends LinearLayout {
    /** Dependencies of this component, resolved once from its Context (see AppContainer). */
    private final AppContainer deps = AppContainer.from(getContext());

    private final Runnable reloadJumlahNotifikasi;
    private int requestId = 0;

    public NotifikasiKhusus(Context ctx, String idUser, Runnable reloadJumlahNotifikasi) {
        super(ctx);
        setOrientation(VERTICAL);
        this.reloadJumlahNotifikasi = reloadJumlahNotifikasi;
        load(true);
    }

    private void load(boolean showLoader) {
        requestId++;
        final int thisRequest = requestId;
        final BaseActivity a = (BaseActivity) getContext();

        if (showLoader) {
            removeAllViews();
            addView(Loading.spinner(a), new LayoutParams(Ui.MATCH, Ui.MATCH));
        }

        a.async(deps.notifikasiModel::getNotifikasiKhusus, notifikasi -> {
            if (thisRequest != requestId || notifikasi == null) {
                return;
            }
            render(a, notifikasi);
        });
    }

    private void render(final BaseActivity a, JsonArray notifikasi) {
        removeAllViews();
        float hb = Ui.hb(a);
        Runnable onRefresh = () -> load(false);
        SwipeRefreshLayout refresh;
        if (notifikasi.size() >= 1) {
            LinearLayout list = Ui.column(a);
            for (int i = 0; i < notifikasi.size(); i++) {
                list.addView(new NotifikasiKhususItem(a, Json.objAt(notifikasi, i), statusToggleNotifikasi -> {
                    if (statusToggleNotifikasi != null && statusToggleNotifikasi) {
                        load(true); // setState(() { reload++; })
                        reloadJumlahNotifikasi.run();
                    } else {
                        a.toast("Gagal mengubah status notifikasi. Silahkan coba lagi !");
                    }
                }), new LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            refresh = DetailUi.refreshable(a, list, onRefresh, false);
        } else {
            LinearLayout column = Ui.column(a);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(a, "empty.png", hb * 30f));
            column.addView(Ui.spaceH(a, 10));
            column.addView(Ui.text(a, "Tidak ada notifikasi apapun untuk anda", hb * 3.5f,
                    CustomColors.textMuted));
            refresh = DetailUi.refreshable(a, column, onRefresh, true);
        }
        addView(refresh, new LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
