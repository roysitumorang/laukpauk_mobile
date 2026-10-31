package id.laukpauk.view.pembeli.components.keranjang;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.components.detailtransaksi.GiftTransaksiPembeli;
import id.laukpauk.view.penjual.PesananScreenUi;

/**
 * Port of lib/views/pembeli/components/Keranjang/ListKeranjang.dart (an Expanded in Dart: the parent gives
 * this view a weighted/MATCH layout).
 */
public class ListKeranjang extends FrameLayout {
    public ListKeranjang(Context ctx, JsonArray dataKeranjangPerPenjual, final Runnable onRefresh,
                         final Async.Done<String> onRemoveItemSuccessed, final Runnable onEditItemSuccessed) {
        super(ctx);
        float hb = Ui.hb(ctx);

        final Runnable refreshAction = new Runnable() {
            @Override
            public void run() {
                if (onRefresh != null) {
                    onRefresh.run();
                }
            }
        };

        if (dataKeranjangPerPenjual.size() >= 1) {
            final LinearLayout list = Ui.column(ctx);
            for (JsonElement item : dataKeranjangPerPenjual) {
                list.addView(new PenjualCard(ctx, id.laukpauk.core.Json.obj(item), onRemoveItemSuccessed,
                        onEditItemSuccessed));
            }
            final FrameLayout giftHolder = new FrameLayout(ctx);
            list.addView(giftHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            final SwipeRefreshLayout refresh = DetailUi.refreshable(ctx, list, null, false);
            refresh.setOnRefreshListener(() -> {
                refreshAction.run();
                refresh.setRefreshing(false); // Dart: onRefresh returns immediately
            });
            addView(refresh, new LayoutParams(Ui.MATCH, Ui.MATCH));

            // FutureBuilder<String>(future: LocalStorage.getCartCode()) -> GiftTransaksiPembeli(cartCode, useCard: true)
            final Context c = ctx;
            PesananScreenUi.run(ctx, () -> LocalStorage.getCartCode(), (String cartCode) -> {
                if (cartCode != null) {
                    giftHolder.addView(new GiftTransaksiPembeli(c, null, cartCode, false, true),
                            new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            });
        } else {
            LinearLayout content = Ui.column(ctx);
            content.setGravity(Gravity.CENTER);
            content.addView(Ui.asset(ctx, "empty-cart.png", hb * 85));
            content.addView(Ui.spaceH(ctx, hb * 15.0f));
            content.addView(CustomButton.button(ctx, "Belanja Yuk",
                    () -> Nav.pushAndRemoveUntil(getContext(), SplashScreenActivity.class, null),
                    new CustomButton.Opts().color(CustomButton.themeColor).pill()));
            final SwipeRefreshLayout refresh = DetailUi.refreshable(ctx, content, null, true);
            refresh.setOnRefreshListener(() -> {
                refreshAction.run();
                refresh.setRefreshing(false);
            });
            addView(refresh, new LayoutParams(Ui.MATCH, Ui.MATCH));
        }
    }
}
