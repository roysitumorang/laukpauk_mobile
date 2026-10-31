package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.PaymentChannelModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/Checkout/PilihanMetodePembayaran.dart: the content of the payment
 * method bottom sheet (opened by CheckoutModel.showMetodePembayaran). Build it with an Activity context.
 * `checkoutCallback` receives {responseCheckout, dataCheckout} after a successful COD / TF_BCA / credit
 * card order (see PembayaranModel.buatOrderNextStep).
 */
public class PilihanMetodePembayaran extends LinearLayout {
    private final long totalTagihan;
    private final JsonObject checkoutData;
    private final Async.Done<JsonObject> checkoutCallback;

    private String selectedChannelCode, moreData;
    private JsonObject metodePembayaranTerpilihMap;

    private final List<String> eWallet = Arrays.asList("ovo", "linkaja", "dana");
    private final List<String> channelCodeNeedsMoreData = Arrays.asList("cod", "ovo", "linkaja", "dana",
            "kartu_kredit");

    private boolean switchCollectData = false;
    private View collectDataUI = null;
    private JsonArray channels; // null until loaded (FutureBuilder waiting)
    private CustomButton.Btn buatOrderButton;

    /** Dart: PilihanMetodePembayaran(this.context, this.checkoutData, this.totalTagihan, {checkoutCallback}). */
    public PilihanMetodePembayaran(Context ctx, Context sheetContext, JsonObject checkoutData,
                                   long totalTagihan, Async.Done<JsonObject> checkoutCallback) {
        this(ctx, checkoutData, totalTagihan, checkoutCallback);
    }

    public PilihanMetodePembayaran(Context ctx, JsonObject checkoutData, long totalTagihan,
                                   Async.Done<JsonObject> checkoutCallback) {
        super(ctx);
        this.checkoutData = checkoutData;
        this.totalTagihan = totalTagihan;
        this.checkoutCallback = checkoutCallback;
        setOrientation(VERTICAL);
        Ui.pad(this, 15, 20, 15, 20);
        render();

        BaseActivity a = CheckoutUi.activity(ctx);
        if (a != null) {
            a.async(() -> PaymentChannelModel.getPaymentChannels(), (JsonArray list) -> {
                channels = list;
                render();
            });
        }
    }

    private void backToChannelPayment() {
        switchCollectData = false;
        collectDataUI = null;
        render();
    }

    private void onChannelCodeSelected(String metodePembayaranSelected, JsonObject detailMetodePembayaran) {
        Context ctx = getContext();
        String selectedChannelCodeLC = metodePembayaranSelected.toLowerCase();
        boolean needsMoreData = channelCodeNeedsMoreData.contains(selectedChannelCodeLC);

        if (needsMoreData) {
            if (selectedChannelCodeLC.equals("kartu_kredit")) {
                View ui = new CreditCardData(ctx, selectedChannelCodeLC, totalTagihan, detailMetodePembayaran,
                        checkoutData, checkoutCallback, this::backToChannelPayment);
                switchCollectData = true;
                collectDataUI = ui;
                selectedChannelCode = selectedChannelCodeLC;
                metodePembayaranTerpilihMap = detailMetodePembayaran;
                render();
            }
            if (selectedChannelCodeLC.equals("cod")) {
                View ui = new UangYangDipersiapkan(ctx, totalTagihan, detailMetodePembayaran, checkoutData,
                        selectedChannelCodeLC, checkoutCallback, this::backToChannelPayment);
                switchCollectData = true;
                collectDataUI = ui;
                selectedChannelCode = selectedChannelCodeLC;
                metodePembayaranTerpilihMap = detailMetodePembayaran;
                render();
            }
            if (eWallet.contains(selectedChannelCodeLC)) {
                View ui = new NomorTelepon(ctx, detailMetodePembayaran, checkoutData, selectedChannelCodeLC,
                        checkoutCallback, this::backToChannelPayment);
                switchCollectData = true;
                collectDataUI = ui;
                selectedChannelCode = selectedChannelCodeLC;
                metodePembayaranTerpilihMap = detailMetodePembayaran;
                render();
            }
        } else {
            boolean isTFBCA = selectedChannelCodeLC.equals("tf_bca");

            Integer uniqueCode = null;
            if (isTFBCA) {
                uniqueCode = random(1, 200);
            }

            moreData = (uniqueCode != null) ? String.valueOf(uniqueCode) : "";
            selectedChannelCode = selectedChannelCodeLC;
            metodePembayaranTerpilihMap = detailMetodePembayaran;
            render();
        }
    }

    private int random(int min, int max) {
        return min + new Random().nextInt(max - min);
    }

