package id.laukpauk.view.pembeli.components.notifikasi;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/Notifikasi/NotifikasiTab.dart. */
public class NotifikasiTab extends LinearLayout {

    public NotifikasiTab(Context ctx, String currentJenisNotifikasi, Async.Done<String> onTabClicked,
                         int jumlahNotifikasiGlobal, int jumlahNotifikasiKhusus) {
        super(ctx);
        setOrientation(HORIZONTAL);
        String jenisNotifikasi = currentJenisNotifikasi.toLowerCase();

        addView(tab(ctx, "Info", jenisNotifikasi.equals("update"), "update", onTabClicked),
                new LayoutParams(0, Ui.WRAP, 1f));
        addView(tab(ctx, "Notifikasi", jenisNotifikasi.equals("khususuntukkamu"), "khususuntukkamu", onTabClicked),
                new LayoutParams(0, Ui.WRAP, 1f));
    }

    private static FrameLayout tab(Context ctx, String label, boolean active, final String jenis,
                                   final Async.Done<String> onTabClicked) {
        FrameLayout box = new FrameLayout(ctx);
        box.setBackgroundColor(active ? 0x153A5160 : 0x9966CC99);
        Ui.padXY(box, 0, 15);
        Txt.Style style = new Txt.Style(14f)
                .color(active ? CustomColors.textMuted : CustomColors.nearlyWhite)
                .bold(!active);
        TextView text = Ui.text(ctx, label, style);
        box.addView(text, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
        Layouts.ripple(box);
        box.setOnClickListener(v -> onTabClicked.run(jenis));
        return box;
    }
}
