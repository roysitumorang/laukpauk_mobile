package id.laukpauk.view.pembeli.components.produkdidalampasar;

import android.content.Context;

import com.google.gson.JsonObject;

/** The Dart class name of ProdukDiDalamPasarItem.dart (same constructor). */
public class ProdukInThisPasarItem extends ProdukDiDalamPasarItem {
    public ProdukInThisPasarItem(Context ctx, JsonObject dataProduk, Runnable deteksiADJD, Double widgetWidth) {
        super(ctx, dataProduk, deteksiADJD, widgetWidth);
    }

    public ProdukInThisPasarItem(Context ctx, JsonObject dataProduk, Runnable deteksiADJD) {
        super(ctx, dataProduk, deteksiADJD, null);
    }
}
