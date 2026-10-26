package id.laukpauk.models;

import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;

/** API-backed homepage data methods ported from lib/models/HomepageModel.dart. */
public final class HomepageModel {
    private HomepageModel() { }
    public static void getArtikel(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Artikel/getArtikel", false, null, cb);
    }
    public static void getJumlahVoucher(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/User/getJumlahVoucher", true, null, cb);
    }
    public static void getTotalDompet(Context c, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/User/getTotalDompet", true, null, cb);
    }
    public static void getDataPasar(Context c, String limitPasar, String namaPasar,
                                    NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Pasar/getPasar", false,
                NativeApiRepository.params("limitPasar", limitPasar == null ? "" : limitPasar,
                        "namaPasar", namaPasar == null ? "" : namaPasar), cb);
    }
    public static void getDataPasarAround(Context c, String latitude, String longitude,
                                          String jadwalDeliveryCode, String limitPasar,
                                          String namaPasar, NativeApiClient.Callback cb) {
        NativeApiRepository.get(c, "/Pasar/getPasarAroundOfThisLatLong", false,
                NativeApiRepository.params("latitude", latitude, "longitude", longitude,
                        "jadwalDeliveryCode", jadwalDeliveryCode,
                        "limitPasar", limitPasar == null ? "" : limitPasar,
                        "namaPasar", namaPasar == null ? "" : namaPasar), cb);
    }
}
