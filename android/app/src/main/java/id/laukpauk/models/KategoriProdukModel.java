package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Port of lib/models/KategoriProdukModel.dart. */
public final class KategoriProdukModel {
 private KategoriProdukModel() {}
 public static void getKategoriProduk(Context c, Integer limitData, NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/KategoriProduk/getKategoriProduk",false,
   limitData == null ? null : NativeApiRepository.params("limitKategoriProduk",String.valueOf(limitData)),cb);
 }
 public static void getProdukInThisCategory(Context c,String idKategoriProduk,String select,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/getProductsInThisCategory/"+idKategoriProduk,false,
   select == null ? null : NativeApiRepository.params("select",select),cb);
 }
}
