package id.laukpauk.ui;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import id.laukpauk.core.CustomColors;

/** lib/global-components/Yuhu.dart: the "selisih timbangan" notice card. */
public class Yuhu extends LinearLayout {
    public Yuhu(Context ctx) {
        super(ctx);
        float hb = Ui.hb(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackground(Ui.shape(ctx, 0x5566CC99, 8f));
        Ui.pad(this, 25, 15, 25, 15);
        LinearLayout.LayoutParams self = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        self.topMargin = Ui.dp(ctx, 15);
        setLayoutParams(self);

        ImageView img = Ui.asset(ctx, "icons/timbangan.png", hb * 10f);
        addView(img, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 10f), Ui.WRAP));
        addView(Ui.spaceW(ctx, 20));

        LinearLayout column = Ui.column(ctx);
        column.addView(Ui.text(ctx, "Yuhuuu…",
                new Txt.Style(hb * 3.5f).color(CustomColors.nearlyBlack).bold()));
        column.addView(Ui.spaceH(ctx, 3.25f));
        column.addView(Ui.text(ctx, "Akan kami hubungi jika ada selisih timbangan ya…",
                new Txt.Style(hb * 3.0f).color(CustomColors.nearlyBlack)));
        addView(column, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
    }
}
