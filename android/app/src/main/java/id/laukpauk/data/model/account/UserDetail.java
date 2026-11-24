package id.laukpauk.data.model.account;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/** The signed-in buyer: {@code data} of {@code GET buyer/users/me}. Only the keys the app uses are declared. */
public class UserDetail {
    public String id;
    public String name;
    @Nullable public String username;
    @Nullable public String address;
    /** E.164, e.g. +6281234567890. */
    @Json(name = "mobile_phone")
    public String phoneNumber;
    @Json(name = "mobile_phone_confirmed_at")
    @Nullable public String mobilePhoneConfirmedAt;
    /** A full URL, or a legacy file name in the user upload folder; empty when none. */
    @Nullable public String photo;
    @Json(name = "referral_code")
    @Nullable public String referralCode;
    /** RFC 3339. */
    @Json(name = "created_at")
    @Nullable public String createdAt;
    /** Wallet (dompet) balance in rupiah; null when the backend has none. */
    @Nullable public Long wallet;

    public long walletOrZero() {
        return wallet == null ? 0L : wallet;
    }
}
