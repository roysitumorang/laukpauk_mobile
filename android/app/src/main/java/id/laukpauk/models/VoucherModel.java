package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/VoucherModel.dart. */
public final class VoucherModel {
 private VoucherModel() {}
 public static void getVoucherDetail(Context c,String idVoucher,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Voucher/getVoucher/"+idVoucher,true,null,cb);
 }
 public static void getStatusVoucher(Context c,String idVoucher,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Voucher/getStatusVoucher/"+idVoucher,true,null,cb);
 }
}
