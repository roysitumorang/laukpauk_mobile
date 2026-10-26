package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Seller product mutation API paths observed in lib/models/ProdukPenjualModel.dart. */
public final class ProdukPenjualModel {
 private ProdukPenjualModel() {}
 public static void toggleProdukPalingLaris(Context c,String endpoint,String idProduk,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/"+endpoint+"/"+idProduk,true,null,cb);
 }
 public static void ubahKategoriProduk(Context c,String idProduk,String kategori,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/ubahKategoriProduk/"+idProduk,true,
   NativeApiRepository.params("kategori",kategori),cb);
 }
 public static void hapusProduk(Context c,String idProduk,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/hapusProduk/"+idProduk,true,null,cb);
 }
 public static void hapusPaket(Context c,String idProduk,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/hapusPaket/"+idProduk,true,null,cb);
 }
}
