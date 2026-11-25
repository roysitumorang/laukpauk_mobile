package id.laukpauk.data.model.orders;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.List;

/** A buyer's transaction of {@code buyer/transactions} (create, list rows and detail). */
public class BuyerTransaction {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_CANCELLED = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_WITHDRAWAL_PENDING = 3;
    public static final int STATUS_WITHDRAWN = 4;
    public static final int STATUS_PROCESSED = 5;
    public static final int STATUS_SHIPPED = 6;
    public static final int STATUS_COMPLAINED = 7;
    public static final int STATUS_COMPLETED = 8;

    @Nullable public String id;
    @Json(name = "reference_number")
    @Nullable public String referenceNumber;
    public int status;
    @Json(name = "recipient_name")
    @Nullable public String recipientName;
    @Json(name = "recipient_mobile_phone")
    @Nullable public String recipientMobilePhone;
    @Json(name = "buyer_note")
    @Nullable public String buyerNote;
    @Json(name = "unique_code")
    public long uniqueCode;
    /** The items' total plus the unique code. */
    @Json(name = "shopping_cost")
    public long shoppingCost;
    /** 0 = delivery cost, 1 = shopping cost; null without a voucher. */
    @Json(name = "voucher_type")
    @Nullable public Integer voucherType;
    @Json(name = "voucher_discount")
    public long voucherDiscount;
    @Json(name = "reward_points_discount")
    public long rewardPointsDiscount;
    @Json(name = "wallet_discount")
    public long walletDiscount;
    @Json(name = "delivery_estimated_cost")
    public long deliveryEstimatedCost;
    @Json(name = "delivery_street_address")
    @Nullable public String deliveryStreetAddress;
    @Json(name = "delivery_street_address_note")
    @Nullable public String deliveryStreetAddressNote;
    /** e.g. "2026-11-26T07:00:00+07:00". */
    @Json(name = "delivery_datetime")
    @Nullable public String deliveryDatetime;
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
    @Json(name = "created_at")
    @Nullable public String createdAt;
    @Json(name = "withdrawal_reason")
    @Nullable public String withdrawalReason;
    /** The amount after the seller's revisions (out-of-stock lines, changed quantities). */
    @Json(name = "revised_payment_amount")
    public long revisedPaymentAmount;
    @Json(name = "refund_amount")
    public long refundAmount;
    @Nullable public List<BuyerTransactionItem> items;

    /** The items' total without the unique code. */
    public long itemsTotal() {
        return Math.max(0, shoppingCost - uniqueCode);
    }

    /** Cancelled or withdrawn. */
    public boolean isCancelled() {
        return status == STATUS_CANCELLED || status == STATUS_WITHDRAWN;
    }

    /** The voucher reduced the delivery cost (else the shopping cost). */
    public boolean isDeliveryVoucher() {
        return voucherType != null && voucherType == 0;
    }
}
