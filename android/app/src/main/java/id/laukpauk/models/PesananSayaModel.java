package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read API port for buyer order details; order state-changing actions need their original POST payloads. */
public final class PesananSayaModel {
 private PesananSayaModel() {}
 public static void getPesananDiterima(Context c,String idTransaksi,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Transaksi/pesananDiterima/"+idTransaksi,true,null,cb);
 }
}
