package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/KodePromoModel.dart. */
public final class KodePromoModel {
 private KodePromoModel() {}
 public static void cekKodePromo(Context c,String kodePromo,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Promo/cekKodePromo/"+kodePromo,true,null,cb);
 }
}
