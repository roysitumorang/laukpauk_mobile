package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/GiftModel.dart. */
public final class GiftModel {
 private GiftModel() {}
 public static void getGifts(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Gift/getGift",false,null,cb);
 }
 public static void getGift(Context c,String idGift,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Gift/getGift/"+idGift,false,null,cb);
 }
}
