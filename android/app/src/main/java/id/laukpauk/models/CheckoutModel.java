package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
/** Read-side checkout API port. Mutating checkout/payment is intentionally not exposed until request/response parity is verified. */
public final class CheckoutModel {
 private CheckoutModel() {}
 public static void getVoucher(Context c,int totalBelanja,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getVoucher",true,
   NativeApiRepository.params("entitasVoucher","id, nama, foto, minimumBelanja, nilai, jenis, kode, maximumUsage",
    "forThisTotalBelanja",String.valueOf(totalBelanja)),cb);
 }
 public static void getTotalKeranjang(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getTotalKeranjang",true,null,cb);
 }
 public static void getTotalOngkir(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getTotalOngkir",true,null,cb);
 }
 public static void getJumlahCart(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getJumlahCart",true,null,cb);
 }
 public static void getDiskonKategori(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/User/getDiskonKategori",true,null,cb);
 }
 public static void isJadwalDeliveryOpened(Context c,String jadwalDeliveryCode,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/JadwalDelivery/isJadwalDeliveryOpened",true,
   NativeApiRepository.params("jadwalDeliveryCode",jadwalDeliveryCode == null ? NativePreferences.getString(c,"savedJadwalDeliveryCode","") : jadwalDeliveryCode),cb);
 }
}
