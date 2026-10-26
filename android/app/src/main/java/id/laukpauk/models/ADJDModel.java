package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
/** Delivery address / delivery schedule state helper, porting the persistent state portion of ADJDModel. */
public final class ADJDModel {
 private ADJDModel() {}
 public static boolean isAddressAndScheduleSet(Context c) {
  String lat = NativePreferences.getString(c,"savedLatitude","");
  String lng = NativePreferences.getString(c,"savedLongitude","");
  String schedule = NativePreferences.getString(c,"savedJadwalDeliveryCode","");
  return lat != null && !lat.isEmpty() && lng != null && !lng.isEmpty()
      && schedule != null && !schedule.isEmpty();
 }
 public static void getAddressLabel(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getDetailUser",true,null,cb);
 }
}
