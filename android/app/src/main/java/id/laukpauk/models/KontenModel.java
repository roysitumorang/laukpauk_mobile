package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Content listing/detail read APIs from lib/models/KontenModel.dart. */
public final class KontenModel {
 private KontenModel() {}
 public static void getKontenAplikasi(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Konten/getKonten",false,null,cb);
 }
 public static void getProdukKonten(Context c,String idKonten,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Konten/getKontenItemFromThisKonten/"+idKonten,true,null,cb);
 }
}
