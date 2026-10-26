package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
import java.util.Map;
/** Price/unit APIs ported from lib/models/UpdateHargaModel.dart. */
public final class UpdateHargaModel {
 private UpdateHargaModel() {}
 public static void hapusSatuan(Context c,String idSatuanProduk,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/HargaProduk/hapusHargaProduk/"+idSatuanProduk,true,null,cb);
 }
 public static void editSatuanProduk(Context c,String idSatuanProduk,Map<String,String> data,NativeApiClient.Callback cb) {
  String suffix=idSatuanProduk == null || idSatuanProduk.isEmpty() ? "" : "/"+idSatuanProduk;
  NativeApiRepository.postForm(c,"/HargaProduk/saveHargaProduk"+suffix,true,data,null,cb);
 }
}
