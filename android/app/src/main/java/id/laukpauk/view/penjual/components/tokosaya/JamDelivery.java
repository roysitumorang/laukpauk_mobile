package id.laukpauk.view.penjual.components.tokosaya;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/** Port of lib/views/penjual/components/TokoSaya/JamDelivery.dart. */
public class JamDelivery extends LinearLayout {
    private boolean sixNineSelected = false;
    private boolean nineTwelveSelected = false;
    private boolean thirdTeenSelected = false;

    private CheckBox sixNine;
    private CheckBox nineTwelve;
    private CheckBox thirdTeen;

    public JamDelivery(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        render();
        getJadwalDeliveryPenjual();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    private void getJadwalDeliveryPenjual() {
        PenjualUi.async(getContext(), () -> {
            String params = HTTPRequest.getQueryString(null, true);

            Http.Response httpResponse = Http.getApi("/Penjual/getJadwalDeliveryPenjual?" + params);
            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "jadwalDeliveryPenjual");
            }
            return null;
        }, (JsonArray jadwalDeliveryPenjual) -> {
            if (jadwalDeliveryPenjual != null && jadwalDeliveryPenjual.size() >= 1) {
                for (JsonElement e : jadwalDeliveryPenjual) {
                    int jadwalDeliveryCode = Json.parseInt(Json.str(Json.obj(e), "jadwalDeliveryCode"), 0);
                    if (jadwalDeliveryCode == 1) {
                        sixNineSelected = true;
                    }
                    if (jadwalDeliveryCode == 2) {
                        nineTwelveSelected = true;
                    }
                    if (jadwalDeliveryCode == 3) {
                        thirdTeenSelected = true;
                    }
                }
                sixNine.setChecked(sixNineSelected);
                nineTwelve.setChecked(nineTwelveSelected);
                thirdTeen.setChecked(thirdTeenSelected);
            }
        });
    }

    /** Dart toggleJadwalDelivery(code, readAbleJadwal): the dialog on failure, the status passed on. */
    private void toggleJadwalDelivery(final String jadwalDeliveryCode, final String readAbleJadwal,
                                      final id.laukpauk.core.Async.Done<Boolean> done) {
        activity().async(() -> {
            boolean toggleStatus = false;
            Map<String, String> body = new LinkedHashMap<>();
            body.put("jadwalDeliveryCode", jadwalDeliveryCode);

            String params = HTTPRequest.getQueryString(null, true);
            Map<String, String> postBody = HTTPRequest.postBody(body);

            Http.Response httpResponse = Http.postApi("/Penjual/toggleJadwalDelivery?" + params, postBody);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                toggleStatus = Json.bool(dataObject, "toggleStatus");
            }
            return toggleStatus;
        }, toggleStatus -> {
            if (!toggleStatus) {
                PenjualUi.alertPlain(activity(), "Pengaturan Jadwal Delivery",
                        "Gagal mengubah pengaturan jadwal delivery " + readAbleJadwal, 4f);
            }
            done.run(toggleStatus);
        });
    }

    private CheckBox checkbox(Context ctx, final String code, final String readable, final int index) {
        final CheckBox cb = new CheckBox(ctx);
        cb.setButtonTintList(PenjualUi.tint(PenjualUi.defaultBlue));
        cb.setPadding(0, 0, 0, 0);
        cb.setOnClickListener(v -> {
            // The Flutter Checkbox only changes with its `value`: undo the local toggle until the server agreed.
            cb.setChecked(!cb.isChecked());
            toggleJadwalDelivery(code, readable, toggle -> {
                if (toggle) {
                    if (index == 0) {
                        sixNineSelected = !sixNineSelected;
                        cb.setChecked(sixNineSelected);
                    } else if (index == 1) {
                        nineTwelveSelected = !nineTwelveSelected;
                        cb.setChecked(nineTwelveSelected);
                    } else {
                        thirdTeenSelected = !thirdTeenSelected;
                        cb.setChecked(thirdTeenSelected);
                    }
                }
            });
        });
        return cb;
    }

    private LinearLayout line(Context ctx, String name, String estimasi, CheckBox cb) {
        LinearLayout row = Ui.row(ctx);
        row.addView(PenjualUi.text(ctx, name, 3f, 0xDD000000, false));
        row.addView(Ui.spacer(ctx));
        row.addView(PenjualUi.text(ctx, estimasi, 3f, 0xDD000000, false));
        row.addView(Ui.spacer(ctx));
        row.addView(cb, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        return row;
    }

    private void render() {
        removeAllViews();
        Context ctx = getContext();
        LinearLayout card = PenjualUi.cardPadded(ctx, 15, 10);
        addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        card.addView(PenjualUi.text(ctx, "Jam Delivery", 4f, 0xDD000000, true));

        sixNine = checkbox(ctx, "1", "Pagi", 0);
        nineTwelve = checkbox(ctx, "2", "Siang", 1);
        thirdTeen = checkbox(ctx, "3", "Sore", 2);

        card.addView(line(ctx, "Delivery Pagi", "Estimasi Jam 06.00 s/d 09.00 WIB", sixNine),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(line(ctx, "Delivery Siang", "Estimasi Jam 09.00 s/d 12.00 WIB", nineTwelve),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(line(ctx, "Delivery Sore", "Estimasi Jam 13.00 s/d 15.00 WIB", thirdTeen),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
