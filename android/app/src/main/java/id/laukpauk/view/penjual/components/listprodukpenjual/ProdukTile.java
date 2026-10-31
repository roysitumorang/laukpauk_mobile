package id.laukpauk.view.penjual.components.listprodukpenjual;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/**
 * The shared look of ListProdukPenjualItem / ProdukPaketPenjualItem:
 * Padding(h10) > Container(margin bottom 5, white, radius 10) > Padding(h5, v7.5) > InkWell > Padding(v7.5) >
 * ListTile(leading: round image, title, subtitle, trailing: trash icon).
 */
final class ProdukTile {
    private ProdukTile() {}

    /** `fotoUrl` null = the placeholder asset. `subtitle` is the already built subtitle column. */
    static View build(Context ctx, String fotoUrl, String namaUpper, View subtitle, Runnable onTap, Runnable onDelete) {
        float hb = Ui.hb(ctx);

        LinearLayout outer = Ui.column(ctx);
        Ui.pad(outer, 10, 0, 10, 0);

        LinearLayout box = Ui.column(ctx);
        box.setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10f));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        boxLp.bottomMargin = Ui.dp(ctx, 5);
        outer.addView(box, boxLp);

        LinearLayout inner = Ui.column(ctx);
        Ui.pad(inner, 5, 7.5f + 7.5f, 5, 7.5f + 7.5f); // Padding(h5, v7.5) + Padding(v7.5)
        box.addView(inner, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        Ui.onTap(inner, onTap);

        // ListTile: contentPadding horizontal 16
        LinearLayout tile = Ui.row(ctx);
        Ui.pad(tile, 16, 0, 16, 0);
        inner.addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ImageView leading;
        if (fotoUrl != null) {
            leading = new ImageView(ctx);
            Ui.loadNetworkCircle(leading, fotoUrl);
            tile.addView(leading, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 13.5f), Ui.dp(ctx, hb * 13.5f)));
        } else {
            leading = Ui.asset(ctx, "placeholder-150.png", hb * 13.5f, hb * 13.5f);
            tile.addView(leading);
        }
        tile.addView(Ui.spaceW(ctx, 16));

        LinearLayout text = Ui.column(ctx);
        TextView title = PenjualUi.text(ctx, namaUpper, 3.25f, CustomColors.textSuccess, true);
        text.addView(title);
        text.addView(subtitle);
        tile.addView(text, Ui.weightW(1f));
        tile.addView(Ui.spaceW(ctx, 16));

        ImageView trash = Ui.icon(ctx, R.drawable.ic_delete, hb * 3.5f, CustomColors.textDanger);
        Ui.onTap(trash, onDelete);
        tile.addView(trash);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        return outer;
    }
}
