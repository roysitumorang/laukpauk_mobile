package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
/** Read-side seller shop operations from lib/models/TokoSayaModel.dart. */
public final class TokoSayaModel {
 private TokoSayaModel() {}
 public static void getDetailToko(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Penjual/getDetailPenjual/"+NativePreferences.getCurrentUserId(c),true,null,cb);
 }
}
