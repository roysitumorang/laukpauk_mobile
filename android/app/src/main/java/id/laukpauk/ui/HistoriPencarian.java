package id.laukpauk.ui;

import android.content.Context;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.HistoriPencarianModel;

/** lib/global-components/HistoriPencarian.dart: the saved search history card. */
public class HistoriPencarian extends LinearLayout {
    private JsonArray historiPencarianState = new JsonArray();

    public HistoriPencarian(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        render();
        Async.run(() -> HistoriPencarianModel.getHistoriPencarian(), (JsonArray historiPencarian) -> {
            historiPencarianState = historiPencarian;
            render();
        });
    }

    private void render() {
        Context ctx = getContext();
        removeAllViews();
        boolean hPSExist = historiPencarianState.size() >= 1;

        Ui.padAll(this, 10);
        setBackground(Ui.shape(ctx, 0xFFF1F1F1, 10f));

        LinearLayout header = Ui.row(ctx);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.addView(Ui.icon(ctx, R.drawable.ic_history, 24, CustomColors.grey));
        header.addView(Ui.spaceW(ctx, 10));
        header.addView(Ui.text(ctx, "Histori Pencarian", 14f, CustomColors.nearlyBlack));
        addView(header, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));

        if (hPSExist) {
            LinearLayout list = Ui.column(ctx);
            for (int i = 0; i < historiPencarianState.size(); i++) {
                final JsonObject histori = Json.objAt(historiPencarianState, i);
                LinearLayout item = Ui.column(ctx);
                Ui.pad(item, 5, 0, 5, 10);
                item.addView(Ui.text(ctx, Json.str(histori, "title"), 14f, CustomColors.grey));
                item.addView(Ui.spaceH(ctx, 3));
                item.addView(Ui.text(ctx, Json.str(histori, "onTime"), 11.5f, CustomColors.textMuted));
                item.setOnClickListener(v -> Async.run(
                        () -> HistoriPencarianModel.addHistoriPencarian(Json.str(histori, "title")),
                        (Boolean ok) -> { }));
                list.addView(item, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            }
            addView(list, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            addView(Ui.text(ctx, "Belum ada pencarian ..", 11.5f, CustomColors.grey));
        }
    }
}
