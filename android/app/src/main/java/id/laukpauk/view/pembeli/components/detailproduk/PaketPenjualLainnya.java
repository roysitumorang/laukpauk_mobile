package id.laukpauk.view.pembeli.components.detailproduk;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.view.pembeli.DetailPaketActivity;

/** Port of lib/views/pembeli/components/DetailProduk/PaketPenjualLainnya.dart. */
public class PaketPenjualLainnya extends LainnyaList {
    private final String currentProduct;

    public PaketPenjualLainnya(Context ctx, String currentProduct) {
        super(ctx, ServerPath.uploadGambarPaket, true, 10f, 3.25f, "Maaf mak, tidak ada paket lainnya…",
                CustomColors.textDanger, DetailPaketActivity.class, "idPaket");
        this.currentProduct = currentProduct;
        start();
    }

    @Override
    JsonArray fetch() {
        String idPaket = currentProduct;
        String params = HTTPRequest.getQueryString("idPaket=" + idPaket);

        Http.Response httpResponse = Http.getApi("/Produk/getPaketLainnya?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "paketLainnya");
        }
        return new JsonArray();
    }
}
