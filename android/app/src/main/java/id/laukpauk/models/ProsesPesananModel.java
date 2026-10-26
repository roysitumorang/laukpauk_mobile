package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read-side seller order operations from lib/models/ProsesPesananModel.dart. */
public final class ProsesPesananModel {
 private ProsesPesananModel() {}
 public static void getProsesPesanan(Context c,String idTransaksi,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Transaksi/prosesPesanan/"+idTransaksi,true,null,cb);
 }
 public static void getPesananMasukItem(Context c,String idTransaksi,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Penjual/getPesananMasukItem/"+idTransaksi,true,null,cb);
 }
}
