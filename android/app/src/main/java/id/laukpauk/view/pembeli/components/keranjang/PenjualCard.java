package id.laukpauk.view.pembeli.components.keranjang;

import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.pembeli.ProdukDiDalamPasarActivity;

/** Port of lib/views/pembeli/components/Keranjang/PenjualCard.dart. */
public class PenjualCard extends LinearLayout {
    public PenjualCard(Context ctx, final JsonObject dataKeranjang, Async.Done<String> onRemoveItemSuccessed,
                       Runnable onEditItemSuccessed) {
        super(ctx);
        setOrientation(VERTICAL);
        // Card(elevation: 5, radius 10, default margin 4)
        setBackground(Ui.shape(ctx, CustomColors.white, 10f));
        setElevation(Ui.dp(ctx, 5));
        LinearLayout.LayoutParams own = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        int m = Ui.dp(ctx, 4);
        own.setMargins(m, m, m, m);
        setLayoutParams(own);
        Ui.padAll(this, 15);

        String lokasiPasar = Json.strOrNull(dataKeranjang, "lokasiPasar");
        if (lokasiPasar == null) {
            lokasiPasar = "Pasar Tanpa Identitas";
        }
        String namaPenjual = Json.strOrNull(dataKeranjang, "namaPenjual");
        if (namaPenjual == null) {
            namaPenjual = "Penjual Tidak Dikenal";
        }
        final String lokasiPasarFinal = lokasiPasar;

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(header, 0, 0, 0, 10);
        header.addView(Ui.icon(ctx, R.drawable.ic_location_on, 17, CustomColors.black));
        header.addView(Ui.spaceW(ctx, 10));

        LinearLayout names = new LinearLayout(ctx);
        names.setOrientation(HORIZONTAL);
        names.setGravity(Gravity.CENTER_VERTICAL);
        TextView penjual = Ui.text(ctx, namaPenjual, 14f, CustomColors.green);
        Ui.onTap(penjual, () -> Nav.push(getContext(), PenjualActivity.class,
                Json.of("id", Json.str(dataKeranjang, "idPenjual"))));
        names.addView(penjual);
        names.addView(Ui.text(ctx, " di ", 14f, CustomColors.nearlyBlack));
        TextView pasar = Ui.text(ctx, lokasiPasar, 14f, CustomColors.textInfo);
        Ui.onTap(pasar, () -> Nav.push(getContext(), ProdukDiDalamPasarActivity.class,
                Json.of("idPasar", Json.str(dataKeranjang, "pasarPenjual"), "namaPasar", lokasiPasarFinal)));
        names.addView(pasar);
        header.addView(names, Ui.weightW(1f));
        addView(header, new LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout cartProduct = Ui.column(ctx);
        JsonArray produkPenjual = Json.array(dataKeranjang, "produkPenjual");
        for (JsonElement produk : produkPenjual) {
            cartProduct.addView(new ProductPenjual(ctx, Json.obj(produk), onRemoveItemSuccessed,
                    onEditItemSuccessed), new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        addView(cartProduct, new LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
