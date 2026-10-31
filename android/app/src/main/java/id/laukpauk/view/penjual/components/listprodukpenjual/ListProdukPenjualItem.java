package id.laukpauk.view.penjual.components.listprodukpenjual;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Async;
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

/** Port of lib/views/penjual/components/ListProdukPenjual/ListProdukPenjualItem.dart. */
public class ListProdukPenjualItem extends LinearLayout {
    public ListProdukPenjualItem(final Context ctx, final JsonObject dataProduk,
                                 final Async.Done<Boolean> hapusProdukCallback,
                                 final Async.Done<Boolean> editProdukCallback) {
        super(ctx);
        setOrientation(VERTICAL);

        String namaKategoriProduk = null;
        if (!Json.isNull(Json.get(dataProduk, "namaKategoriProduk"))) {
            namaKategoriProduk = Json.str(dataProduk, "namaKategoriProduk");
        }

        final String idProduk = Json.str(dataProduk, "id");
        final String foto = Json.str(dataProduk, "foto");
        final String keterangan = Json.str(dataProduk, "keterangan");
        final String nama = Json.str(dataProduk, "nama");

        LinearLayout subtitle = Ui.column(ctx);
        TextView kategori = PenjualUi.text(ctx,
                namaKategoriProduk != null ? namaKategoriProduk : "Kategori Produk Tidak Diketahui", 3f, 0xDD000000,
                false);
        if (namaKategoriProduk == null) {
            kategori.setTypeface(kategori.getTypeface(), Typeface.ITALIC);
        }
        subtitle.addView(kategori);

        if (keterangan.length() >= 1) {
            LinearLayout k = Ui.column(ctx);
            k.addView(PenjualUi.text(ctx, "Keterangan Produk", 3f, 0xDD000000, false));
            k.addView(Ui.spaceH(ctx, 1.5f));
            k.addView(PenjualUi.text(ctx, keterangan, 3f, 0xDD000000, false));
            subtitle.addView(k, PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 0, 5, 0, 0));
        }

        TextView edit = PenjualUi.text(ctx, "Edit Produk", 2.75f, CustomColors.textWarning, false);
        Ui.onTap(edit, () -> Nav.pushForResult((BaseActivity) ctx, TambahProdukActivity.class,
                Json.of("idProduk", idProduk, "jenisProduk", "satuan"), result -> {
                    if (result != null && result.isJsonPrimitive()) {
                        editProdukCallback.run(Json.asBool(result, false));
                    }
                }));
        subtitle.addView(edit, PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 0, 10, 0, 0));

        String fotoUrl = (foto.length() >= 1)
                ? ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/" + foto : null;

        View tile = ProdukTile.build(ctx, fotoUrl, nama.toUpperCase(Locale.ROOT), subtitle,
                () -> Nav.push(ctx, DetailProdukPenjualActivity.class,
                        Json.of("id", idProduk, "jenisProduk", "satuan")),
                () -> {
                    final BaseActivity activity = (BaseActivity) ctx;
                    ProdukPenjualModel.showKonfirmasi(activity, "Konfirmasi Hapus Produk",
                            "Apakah anda yakin akan menghapus produk " + nama + " ini?",
                            handle -> ShopModelUi.alertActions(activity,
                                    CustomAlert.textButton(activity, "Batal", () -> handle.close(Boolean.FALSE)),
                                    CustomAlert.textButton(activity, "Lanjutkan", () -> handle.close(Boolean.TRUE))),
                            konfirmasiHapus -> {
                                if (konfirmasiHapus != null && konfirmasiHapus) {
                                    activity.async(() -> ProdukPenjualModel.hapusProduk(idProduk),
                                            statusHapusProduk -> hapusProdukCallback.run(statusHapusProduk));
                                }
                            });
                });
        addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
