package id.laukpauk.view.pembeli.components.detailproduk;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.view.pembeli.DetailProdukActivity;

/** Port of lib/views/pembeli/components/DetailProduk/ProdukPenjualLainnya.dart. */
public class ProdukPenjualLainnya extends LainnyaList {
    private final String currentProduct;

    public ProdukPenjualLainnya(Context ctx, String currentProduct) {
        super(ctx, ServerPath.uploadGambarProduk, false, 7.5f, 2.75f, "Maaf mak, tidak ada produk lainnya…",
                CustomColors.textMuted, DetailProdukActivity.class, "idProduk");
        this.currentProduct = currentProduct;
        start();
    }

    @Override
    JsonArray fetch() {
        String idProduk = currentProduct;
        String params = HTTPRequest.getQueryString("idProduk=" + idProduk);

        Http.Response httpResponse = Http.getApi("/Produk/getProdukLainnya?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "produkLainnya");
        }
        return new JsonArray();
    }
}
