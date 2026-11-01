package id.laukpauk.view.pembeli.components.resep;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/Resep/ResepItemItem.dart (one ingredient row). */
public class ResepItemItem extends LinearLayout {
    private final BaseActivity activity;
    private final int indexData;
    private final JsonObject resepItem;
    private final Async.Done<JsonObject> selectedItemCallback;
    private final Async.Done<JsonObject> cancelSelectedItemCallback;
    private final Async.Done<JsonObject> quantityChangedCallback;
    private final Runnable deteksiADJD;
    private boolean isThisItemSelected;

    private String satuanProdukResepSelected = "0";
    private int quantitySelected = 1;

    // FutureBuilder state of getSatuanProdukResep
    private boolean satuanLoaded;
    private JsonArray satuanProdukResep = new JsonArray();

    /**
     * Dart: ResepItemItem(indexData, resepItem, selectedItemCallback, cancelSelectedItemCallback,
     * quantityChangedCallback, {required deteksiADJD, isThisItemSelected = false}).
     */
    public ResepItemItem(Context ctx, int indexData, JsonObject resepItem,
                         Async.Done<JsonObject> selectedItemCallback,
                         Async.Done<JsonObject> cancelSelectedItemCallback,
                         Async.Done<JsonObject> quantityChangedCallback,
                         Runnable deteksiADJD, boolean isThisItemSelected) {
        super(ctx);
        this.activity = (BaseActivity) ctx;
        this.indexData = indexData;
        this.resepItem = resepItem;
        this.selectedItemCallback = selectedItemCallback;
        this.cancelSelectedItemCallback = cancelSelectedItemCallback;
        this.quantityChangedCallback = quantityChangedCallback;
        this.deteksiADJD = deteksiADJD;
        this.isThisItemSelected = isThisItemSelected;
        setOrientation(VERTICAL);

        render();

        final String idProduk = Json.str(resepItem, "idProduk");
        activity.async(() -> getSatuanProdukResep(idProduk), list -> {
            satuanLoaded = list != null;
            satuanProdukResep = list != null ? list : new JsonArray();
            render();
        });
    }

    /** Parent rebuild with a new isThisItemSelected (Dart: the widget's constructor param). */
    public void setSelected(boolean selected) {
        if (this.isThisItemSelected != selected) {
            this.isThisItemSelected = selected;
            render();
        }
    }

