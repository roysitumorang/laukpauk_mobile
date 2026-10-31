package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.homepage.Artikel;
import id.laukpauk.view.pembeli.components.homepage.HeroSlider;
import id.laukpauk.view.pembeli.components.homepage.KontenAplikasi;
import id.laukpauk.view.pembeli.components.homepage.PasarSekitarSaya;
import id.laukpauk.view.pembeli.components.homepage.WalletAndVoucher;

/**
 * Port of lib/views/pembeli/HomePage.dart. Footer destination; no arguments.
 *
 * Dart's MainModel.initCallbackFromNativeCode (a MethodChannel that the native Android code used to
 * report "jadwalDeliveryKadaluarsa" / "updateAvailable") has no equivalent here: those watchers are
 * started by MainModel.startBackgroundWatchers and show their dialogs themselves.
 */
public class HomePageActivity extends BaseActivity {
    private int cobaLagi = 0;
    private Header header;
    private LinearLayout content;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        render();
        initialization();
    }

    /** _initialization(): gift check, "make sure ADJD" reminder, then the unread-notification count. */
    private void initialization() {
        HeaderModel.giftChecking(this, v1 -> {
            HeaderModel.makeSureADJD(this, v2 -> {
                async(() -> HeaderModel.getJumlahNotifikasi(), (Integer jumlahNotifikasi) -> {
                    AppState.get().setJumlahNotifikasi(jumlahNotifikasi);
                });
            });
        });
    }

    private void render() {
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);

        header = new Header(this, true, Integer.valueOf(cobaLagi), null);
        root.addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        content = Ui.column(this);
        refresh = new SwipeRefreshLayout(this);
        refresh.setColorSchemeColors(CustomColors.green);
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.addView(content, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        refresh.addView(scroll, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.MATCH));
        refresh.setOnRefreshListener(() -> {
            // onRefresh: setState(() => cobaLagi++) -> the whole page (and the Header's ADJD flow) is rebuilt
            cobaLagi++;
            buildContent();
            header.askADJD();
            refresh.setRefreshing(false);
        });
        root.addView(refresh, Ui.weightH(1f));
        buildContent();

        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
    }

    private void buildContent() {
        content.removeAllViews();
        content.addView(new HeroSlider(this));

        LinearLayout padded = Ui.column(this);
        Ui.pad(padded, 10, 10, 10, 0);
        padded.addView(new WalletAndVoucher(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(Ui.spaceH(this, 20.0f));
        padded.addView(new PasarSekitarSaya(this, true, true, false, false, true),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(new KontenAplikasi(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(new Artikel(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        padded.addView(Ui.spaceH(this, 15.0f));
        content.addView(padded, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
