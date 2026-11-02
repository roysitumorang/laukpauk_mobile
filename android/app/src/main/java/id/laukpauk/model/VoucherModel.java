package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;

/** Port of lib/models/VoucherModel.dart. */
public final class VoucherModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public VoucherModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** Dart: getVoucherDetail(idVoucher) -> Future&lt;dynamic&gt;. BLOCKING. `dataVoucher`, or false on failure. */
    public JsonElement getVoucherDetail(String idVoucher) {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Voucher/getVoucher/" + idVoucher + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            return ShopModelUi.dyn(dataObject.get("dataVoucher"));
        }

        return ShopModelUi.FALSE;
    }
}
