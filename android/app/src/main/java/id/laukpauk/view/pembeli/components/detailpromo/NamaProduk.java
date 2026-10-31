package id.laukpauk.view.pembeli.components.detailpromo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.model.ShopModelUi;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.TambahProdukActivity;

/** Port of lib/views/pembeli/components/DetailPromo/NamaProduk.dart. */
public class NamaProduk extends LinearLayout {

    private static final class CartResult {
        JsonObject response;
        int jumlahKeranjang;
    }

    /**
     * Dart: NamaProduk(detailProduk, currentUserIDString, editProdukCallback).
     * `editProdukCallback` receives the edit result (true/false) of TambahProduk.
     */
    public NamaProduk(Context ctx, JsonObject detailProduk, String currentUserIDString,
                      final Async.Done<Boolean> editProdukCallback) {
        super(ctx);
        setOrientation(HORIZONTAL);
        final BaseActivity activity = (BaseActivity) ctx;
        float hb = Ui.hb(ctx);

        String namaProduk = Json.str(detailProduk, "namaProduk");
        String namaKategoriProduk = Json.str(detailProduk, "namaKategoriProduk");
        final String idProduk = Json.str(detailProduk, "idProduk");
        String idPenjual = Json.str(detailProduk, "idPenjual");

        final String idSatuan = Json.str(detailProduk, "idSatuan");

        final String idPromo = Json.str(detailProduk, "idPromo");
        String statusPromo = Json.str(detailProduk, "statusPromo");
        String statusPromoLC = statusPromo.toLowerCase(Locale.ROOT);

        final boolean isItMyProduct = idPenjual.equals(currentUserIDString);

        LinearLayout left = Ui.column(ctx);
        TextView nama = Ui.text(ctx, namaProduk.toUpperCase(Locale.ROOT), hb * 4.5f,
                CustomColors.green, true);
        left.addView(nama);
        left.addView(Ui.spaceH(ctx, 5));
        left.addView(Ui.text(ctx, namaKategoriProduk.toUpperCase(Locale.ROOT), hb * 3f,
                CustomColors.textMuted));
        left.addView(Ui.spaceH(ctx, 15));
        addView(left, new LayoutParams(0, Ui.WRAP, 1f));
        addView(Ui.spaceW(ctx, 10));

        if (statusPromoLC.equals("aktif")) {
            CustomButton.Btn button = CustomButton.button(ctx, isItMyProduct ? "Edit" : "Beli", () -> {
                if (isItMyProduct) {
                    Nav.pushForResult(activity, TambahProdukActivity.class,
                            Json.of("idProduk", idProduk, "jenisProduk", "satuan"), result -> {
                                if (result == null || result.isJsonNull()) {
                                    return; // Dart: null can't be assigned to `bool`, callback never runs
                                }
                                if (editProdukCallback != null) {
                                    editProdukCallback.run(Json.asBool(result, false));
                                }
                            });
                } else {
                    boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
                    if (isADJDSet) {
                        activity.async(() -> {
                            CartResult r = new CartResult();
                            r.response = ProdukModel.addToCart(idProduk, idSatuan, "1", idPromo);
                            return r;
                        }, r -> {
                            if (r == null || r.response == null) {
                                return;
                            }
                            JsonObject response = r.response;
                            JsonElement status = Json.get(response, "statusAddCart");
                            if (status != null && !status.isJsonNull()) {
                                boolean statusAddToCart = Json.asBool(status, false);
                                String text = statusAddToCart
                                        ? "Berhasil ditambahkan ke dalam keranjang !"
                                        : response.has("message")
                                        ? Json.str(response, "message")
                                        : "Produk gagal masuk ke keranjang! Silahkan coba lagi!";
                                ShopModelUi.keranjangSnackbar(activity, text, 1);

                                activity.async(() -> HeaderModel.getJumlahCart(), jumlah -> {
                                    if (jumlah != null) {
                                        AppState.get().setJumlahCart(jumlah);
                                    }
                                });
                            }
                        });
                    } else {
                        ProdukModel.adjdNotSetAlert(activity, tentukanADJD -> {
                            // Dart: print('seharusnya tentukan lokasi') only
                        });
                    }
                }
            }, new CustomButton.Opts()
                    .color(!isItMyProduct ? CustomButton.themeColor : CustomButton.warningColor)
                    .pill());
            LayoutParams lp = new LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.gravity = Gravity.TOP;
            addView(button, lp);
        }
    }
}
