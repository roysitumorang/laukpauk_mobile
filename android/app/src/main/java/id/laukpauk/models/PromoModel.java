package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/PromoModel.dart API operations. */
public final class PromoModel {
 private PromoModel() {}
 public static void getProdukPromo(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Promo/getPromoAroundOfThisLatLong",true,
   NativeApiRepository.params("latitude",NativePreferences.getString(c,"savedLatitude",""),
   "longitude",NativePreferences.getString(c,"savedLongitude",""),
   "jadwalDeliveryCode",NativePreferences.getString(c,"savedJadwalDeliveryCode","")),cb);
 }
 public static void getDetailPromoItem(Context c,String idPromoItem,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Promo/getPromoItem/"+idPromoItem,false,null,cb);
 }
}
