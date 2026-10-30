package id.laukpauk.view.pembeli.components.detailproduk;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/**
 * Shared body of ProdukPenjualLainnya and PaketPenjualLainnya (identical Dart widgets apart from
 * the endpoint, the texts, the image folder and the target screen). Package private.
 */
abstract class LainnyaList extends FrameLayout {
    private final String uploadFolder;
    private final boolean withBorder;
    private final float gapBelowImage;
    private final float nameSizeHb;
    private final String emptyText;
    private final int emptyColor;
    private final Class<? extends Activity> target;
    private final String targetArgKey;

    LainnyaList(Context ctx, String uploadFolder, boolean withBorder, float gapBelowImage, float nameSizeHb,
                String emptyText, int emptyColor, Class<? extends Activity> target, String targetArgKey) {
        super(ctx);
        this.uploadFolder = uploadFolder;
        this.withBorder = withBorder;
        this.gapBelowImage = gapBelowImage;
        this.nameSizeHb = nameSizeHb;
        this.emptyText = emptyText;
        this.emptyColor = emptyColor;
        this.target = target;
        this.targetArgKey = targetArgKey;
        addView(DetailUi.smallSpinner(ctx, 15), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));
    }

    /** BLOCKING: the "other items" array ([] when the request failed). */
    abstract JsonArray fetch();

    final void start() {
        Async.Work<JsonArray> work = this::fetch;
        Async.Done<JsonArray> done = list -> render(list != null ? list : new JsonArray());
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).async(work, done);
        } else {
            Async.run(work, done);
        }
    }

    private void render(JsonArray list) {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        removeAllViews();
        if (list.size() >= 1) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < list.size(); i++) {
                final JsonObject item = Json.objAt(list, i);
                LinearLayout cell = Ui.column(ctx);
                Layouts.ripple(cell);
                cell.setOnClickListener(v -> Nav.pushReplacement((Activity) getContext(), target,
                        Json.of(targetArgKey, Json.str(item, "id"))));

                ImageView image = new ImageView(ctx);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Ui.loadNetwork(image, ServerPath.baseURL + "/" + uploadFolder + "/compress/" + Json.str(item, "foto"));
                Ui.clipRounded(image, 10f);
                FrameLayout imageFrame = new FrameLayout(ctx);
                GradientDrawable bg = withBorder
                        ? Ui.shape(ctx, CustomColors.nearlyWhite, 10f, CustomColors.green, 0.5f)
                        : Ui.shape(ctx, CustomColors.nearlyWhite, 10f);
                imageFrame.setBackground(bg);
                imageFrame.addView(image, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                cell.addView(imageFrame, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 35), Ui.dp(ctx, hb * 22)));
                cell.addView(Ui.spaceH(ctx, gapBelowImage));

                TextView nama = Ui.text(ctx, Json.str(item, "nama"), hb * nameSizeHb, 0xDD000000);
                nama.setSingleLine(true);
                cell.addView(Layouts.hscroll(ctx, nama), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

                row.addView(cell, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 35), Ui.WRAP));
                row.addView(Ui.spaceW(ctx, 5));
            }
            addView(Layouts.hscroll(ctx, row), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, hb * 29)));
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.pad(empty, 0, 15, 0, 15);
            empty.addView(Ui.asset(ctx, "empty.png", hb * 20));
            empty.addView(Ui.spaceH(ctx, 5));
            empty.addView(Ui.text(ctx, emptyText, hb * 3.25f, emptyColor));
            addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
