package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
import java.util.LinkedHashMap;
import java.util.Map;
/** Text-field subset of lib/models/TambahProdukModel.dart. Image/multipart upload is not implemented yet. */
public final class TambahProdukModel {
 private TambahProdukModel() {}
 public static void saveProduk(Context c,String idProduk,Map<String,String> fields,NativeApiClient.Callback cb) {
  String suffix=idProduk == null || idProduk.isEmpty() ? "" : "/"+idProduk;
  Map<String,String> body=new LinkedHashMap<>();
  if(fields!=null) body.putAll(fields);
  NativeApiRepository.postForm(c,"/Produk/saveProduk"+suffix,true,body,null,cb);
 }
}
