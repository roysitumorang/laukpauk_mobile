package id.laukpauk.view.pembeli.components.notifikasi;

import id.laukpauk.core.AppContainer;

import android.content.Intent;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.util.CustomColors;
import id.laukpauk.util.Json;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.NotifikasiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Routing;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Notifikasi/NotifikasiKhususItem.dart. */
public class NotifikasiKhususItem extends LinearLayout {
    /** Dependencies of this component, resolved once from its Context (see AppContainer). */
    private final AppContainer deps = AppContainer.from(getContext());


    public NotifikasiKhususItem(final BaseActivity ctx, JsonObject dataNotifikasiKhususItem,
                                final Async.Done<Boolean> toggleNotifikasiCallback) {
        super(ctx);
        setOrientation(HORIZONTAL);
        float hb = Ui.hb(ctx);

        JsonObject notifikasi = dataNotifikasiKhususItem;

        final String id = Json.str(notifikasi, "id");
        String judul = Json.str(notifikasi, "judul");
        String isi = Json.str(notifikasi, "isi");
        final String routeName = Json.str(notifikasi, "routeName");
        final String routeParams = Json.str(notifikasi, "routeParams");
        final boolean isRead = Json.bool(notifikasi, "isRead");
        String jenisNotifikasi = Json.str(notifikasi, "jenisNotifikasi");
        String waktu = Json.str(notifikasi, "waktu");

        // Container(color) > Padding(symmetric(horizontal: 10, vertical: 10)) > ListTile
        setBackgroundColor(!isRead ? 0x2566CC99 : 0xFFFFFFFF);
        Ui.pad(this, 10 + 16, 10 + 8, 10 + 16, 10 + 8);

        addView(Ui.asset(ctx, "notifikasi/" + jenisNotifikasi + ".png", hb * 10f, hb * 10f));
        addView(Ui.spaceW(ctx, 16));

        LinearLayout texts = Ui.column(ctx);
        texts.addView(Ui.text(ctx, judul.toUpperCase(Locale.ROOT),
                new Txt.Style(hb * 3.5f)
                        .color(!isRead ? CustomColors.textSuccess : CustomColors.nearlyBlack).bold()));
        texts.addView(Ui.text(ctx, isi, hb * 3f, 0x8A000000));
        texts.addView(Ui.spaceH(ctx, 3.5f));
        texts.addView(Ui.text(ctx, waktu, hb * 2.5f, 0x8A000000));
        texts.addView(Ui.spaceH(ctx, 5));
        texts.addView(Ui.text(ctx, isRead ? "Sudah dibaca" : "Belum dibaca", 14f, CustomColors.textInfo, true));
        addView(texts, Ui.weightW(1f));

        Layouts.ripple(this);
        setOnClickListener(v -> ctx.async(() -> {
            boolean setAsRead;
            if (!isRead) {
                setAsRead = deps.notifikasiModel.setAs(id, "read", "khususuntukkamu");
            } else {
                setAsRead = true;
            }
            return setAsRead;
        }, setAsRead -> {
            if (setAsRead != null && setAsRead) {
                Intent intent = Routing.intentFor(ctx, routeName, routeParams);
                ctx.startActivity(intent);
                toggleNotifikasiCallback.run(true);

                ctx.async(deps.headerModel::getJumlahNotifikasi,
                        jumlahNotifikasi -> deps.appState.setJumlahNotifikasi(jumlahNotifikasi));
            }
        }));
    }
}
