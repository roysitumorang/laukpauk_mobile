package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read-side and profile API operations from lib/models/ProfilSayaModel.dart. */
public final class ProfilSayaModel {
 private ProfilSayaModel() {}
 public static void getDetailActiveUser(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getDetailUser",true,null,cb);
 }
}
