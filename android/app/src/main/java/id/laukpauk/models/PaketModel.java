package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
import java.util.Map;
/** API/data operations ported from lib/models/PaketModel.dart. */
public final class PaketModel {
 private PaketModel() {}
 public static void detailPaket(Context c,String idPaket,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/detailPaket",false,NativeApiRepository.params("idPaket",idPaket == null ? "" : idPaket),cb);
 }
 public static void addToCart(Context c,String idPaket,NativeApiClient.Callback cb) {
  Map<String,String> body=NativeApiRepository.params("cartCode",NativePreferences.getCartCode(c),
   "idProduk",idPaket,"jenisProduk","paket","quantity","1",
   "jadwalDeliveryCode",NativePreferences.getString(c,"savedJadwalDeliveryCode",""));
  NativeApiRepository.postForm(c,"/Keranjang/saveCart",false,body,null,cb);
 }
}
