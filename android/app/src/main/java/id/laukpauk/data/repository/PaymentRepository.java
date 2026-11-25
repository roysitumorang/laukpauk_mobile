package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.BuyerCartItem;
import id.laukpauk.data.model.payment.BuyerTransaction;
import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.data.model.payment.PaymentChannelsResponse;
import id.laukpauk.data.model.payment.TransactionResponses;
import id.laukpauk.data.remote.PaymentApi;

/** Payment channels and placing the order ({@code buyer/payment_channels}, {@code POST buyer/transactions}). */
public class PaymentRepository extends BaseRepository {

    private final PaymentApi api;

    public PaymentRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(PaymentApi.class);
    }

    /** The payment channels for the amount to pay ({@code GET buyer/payment_channels?amount=...}). */
    public LiveData<Resource<List<PaymentChannel>>> getPaymentChannels(long amount) {
        return load(api.buyerPaymentChannels(Math.max(0, amount)), PaymentRepository::channels);
    }

    /** {@code data} of {@code GET buyer/payment_channels}, never null. */
    static List<PaymentChannel> channels(PaymentChannelsResponse body) {
        return body == null || body.data == null ? new ArrayList<PaymentChannel>() : body.data;
    }

    /** What the checkout sends with the order; see {@link #transactionBody}. */
    public static final class Order {
        public String cartId;
        public List<BuyerCartItem> cartItems;
        public String recipientName;
        public String recipientMobilePhone;
        public String buyerNote;
        public String deliveryAddressId;
        /** The chosen delivery time exactly as {@code GET buyer/delivery_plans/now} sent it. */
        public String deliveryDatetime;
        public boolean rewardPointsDiscounted;
        @Nullable public String voucherId;
        public long walletAmount;
        @Nullable public String paymentChannelId;
    }

    /**
     * {@code POST buyer/transactions}: success carries the transaction; an error carries the backend's
     * {@code message} (e.g. "voucher_id: quota exceeded").
     */
    public void createTransaction(Order order, ResultCallback<BuyerTransaction> callback) {
        enqueue(api.createTransaction(transactionBody(order)),
                (TransactionResponses.Created body) -> body == null ? null : body.data, callback);
    }

    /**
     * The request body: {@code cart_id} and {@code items[]} ({@code cart_item_id}, {@code buyer_note}) of
     * {@code GET buyer/cart}, the recipient, {@code buyer_note}, {@code delivery_address_id}, {@code delivery_datetime},
     * either {@code reward_points_discounted} or {@code voucher_id}, the wallet ({@code wallet_discounted},
     * {@code wallet_amount}) and {@code payment_channel_id} (the id of the chosen channel).
     */
    static Map<String, Object> transactionBody(Order o) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cart_id", nz(o.cartId));
        List<Map<String, Object>> items = new ArrayList<>();
        if (o.cartItems != null) {
            for (BuyerCartItem item : o.cartItems) {
                if (item == null || item.id == null) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("cart_item_id", item.id);
                row.put("buyer_note", nz(item.note).trim());
                items.add(row);
            }
        }
        body.put("items", items);
        body.put("recipient_name", nz(o.recipientName).trim());
        body.put("recipient_mobile_phone", nz(o.recipientMobilePhone).trim());
        body.put("buyer_note", nz(o.buyerNote).trim());
        body.put("delivery_address_id", nz(o.deliveryAddressId));
        body.put("delivery_datetime", nz(o.deliveryDatetime));
        body.put("reward_points_discounted", o.rewardPointsDiscounted);
        if (!o.rewardPointsDiscounted && o.voucherId != null && !o.voucherId.isEmpty()) {
            body.put("voucher_id", o.voucherId);
        }
        body.put("wallet_discounted", o.walletAmount > 0);
        body.put("wallet_amount", Math.max(0, o.walletAmount));
        if (o.paymentChannelId != null && !o.paymentChannelId.isEmpty()) {
            body.put("payment_channel_id", o.paymentChannelId);
        }
        return body;
    }

    private static String nz(@Nullable String s) {
        return s == null ? "" : s;
    }
}
