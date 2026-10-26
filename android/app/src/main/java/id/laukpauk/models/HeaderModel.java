package id.laukpauk.models;

import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;

/** API portions of lib/models/HeaderModel.dart. UI dialogs are implemented natively by Activities. */
public final class HeaderModel {
    private HeaderModel() { }
    public static void getJumlahCart(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/User/getJumlahCart", true, null, cb);
    }
    public static void getJumlahNotifikasi(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/User/getJumlahNotifikasi", true, null, cb);
    }
}
