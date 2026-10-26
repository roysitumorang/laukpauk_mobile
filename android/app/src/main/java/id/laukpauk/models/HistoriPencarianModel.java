package id.laukpauk.models;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
/** Native persistence port for lib/models/HistoriPencarianModel.dart. */
public final class HistoriPencarianModel {
 private static final String FILE="laukpauk_search_history";
 private static final String KEY="queries";
 private HistoriPencarianModel() {}
 public static synchronized JSONArray getHistoriPencarian(Context c) {
  try { return new JSONArray(c.getSharedPreferences(FILE,Context.MODE_PRIVATE).getString(KEY,"[]")); }
  catch(Exception e) { return new JSONArray(); }
 }
 public static synchronized void addHistoriPencarian(Context c,String query) {
  if(query == null || query.trim().isEmpty()) return;
  JSONArray old=getHistoriPencarian(c), next=new JSONArray();
  next.put(query.trim());
  for(int i=0;i<old.length() && next.length()<10;i++) {
   String value=old.optString(i);
   if(!value.equalsIgnoreCase(query.trim())) next.put(value);
  }
  c.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().putString(KEY,next.toString()).apply();
 }
}
