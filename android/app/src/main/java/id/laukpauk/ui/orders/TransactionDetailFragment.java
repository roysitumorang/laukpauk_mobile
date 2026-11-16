package id.laukpauk.ui.orders;

import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.databinding.FragmentTransactionDetailBinding;
import id.laukpauk.databinding.ItemOrdersBreakdownRowBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import id.laukpauk.util.ServerPath;

/** DetailTransaksi.dart. Arg: transactionId. */
public class TransactionDetailFragment extends Fragment {

    private FragmentTransactionDetailBinding binding;
    private TransactionDetailViewModel viewModel;
    private TransactionItemViewModel itemsViewModel;
    private String transactionId;
    private Transaction current;
    private boolean awaitingDelivery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        transactionId = requireArguments().getString("idTransaksi", "");
        AppBars.setup(this, binding.appBar, getString(R.string.orders_transaction_detail));
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new TransactionDetailViewModel(
                c.transactionRepository, c.deliveryRepository, c.settingRepository, c.cartRepository, transactionId)))
                .get(TransactionDetailViewModel.class);
        itemsViewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new TransactionItemViewModel(c.transactionRepository, transactionId)))
                .get(TransactionItemViewModel.class);
        TransactionItemBinder.bind(this, binding.items, itemsViewModel);

        binding.empty.emptyText.setText(R.string.orders_detail_not_found);
        binding.refresh.setOnRefreshListener(() -> viewModel.load());
        binding.viewInvoice.setOnClickListener(v -> {
            if (!OrderUi.openUrl(this, ServerPath.baseURL + "/transaksi/invoice/" + transactionId)) {
                Messages.snack(this, getString(R.string.orders_url_invalid));
            }
        });
        binding.btnOrderAgain.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idTransaksi", transactionId);
            NavHostFragment.findNavController(this).navigate(R.id.repeatOrderFragment, args);
        });
        binding.btnReceived.setOnClickListener(v -> OrderUi.confirmReceived(this, this::onReceived));
        binding.btnReorder.setOnClickListener(v -> onReorder());

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::onDetail);
        viewModel.getBankAccount().observe(getViewLifecycleOwner(), r -> {
            if (current != null) {
                renderStatus(current);
            }
        });
        viewModel.getDelivered().observe(getViewLifecycleOwner(), res -> {
            if (res == null || res.isLoading() || !awaitingDelivery) {
                return;
            }
            // FutureBuilder<int?>: an error keeps the spinner
            if (res.isSuccess() && res.data != null) {
                binding.refundProgress.setVisibility(View.GONE);
                binding.refundBox.setVisibility(res.data <= 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void onDetail(Resource<Transaction> res) {
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
        renderBreakdown(current);
        itemsViewModel.load(current.cartCode);
        awaitingDelivery = current.statusLower().equals("rejected");
        binding.refundSlot.setVisibility(awaitingDelivery ? View.VISIBLE : View.GONE);
        binding.refundProgress.setVisibility(View.VISIBLE);
        binding.refundBox.setVisibility(View.GONE);
        if (awaitingDelivery) {
            binding.refundTitle.setText(getString(R.string.orders_fund_refund_format,
                    Currency.formatRupiah(current.refundedShippingFee + current.refundedShoppingMoney, 0)));
            binding.shippingFeeRefund.setText(getString(R.string.orders_shipping_fee_refund_format,
                    Currency.formatRupiah(current.refundedShippingFee, 0)));
            binding.shoppingRefund.setText(getString(R.string.orders_shopping_refund_format,
                    Currency.formatRupiah(current.refundedShoppingMoney, 0)));
            viewModel.loadDelivered();
        }
    }

    private void renderStatus(Transaction t) {
        String status = OrderUi.nz(t.status);
        String lc = t.statusLower();
        boolean rejected = lc.equals("rejected");

        binding.progressGroup.setVisibility(rejected ? View.GONE : View.VISIBLE);
        binding.steps.setup(status, 39f, 15f, false);
        binding.btnOrderAgain.setVisibility(lc.equals("received") ? View.VISIBLE : View.GONE);
        binding.btnReceived.setVisibility(lc.equals("otw") ? View.VISIBLE : View.GONE);
        binding.statusText.setText(getString(R.string.orders_status_format,
                OrderUi.statusReadable(this, status).toUpperCase()));
        binding.noteBox.setVisibility(lc.equals("otw") ? View.VISIBLE : View.GONE);
        binding.number.setText(OrderUi.nz(t.transactionNumber));
        binding.method.setText(OrderUi.nz(t.paymentMethodName));

        binding.howToPay.setVisibility(t.isTfBca() ? View.VISIBLE : View.GONE);
        binding.howToPay.setOnClickListener(v -> openTransferBank(t));

        String note = OrderUi.nz(t.note);
        binding.noteGroup.setVisibility(note.isEmpty() ? View.GONE : View.VISIBLE);
        binding.note.setText(note);

        binding.reasonBox.setVisibility(rejected ? View.VISIBLE : View.GONE);
        String reason = OrderUi.nz(t.statusReason);
        if (reason.isEmpty()) {
            binding.reason.setText(R.string.orders_without_description);
            binding.reason.setTextColor(requireContext().getColor(R.color.text_muted));
            binding.reason.setTypeface(binding.reason.getTypeface(), android.graphics.Typeface.ITALIC);
        } else {
            binding.reason.setText(reason);
            binding.reason.setTextColor(requireContext().getColor(R.color.nearly_black));
        }
    }

    private void openTransferBank(Transaction t) {
        String[] acct = viewModel.getBankAccount().getValue();
        Map<String, Object> payment = Json.of(
                "totalBayar", t.overallTotal(),
                "kodeUnik", t.paymentMethodMoreData,
                "nomorRekening", acct != null ? acct[0] : getString(R.string.orders_bank_account_placeholder),
                "pemilikNomorRekening", acct != null ? acct[1] : getString(R.string.orders_owner_placeholder),
                "idTransaksi", transactionId,
                "nomorTransaksi", OrderUi.nz(t.transactionNumber));
        Map<String, Object> responseCheckout = Json.of("idTransaksi", transactionId, "paymentResponse", payment);
        Map<String, Object> data = Json.of("responseCheckout", responseCheckout);
        Bundle args = new Bundle();
        args.putString("returnedDataFromCheckout", Json.toJson(data));
        NavHostFragment.findNavController(this).navigate(R.id.transferBankFragment, args);
    }

    private void addRow(int label, String value, boolean bold) {
        ItemOrdersBreakdownRowBinding row = ItemOrdersBreakdownRowBinding.inflate(LayoutInflater.from(requireContext()),
                binding.breakdown, false);
        row.label.setText(label);
        row.value.setText(value);
        if (bold) {
            row.label.setTypeface(row.label.getTypeface(), android.graphics.Typeface.BOLD);
            row.value.setTypeface(row.value.getTypeface(), android.graphics.Typeface.BOLD);
        }
        binding.breakdown.addView(row.getRoot());
    }

    private static String minus(Fragment f, int amount) {
        return f.getString(R.string.orders_minus_format, Currency.formatRupiah(amount, 0));
    }

    private void renderBreakdown(Transaction t) {
        binding.address.setText(OrderUi.nz(t.deliveryAddress));
        boolean hasDetail = t.fullAddressDetail != null;
        binding.addressDetailGroup.setVisibility(hasDetail ? View.VISIBLE : View.GONE);
        binding.addressDetail.setText(OrderUi.nz(t.fullAddressDetail));
        binding.schedule.setText(getString(R.string.orders_schedule_format, OrderUi.nz(t.formattedDeliveryDate),
                OrderUi.nz(t.deliverySchedule)));

        String type = OrderUi.nz(t.voucherType).toLowerCase();
        binding.breakdown.removeAllViews();
        addRow(R.string.orders_wallet_deduction, minus(this, t.walletDiscount), false);
        addRow(R.string.orders_promo_code_deduction, minus(this, t.promoDiscount), false);
        addRow(R.string.orders_points_discount, minus(this, t.pointOut), false);
        addRow(R.string.orders_delivery_fee, Currency.formatRupiah(t.shippingFee, 0), false);
        if (t.voucherValue >= 1 && type.equals("ongkir")) {
            addRow(R.string.orders_shipping_voucher_discount, minus(this, t.voucherValue), false);
        }
        addRow(R.string.orders_shopping_total, Currency.formatRupiah(t.shoppingTotal, 0), false);
        if (type.equals("belanja") && t.voucherValue >= 1) {
            addRow(R.string.orders_shopping_voucher_discount, minus(this, t.voucherValue), false);
        }
        if (t.categoryDiscount >= 1) {
            addRow(R.string.orders_discount, minus(this, t.categoryDiscount), false);
        }
        if (t.isTfBca()) {
            addRow(R.string.orders_unique_code, Currency.formatRupiah(t.paymentMethodMoreData, 0), false);
        }
        addRow(R.string.orders_total_paid, Currency.formatRupiah(t.totalPayment(), 0), true);
    }

    private void onReceived() {
        viewModel.orderReceived(result -> {
            if (!isAdded()) {
                return;
            }
            boolean ok = result.isSuccess() && Boolean.TRUE.equals(result.data);
            if (ok) {
                viewModel.load();
            }
            Messages.snack(this, getString(ok ? R.string.orders_received_success : R.string.orders_received_failed));
        });
    }

    // ------------------------------------------------------------------ belanja ulang (rejected transaction)

    private void setBusy(boolean busy) {
        if (binding != null) {
            binding.btnReorder.setEnabled(!busy);
        }
    }

    /** Re-adds the rejected transaction to the cart; the delivery address and schedule are asked at checkout. */
    private void onReorder() {
        setBusy(true);
        doReorder();
    }

    private void doReorder() {
        viewModel.reorder(result -> {
            if (!isAdded()) {
                return;
            }
            if (!result.isSuccess() || result.data == null) {
                setBusy(false);
                return;
            }
            final boolean success = result.data.reorderStatus;
            String sub = getString(success ? R.string.orders_reorder_success
                    : R.string.orders_reorder_failed);
            if (result.data.message != null) {
                sub = sub + " " + result.data.message;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.orders_reorder)
                    .setMessage(sub)
                    .setCancelable(false)
                    .setPositiveButton(success ? R.string.orders_ok : R.string.orders_try_again, (d, w) -> {
                        setBusy(false);
                        if (success) {
                            viewModel.refreshCartCount();
                            NavHostFragment.findNavController(this).navigate(R.id.cartFragment);
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
