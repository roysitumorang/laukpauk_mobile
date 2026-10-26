package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/PaymentChannelModel.dart. */
public final class PaymentChannelModel {
 private PaymentChannelModel() {}
 public static void getPaymentChannels(Context c,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Xendit/getPaymentChannels",true,null,cb);
 }
}
