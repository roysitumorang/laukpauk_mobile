package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/HomePage/JadwalPengantaran.dart. */
public class JadwalPengantaran extends FrameLayout {
    private final Async.Done<JsonObject> onJadwalDeliverySelected;
    private String selectedJadwalPengantaran = "";
    private JsonArray jadwalDelivery; // null while loading

    public JadwalPengantaran(Context ctx, Async.Done<JsonObject> onJadwalDeliverySelected) {
        super(ctx);
        this.onJadwalDeliverySelected = onJadwalDeliverySelected;
        render();
        HomeUi.act(ctx).async(() -> getJadwalDelivery(), result -> {
            jadwalDelivery = result;
            render();
        });
    }

    /** BLOCKING. */
    private static JsonArray getJadwalDelivery() {
        Http.Response httpResponse = Http.get(ServerPath.apiBaseURL + "/JadwalDelivery/getAvailableJadwalDelivery");
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "availableJadwalDelivery");
        }
        return new JsonArray();
    }

    private void render() {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        removeAllViews();

        if (jadwalDelivery == null) {
            View spinner = DetailUi.smallSpinner(ctx, 15);
            spinner.setPadding(0, Ui.dp(ctx, 25), 0, Ui.dp(ctx, 25));
            addView(spinner, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        if (jadwalDelivery.size() >= 1) {
            FlowLayout wrap = new FlowLayout(ctx, 0, 5.0f, true);
            for (int i = 0; i < jadwalDelivery.size(); i++) {
                final JsonObject jadwal = Json.objAt(jadwalDelivery, i);
                boolean isFull = Json.bool(jadwal, "isFull");
                boolean isOpened = Json.bool(jadwal, "isOpened");

                final String readAbleJadwalPengantaran = Json.str(jadwal, "day") + " " + Json.str(jadwal, "sessi");
                boolean isSelected = selectedJadwalPengantaran.equals(readAbleJadwalPengantaran);

                boolean disabled = isFull || !isOpened;
                int textColor = disabled ? CustomColors.textMuted
                        : isSelected ? CustomColors.nearlyWhite : CustomColors.green;

                LinearLayout item = Ui.column(ctx);
                item.setGravity(Gravity.CENTER);
                Ui.padXY(item, 5, 10);
                item.setBackground(Ui.shape(ctx,
                        disabled ? CustomColors.textLightMuted
                                : isSelected ? CustomColors.green : CustomColors.nearlyWhite,
                        10,
                        disabled ? CustomColors.textLightMuted : CustomColors.green, 1));

                item.addView(centered(Ui.text(ctx, Json.str(jadwal, "day"), hb * 3.5f, textColor, true)));
                item.addView(centered(Ui.text(ctx, "Delivery " + Json.str(jadwal, "sessi"), hb * 3f, textColor)));
                item.addView(Ui.spaceH(ctx, 5));
                TextView jam = Ui.text(ctx, "Jam " + Json.str(jadwal, "rentangWaktu"), hb * 2.75f, textColor);
                jam.setGravity(Gravity.CENTER);
                item.addView(centered(jam));
                if (isFull) {
                    TextView full = Ui.text(ctx, "Full", hb * 3.25f, CustomColors.textDanger);
                    full.setPadding(0, Ui.dp(ctx, 5), 0, 0);
                    item.addView(centered(full));
                }
                TextView buka = Ui.text(ctx, isOpened ? "Buka" : "Full", hb * 3.25f,
                        isOpened ? CustomColors.textSuccess : CustomColors.textDanger);
                buka.setPadding(0, Ui.dp(ctx, 5), 0, 0);
                item.addView(centered(buka));

                final boolean tappable = !isFull && isOpened;
                Ui.onTap(item, () -> {
                    if (tappable) {
                        selectedJadwalPengantaran = readAbleJadwalPengantaran;
                        render();
                        if (onJadwalDeliverySelected != null) {
                            onJadwalDeliverySelected.run(jadwal);
                        }
                    }
                });

                // Container(margin: EdgeInsets.only(left: 5.0), width: horizontalBlock * 32.5)
                FrameLayout cell = new FrameLayout(ctx);
                cell.setPadding(Ui.dp(ctx, 5), 0, 0, 0);
                cell.addView(item, new FrameLayout.LayoutParams(Ui.dp(ctx, hb * 32.5f), Ui.WRAP));
                wrap.addView(cell, new android.view.ViewGroup.LayoutParams(Ui.WRAP, Ui.WRAP));
            }
            LinearLayout holder = Ui.column(ctx);
            holder.setPadding(0, Ui.dp(ctx, 10), 0, 0);
            holder.addView(wrap, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            addView(holder, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        TextView none = Ui.text(ctx, "Tidak ada jadwal pengantaran", 14f, 0xDD000000);
        none.setGravity(Gravity.CENTER);
        addView(none, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private static View centered(TextView tv) {
        tv.setLayoutParams(new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        return tv;
    }
}
