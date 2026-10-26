package id.laukpauk.models;
import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
/** Read-side product APIs ported from lib/models/ProdukModel.dart. */
public final class ProdukModel {
 private ProdukModel() {}
 public static void getDetailProduk(Context c,String idProduk,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/detailProduk",false,NativeApiRepository.params("idProduk",idProduk == null ? "" : idProduk),cb);
 }
 public static void getLowestPrice(Context c,String idProduk,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/getLowestPriceOfThisProduct/"+idProduk,false,null,cb);
 }
 public static void isThisProductInCA(Context c,String idProduk,String latitude,String longitude,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/isThisProductInCA",false,NativeApiRepository.params("idProduk",idProduk,"latitude",latitude,"longitude",longitude),cb);
 }
 public static void isProductSellerAvailable(Context c,String idProduk,String jadwalDeliveryCode,NativeApiClient.Callback cb) {
  NativeApiRepository.get(c,"/Produk/isProductSellerAvailable",false,NativeApiRepository.params("idProduk",idProduk,"jadwalDeliveryCode",jadwalDeliveryCode),cb);
 }
}
