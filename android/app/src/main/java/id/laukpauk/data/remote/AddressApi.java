package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.address.AddressResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** The signed-in buyer's delivery addresses (JWT required). */
public interface AddressApi {

    /** The addresses, {@code sort_by=main:desc} puts the main address first; {@code limit} is 10, 25, 50 or 100. */
    @GET("buyer/addresses")
    Call<AddressResponses.AddressList> list(@Query("limit") int limit, @Query("sort_by") String sortBy);

    /** A next page: {@code data.links.next}, a full URL. */
    @GET
    Call<AddressResponses.AddressList> page(@Url String next);

    /**
     * Creates an address. Body: {@code longitude}, {@code latitude}, {@code street_address}, {@code delivery_note},
     * {@code label}, {@code recipient_name}, {@code recipient_mobile_phone}, {@code main}.
     */
    @POST("buyer/addresses")
    Call<AddressResponses.Saved> create(@Body Map<String, Object> body);

    /** Updates an address; same body as {@link #create}. */
    @POST("buyer/addresses/{id}")
    Call<AddressResponses.Saved> update(@Path("id") String id, @Body Map<String, Object> body);
}
