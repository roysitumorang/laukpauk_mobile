package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** API operations from lib/models/ResepModel.dart; callbacks carry raw JSON for native UI rendering. */
public final class ResepModel {
 private ResepModel() {}
 public static void getResep(Context c,NativeApiClient.Callback cb) {
  NativeApiClient.get(id.laukpauk.ApiConfiguration.baseUrl(),"/Resep/getResep",null,cb);
 }
 public static void getResepItem(Context c,String idResep,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Resep/getResepItemFromThisResep/"+idResep,false,
   NativeApiRepository.params("entitasResepItem","id, namaProduk, takaran, idProduk"),cb);
 }
}
