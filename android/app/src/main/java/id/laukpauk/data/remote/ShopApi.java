package id.laukpauk.data.remote;

import id.laukpauk.data.model.shop.ShopResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Buyer view of a seller (versionCode is added by ApiClient). */
public interface ShopApi {

    @GET("Penjual/getDetailPenjual/{idPenjual}")
    Call<ShopResponses.Detail> getDetailPenjual(@Path("idPenjual") String idPenjual);

    @GET("Penjual/getProdukPenjual/{idPenjual}")
    Call<ShopResponses.ProdukList> getProdukPenjual(@Path("idPenjual") String idPenjual,
                                                    @Query("getProductEntities") String entities);

    @GET("Penjual/getPaketPenjual/{idPenjual}")
    Call<ShopResponses.PaketList> getPaketPenjual(@Path("idPenjual") String idPenjual,
                                                  @Query("getPaketEntities") String entities);
}
