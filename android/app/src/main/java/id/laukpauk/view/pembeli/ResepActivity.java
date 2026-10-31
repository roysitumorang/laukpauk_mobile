package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Locale;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.ResepModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.resep.ResepCard;

/** Port of lib/views/pembeli/Resep.dart (a Footer tab). No args. */
public class ResepActivity extends BaseActivity {
    private JsonArray resepState = new JsonArray();
    private JsonArray resepImmutableState = new JsonArray();
    private boolean isReadyToShow = false;
    private String pencarian = "";

    private FrameLayout bodyHolder;
    private FrameLayout listHolder;
    private View readyContent;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        bodyHolder = new FrameLayout(this);
        root.addView(bodyHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        bodyHolder.addView(Loading.view(this));
        initialization();
    }

    /** Dart: initialization(). */
    private void initialization() {
        ResepModel.getResep(this, dataResep -> {
            if (dataResep == null) {
                dataResep = new JsonArray();
            }
            isReadyToShow = true;
            resepState = dataResep;
            resepImmutableState = dataResep;
            render();
        });
    }

    private void render() {
        if (readyContent == null) {
            bodyHolder.removeAllViews();
            LinearLayout column = Ui.column(this);
            column.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            LinearLayout searchHolder = Ui.column(this);
            Ui.pad(searchHolder, 5, 10, 5, 10);
            searchHolder.addView(new Pencarian(this, string -> {
                String stringLC = string.toLowerCase(Locale.ROOT);
                JsonArray filteredResep = new JsonArray();
                for (JsonElement e : resepImmutableState) {
                    String namaResep = Json.str(Json.obj(e), "nama");
                    if (namaResep.toLowerCase(Locale.ROOT).indexOf(stringLC) >= 0) {
                        filteredResep.add(e);
                    }
                }
                pencarian = stringLC;
                resepState = filteredResep;
                renderList();
            }, () -> {
                pencarian = "";
                resepState = resepImmutableState;
                renderList();
            }, "Cari Resep dengan Nama", pencarian), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            column.addView(searchHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            listHolder = new FrameLayout(this);
            column.addView(listHolder, Ui.weightH(1f));
            readyContent = column;
            bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
        renderList();
    }

    private void renderList() {
        listHolder.removeAllViews();
        float hb = Ui.hb(this);
        View content;
        boolean fill;
        if (resepState.size() >= 1) {
            LinearLayout list = Ui.column(this);
            Ui.padXY(list, 10, 0);
            for (JsonElement resep : resepState) {
                list.addView(new ResepCard(this, resep), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            content = list;
            fill = false;
        } else {
            LinearLayout empty = Ui.column(this);
            empty.setGravity(Gravity.CENTER);
            empty.addView(Ui.asset(this, "empty.png", hb * 35));
            empty.addView(Ui.spaceH(this, 10));
            empty.addView(Ui.text(this, "Tidak Ada Resep", hb * 3.5f, CustomColors.textMuted));
            content = empty;
            fill = true;
        }
        SwipeRefreshLayout refresh = DetailUi.refreshable(this, content, () -> {
            ResepModel.getResep(this, dataResep -> {
                if (dataResep == null) {
                    dataResep = new JsonArray();
                }
                resepState = dataResep;
                resepImmutableState = dataResep;
                renderList();
            });
        }, fill);
        listHolder.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
