package id.laukpauk.view.pembeli.components.detailtransaksi;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/DetailTransaksi/StatusTransaksi.dart (4 step delivery indicator). */
public class StatusTransaksi extends LinearLayout {
    private static final int[] DELIVERY_ICONS = {
            R.drawable.ic_fa_check_circle_regular, R.drawable.ic_fa_box_open,
            R.drawable.ic_fa_shipping_fast, R.drawable.ic_fa_people_carry};
    private static final String[] ICON_LABELS = {"Diterima", "Proses", "Kirim", "Selesai"};

    /** Dart: StatusTransaksi(statusTransaksi, {iconWidth = 45.0, iconSize = 15, showIconLabel = false, iconLabelSize = 10}). */
    public StatusTransaksi(Context ctx, String statusTransaksi, double iconWidth, double iconSize,
                           boolean showIconLabel, double iconLabelSize) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        double totalIconWidth = iconWidth * 4;
        double totalJarakSeluruhIcon = totalIconWidth;
        double jarakPerIcon = (totalJarakSeluruhIcon / 3) - iconWidth;

        int statusDeliveryInt = 0;
        String statusTransaksiLS = statusTransaksi == null ? "" : statusTransaksi.toLowerCase();
        if (statusTransaksiLS.equals("approved")) {
            statusDeliveryInt = 1;
        }
        if (statusTransaksiLS.equals("processing")) {
            statusDeliveryInt = 2;
        }
        if (statusTransaksiLS.equals("otw")) {
            statusDeliveryInt = 3;
        }
        if (statusTransaksiLS.equals("received")) {
            statusDeliveryInt = 4;
        }

        int currentIndex = 1;
        for (int icon : DELIVERY_ICONS) {
            int widgetColor = (currentIndex <= statusDeliveryInt) ? CustomColors.green : CustomColors.textMuted;
            String iconLabel = ICON_LABELS[currentIndex - 1];

            currentIndex++;

            LinearLayout column = Ui.column(ctx);

            LinearLayout row = Ui.row(ctx);
            row.setGravity(Gravity.CENTER_VERTICAL);
            FrameLayout circle = new FrameLayout(ctx);
            circle.setBackground(Ui.shape(ctx, widgetColor, 50f));
            circle.addView(Ui.icon(ctx, icon, (float) iconSize, CustomColors.nearlyWhite),
                    new FrameLayout.LayoutParams(Ui.dp(ctx, (float) iconSize), Ui.dp(ctx, (float) iconSize),
                            Gravity.CENTER));
            row.addView(circle, new LinearLayout.LayoutParams(Ui.dp(ctx, (float) iconWidth),
                    Ui.dp(ctx, (float) iconWidth)));
            if (currentIndex <= DELIVERY_ICONS.length) {
                View line = new View(ctx);
                line.setBackgroundColor((currentIndex <= statusDeliveryInt) ? CustomColors.green
                        : CustomColors.textMuted);
                row.addView(line, new LinearLayout.LayoutParams(Ui.dp(ctx, (float) jarakPerIcon), Ui.dp(ctx, 3)));
            }
            column.addView(row, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

            if (showIconLabel) {
                TextView label = Ui.text(ctx, iconLabel, (float) iconLabelSize, 0xDD000000);
                label.setGravity(Gravity.CENTER);
                Ui.pad(label, 0, 2.5f, 0, 0);
                column.addView(label, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            }
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            addView(column, clp);
        }
    }

    public StatusTransaksi(Context ctx, String statusTransaksi) {
        this(ctx, statusTransaksi, 45.0, 15, false, 10);
    }
}
