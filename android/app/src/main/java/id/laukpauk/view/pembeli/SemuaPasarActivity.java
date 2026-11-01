package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.homepage.PasarSekitarSaya;

/** Port of lib/views/pembeli/SemuaPasar.dart. No arguments. */
public class SemuaPasarActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, "Semua Pasar"));

        LinearLayout column = Ui.column(this);
        Ui.padXY(column, 10, 10);
        column.addView(new PasarSekitarSaya(this, true, false, true, true, false),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        // the Dart Column does not scroll; a ScrollView keeps a long list of markets reachable
        ScrollView scroll = new ScrollView(this);
        scroll.addView(column, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(scroll, Ui.weightH(1f));
        setRoot(root);
    }
}
