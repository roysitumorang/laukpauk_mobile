package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read-side seller history API from lib/models/HistoriPenjualanModel.dart. */
public final class HistoriPenjualanModel {
 private HistoriPenjualanModel() {}
 public static void getHistoriPenjualan(Context c,String idPenjual,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Penjual/getHistoriPenjualan"+(idPenjual == null ? "" : idPenjual),true,null,cb);
 }
 public static void getTotalTransaksiForThisPenjual(Context c,String idTransaksi,String idPenjual,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Transaksi/getTotalTransaksiForThisPenjual/"+idTransaksi+(idPenjual == null ? "" : idPenjual),true,null,cb);
 }
}
