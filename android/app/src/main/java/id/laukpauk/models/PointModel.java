package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/PointModel.dart. */
public final class PointModel {
 public static final String CREATED_FROM_ADMIN = "admin";
 public static final String CREATED_FROM_TRANSAKSI = "transaksi";
 private PointModel() {}
 public static void getPoint(Context c,String queryString,NativeApiClient.Callback cb) {
  String q = queryString == null ? "historiPoint=true&point=true" : queryString;
  java.util.Map<String,String> params = new java.util.LinkedHashMap<>();
  for(String part : q.split("&")) {
   int i = part.indexOf('=');
   if(i > 0) params.put(part.substring(0,i),part.substring(i+1));
  }
  NativeApiRepository.get(c,"/Point/getPoint",true,params,cb);
 }
}
