package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import id.laukpauk.data.NativeApiRepository;
import org.json.JSONObject;
/** Port of the market product listing API in lib/models/PasarModel.dart. */
public final class PasarModel {
 private PasarModel() {}
 public static void getProdukInThisPasar(Context c,String idPasar,String useLimit,String startFrom,String cariProduk,NativeApiClient.Callback cb) {
  JSONObject location = new JSONObject();
  String jadwal = NativePreferences.getString(c,"savedJadwalDeliveryCode","");
  try {
   if(jadwal != null && !jadwal.isEmpty()) location.put("jadwalDeliveryCode",jadwal);
  } catch(Exception ignored) {}
  java.util.Map<String,String> p = NativeApiRepository.params("useLimit",useLimit == null ? "" : useLimit);
  if(startFrom != null) p.put("startFrom",startFrom);
  if(cariProduk != null) p.put("cariProduk",cariProduk);
  if(jadwal != null && !jadwal.isEmpty()) p.put("jadwalDeliveryCode",jadwal);
  NativeApiRepository.get(c,"/Pasar/getProdukInThisPasar/"+idPasar,true,p,cb);
 }
}
