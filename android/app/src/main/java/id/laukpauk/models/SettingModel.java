package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of the read-side settings APIs in lib/models/SettingModel.dart. */
public final class SettingModel {
 private SettingModel() {}
 public static void getSettingValue(Context c,String key,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Pengaturan/getSettingValue/"+key,true,null,cb);
 }
 public static void getContactUsWhatsappConfiguration(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Pengaturan/getContactUsWhatsappConfiguration",true,null,cb);
 }
}
