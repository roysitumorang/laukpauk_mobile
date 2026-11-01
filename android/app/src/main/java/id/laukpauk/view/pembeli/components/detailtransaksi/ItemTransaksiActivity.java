package id.laukpauk.view.pembeli.components.detailtransaksi;

import android.os.Bundle;

import com.google.gson.JsonObject;

import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.core.CustomColors;

/**
 * ItemTransaksi pushed as a route (HistoriPenjualanItem).
 * Args: idTransaksi (String), idPenjual (String, optional), scaffold (boolean), cartCode (String, optional).
 */
public class ItemTransaksiActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        JsonObject args = args();
        String idTransaksi = Json.str(args, "idTransaksi");
        String idPenjual = Json.strOrNull(args, "idPenjual");
        String cartCode = Json.strOrNull(args, "cartCode");
        boolean scaffold = Json.bool(args, "scaffold");

        android.widget.LinearLayout root = id.laukpauk.ui.Ui.column(this);
        root.setBackgroundColor(CustomColors.white);
        root.addView(new ItemTransaksi(this, idTransaksi, idPenjual, scaffold, cartCode),
                new android.widget.LinearLayout.LayoutParams(id.laukpauk.ui.Ui.MATCH, id.laukpauk.ui.Ui.MATCH));
        setRoot(root);
    }
}
