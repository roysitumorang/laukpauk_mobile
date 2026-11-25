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
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.databinding.FragmentTransactionDetailBinding;
import id.laukpauk.databinding.ItemOrdersBreakdownRowBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import id.laukpauk.util.ServerPath;

/** DetailTransaksi.dart: one transaction ({@code GET buyer/transactions/:id}). Arg: idTransaksi. */
public class TransactionDetailFragment extends Fragment {

    private FragmentTransactionDetailBinding binding;
    private TransactionDetailViewModel viewModel;
    private TransactionItemViewModel itemsViewModel;
    private String transactionId;
    private BuyerTransaction current;
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

    private void onDetail(Resource<BuyerTransaction> res) {
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
        itemsViewModel.show(current);
        awaitingDelivery = current.isCancelled();
        binding.refundSlot.setVisibility(awaitingDelivery ? View.VISIBLE : View.GONE);
        binding.refundProgress.setVisibility(View.VISIBLE);
        binding.refundBox.setVisibility(View.GONE);
        if (awaitingDelivery) {
            binding.refundTitle.setText(getString(R.string.orders_fund_refund_format,
                    Currency.formatRupiah(current.refundAmount, 0)));
            // the backend gives one refund amount, without the delivery / shopping split
            binding.shippingFeeRefund.setVisibility(View.GONE);
            binding.shoppingRefund.setVisibility(View.GONE);
            viewModel.loadDelivered();
        }
    }

    private void renderStatus(BuyerTransaction t) {
        boolean cancelled = t.isCancelled();

        binding.progressGroup.setVisibility(cancelled ? View.GONE : View.VISIBLE);
        binding.steps.setup(OrderUi.step(t.status), 39f, 15f, false);
        binding.btnOrderAgain.setVisibility(t.status == BuyerTransaction.STATUS_COMPLETED ? View.VISIBLE : View.GONE);
        binding.btnReceived.setVisibility(t.status == BuyerTransaction.STATUS_SHIPPED ? View.VISIBLE : View.GONE);
        binding.statusText.setText(getString(R.string.orders_status_format,
                OrderUi.statusLabel(requireContext(), t.status).toUpperCase()));
        binding.noteBox.setVisibility(t.status == BuyerTransaction.STATUS_SHIPPED ? View.VISIBLE : View.GONE);
        binding.number.setText(OrderUi.nz(t.referenceNumber));
        binding.method.setText(OrderUi.nz(t.paymentChannelName));

        // manual bank transfer guide: no such channel any more
        binding.howToPay.setVisibility(View.GONE);
        binding.howToPay.setOnClickListener(v -> openTransferBank(t));

        String note = OrderUi.nz(t.buyerNote);
        binding.noteGroup.setVisibility(note.isEmpty() ? View.GONE : View.VISIBLE);
        binding.note.setText(note);

        binding.reasonBox.setVisibility(cancelled ? View.VISIBLE : View.GONE);
        String reason = OrderUi.nz(t.withdrawalReason);
        if (reason.isEmpty()) {
            binding.reason.setText(R.string.orders_without_description);
            binding.reason.setTextColor(requireContext().getColor(R.color.text_muted));
            binding.reason.setTypeface(binding.reason.getTypeface(), android.graphics.Typeface.ITALIC);
        } else {
            binding.reason.setText(reason);
            binding.reason.setTextColor(requireContext().getColor(R.color.nearly_black));
        }
    }

    private void openTransferBank(BuyerTransaction t) {
        String[] acct = viewModel.getBankAccount().getValue();
        Map<String, Object> payment = Json.of(
                "totalBayar", OrderUi.total(t),
                "kodeUnik", t.uniqueCode,
                "nomorRekening", acct != null ? acct[0] : getString(R.string.orders_bank_account_placeholder),
                "pemilikNomorRekening", acct != null ? acct[1] : getString(R.string.orders_owner_placeholder),
                "idTransaksi", transactionId,
                "nomorTransaksi", OrderUi.nz(t.referenceNumber));
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

    private static String minus(Fragment f, long amount) {
        return f.getString(R.string.orders_minus_format, Currency.formatRupiah(amount, 0));
    }

    private void renderBreakdown(BuyerTransaction t) {
        binding.address.setText(OrderUi.nz(t.deliveryStreetAddress));
        String addressNote = OrderUi.nz(t.deliveryStreetAddressNote).trim();
        binding.addressDetailGroup.setVisibility(addressNote.isEmpty() ? View.GONE : View.VISIBLE);
        binding.addressDetail.setText(addressNote);
        binding.schedule.setText(getString(R.string.orders_schedule_format,
                OrderUi.format(t.deliveryDatetime, getString(R.string.orders_time_format)),
                OrderUi.format(t.deliveryDatetime, "HH:mm")));

        binding.breakdown.removeAllViews();
        addRow(R.string.orders_shopping_total, Currency.formatRupiah(t.itemsTotal(), 0), false);
        if (t.voucherDiscount > 0 && !t.isDeliveryVoucher()) {
            addRow(R.string.orders_shopping_voucher_discount, minus(this, t.voucherDiscount), false);
        }
        addRow(R.string.orders_delivery_fee, Currency.formatRupiah(t.deliveryEstimatedCost, 0), false);
        if (t.voucherDiscount > 0 && t.isDeliveryVoucher()) {
            addRow(R.string.orders_shipping_voucher_discount, minus(this, t.voucherDiscount), false);
        }
        if (t.rewardPointsDiscount > 0) {
            addRow(R.string.orders_points_discount, minus(this, t.rewardPointsDiscount), false);
        }
        if (t.walletDiscount > 0) {
            addRow(R.string.orders_wallet_deduction, minus(this, t.walletDiscount), false);
        }
        if (t.uniqueCode > 0) {
            addRow(R.string.orders_unique_code, Currency.formatRupiah(t.uniqueCode, 0), false);
        }
        addRow(R.string.orders_total_paid, Currency.formatRupiah(t.paymentAmount, 0), true);
        if (t.revisedPaymentAmount != t.paymentAmount) {
            addRow(R.string.orders_revised_total, Currency.formatRupiah(t.revisedPaymentAmount, 0), true);
        }
        if (t.refundAmount > 0) {
            addRow(R.string.orders_refund, Currency.formatRupiah(t.refundAmount, 0), false);
        }
        if (t.status == BuyerTransaction.STATUS_PENDING) {
            String va = OrderUi.nz(t.paymentVirtualAccountNumber);
            if (!va.isEmpty()) {
                addRow(R.string.orders_virtual_account, va, false);
            }
            String due = OrderUi.format(t.paymentDueDate, "d MMMM yyyy HH:mm");
            if (!due.isEmpty()) {
                addRow(R.string.orders_pay_before, due, false);
            }
        }
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
