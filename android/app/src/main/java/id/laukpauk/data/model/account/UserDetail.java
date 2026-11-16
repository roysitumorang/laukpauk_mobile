package id.laukpauk.data.model.account;

import com.squareup.moshi.Json;

/** The signed-in user ("detailUser" of User/getDetailUser). */
public class UserDetail {
    public String id;
    @Json(name = "nama")
    public String name;
    public String username;
    @Json(name = "alamat")
    public String address;
    @Json(name = "nomorHP")
    public String phoneNumber;
    @Json(name = "foto")
    public String photo;
    @Json(name = "kodeReferal")
    public String referralCode;
    public String createdAt;
}
