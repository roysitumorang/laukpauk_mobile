package id.laukpauk.ui.seller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.SellerNumbers;
import id.laukpauk.data.model.seller.orders.PesananMasuk;
import id.laukpauk.databinding.ItemSellerPesananMasukBinding;
import id.laukpauk.util.Currency;

/** PesananMasukItem: an incoming order card with its status stepper and the "Proses" button. */
public class PesananMasukAdapter extends ListAdapter<PesananMasuk, PesananMasukAdapter.Holder> {

    public interface Listener {
        void onProses(PesananMasuk item);
    }

    private final Listener listener;

    public PesananMasukAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<PesananMasuk>() {
            @Override
            public boolean areItemsTheSame(@NonNull PesananMasuk a, @NonNull PesananMasuk b) {
                return Objects.equals(a.id, b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PesananMasuk a, @NonNull PesananMasuk b) {
                return Objects.equals(a.status, b.status) && Objects.equals(a.totalBelanja, b.totalBelanja)
                        && Objects.equals(a.ongkir, b.ongkir) && Objects.equals(a.potonganWallet, b.potonganWallet)
                        && Objects.equals(a.nomorTransaksi, b.nomorTransaksi)
                        && Objects.equals(a.nilaiVoucher, b.nilaiVoucher);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSellerPesananMasukBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final PesananMasuk p = getItem(position);
        ItemSellerPesananMasukBinding b = h.binding;
        Context ctx = b.getRoot().getContext();
        String status = nz(p.status);
        String jenisVoucher = nz(p.jenisVoucher).toLowerCase();
        int nilaiVoucher = SellerNumbers.toInt(p.nilaiVoucher);
        int totalBelanja = SellerNumbers.toInt(p.totalBelanja);
        int ongkir = SellerNumbers.toInt(p.ongkir);
        if (jenisVoucher.equals("belanja")) {
            totalBelanja -= nilaiVoucher;
        }
        if (jenisVoucher.equals("ongkir")) {
            ongkir -= nilaiVoucher;
        }
        int bersih = (totalBelanja + ongkir) - SellerNumbers.toInt(p.potonganWallet);
        boolean rejected = status.toLowerCase().equals("rejected");

        b.card.setCardBackgroundColor(ContextCompat.getColor(ctx, rejected ? R.color.seller_pesanan_rejected_bg
                : R.color.nearly_white));
        b.card.setStrokeColor(ContextCompat.getColor(ctx, rejected ? R.color.text_muted : R.color.nearly_white));
        b.nomor.setText(p.nomorTransaksi);
        b.payment.setText(ctx.getString(R.string.seller_pesanan_payment,
                nz(p.namaMetodePembayaran).toUpperCase(Locale.getDefault())));
        b.eta.setText(ctx.getString(R.string.seller_pesanan_eta, nz(p.dayDelivery).toUpperCase(Locale.getDefault()),
                nz(p.jadwalDelivery)));
        b.waktu.setText(p.waktuCheckout);
        int readable = SellerOrderStatus.readable(status);
        b.status.setText(ctx.getString(R.string.seller_pesanan_status_pesanan,
                readable == 0 ? "" : ctx.getString(readable)));
        b.total.setText(Currency.formatRupiah(bersih, 0));
        b.total.setTextColor(ContextCompat.getColor(ctx, rejected ? R.color.text_muted : R.color.green));
        b.actionRow.setVisibility(rejected ? View.GONE : View.VISIBLE);
        if (!rejected) {
            SellerOrderStatus.bind(b.stepper, status);
            b.proses.setVisibility(status.toLowerCase().equals("processing") ? View.VISIBLE : View.GONE);
            b.proses.setOnClickListener(v -> listener.onProses(p));
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemSellerPesananMasukBinding binding;

        Holder(ItemSellerPesananMasukBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
