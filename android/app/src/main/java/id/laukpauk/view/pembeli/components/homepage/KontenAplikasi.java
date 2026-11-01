package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;

import id.laukpauk.core.Json;
import id.laukpauk.model.KontenModel;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/HomePage/KontenAplikasi.dart. */
public class KontenAplikasi extends FrameLayout {
    public KontenAplikasi(Context ctx) {
        super(ctx);
        LinearLayout loading = Ui.column(ctx);
        loading.setPadding(0, Ui.dp(ctx, 25), 0, Ui.dp(ctx, 25));
        loading.addView(Loading.view(ctx));
        addView(loading, new LayoutParams(Ui.MATCH, Ui.WRAP));

        HomeUi.act(ctx).async(() -> KontenModel.getKontenAplikasi(), (JsonArray kontenAplikasi) -> {
            removeAllViews();
            if (kontenAplikasi != null && kontenAplikasi.size() >= 1) {
                LinearLayout column = Ui.column(getContext());
                for (int i = 0; i < kontenAplikasi.size(); i++) {
                    column.addView(new Konten(getContext(), Json.objAt(kontenAplikasi, i)),
                            new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
                addView(column, new LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });
    }
}
