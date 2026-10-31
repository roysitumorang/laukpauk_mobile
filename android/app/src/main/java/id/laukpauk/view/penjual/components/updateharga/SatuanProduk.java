package id.laukpauk.view.penjual.components.updateharga;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/**
 * Port of lib/views/penjual/components/UpdateHarga/SatuanProduk.dart.
 * Constructor: (ctx, idProduk, showNomorUrut, updateAble, showOptions); the short one uses the Dart defaults
 * (false, true, false).
 */
public class SatuanProduk extends LinearLayout {
    private final String idProduk;
    private final boolean showNomorUrut;
    private final boolean updateAble;
    private final boolean showOptions;

    private FrameLayout listHolder;

    public SatuanProduk(Context ctx, String idProduk) {
        this(ctx, idProduk, false, true, false);
    }

    public SatuanProduk(Context ctx, String idProduk, boolean showNomorUrut, boolean updateAble,
                        boolean showOptions) {
        super(ctx);
        this.idProduk = idProduk;
        this.showNomorUrut = showNomorUrut;
        this.updateAble = updateAble;
        this.showOptions = showOptions;
        setOrientation(VERTICAL);
        render();
        reload();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    /** Dart getSatuanProduk(). BLOCKING. */
    private JsonArray getSatuanProduk() {
        String entitasHargaProduk = "id, nama, harga, diskon, jenisHarga, keterangan, idProduk";
        String params = HTTPRequest.getQueryString(
                "idProduk=" + idProduk + "&entitasHargaProduk=" + entitasHargaProduk);

        Http.Response httpResponse = Http.getApi("/Produk/getHargaProduk?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "hargaProduk");
        }
        return new JsonArray();
    }

    private void render() {
        final Context ctx = getContext();

        LinearLayout header = Ui.row(ctx);
        header.addView(PenjualUi.text(ctx, "Daftar Satuan Produk", 4f, 0xDD000000, true));
        header.addView(Ui.spacer(ctx));
        TextView tambah = PenjualUi.text(ctx, "Tambah Harga Baru", 3.25f, CustomColors.textPrimary, false);
        Ui.onTap(tambah, () -> Nav.pushForResult(activity(), EditSatuanProdukActivity.class,
                Json.of("idProduk", idProduk, "appBarTitle", "Satuan Produk Baru", "isUpdate", false), result -> {
                    if (result != null && result.isJsonPrimitive()) {
                        if (Json.asBool(result, false)) {
                            reload();
                        } else {
                            PenjualUi.alertOk(activity(), "Tambah Satuan Produk",
                                    "Gagal menambah data satuan (harga) produk. Silahkan coba lagi !", 4f, 3.25f, null);
                        }
                    }
                }));
        header.addView(tambah);
        addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));

        listHolder = new FrameLayout(ctx);
        addView(listHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        showSpinner();
    }

    private void showSpinner() {
        Context ctx = getContext();
        listHolder.removeAllViews();
        FrameLayout box = new FrameLayout(ctx);
        Ui.padXY(box, 0, 25);
        box.addView(PenjualUi.spinner(ctx, 15), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));
        listHolder.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    /** setState(() { onSatuanProdukSuccessDeletedState++ }): fetch again; the old list stays until it arrives. */
    private void reload() {
        PenjualUi.async(getContext(), () -> getSatuanProduk(), satuanProduk -> renderList(satuanProduk));
    }

    private void renderList(JsonArray satuanProduk) {
        Context ctx = getContext();
        listHolder.removeAllViews();

        if (satuanProduk.size() >= 1) {
            LinearLayout column = Ui.column(ctx);
            int index = 0;
            for (JsonElement e : satuanProduk) {
                index++;
                column.addView(new SatuanProdukItem(ctx, Json.obj(e), showNomorUrut ? index : 0, showOptions,
                        updateAble, () -> reload(), () -> reload()), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            listHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        LinearLayout empty = PenjualUi.emptyState(ctx, 25f, 5f, "Daftar satuan (harga) tidak ada", 3f, 0xDD000000);
        Ui.padXY(empty, 0, 15);
        listHolder.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER));
    }
}
