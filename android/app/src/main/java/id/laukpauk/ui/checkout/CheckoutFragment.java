package id.laukpauk.ui.checkout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import id.laukpauk.R;
import id.laukpauk.util.Currency;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.model.payment.BuyerTransaction;
import id.laukpauk.data.repository.CheckoutRepository;
import id.laukpauk.databinding.FragmentCheckoutBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.delivery.DeliveryFlow;

/**
 * Checkout.dart: recipient form, delivery schedule, payment summary and the "Bayar" bar. Needs a signed-in,
 * verified user. State lives in {@link CheckoutViewModel}; the sections are bound by small helper classes.
 */
public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private CheckoutViewModel vm;
    private AppContainer c;
    private SubTotalSection subTotal;
    private ShippingScheduleSection schedule;
    private PaymentSummarySection summary;
    private PaymentSheet paymentSheet;
    private boolean begun;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        c = AppContainer.from(requireContext());
        vm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new CheckoutViewModel(c.checkoutRepository, c.paymentRepository)))
                .get(CheckoutViewModel.class);
        AppBars.setup(this, binding.appBar, getString(R.string.checkout_title));
        begun = false;
        AuthGate.require(this, this::begin);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (paymentSheet != null) {
            paymentSheet.dismiss();
            paymentSheet = null;
        }
        binding = null;
    }

    private NavController nav() {
        return NavHostFragment.findNavController(this);
    }

    private void begin() {
        if (binding == null || begun) {
            return;
        }
        begun = true;
        subTotal = new SubTotalSection(binding.subTotal, this::onPay);
        schedule = new ShippingScheduleSection(binding.schedule, this::onEditSchedule);
        summary = new PaymentSummarySection(binding.summary, new PaymentSummarySection.Listener() {
            @Override
            public void onChooseVoucher() {
                chooseVoucher();
            }

            @Override
            public void onCancelVoucher() {
                vm.removeVoucher();
            }

            @Override
            public void onPointUsed(boolean used) {
                vm.setPointUsed(used);
            }

            @Override
            public void onPromoCode(String code) {
                vm.onPromoCode(code);
            }
        });
        fillForm();
        watch(binding.etName, vm::setName);
        watch(binding.etPhone, vm::setPhone);
        watch(binding.etNote, vm::setNote);
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(() -> {
            vm.refresh();
            binding.refresh.setRefreshing(false);
        });

        c.deliveryRepository.selection().observe(getViewLifecycleOwner(), sel -> vm.reloadSchedule());
        vm.schedule().observe(getViewLifecycleOwner(),
                j -> schedule.render(j, c.deliveryRepository.current().address));
        vm.phase().observe(getViewLifecycleOwner(), phase -> {
            boolean ready = phase == CheckoutViewModel.PHASE_READY;
            binding.content.setVisibility(ready ? View.VISIBLE : View.GONE);
            binding.subTotal.getRoot().setVisibility(ready ? View.VISIBLE : View.GONE);
            binding.progress.setVisibility(ready ? View.GONE : View.VISIBLE);
        });
        vm.summary().observe(getViewLifecycleOwner(), s -> {
            subTotal.render(s);
            summary.render(s);
        });
        vm.formLoaded().observe(getViewLifecycleOwner(), e -> {
            if (e != null && e.consume() != null) {
                fillForm();
            }
        });
        vm.redirect().observe(getViewLifecycleOwner(), e -> {
            Integer step = e == null ? null : e.consume();
            if (step != null) {
                onRedirect(step);
            }
        });
        vm.voucherRejected().observe(getViewLifecycleOwner(), e -> {
            Voucher v = e == null ? null : e.consume();
            if (v != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.checkout_voucher_not_title)
                        .setMessage(getString(R.string.checkout_voucher_not, Currency.formatRupiah(v.minimumShopping, 0)))
                        .setPositiveButton(R.string.checkout_ok, null)
                        .show();
            }
        });
        vm.orderResult().observe(getViewLifecycleOwner(), e -> {
            CheckoutViewModel.OrderOutcome outcome = e == null ? null : e.consume();
            if (outcome != null) {
                onOrderOutcome(outcome);
            }
        });
        vm.start();
    }

    // ------------------------------------------------------------------ form

    private void fillForm() {
        setText(binding.etName, vm.name());
        setText(binding.etPhone, vm.phone());
        setText(binding.etAddress, vm.address());
        setText(binding.etNote, vm.note());
    }

    private static void setText(EditText e, String value) {
        if (!e.getText().toString().equals(value)) {
            e.setText(value);
        }
    }

    private interface TextSink {
        void accept(String value);
    }

    private static void watch(EditText e, TextSink sink) {
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sink.accept(s.toString());
            }
        });
    }

    private void info(int title, int message, @Nullable Runnable afterOk) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.checkout_ok, (d, w) -> {
                    if (afterOk != null) {
                        afterOk.run();
                    }
                })
                .show();
    }

    // ------------------------------------------------------------------ prerequisites

    private void onRedirect(int step) {
        switch (step) {
            case CheckoutRepository.STEP_NOT_VERIFIED:
                nav().navigate(R.id.userVerificationOtpFragment);
                break;
            case CheckoutRepository.STEP_OTP_NOT_COMPLETED:
                nav().navigate(R.id.oTPNotCompletedFragment);
                break;
            case CheckoutRepository.STEP_SCHEDULE_CLOSED:
                scheduleClosed();
                break;
            default:
                info(R.string.checkout_title, R.string.checkout_insufficient, () -> CheckoutNav.goHome(this, false));
                break;
        }
    }

    private void scheduleClosed() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.checkout_schedule_closed_title)
                .setMessage(R.string.checkout_schedule_closed)
                .setCancelable(false)
                .setPositiveButton(R.string.checkout_choose_other_schedule, (d, w) -> chooseNewSchedule())
                .setNegativeButton(R.string.checkout_next_time, (d, w) -> CheckoutNav.goHome(this, false))
                .show();
    }

    /** Legacy deleteAlamatDanJadwalDelivery + HeaderModel.adjd: clear, ask again, then restart from the splash. */
    private void chooseNewSchedule() {
        c.deliveryRepository.clear();
        DeliveryFlow.ask(this, saved -> {
            if (saved && isAdded()) {
                CheckoutNav.goHome(this, false);
            }
        });
    }

    private void onEditSchedule() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.checkout_edit_address_schedule)
                .setMessage(R.string.checkout_edit_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.checkout_continue, (d, w) -> chooseNewSchedule())
                .show();
    }

    // ------------------------------------------------------------------ voucher / pay

    private void chooseVoucher() {
        CheckoutSummary s = vm.summary().getValue();
        if (s == null) {
            return;
        }
        VoucherSheet.show(this, vm.vouchers(), s.subTotal, vm.usedVoucher(), vm::chooseVoucher);
    }

    private void onPay() {
        CheckoutSummary s = vm.summary().getValue();
        if (s == null) {
            return;
        }
        if (!s.isFormCompleted) {
            info(R.string.checkout_title, R.string.checkout_form_incomplete, null);
            return;
        }
        if (vm.schedule().getValue() == null) {
            return; // legacy: no saved schedule, nothing happens
        }
        if (!CheckoutRepository.hasPositiveTotal(s.overallTotal)) {
            info(R.string.checkout_title, R.string.checkout_insufficient, null);
            return;
        }
        paymentSheet = new PaymentSheet(this, vm, s.overallTotal);
        paymentSheet.show();
    }

    // ------------------------------------------------------------------ after the order

    /**
     * {@code POST buyer/transactions} answered: on success the cart is gone and the order success screen shows the
     * transaction; otherwise the backend's reason is shown.
     */
    private void onOrderOutcome(CheckoutViewModel.OrderOutcome o) {
        if (o.transaction == null) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.checkout_failed_title)
                    .setMessage(o.message == null || o.message.isEmpty() ? getString(R.string.checkout_failed) : o.message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.checkout_try_again, null)
                    .show();
            return;
        }
        closeSheet();
        Bundle args = new Bundle();
        args.putString(OrderSuccessFragment.ARG_TRANSACTION, c.apiClient.toJson(o.transaction, BuyerTransaction.class));
        args.putBoolean("removeTransactionSession", true);
        CheckoutNav.toResult(this, R.id.orderSuccessFragment, args);
    }

    private void closeSheet() {
        if (paymentSheet != null) {
            paymentSheet.dismiss();
            paymentSheet = null;
        }
    }
}
