package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read-side repeat-order API from lib/models/ROModel.dart. */
public final class ROModel {
 private ROModel() {}
 public static void getItemTransaksi(Context c,String query,NativeApiClient.Callback cb) {
  java.util.Map<String,String> p = new java.util.LinkedHashMap<>();
  if(query != null) for(String pair:query.split("&")) { int i=pair.indexOf('='); if(i>0) p.put(pair.substring(0,i),pair.substring(i+1)); }
  NativeApiRepository.get(c,"/RO/getItemTransaksi",true,p,cb);
 }
}