    private void render() {
        final Context ctx = getContext();
        removeAllViews();
        buatOrderButton = null;
        if (switchCollectData) {
            if (collectDataUI != null) {
                CheckoutUi.detach(collectDataUI);
                addView(collectDataUI, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            return;
        }

        float hb = Ui.hb(ctx);
        boolean dataOK;
        if (channelCodeNeedsMoreData.contains(selectedChannelCode)) {
            dataOK = (selectedChannelCode != null && moreData != null)
                    ? moreData.length() >= 1 && metodePembayaranTerpilihMap != null
                    : false;
        } else {
            dataOK = selectedChannelCode != null;
        }

        LinearLayout col = Ui.column(ctx);

        LinearLayout head = Ui.row(ctx);
        LinearLayout titles = Ui.column(ctx);
        titles.addView(CheckoutUi.text(ctx, "Metode Pembayaran", hb * 4, CheckoutUi.TEXT, true));
        titles.addView(CheckoutUi.text(ctx, "Silahkan pilih metode pembayaran yang anda inginkan : ", hb * 3,
                CheckoutUi.TEXT, false));
        head.addView(titles, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        if (dataOK) {
            buatOrderButton = CustomButton.button(ctx, "Buat Orderan", this::buatOrder,
                    new CustomButton.Opts().color(CustomColors.green).pill());
            head.addView(buatOrderButton, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        col.addView(head, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, 10));

        if (channels == null) {
            col.addView(CheckoutUi.spinner(ctx, 25, 15), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            for (int i = 0; i < channels.size(); i++) {
                col.addView(channelRow(ctx, Json.objAt(channels, i), hb), new LinearLayout.LayoutParams(Ui.MATCH,
                        Ui.WRAP));
            }
        }

        ScrollView scroll = new ScrollView(ctx);
        scroll.addView(col, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(scroll, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private View channelRow(Context ctx, final JsonObject metodePembayaran, float hb) {
        String channelCode = Json.str(metodePembayaran, "channel_code");
        final String channelCodeLC = channelCode.toLowerCase();
        String channelCodeUC = channelCode.toUpperCase();

        long minimalPembayaranInt = Json.longVal(metodePembayaran, "minimalPembayaran");
        final boolean minimalPembayaranTerpenuhi = totalTagihan >= minimalPembayaranInt;

        LinearLayout tile = Ui.row(ctx);
        Ui.padXY(tile, 0, 4);
        tile.setAlpha(minimalPembayaranTerpenuhi ? 1f : 0.38f);

        RadioButton radio = new RadioButton(ctx);
        radio.setClickable(false);
        radio.setFocusable(false);
        radio.setButtonTintList(ColorStateList.valueOf(CustomColors.green));
        radio.setChecked(channelCodeLC.equals(selectedChannelCode));
        radio.setEnabled(minimalPembayaranTerpenuhi);
        tile.addView(radio, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        tile.addView(Ui.spaceW(ctx, 8));

        LinearLayout body = Ui.column(ctx);
        LinearLayout titleRow = Ui.row(ctx);
        titleRow.addView(Ui.asset(ctx, "payment/" + channelCodeUC + ".png", 60f));
        titleRow.addView(Ui.spaceW(ctx, 10));
        titleRow.addView(CheckoutUi.text(ctx, Json.str(metodePembayaran, "name"), hb * 3.5f, CheckoutUi.TEXT,
                false));
        body.addView(titleRow);
        if (!minimalPembayaranTerpenuhi) {
            body.addView(CheckoutUi.text(ctx, "Minimal pembayaran "
                    + Currency.formatRupiah(minimalPembayaranInt, 0), 14f, CustomColors.textDanger, false),
                    CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
        }
        tile.addView(body, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        if (minimalPembayaranTerpenuhi) {
            id.laukpauk.ui.Layouts.ripple(tile);
            tile.setOnClickListener(v -> onChannelCodeSelected(channelCodeLC, metodePembayaran));
        }
        return tile;
    }

    private void buatOrder() {
        BaseActivity a = CheckoutUi.activity(getContext());
        if (a == null || selectedChannelCode == null || metodePembayaranTerpilihMap == null || moreData == null) {
            return;
        }
        String channelCodeTerpilihLC = selectedChannelCode.toLowerCase();
        CheckoutUi.buatOrder(a, channelCodeTerpilihLC, metodePembayaranTerpilihMap, checkoutData, moreData,
                checkoutCallback, busy -> {
                    if (buatOrderButton != null) {
                        buatOrderButton.setBusy(busy);
                    }
                });
    }
}
