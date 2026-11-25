package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.address.AddressResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** The signed-in buyer's delivery addresses, {@code buyer/delivery_addresses} (JWT required). */
public interface AddressApi {

    /** The addresses, the main one first; {@code limit} is 10, 25, 50 or 100. */
    @GET("buyer/delivery_addresses")
    Call<AddressResponses.AddressList> list(@Query("limit") int limit);

    /** A next page: {@code data.links.next}, a full URL. */
    @GET
    Call<AddressResponses.AddressList> page(@Url String next);

    /**
     * Creates an address. Body: {@code destination_longitude}, {@code destination_latitude},
     * {@code street_address}, {@code street_address_note}, {@code label}, {@code main}. 422 when the point is out of the delivery area or already saved.
     */
    @POST("buyer/delivery_addresses")
    Call<AddressResponses.Saved> create(@Body Map<String, Object> body);

    /** Updates an address; same body as {@link #create}, but the backend keeps the saved coordinates. */
    @PUT("buyer/delivery_addresses/{id}")
    Call<AddressResponses.Saved> update(@Path("id") String id, @Body Map<String, Object> body);
}
