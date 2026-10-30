package id.laukpauk.view.penjual.components.updateharga;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.UpdateHargaModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/**
 * Port of lib/views/penjual/components/UpdateHarga/SatuanProdukItem.dart.
 * `nomorUrut` is the Dart dynamic `nomorUrut`: a positive number to show "N." or 0 for Dart's `false`.
 */
public class SatuanProdukItem extends LinearLayout {
    private final JsonObject satuan;
    private final int nomorUrut;
    private final boolean showOptions;
    private final boolean updateAble;
    private final Runnable onSatuanProdukDeleted;
    private final Runnable onSatuanProdukUpdated;

    public SatuanProdukItem(Context ctx, JsonObject satuan, int nomorUrut, boolean showOptions, boolean updateAble,
                            Runnable onSatuanProdukDeleted, Runnable onSatuanProdukUpdated) {
        super(ctx);
        this.satuan = satuan;
        this.nomorUrut = nomorUrut;
        this.showOptions = showOptions;
        this.updateAble = updateAble;
        this.onSatuanProdukDeleted = onSatuanProdukDeleted;
        this.onSatuanProdukUpdated = onSatuanProdukUpdated;
        setOrientation(VERTICAL);
        render();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    private EditText numberField(String hint, String initial) {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        EditText e = new EditText(ctx);
        PenjualUi.styleEdit(ctx, e, hint, InputType.TYPE_CLASS_NUMBER, hb * 3.25f, 0xDD000000, 0x61000000);
        int v = Ui.dp(ctx, Ui.vb(ctx) * 2);
        e.setPadding(Ui.dp(ctx, 15), v, Ui.dp(ctx, 15), v);
        e.setText(initial);
        return e;
    }

    private void render() {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        Ui.pad(this, 0, 0, 0, 10);

        String keterangan = Json.str(satuan, "keterangan");

        LinearLayout row = Ui.row(ctx);
        addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (nomorUrut > 0) {
            row.addView(PenjualUi.text(ctx, nomorUrut + ".", 3.25f, 0xDD000000, true));
            row.addView(Ui.spaceW(ctx, 10));
        }

        LinearLayout namaCol = Ui.column(ctx);
        namaCol.addView(PenjualUi.text(ctx, Json.str(satuan, "nama"), 3.25f, 0xDD000000, false));
        namaCol.addView(Ui.spaceH(ctx, 2.5f));
        TextView chip = PenjualUi.text(ctx, "Harga " + Json.str(satuan, "jenisHarga").toUpperCase(Locale.ROOT), 2.5f,
                CustomColors.nearlyWhite, false);
        chip.setBackground(Ui.shape(ctx, CustomColors.textPrimary, 10f));
        Ui.padXY(chip, 5, 1.5f);
        namaCol.addView(chip, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        row.addView(namaCol, Ui.weightW(1f));
        row.addView(Ui.spaceW(ctx, 15));

        if (updateAble) {
            // Editable fields (the Dart item shows them but never saves them)
            EditText harga = numberField("Harga", Json.str(satuan, "harga"));
            row.addView(harga, Ui.weightW(1f));
            row.addView(Ui.spaceW(ctx, 5));
            EditText diskon = numberField("Diskon", Json.str(satuan, "diskon"));
            row.addView(diskon, Ui.weightW(1f));
        } else {
            int harga = Json.parseInt(Json.str(satuan, "harga"), 0);
            int diskon = Json.parseInt(Json.str(satuan, "diskon"), 0);
            TextView hargaText = PenjualUi.text(ctx, Currency.formatRupiah(harga, 0), 3.25f, 0xDD000000, false);
            hargaText.setGravity(Gravity.END);
            row.addView(hargaText, Ui.weightW(1f));
            row.addView(Ui.spaceW(ctx, 5));
            TextView diskonText = PenjualUi.text(ctx, Currency.formatRupiah(diskon, 0), 3.25f, 0xDD000000, false);
            diskonText.setGravity(Gravity.END);
            row.addView(diskonText, Ui.weightW(1f));
        }

        if (showOptions) {
            LinearLayout options = Ui.row(ctx);
            ImageView edit = Ui.icon(ctx, R.drawable.ic_edit, hb * 3.25f, CustomColors.textWarning);
            Ui.onTap(edit, () -> onEdit());
            options.addView(edit);
            options.addView(Ui.spaceW(ctx, 10));
            ImageView hapus = Ui.icon(ctx, R.drawable.ic_delete, hb * 3.25f, CustomColors.textDanger);
            Ui.onTap(hapus, () -> onHapus());
            options.addView(hapus);
            row.addView(options, PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 15, 0, 0, 0));
        }

        if (keterangan.length() >= 1) {
            LinearLayout box = Ui.column(ctx);
            box.setBackground(Ui.shape(ctx, 0x4466CC99, 10f));
            Ui.pad(box, 10, 5 + 5, 10, 5);
            box.addView(PenjualUi.text(ctx, "Keterangan", 3.25f, 0xDD000000, true));
            box.addView(Ui.spaceH(ctx, 2));
            box.addView(PenjualUi.text(ctx, keterangan, 3f, 0xDD000000, false));
            box.addView(Ui.spaceH(ctx, 3.5f));
            addView(box, PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 0, 5, 0, 0));
        }
    }

    private void onEdit() {
        Nav.pushForResult(activity(), EditSatuanProdukActivity.class,
                Json.of("dataSatuan", satuan, "idProduk", Json.str(satuan, "idProduk")), result -> {
                    if (result != null && result.isJsonPrimitive()) {
                        if (Json.asBool(result, false)) {
                            onSatuanProdukUpdated.run();
                        } else {
                            PenjualUi.alertOk(activity(), "Update Satuan Produk",
                                    "Gagal memperbaharui data satuan (harga) produk. Silahkan coba lagi !", 4f, 3.25f,
                                    null);
                        }
                    }
                });
    }

    private void onHapus() {
        final BaseActivity activity = activity();
        UpdateHargaModel.konfirmasiHapusSatuan(activity, satuan, konfirmasiHapus -> {
            if (konfirmasiHapus != null && konfirmasiHapus) {
                final String idSatuanProduk = Json.str(satuan, "id");
                activity.async(() -> UpdateHargaModel.hapusSatuan(idSatuanProduk), statusHapusSatuan -> {
                    if (statusHapusSatuan) {
                        onSatuanProdukDeleted.run();
                    } else {
                        PenjualUi.alertOk(activity, "Hapus Satuan Produk",
                                "Gagal menghapus satuan (harga) produk. Silahkan coba lagi !", 4f, 3.25f, null);
                    }
                });
            }
        });
    }
}