    /** Dart: getSatuanProdukResep(idProduk). BLOCKING. */
    private JsonArray getSatuanProdukResep(String idProduk) {
        String params = HTTPRequest.getQueryString("idProduk=" + idProduk);

        Http.Response httpResponse = Http.getApi("/Produk/getHargaProduk?" + params);
        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "hargaProduk");
        }
        return new JsonArray();
    }

    private void render() {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        removeAllViews();

        String jenisHargaTerpilih = "", namaSatuanTerpilih = "";
        int hargaHargaTerpilih = 0, diskonHargaTerpilih = 0, hargaBersih;
        if (!satuanProdukResepSelected.equals("0")) {
            JsonObject map = Json.parseObject(satuanProdukResepSelected);
            jenisHargaTerpilih = Json.str(map, "jenisHarga");
            namaSatuanTerpilih = Json.str(map, "nama");
            hargaHargaTerpilih = Json.parseInt(Json.str(map, "harga"), 0);
            diskonHargaTerpilih = Json.parseInt(Json.str(map, "diskon"), 0);
        }
        hargaBersih = hargaHargaTerpilih - diskonHargaTerpilih;

        final boolean isSelected = isThisItemSelected;

        // Container(margin: bottom 10, padding: selected ? all 10 : null, decoration)
        if (isSelected) {
            Ui.padAll(this, 10);
            setBackground(Ui.shape(ctx, 0x3366CC99, 10));
        } else {
            Ui.padAll(this, 0);
            setBackground(null);
        }

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.TOP);
        addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));

        row.addView(Ui.text(ctx, indexData + ".", 14f, 0xDD000000, true));
        row.addView(Ui.spaceW(ctx, 5));

        LinearLayout column = Ui.column(ctx);
        row.addView(column, new LayoutParams(0, Ui.WRAP, 1f));

        LinearLayout top = new LinearLayout(ctx);
        top.setOrientation(HORIZONTAL);
        top.setGravity(Gravity.TOP);
        column.addView(top, new LayoutParams(Ui.MATCH, Ui.WRAP));

        top.addView(Ui.text(ctx, Json.str(resepItem, "namaProduk").toUpperCase(Locale.ROOT),
                hb * 3.5f, 0xDD000000, true), new LayoutParams(0, Ui.WRAP, 1f));

        if (!satuanLoaded) {
            top.addView(DetailUi.smallSpinner(ctx, 10), new LayoutParams(Ui.dp(ctx, 10), Ui.dp(ctx, 10)));
        } else if (satuanProdukResep.size() >= 1) {
            LinearLayout controls = new LinearLayout(ctx);
            controls.setOrientation(HORIZONTAL);
            controls.setGravity(Gravity.CENTER_VERTICAL);

            // DropdownButton<String> of satuan
            String currentLabel = satuanProdukResepSelected.equals("0") ? "Pilih Satuan"
                    : Json.str(Json.parseObject(satuanProdukResepSelected), "nama");
            final java.util.ArrayList<String> labels = new java.util.ArrayList<>();
            labels.add("Pilih Satuan");
            for (JsonElement s : satuanProdukResep) {
                labels.add(Json.str(Json.obj(s), "nama"));
            }
            controls.addView(dropdown(currentLabel, labels, hb * 3, index -> {
                if (index == 0) {
                    return; // Dart: jsonDecode('0') is not a Map -> the handler throws, nothing happens
                }
                String selectedSatuan = Json.toJson(satuanProdukResep.get(index - 1));
                JsonObject selectedSatuanMap = Json.parseObject(selectedSatuan);
                String jenisHarga = Json.str(selectedSatuanMap, "jenisHarga");
                boolean isHargaPas = jenisHarga.toLowerCase(Locale.ROOT).equals("pas");
                int quantity = isHargaPas ? 1 : quantitySelected;

                int harga = Json.parseInt(Json.str(selectedSatuanMap, "harga"), 0);
                int diskon = Json.parseInt(Json.str(selectedSatuanMap, "diskon"), 0);
                int hb2 = harga - diskon;
                int subTotal = hb2 * quantity;

                JsonObject data = Json.of("idItem", Json.str(resepItem, "id"), "satuan", selectedSatuan,
                        "idProduk", Json.str(resepItem, "idProduk"), "quantity", quantity,
                        "subTotal", subTotal);
                selectedItemCallback.run(data);
                satuanProdukResepSelected = selectedSatuan;
                if (isHargaPas) {
                    quantitySelected = quantity;
                }
                render();
            }));

            if (jenisHargaTerpilih.toLowerCase(Locale.ROOT).equals("tidak pas")) {
                final java.util.ArrayList<String> qtyLabels = new java.util.ArrayList<>();
                for (int q = 1; q <= 10; q++) {
                    qtyLabels.add(q + " Item");
                }
                controls.addView(dropdown(quantitySelected + " Item", qtyLabels, hb * 3, index -> {
                    int quantity = index + 1;
                    JsonObject satuanProdukMap = Json.parseObject(satuanProdukResepSelected);

                    int harga = Json.parseInt(Json.str(satuanProdukMap, "harga"), 0);
                    int diskon = Json.parseInt(Json.str(satuanProdukMap, "diskon"), 0);
                    int hb2 = harga - diskon;
                    int subTotal = hb2 * quantity;

                    JsonObject data = Json.of("idItem", Json.str(resepItem, "id"),
                            "satuan", satuanProdukResepSelected, "quantity", quantity,
                            "subTotal", subTotal);
                    quantityChangedCallback.run(data);

                    quantitySelected = quantity;
                    render();
                }));
            }

            android.widget.ImageView cart = Ui.icon(ctx,
                    !isSelected ? R.drawable.ic_add_shopping_cart : R.drawable.ic_delete,
                    hb * 4, !isSelected ? CustomColors.green : CustomColors.textDanger);
            LayoutParams cartLp = new LayoutParams(Ui.dp(ctx, hb * 4), Ui.dp(ctx, hb * 4));
            cartLp.leftMargin = Ui.dp(ctx, 15);
            cart.setOnClickListener(v -> onCartTap(isSelected));
            controls.addView(cart, cartLp);

            top.addView(controls, new LayoutParams(Ui.WRAP, Ui.WRAP));
        } else {
            TextView none = Ui.text(ctx, "Tidak ada satuan", hb * 3, CustomColors.textMuted);
            none.setTypeface(none.getTypeface(), android.graphics.Typeface.ITALIC);
            top.addView(none, new LayoutParams(Ui.WRAP, Ui.WRAP));
        }

        column.addView(Ui.text(ctx, "Diperlukan " + Json.str(resepItem, "takaran"), hb * 3, 0xDD000000));

        if (isSelected) {
            LinearLayout sel = Ui.column(ctx);
            LayoutParams selLp = new LayoutParams(Ui.MATCH, Ui.WRAP);
            selLp.topMargin = Ui.dp(ctx, 5);
            sel.addView(Ui.text(ctx, "Diorder " + Currency.formatSatuan(quantitySelected,
                    "x " + namaSatuanTerpilih + " @ " + Currency.formatRupiah(hargaBersih, 0)),
                    hb * 3, 0xDD000000));
            sel.addView(Ui.spaceH(ctx, 10));
            sel.addView(Ui.text(ctx, "Sub Total " + Currency.formatRupiah((long) hargaBersih * quantitySelected, 0),
                    hb * 3.25f, CustomColors.textSuccess, true));
            column.addView(sel, selLp);
        }
    }

    /** The cart/trash icon's GestureDetector.onTap. */
    private void onCartTap(final boolean isSelected) {
        boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            String satuanTerpilih = satuanProdukResepSelected;
            int harga, diskon, hargaBersih, subTotal;

            if (satuanProdukResepSelected.equals("0")) {
                JsonObject first = Json.obj(satuanProdukResep.get(0));
                String encodedSatuan = Json.toJson(first);
                satuanProdukResepSelected = encodedSatuan;
                satuanTerpilih = encodedSatuan;

                harga = Json.has(first, "harga") ? Json.parseInt(Json.str(first, "harga"), 0) : 0;
                diskon = Json.has(first, "diskon") ? Json.parseInt(Json.str(first, "diskon"), 0) : 0;
                render();
            } else {
                JsonObject satuanTerpilihMap = Json.parseObject(satuanTerpilih);
                harga = Json.parseInt(Json.str(satuanTerpilihMap, "harga"), 0);
                diskon = Json.parseInt(Json.str(satuanTerpilihMap, "diskon"), 0);
            }

            hargaBersih = harga - diskon;
            subTotal = hargaBersih * quantitySelected;

            JsonObject dataToManipulate = Json.of("idItem", Json.str(resepItem, "id"),
                    "satuan", satuanTerpilih, "idProduk", Json.str(resepItem, "idProduk"),
                    "quantity", quantitySelected, "subTotal", subTotal);

            if (!isSelected) {
                selectedItemCallback.run(dataToManipulate);
            } else {
                cancelSelectedItemCallback.run(dataToManipulate);
            }
        } else {
            ProdukModel.adjdNotSetAlert(activity, lakukanDeteksiADJD -> {
                if (lakukanDeteksiADJD != null && lakukanDeteksiADJD) {
                    deteksiADJD.run();
                }
            });
        }
    }

    /** A DropdownButton(isDense, no underline): the current label + arrow; tapping pops up the choices. */
    private View dropdown(String current, final List<String> labels, float textSizeSp,
                          final Async.Done<Integer> onSelected) {
        final Context ctx = getContext();
        final LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.addView(Ui.text(ctx, current, textSizeSp, CustomColors.nearlyBlack));
        box.addView(Ui.icon(ctx, R.drawable.ic_arrow_drop_down, 24, 0xFF757575));
        box.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(ctx, box);
            for (int i = 0; i < labels.size(); i++) {
                menu.getMenu().add(0, i, i, labels.get(i));
            }
            menu.setOnMenuItemClickListener(item -> {
                onSelected.run(item.getItemId());
                return true;
            });
            menu.show();
        });
        return box;
    }
}
