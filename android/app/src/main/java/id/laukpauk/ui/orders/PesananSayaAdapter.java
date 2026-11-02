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
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.databinding.ItemOrdersPesananSayaBinding;
import id.laukpauk.util.Currency;

/** PesananSayaItem.dart: one transaction card of the history list. */
public class PesananSayaAdapter extends ListAdapter<Transaksi, PesananSayaAdapter.Holder> {

    public interface Listener {
        void onOpen(Transaksi t);

        void onPesanLagi(Transaksi t);

        void onDiterima(Transaksi t);

        void onKonfirmasiBayar(Transaksi t);
    }

    private static final DiffUtil.ItemCallback<Transaksi> DIFF = new DiffUtil.ItemCallback<Transaksi>() {
        @Override
        public boolean areItemsTheSame(@NonNull Transaksi a, @NonNull Transaksi b) {
            return OrderUi.nz(a.id).equals(OrderUi.nz(b.id));
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaksi a, @NonNull Transaksi b) {
            return OrderUi.nz(a.status).equals(OrderUi.nz(b.status)) && a.totalBayar() == b.totalBayar()
                    && OrderUi.nz(a.nomorTransaksi).equals(OrderUi.nz(b.nomorTransaksi))
                    && OrderUi.nz(a.jadwalDelivery).equals(OrderUi.nz(b.jadwalDelivery));
        }
    };

    private final Listener listener;

    public PesananSayaAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemOrdersPesananSayaBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.bind(getItem(position), listener);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        private final ItemOrdersPesananSayaBinding b;

        Holder(ItemOrdersPesananSayaBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Transaksi t, Listener listener) {
            String status = OrderUi.nz(t.status);
            String lc = status.toLowerCase();
            boolean rejected = lc.equals("rejected");
            String metode = OrderUi.nz(t.namaMetodePembayaran);

            b.inner.setBackgroundResource(rejected ? R.drawable.bg_orders_item_rejected
                    : R.drawable.bg_orders_item_normal);
            b.nomor.setText(OrderUi.nz(t.nomorTransaksi));
            b.metode.setText(metode.toUpperCase());
            b.eta.setText(b.getRoot().getContext().getString(R.string.orders_eta_format,
                    OrderUi.nz(t.dayDelivery).toUpperCase(), OrderUi.nz(t.jadwalDelivery)));
            b.waktu.setText(OrderUi.nz(t.waktuCheckout));
            b.status.setText(b.getRoot().getContext().getString(R.string.orders_status_pesanan_format,
                    readable(lc)));
            b.total.setText(Currency.formatRupiah(t.totalBayar(), 0));
            b.total.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                    rejected ? R.color.text_muted : R.color.green));

            b.progressRow.setVisibility(rejected ? View.GONE : View.VISIBLE);
            b.steps.setup(status, 37.5f, 17f, true);
            b.btnPesanLagi.setVisibility(lc.equals("received") ? View.VISIBLE : View.GONE);
            b.btnDiterima.setVisibility(lc.equals("otw") ? View.VISIBLE : View.GONE);
            b.btnBayar.setVisibility(lc.equals("pending") && metode.toLowerCase().equals("tf_bca")
                    ? View.VISIBLE : View.GONE);

            b.card.setOnClickListener(v -> listener.onOpen(t));
            b.btnPesanLagi.setOnClickListener(v -> listener.onPesanLagi(t));
            b.btnDiterima.setOnClickListener(v -> listener.onDiterima(t));
            b.btnBayar.setOnClickListener(v -> listener.onKonfirmasiBayar(t));
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
