package id.laukpauk.data.remote;

import id.laukpauk.data.model.search.SearchResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Search endpoint (JSON; ApiClient adds the auth and version headers). */
public interface SearchApi {

    @GET("Konten/pencarian")
    Call<SearchResponses.Suggestions> pencarian(@Query("term") String term);
}
