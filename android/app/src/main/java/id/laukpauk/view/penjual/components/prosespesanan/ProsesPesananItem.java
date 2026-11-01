package id.laukpauk.view.penjual.components.prosespesanan;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.keranjang.CartUi;
import id.laukpauk.view.penjual.PesananItemTidakTersediaActivity;
import id.laukpauk.view.penjual.PesananScreenUi;

/** Port of lib/views/penjual/components/ProsesPesanan/ProsesPesananItem.dart. */
public class ProsesPesananItem extends LinearLayout {
    private final JsonObject pesananItem;
    private final Async.Done<Boolean> pesananItemCallback;

    public ProsesPesananItem(Context ctx, JsonObject pesananItem, Async.Done<Boolean> pesananItemCallback) {
        super(ctx);
        this.pesananItem = pesananItem;
        this.pesananItemCallback = pesananItemCallback;
        setOrientation(VERTICAL);
        render();
    }

    /** Dart: pembatalanKlaimTidakAda(). Blocking. */
    private boolean pembatalanKlaimTidakAda() {
        Map<String, String> httpBody = new HashMap<String, String>();
        httpBody.put("idItem", Json.str(pesananItem, "id"));

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> body = HTTPRequest.postBody(httpBody);

        Http.Response httpResponse = Http.postApi("/Transaksi/pembatalanKlaimTidakAda?" + params, body);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusPembatalanKlaimTidakTersedia");
        }
        return false;
    }

    private void onTrailingTap(boolean isRejected) {
        final Context ctx = getContext();
        if (isRejected) {
            PesananScreenUi.run(ctx, () -> Boolean.valueOf(pembatalanKlaimTidakAda()), (Boolean status) -> {
                if (pesananItemCallback != null) {
                    pesananItemCallback.run(status);
                }
            });
        } else {
            Activity a = CartUi.activity(ctx);
            if (a instanceof BaseActivity) {
                Nav.pushForResult((BaseActivity) a, PesananItemTidakTersediaActivity.class,
                        Json.of("pesananItem", pesananItem), (JsonElement result) -> {
                            if (result != null && !result.isJsonNull() && pesananItemCallback != null) {
                                pesananItemCallback.run(Json.asBool(result, false));
                            }
                        });
            }
        }
    }

    private void render() {
        removeAllViews();
        Context ctx = getContext();
        float hb = Ui.hb(ctx);

        int quantity = Json.parseInt(Json.str(pesananItem, "quantity"), 0);
        String jenisProduk = Json.str(pesananItem, "jenisProduk");
        String namaSatuan = jenisProduk.toLowerCase().equals("paket") ? "pcs"
                : Json.strOrNull(pesananItem, "namaSatuan");
        namaSatuan = (namaSatuan != null) ? namaSatuan : "Satuan Tidak Diketahui";
        int harga = Json.parseInt(Json.str(pesananItem, "harga"), 0);
        int diskon = Json.parseInt(Json.str(pesananItem, "diskon"), 0);
        String fotoProduk = Json.str(pesananItem, "fotoProduk");
        String catatan = Json.str(pesananItem, "catatan");
        String namaProduk = Json.str(pesananItem, "namaProduk");
        String status = Json.strOrNull(pesananItem, "status");

        Boolean isProdukAvailable = null;
        boolean isRejected = false;
        if (status != null) {
            isRejected = status.toLowerCase().equals("rejected");
            isProdukAvailable = !isRejected;
        }

        // Container(margin bottom 5, decoration, padding vertical 10) > ListTile
        LinearLayout box = Ui.column(ctx);
        box.setBackground(Ui.shape(ctx, isRejected ? 0x11DC3545 : CustomColors.nearlyWhite, 10f,
                isRejected ? CustomColors.textDanger : CustomColors.green, 1.5f));
        Ui.pad(box, 0, 10, 0, 10);
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        boxLp.bottomMargin = Ui.dp(ctx, 5);
        addView(box, boxLp);

        LinearLayout tile = new LinearLayout(ctx);
        tile.setOrientation(HORIZONTAL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(tile, 16, 8, 16, 8);
        box.addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ImageView image = Ui.network(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/"
                + fotoProduk, hb * 13.5f, hb * 13.5f);
        Ui.clipRounded(image, hb * 13.5f / 2f);
        tile.addView(image);
        tile.addView(Ui.spaceW(ctx, 16));

        LinearLayout texts = Ui.column(ctx);
        tile.addView(texts, Ui.weightW(1f));

        // title
        texts.addView(Ui.text(ctx, namaProduk, hb * 3.75f, CustomColors.green, true));
        int chipColor = (isProdukAvailable != null)
                ? (isProdukAvailable ? CustomColors.textInfo : CustomColors.textDanger)
                : CustomColors.textMuted;
        String chipText = (isProdukAvailable != null)
                ? (isProdukAvailable ? "Tersedia" : "Tidak Tersedia")
                : "Belum diperiksa";
        TextView chip = Ui.text(ctx, chipText, hb * 2.5f, CustomColors.nearlyWhite);
        chip.setBackground(Ui.shape(ctx, chipColor, 10f));
        Ui.padAll(chip, 5);
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        chipLp.setMargins(0, Ui.dp(ctx, 2.5f), 0, Ui.dp(ctx, 5));
        texts.addView(chip, chipLp);

        // subtitle (ListTile subtitle style: bodyText2 in the caption colour)
        final int subColor = 0x8A000000;
        texts.addView(Ui.text(ctx, Currency.formatSatuan(quantity, "x " + namaSatuan), 14f, subColor));
        texts.addView(Ui.spaceH(ctx, 10));
        texts.addView(Ui.text(ctx, "Harga " + Currency.formatRupiah(harga, 0), 14f, CustomColors.darkGreen, true));
        if (diskon >= 1) {
            TextView d = Ui.text(ctx, "Diskon " + Currency.formatRupiah(diskon, 0), hb * 2.5f,
                    CustomColors.textWarning);
            Ui.pad(d, 0, 2.5f, 0, 0);
            texts.addView(d);
        }
        if (catatan.length() >= 1) {
            LinearLayout c = Ui.column(ctx);
            Ui.pad(c, 0, 5, 0, 0);
            c.addView(Ui.text(ctx, "Catatan Tambahan", 14f, subColor, true));
            c.addView(Ui.text(ctx, catatan, hb * 3, CustomColors.textMuted));
            texts.addView(c, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        // trailing
        final boolean rejectedFinal = isRejected;
        tile.addView(Ui.spaceW(ctx, 16));
        ImageView trailing = Ui.icon(ctx,
                isRejected ? R.drawable.ic_fa_check_circle_regular : R.drawable.ic_fa_minus_circle, hb * 4.5f,
                isRejected ? CustomColors.green : CustomColors.textDanger);
        Ui.onTap(trailing, () -> onTrailingTap(rejectedFinal));
        tile.addView(trailing);
    }
}
