package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
import java.util.LinkedHashMap;
import java.util.Map;
/** Checkout API adapter only. Do not treat this as a complete payment flow until payment actions and responses are tested. */
public final class PembayaranModel {
 private PembayaranModel() {}
 public static void buatOrder(Context c,Map<String,String> checkoutData,String channelCode,
                              String channelName,String moreData,String alamatDelivery,
                              NativeApiClient.Callback cb) {
  Map<String,String> body=new LinkedHashMap<>();
  if(checkoutData!=null) body.putAll(checkoutData);
  body.put("metodePembayaran",channelCode == null ? "" : channelCode);
  body.put("namaMetodePembayaran",channelName == null ? "" : channelName);
  body.put("moreDataMetodePembayaran",moreData == null ? "" : moreData);
  body.put("alamatDelivery",alamatDelivery == null ? "" : alamatDelivery);
  NativeApiRepository.postForm(c,"/api-v2/Transaksi/checkout",true,body,null,cb);
 }
}
