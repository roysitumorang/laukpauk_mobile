package id.laukpauk.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.databinding.FragmentDetailTransaksiBinding;
import id.laukpauk.databinding.ItemOrdersRincianRowBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import id.laukpauk.util.ServerPath;

/** DetailTransaksi.dart. Args: idTransaksi, idPenjual?. */
public class DetailTransaksiFragment extends Fragment {

    private FragmentDetailTransaksiBinding binding;
    private DetailTransaksiViewModel viewModel;
    private ItemTransaksiViewModel itemsViewModel;
    private String idTransaksi;
    private String idPenjual;
    private Transaksi current;
    private boolean awaitingDiantar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailTransaksiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        idTransaksi = requireArguments().getString("idTransaksi", "");
        idPenjual = requireArguments().getString("idPenjual");
        AppBars.setup(this, binding.appBar, getString(R.string.orders_detail_transaksi));
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new DetailTransaksiViewModel(
                c.transaksiRepository, c.deliveryRepository, c.settingRepository, c.cartRepository, idTransaksi)))
                .get(DetailTransaksiViewModel.class);
        itemsViewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ItemTransaksiViewModel(c.transaksiRepository, idTransaksi, idPenjual)))
                .get(ItemTransaksiViewModel.class);
        ItemTransaksiBinder.bind(this, binding.items, itemsViewModel, idTransaksi, idPenjual);

        binding.empty.emptyText.setText(R.string.orders_detail_tidak_ditemukan);
        binding.refresh.setOnRefreshListener(() -> viewModel.load());
        binding.lihatInvoice.setOnClickListener(v -> {
            if (!OrderUi.openUrl(this, ServerPath.baseURL + "/transaksi/invoice/" + idTransaksi)) {
                Messages.snack(this, getString(R.string.orders_url_tidak_valid));
            }
        });
        binding.btnPesanLagi.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idTransaksi", idTransaksi);
            NavHostFragment.findNavController(this).navigate(R.id.repeatOrderFragment, args);
        });
        binding.btnDiterima.setOnClickListener(v -> OrderUi.confirmDiterima(this, this::onDiterima));
        binding.btnBelanjaUlang.setOnClickListener(v -> onBelanjaUlang());

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::onDetail);
        viewModel.getRekening().observe(getViewLifecycleOwner(), r -> {
            if (current != null) {
                renderStatus(current);
            }
        });
        viewModel.getDiantar().observe(getViewLifecycleOwner(), res -> {
            if (res == null || res.isLoading() || !awaitingDiantar) {
                return;
            }
            // FutureBuilder<int?>: an error keeps the spinner
            if (res.isSuccess() && res.data != null) {
                binding.refundProgress.setVisibility(View.GONE);
                binding.refundBox.setVisibility(res.data <= 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void onDetail(Resource<Transaksi> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.refresh.isRefreshing() ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        binding.refresh.setRefreshing(false);
        current = res.isSuccess() ? res.data : null;
        binding.content.setVisibility(current != null ? View.VISIBLE : View.GONE);
        binding.empty.getRoot().setVisibility(current != null ? View.GONE : View.VISIBLE);
        if (current == null) {
            return;
        }
        renderStatus(current);
        renderRincian(current);
        itemsViewModel.load(current.cartCode);
        awaitingDiantar = current.statusLower().equals("rejected");
        binding.refundSlot.setVisibility(awaitingDiantar ? View.VISIBLE : View.GONE);
        binding.refundProgress.setVisibility(View.VISIBLE);
        binding.refundBox.setVisibility(View.GONE);
        if (awaitingDiantar) {
            binding.refundTitle.setText(getString(R.string.orders_pengembalian_dana_format,
                    Currency.formatRupiah(current.refundedOngkir + current.refundedUangBelanja, 0)));
            binding.refundOngkir.setText(getString(R.string.orders_refund_ongkir_format,
                    Currency.formatRupiah(current.refundedOngkir, 0)));
            binding.refundBelanja.setText(getString(R.string.orders_refund_belanja_format,
                    Currency.formatRupiah(current.refundedUangBelanja, 0)));
            viewModel.loadDiantar();
        }
    }

    private void renderStatus(Transaksi t) {
        String status = OrderUi.nz(t.status);
        String lc = t.statusLower();
        boolean rejected = lc.equals("rejected");

        binding.progressGroup.setVisibility(rejected ? View.GONE : View.VISIBLE);
        binding.steps.setup(status, 39f, 15f, false);
        binding.btnPesanLagi.setVisibility(lc.equals("received") ? View.VISIBLE : View.GONE);
        binding.btnDiterima.setVisibility(lc.equals("otw") ? View.VISIBLE : View.GONE);
        binding.statusText.setText(getString(R.string.orders_status_format,
                OrderUi.statusReadable(this, status).toUpperCase()));
        binding.noteBox.setVisibility(lc.equals("otw") ? View.VISIBLE : View.GONE);
        binding.nomor.setText(OrderUi.nz(t.nomorTransaksi));
        binding.metode.setText(OrderUi.nz(t.namaMetodePembayaran));

        binding.caraBayar.setVisibility(t.isTfBca() ? View.VISIBLE : View.GONE);
        binding.caraBayar.setOnClickListener(v -> openTransferBank(t));

        String catatan = OrderUi.nz(t.catatan);
        binding.catatanGroup.setVisibility(catatan.isEmpty() ? View.GONE : View.VISIBLE);
        binding.catatan.setText(catatan);

        binding.alasanBox.setVisibility(rejected ? View.VISIBLE : View.GONE);
        String reason = OrderUi.nz(t.statusReason);
        if (reason.isEmpty()) {
            binding.alasan.setText(R.string.orders_tanpa_keterangan);
            binding.alasan.setTextColor(requireContext().getColor(R.color.text_muted));
            binding.alasan.setTypeface(binding.alasan.getTypeface(), android.graphics.Typeface.ITALIC);
        } else {
            binding.alasan.setText(reason);
            binding.alasan.setTextColor(requireContext().getColor(R.color.nearly_black));
        }
    }

    private void openTransferBank(Transaksi t) {
        String[] rek = viewModel.getRekening().getValue();
        JsonObject payment = Json.of(
                "totalBayar", t.totalKeseluruhan(),
                "kodeUnik", t.moreDataMetodePembayaran,
                "nomorRekening", rek != null ? rek[0] : getString(R.string.orders_rekening_placeholder),
                "pemilikNomorRekening", rek != null ? rek[1] : getString(R.string.orders_pemilik_placeholder),
                "idTransaksi", idTransaksi,
                "nomorTransaksi", OrderUi.nz(t.nomorTransaksi));
        JsonObject responseCheckout = Json.of("idTransaksi", idTransaksi, "paymentResponse", payment);
        JsonObject data = Json.of("responseCheckout", responseCheckout);
        Bundle args = new Bundle();
        args.putString("returnedDataFromCheckout", data.toString());
        NavHostFragment.findNavController(this).navigate(R.id.transferBankFragment, args);
    }

    private void addRow(int label, String value, boolean bold) {
        ItemOrdersRincianRowBinding row = ItemOrdersRincianRowBinding.inflate(LayoutInflater.from(requireContext()),
                binding.rincian, false);
        row.label.setText(label);
        row.value.setText(value);
        if (bold) {
            row.label.setTypeface(row.label.getTypeface(), android.graphics.Typeface.BOLD);
            row.value.setTypeface(row.value.getTypeface(), android.graphics.Typeface.BOLD);
        }
        binding.rincian.addView(row.getRoot());
    }

    private static String minus(Fragment f, int amount) {
        return f.getString(R.string.orders_minus_format, Currency.formatRupiah(amount, 0));
    }

    private void renderRincian(Transaksi t) {
        binding.alamat.setText(OrderUi.nz(t.alamatDelivery));
        boolean hasDetail = t.alamatDetailLengkap != null;
        binding.alamatDetailGroup.setVisibility(hasDetail ? View.VISIBLE : View.GONE);
        binding.alamatDetail.setText(OrderUi.nz(t.alamatDetailLengkap));
        binding.jadwal.setText(getString(R.string.orders_jadwal_format, OrderUi.nz(t.formattedTanggalDelivery),
                OrderUi.nz(t.jadwalDelivery)));

        String jenis = OrderUi.nz(t.jenisVoucher).toLowerCase();
        binding.rincian.removeAllViews();
        addRow(R.string.orders_potongan_dompet, minus(this, t.potonganWallet), false);
        addRow(R.string.orders_potongan_kode_promo, minus(this, t.diskonPromo), false);
        addRow(R.string.orders_potongan_poin, minus(this, t.pointOut), false);
        addRow(R.string.orders_ongkos_kirim, Currency.formatRupiah(t.ongkir, 0), false);
        if (t.nilaiVoucher >= 1 && jenis.equals("ongkir")) {
            addRow(R.string.orders_potongan_voucher_ongkir, minus(this, t.nilaiVoucher), false);
        }
        addRow(R.string.orders_total_belanja, Currency.formatRupiah(t.totalBelanja, 0), false);
        if (jenis.equals("belanja") && t.nilaiVoucher >= 1) {
            addRow(R.string.orders_potongan_voucher_belanja, minus(this, t.nilaiVoucher), false);
        }
        if (t.diskonKategori >= 1) {
            addRow(R.string.orders_diskon, minus(this, t.diskonKategori), false);
        }
        if (t.isTfBca()) {
            addRow(R.string.orders_kode_unik, Currency.formatRupiah(t.moreDataMetodePembayaran, 0), false);
        }
        addRow(R.string.orders_total_dibayar, Currency.formatRupiah(t.totalBayar(), 0), true);
    }

    private void onDiterima() {
        viewModel.pesananDiterima(result -> {
            if (!isAdded()) {
                return;
            }
            boolean ok = result.isSuccess() && Boolean.TRUE.equals(result.data);
            if (ok) {
                viewModel.load();
            }
            Messages.snack(this, getString(ok ? R.string.orders_diterima_berhasil : R.string.orders_diterima_gagal));
        });
    }

    // ------------------------------------------------------------------ belanja ulang (rejected transaction)

    private void setBusy(boolean busy) {
        if (binding != null) {
            binding.btnBelanjaUlang.setEnabled(!busy);
        }
    }

    private void onBelanjaUlang() {
        if (viewModel.isDeliverySet()) {
            setBusy(true);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.orders_adjd_title)
                    .setMessage(R.string.orders_adjd_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.orders_ya, (d, w) -> doBelanjaUlang())
                    .setNegativeButton(R.string.orders_tidak, (d, w) -> {
                        viewModel.clearDelivery();
                        DeliveryFlow.ask(this, saved -> {
                            if (saved) {
                                doBelanjaUlang();
                            } else {
                                setBusy(false);
                            }
                        });
                    })
                    .setNeutralButton(R.string.orders_tutup, (d, w) -> setBusy(false))
                    .show();
        } else {
            NavController nav = NavHostFragment.findNavController(this);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.orders_adjd_belum_title)
                    .setMessage(R.string.orders_adjd_belum_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.orders_ok, (d, w) -> nav.navigate(R.id.homePageFragment))
                    .show();
        }
    }

    private void doBelanjaUlang() {
        viewModel.belanjaUlang(result -> {
            if (!isAdded()) {
                return;
            }
            if (!result.isSuccess() || result.data == null) {
                setBusy(false);
                return;
            }
            final boolean success = result.data.statusBelanjaUlang;
            String sub = getString(success ? R.string.orders_belanja_ulang_berhasil
                    : R.string.orders_belanja_ulang_gagal);
            if (result.data.message != null) {
                sub = sub + " " + result.data.message;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.orders_belanja_ulang)
                    .setMessage(sub)
                    .setCancelable(false)
                    .setPositiveButton(success ? R.string.orders_ok : R.string.orders_coba_lagi, (d, w) -> {
                        setBusy(false);
                        if (success) {
                            viewModel.refreshCartCount();
                            NavHostFragment.findNavController(this).navigate(R.id.keranjangFragment);
                        }
                    })
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
