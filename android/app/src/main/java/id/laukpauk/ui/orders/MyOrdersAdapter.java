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
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.databinding.ItemOrdersMyOrdersBinding;
import id.laukpauk.util.Currency;

/** PesananSayaItem.dart: one transaction card of the history list. */
public class MyOrdersAdapter extends ListAdapter<Transaction, MyOrdersAdapter.Holder> {

    public interface Listener {
        void onOpen(Transaction t);

        void onOrderAgain(Transaction t);

        void onReceived(Transaction t);

        void onConfirmPayment(Transaction t);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaction a, @NonNull Transaction b) {
            return OrderUi.nz(a.id).equals(OrderUi.nz(b.id));
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction a, @NonNull Transaction b) {
            return OrderUi.nz(a.status).equals(OrderUi.nz(b.status)) && a.totalPayment() == b.totalPayment()
                    && OrderUi.nz(a.transactionNumber).equals(OrderUi.nz(b.transactionNumber))
                    && OrderUi.nz(a.deliverySchedule).equals(OrderUi.nz(b.deliverySchedule));
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

        void bind(Transaction t, Listener listener) {
            String status = OrderUi.nz(t.status);
            String lc = status.toLowerCase();
            boolean rejected = lc.equals("rejected");
            String method = OrderUi.nz(t.paymentMethodName);

            b.inner.setBackgroundResource(rejected ? R.drawable.bg_orders_item_rejected
                    : R.drawable.bg_orders_item_normal);
            b.number.setText(OrderUi.nz(t.transactionNumber));
            b.method.setText(method.toUpperCase());
            b.eta.setText(b.getRoot().getContext().getString(R.string.orders_eta_format,
                    OrderUi.nz(t.dayDelivery).toUpperCase(), OrderUi.nz(t.deliverySchedule)));
            b.time.setText(OrderUi.nz(t.checkoutTime));
            b.status.setText(b.getRoot().getContext().getString(R.string.orders_order_status_format,
                    readable(lc)));
            b.total.setText(Currency.formatRupiah(t.totalPayment(), 0));
            b.total.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                    rejected ? R.color.text_muted : R.color.green));

            b.progressRow.setVisibility(rejected ? View.GONE : View.VISIBLE);
            b.steps.setup(status, 37.5f, 17f, true);
            b.btnOrderAgain.setVisibility(lc.equals("received") ? View.VISIBLE : View.GONE);
            b.btnReceived.setVisibility(lc.equals("otw") ? View.VISIBLE : View.GONE);
            b.btnPay.setVisibility(lc.equals("pending") && method.toLowerCase().equals("tf_bca")
                    ? View.VISIBLE : View.GONE);

            b.card.setOnClickListener(v -> listener.onOpen(t));
            b.btnOrderAgain.setOnClickListener(v -> listener.onOrderAgain(t));
            b.btnReceived.setOnClickListener(v -> listener.onReceived(t));
            b.btnPay.setOnClickListener(v -> listener.onConfirmPayment(t));
        }

        private String readable(String lc) {
            android.content.Context c = b.getRoot().getContext();
            switch (lc) {
                case "pending":
                    return c.getString(R.string.orders_status_pending);
                case "processing":
                    return c.getString(R.string.orders_status_processing);
                case "otw":
                    return c.getString(R.string.orders_status_otw);
                case "received":
                    return c.getString(R.string.orders_status_received);
                case "rejected":
                    return c.getString(R.string.orders_status_rejected);
                default:
                    return "";
            }
        }
    }
}
