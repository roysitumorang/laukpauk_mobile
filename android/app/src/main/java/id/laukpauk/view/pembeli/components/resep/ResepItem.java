package id.laukpauk.view.pembeli.components.resep;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.AppState;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ResepModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.KeranjangActivity;

/** Port of lib/views/pembeli/components/Resep/ResepItem.dart (ingredient list + "Belanja Resep"). */
public class ResepItem extends LinearLayout {
    private final BaseActivity activity;
    private final String idResep;
    /** idItem -> {idProduk, quantity, idSatuan, subTotal}; insertion ordered like a Dart Map. */
    private JsonObject selectedItems = new JsonObject();
    private boolean isBusy = false;

    private final FrameLayout content;
    private final List<ResepItemItem> itemViews = new ArrayList<>();
    private final List<String> itemIds = new ArrayList<>();
    private LinearLayout summary;

    public ResepItem(Context ctx, String idResep) {
        super(ctx);
        this.activity = (BaseActivity) ctx;
        this.idResep = idResep;
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        addView(Ui.text(ctx, "Bahan - bahan yang diperlukan : ", hb * 4, 0xDD000000, true));
        addView(Ui.spaceH(ctx, hb * 5.0f));

        content = new FrameLayout(ctx);
        addView(content, new LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(DetailUi.smallSpinner(ctx, 15),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));

        activity.async(() -> ResepModel.getResepItem(this.idResep), resepItem -> {
            if (resepItem == null) {
                return; // FutureBuilder never got data
            }
            build(resepItem);
        });
    }

