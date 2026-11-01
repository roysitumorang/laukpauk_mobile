package id.laukpauk.view.penjual.components.listprodukpenjual;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ProdukPenjualModel;
import id.laukpauk.model.ShopModelUi;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.DetailProdukPenjualActivity;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.TambahProdukActivity;

/** Port of lib/views/penjual/components/ListProdukPenjual/ProdukPaketPenjualItem.dart. */
public class ProdukPaketPenjualItem extends LinearLayout {
    public ProdukPaketPenjualItem(final Context ctx, final JsonObject dataProduk,
                                  final Async.Done<Boolean> hapusPaketCallback,
                                  final Async.Done<Boolean> editProdukCallback) {
        super(ctx);
        setOrientation(VERTICAL);

        int harga = Json.parseInt(Json.str(dataProduk, "harga"), 0);
        int diskon = Json.parseInt(Json.str(dataProduk, "diskon"), 0);
        int hargaBersih = harga - diskon;

        final String idProduk = Json.str(dataProduk, "id");
        final String foto = Json.str(dataProduk, "foto");
        final String keterangan = Json.str(dataProduk, "keterangan");
        final String nama = Json.str(dataProduk, "nama");

        LinearLayout subtitle = Ui.column(ctx);
        subtitle.addView(Ui.spaceH(ctx, 2.5f));
        subtitle.addView(PenjualUi.text(ctx, Currency.formatRupiah(hargaBersih, 0), 2.75f, CustomColors.green,
                false));

        if (keterangan.length() >= 1) {
            LinearLayout k = Ui.column(ctx);
            k.addView(PenjualUi.text(ctx, "Keterangan Produk", 2.75f, 0xDD000000, false));
            k.addView(Ui.spaceH(ctx, 1.5f));
            k.addView(PenjualUi.text(ctx, keterangan, 3f, 0xDD000000, false));
            subtitle.addView(k, PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 0, 5, 0, 0));
        }

        android.widget.TextView edit = PenjualUi.text(ctx, "Edit Produk", 2.75f, CustomColors.textWarning, false);
        Ui.onTap(edit, () -> Nav.pushForResult((BaseActivity) ctx, TambahProdukActivity.class,
                Json.of("idProduk", idProduk, "jenisProduk", "paket"), result -> {
                    if (result != null && result.isJsonPrimitive()) {
                        editProdukCallback.run(Json.asBool(result, false));
                    }
                }));
        subtitle.addView(edit, PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 0, 10, 0, 0));

        String fotoUrl = (foto.length() >= 1)
                ? ServerPath.baseURL + "/" + ServerPath.uploadGambarPaket + "/compress/" + foto : null;

        View tile = ProdukTile.build(ctx, fotoUrl, nama.toUpperCase(Locale.ROOT), subtitle,
                () -> Nav.push(ctx, DetailProdukPenjualActivity.class,
                        Json.of("id", idProduk, "jenisProduk", "paket")),
                () -> {
                    final BaseActivity activity = (BaseActivity) ctx;
                    ProdukPenjualModel.showKonfirmasi(activity, "Konfirmasi Hapus Produk",
                            "Apakah anda yakin akan menghapus produk " + nama + " ini?",
                            handle -> ShopModelUi.alertActions(activity,
                                    CustomAlert.textButton(activity, "Batal", () -> handle.close(Boolean.FALSE)),
                                    CustomAlert.textButton(activity, "Lanjutkan", () -> handle.close(Boolean.TRUE))),
                            konfirmasiHapus -> {
                                if (konfirmasiHapus != null && konfirmasiHapus) {
                                    activity.async(() -> ProdukPenjualModel.hapusPaket(idProduk),
                                            statusHapusPaket -> hapusPaketCallback.run(statusHapusPaket));
                                }
                            });
                });
        addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
