package id.laukpauk.data.model.payment;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/** A buyer's transaction, as {@code POST buyer/transactions} answers it in {@code data} (the fields the app shows). */
public class BuyerTransaction {
    @Nullable public String id;
    @Json(name = "reference_number")
    @Nullable public String referenceNumber;
    public int status;
    /** What is left to pay after the discounts; 0 when the order is already paid. */
    @Json(name = "payment_amount")
    public long paymentAmount;
    /** e.g. "2026-11-26T00:34:00+07:00"; null when nothing is left to pay. */
    @Json(name = "payment_due_date")
    @Nullable public String paymentDueDate;
    @Json(name = "payment_channel_id")
    @Nullable public String paymentChannelId;
    @Json(name = "payment_channel_name")
    @Nullable public String paymentChannelName;
    @Json(name = "payment_virtual_account_number")
    @Nullable public String paymentVirtualAccountNumber;
    @Json(name = "payment_virtual_account_expired_at")
    @Nullable public String paymentVirtualAccountExpiredAt;
}