    private void build(JsonArray resepItem) {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        content.removeAllViews();

        if (resepItem.size() < 1) {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.padXY(empty, 0, 15);
            empty.addView(Ui.asset(ctx, "empty.png", hb * 30));
            empty.addView(Ui.spaceH(ctx, 5));
            empty.addView(Ui.text(ctx, "Bahan bahan yang diperlukan tidak ada", hb * 3,
                    CustomColors.textDanger));
            content.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        LinearLayout box = Ui.column(ctx);
        Ui.padXY(box, 15, 0);

        int indexData = 0;
        for (JsonElement el : resepItem) {
            final JsonObject item = Json.obj(el);
            final String itemId = Json.str(item, "id");
            boolean isThisItemSelected = selectedItems.has(itemId);
            indexData++;

            ResepItemItem view = new ResepItemItem(ctx, indexData, item,
                    this::onItemSelected, this::onItemCanceled, this::onQuantityChanged,
                    this::deteksiADJD, isThisItemSelected);
            itemViews.add(view);
            itemIds.add(itemId);
            box.addView(view, bottomMargin(10));
        }

        summary = Ui.column(ctx);
        box.addView(summary, new LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        renderSummary();
    }

    private LayoutParams bottomMargin(int dp) {
        LayoutParams lp = new LayoutParams(Ui.MATCH, Ui.WRAP);
        lp.bottomMargin = Ui.dp(getContext(), dp);
        return lp;
    }

    /** setState(() { selectedItems = ... }) */
    private void changed() {
        for (int i = 0; i < itemViews.size(); i++) {
            itemViews.get(i).setSelected(selectedItems.has(itemIds.get(i)));
        }
        renderSummary();
    }

    private void onItemSelected(JsonObject selectedItemIDAndQuantity) {
        JsonObject decodedSatuan = Json.parseObject(Json.str(selectedItemIDAndQuantity, "satuan"));

        String idItem = Json.str(selectedItemIDAndQuantity, "idItem");
        String idProduk = Json.str(selectedItemIDAndQuantity, "idProduk");
        String quantity = Json.str(selectedItemIDAndQuantity, "quantity");
        String idSatuan = Json.str(decodedSatuan, "id");
        int subTotal = Json.intVal(selectedItemIDAndQuantity, "subTotal");

        if (selectedItems.has(idItem)) {
            JsonObject data = selectedItems.getAsJsonObject(idItem);
            data.addProperty("idProduk", idProduk);
            data.addProperty("quantity", quantity);
            data.addProperty("idSatuan", idSatuan);
            data.addProperty("subTotal", subTotal);
            selectedItems.add(idItem, data);
        } else {
            selectedItems.add(idItem, Json.of("idProduk", idProduk, "quantity", quantity,
                    "idSatuan", idSatuan, "subTotal", subTotal));
        }
        changed();
    }

    private void onItemCanceled(JsonObject canceledItemIDAndQuantity) {
        String idItem = Json.str(canceledItemIDAndQuantity, "idItem");
        selectedItems.remove(idItem);
        changed();
    }

    private void onQuantityChanged(JsonObject itemIDAndQuantity) {
        String idItem = Json.str(itemIDAndQuantity, "idItem");
        String quantity = Json.str(itemIDAndQuantity, "quantity");
        int subTotal = Json.intVal(itemIDAndQuantity, "subTotal");

        JsonObject decodedSatuan = Json.parseObject(Json.str(itemIDAndQuantity, "satuan"));

        if (!selectedItems.has(idItem)) {
            return; // Dart: selectedItems[idItem] is null -> the callback throws
        }
        JsonObject data = selectedItems.getAsJsonObject(idItem);
        data.addProperty("subTotal", subTotal);
        data.addProperty("quantity", quantity);
        data.addProperty("idSatuan", Json.str(decodedSatuan, "id"));
        changed();
    }

    /** Dart: deteksiADJD closure. */
    private void deteksiADJD() {
        ADJDModel.deleteAlamatDanJadwalDelivery();
        HeaderModel.adjd(activity, null, null, null, (JsonObject adjdSet) -> {
            if (adjdSet == null) {
                return;
            }
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String kodeJadwalDelivery = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

            if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                AppState.get().setADJDSet(true);

                ResepUi.snackbar(activity,
                        "Anda sudah bisa berbelanja resep, silahkan lanjutkan belanjanya ya mak!",
                        2000, "OK", null);
            }
        });
    }

    /** The "Total Belanja" row and the "Belanja Resep" button (only while something is selected). */
    private void renderSummary() {
        if (summary == null) {
            return;
        }
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        summary.removeAllViews();

        boolean isBahanCartOK = selectedItems.size() >= 1;
        if (!isBahanCartOK) {
            return;
        }
        long totalBelanja = 0;
        for (String key : selectedItems.keySet()) {
            totalBelanja += Json.intVal(selectedItems.getAsJsonObject(key), "subTotal");
        }

        summary.addView(Ui.spaceH(ctx, 10));

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(Ui.text(ctx, "Total Belanja", hb * 4, 0xDD000000), new LayoutParams(0, Ui.WRAP, 1f));
        row.addView(Ui.text(ctx, Currency.formatRupiah(totalBelanja, 0), hb * 4, CustomColors.green, true));
        summary.addView(row, bottomMargin(15));

        View button = CustomButton.button(ctx, "Belanja Resep", this::belanjaResep,
                new CustomButton.Opts().color(CustomButton.themeColor).block().busy(isBusy));
        summary.addView(button, new LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void belanjaResep() {
        activity.async(() -> {
            String cartCode = LocalStorage.getCartCode();

            JsonObject jadwalDeliveryTerpilih = LocalStorage.savedJadwalDeliveryMap();
            JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();

            String dayDelivery = jadwalDeliveryTerpilih != null
                    ? Json.str(jadwalDeliveryTerpilih, "day") : "Hari Pengataran";
            String jadwalDeliveryCode = Json.strOrNull(savedADJD, "jadwalDeliveryCode");
            if (jadwalDeliveryCode == null) {
                return null; // Dart: assigning null to String throws
            }

            return Json.of("cartCode", cartCode, "selectedItems", Json.toJson(selectedItems),
                    "dayDelivery", dayDelivery, "jadwalDeliveryCode", jadwalDeliveryCode);
        }, httpBody -> {
            if (httpBody == null) {
                return;
            }
            ResepModel.belanjaResep(activity, httpBody, statusAddToCart -> {
                if (statusAddToCart != null && statusAddToCart) {
                    activity.async(() -> HeaderModel.getJumlahCart(), jumlahKeranjang -> {
                        if (jumlahKeranjang != null) {
                            AppState.get().setJumlahCart(jumlahKeranjang);
                        }
                        ResepUi.snackbar(activity,
                                "Berhasil menambah resep item ke keranjang belanja !", 2000,
                                "Lihat Keranjang",
                                () -> Nav.push(activity, KeranjangActivity.class, null));
                    });
                }
            });
        });
    }
}
