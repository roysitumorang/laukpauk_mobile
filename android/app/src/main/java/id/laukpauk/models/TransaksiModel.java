package id.laukpauk.models;

import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;

/** API portions of lib/models/TransaksiModel.dart. Session/UI side effects remain Activity-owned. */
public final class TransaksiModel {
    private TransaksiModel() { }
    public static void getDetailTransaksi(Context c, String idTransaksi, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Transaksi/getTransaksi/" + idTransaksi, true, null, cb);
    }
    public static void getJumlahItemDiantar(Context c, String idTransaksi, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Transaksi/getJumlahItemDiantar/" + idTransaksi, true, null, cb);
    }
    public static void getLastTransactionID(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Transaksi/getLastTransactionID", true, null, cb);
    }
    public static void responseBelanjaUlang(Context c, String idTransaksi, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Transaksi/belanjaUlang/" + idTransaksi, true, null, cb);
    }
}
