package id.laukpauk.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;

/** Port of lib/models/VoucherModel.dart. */
public final class VoucherModel {
    private VoucherModel() {}

    /** Dart: getVoucherDetail(idVoucher) -> Future&lt;dynamic&gt;. BLOCKING. `dataVoucher`, or false on failure. */
    public static JsonElement getVoucherDetail(String idVoucher) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Voucher/getVoucher/" + idVoucher + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            return ShopModelUi.dyn(dataObject.get("dataVoucher"));
        }

        return ShopModelUi.FALSE;
    }
}
