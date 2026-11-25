package id.laukpauk.ui.orders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.laukpauk.R;
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.databinding.ItemOrdersMyOrdersBinding;
import id.laukpauk.util.Currency;

/** PesananSayaItem.dart: one transaction card of the list ({@code GET buyer/transactions} row). */
public class MyOrdersAdapter extends ListAdapter<BuyerTransaction, MyOrdersAdapter.Holder> {

    public interface Listener {
        void onOpen(BuyerTransaction t);

        void onOrderAgain(BuyerTransaction t);

        void onReceived(BuyerTransaction t);

        void onConfirmPayment(BuyerTransaction t);
    }

    private static final DiffUtil.ItemCallback<BuyerTransaction> DIFF = new DiffUtil.ItemCallback<BuyerTransaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull BuyerTransaction a, @NonNull BuyerTransaction b) {
            return OrderUi.nz(a.id).equals(OrderUi.nz(b.id));
        }

        @Override
        public boolean areContentsTheSame(@NonNull BuyerTransaction a, @NonNull BuyerTransaction b) {
            return a.status == b.status && a.paymentAmount == b.paymentAmount
                    && a.revisedPaymentAmount == b.revisedPaymentAmount
                    && OrderUi.nz(a.referenceNumber).equals(OrderUi.nz(b.referenceNumber))
                    && OrderUi.nz(a.deliveryDatetime).equals(OrderUi.nz(b.deliveryDatetime));
        }
    };

    private final Listener listener;

    public MyOrdersAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemOrdersMyOrdersBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position), listener);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        private final ItemOrdersMyOrdersBinding b;

        Holder(ItemOrdersMyOrdersBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(BuyerTransaction t, Listener listener) {
            android.content.Context c = b.getRoot().getContext();
            boolean cancelled = t.isCancelled();

            b.inner.setBackgroundResource(cancelled ? R.drawable.bg_orders_item_rejected
                    : R.drawable.bg_orders_item_normal);
            b.number.setText(OrderUi.nz(t.referenceNumber));
            b.method.setText(OrderUi.nz(t.paymentChannelName).toUpperCase());
            b.eta.setText(c.getString(R.string.orders_eta_format,
                    OrderUi.format(t.deliveryDatetime, "EEEE").toUpperCase(),
                    OrderUi.format(t.deliveryDatetime, "HH:mm")));
            b.time.setText(OrderUi.format(t.createdAt, "d MMMM yyyy HH:mm"));
            b.status.setText(c.getString(R.string.orders_order_status_format, OrderUi.statusLabel(c, t.status)));
            b.total.setText(Currency.formatRupiah(OrderUi.total(t), 0));
            b.total.setTextColor(ContextCompat.getColor(c, cancelled ? R.color.text_muted : R.color.green));

            b.progressRow.setVisibility(cancelled ? View.GONE : View.VISIBLE);
            b.steps.setup(OrderUi.step(t.status), 37.5f, 17f, true);
            b.btnOrderAgain.setVisibility(t.status == BuyerTransaction.STATUS_COMPLETED ? View.VISIBLE : View.GONE);
            b.btnReceived.setVisibility(t.status == BuyerTransaction.STATUS_SHIPPED ? View.VISIBLE : View.GONE);
            // manual bank transfer confirmation: no such channel any more
            b.btnPay.setVisibility(View.GONE);

            b.card.setOnClickListener(v -> listener.onOpen(t));
            b.btnOrderAgain.setOnClickListener(v -> listener.onOrderAgain(t));
            b.btnReceived.setOnClickListener(v -> listener.onReceived(t));
            b.btnPay.setOnClickListener(v -> listener.onConfirmPayment(t));
        }
    }
}
