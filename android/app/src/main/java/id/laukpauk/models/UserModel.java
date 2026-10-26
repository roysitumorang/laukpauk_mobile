package id.laukpauk.models;

import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;

/** Native API port of lib/models/UserModel.dart. */
public final class UserModel {
    private UserModel() { }
    public static void getDetailActiveUser(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/User/getDetailUser", true, null, cb);
    }
    public static void getHistoriPoint(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/User/getRiwayatPoint", true, null, cb);
    }
}
