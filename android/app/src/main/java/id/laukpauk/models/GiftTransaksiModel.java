package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/GiftTransaksiModel.dart. */
public final class GiftTransaksiModel {
 private GiftTransaksiModel() {}
 public static void getGiftTransaksi(Context c,String cartCode,NativeApiClient.Callback cb) {
  String code = cartCode == null ? NativePreferences.getCartCode(c) : cartCode;
  NativeApiRepository.get(c,"/gift/gift-transaksi",false,NativeApiRepository.params("cartCode",code),cb);
 }
}
