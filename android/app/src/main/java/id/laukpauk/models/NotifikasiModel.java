package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read-side API operations ported from lib/models/NotifikasiModel.dart. */
public final class NotifikasiModel {
 private NotifikasiModel() {}
 public static void getUpdateNotifikasi(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getNotifikasi",true,null,cb);
 }
 public static void getNotifikasiKhusus(Context c,String idUser,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getNotifikasiKhusus"+(idUser == null || idUser.isEmpty() ? "" : "/"+idUser),true,null,cb);
 }
 public static void getJumlahNotifikasiKhusus(Context c,String status,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getJumlahNotifikasiKhusus",true,NativeApiRepository.params("status",status),cb);
 }
 public static void getJumlahNotifikasi(Context c,String status,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getJumlahNotifikasi",true,NativeApiRepository.params("status",status),cb);
 }
 public static void getDetailNotifikasiGlobal(Context c,String id,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Notifikasi/getNotifikasi/"+id,true,null,cb);
 }
}
