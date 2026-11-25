package id.laukpauk.ui.checkout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import id.laukpauk.data.model.payment.CardToken;
import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.databinding.SheetCheckoutPaymentBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Environment;
import id.laukpauk.R;

/**
 * Bottom sheet "Metode Pembayaran" (legacy PilihanMetodePembayaran + UangYangDipersiapkan + NomorTelepon +
 * CreditCardData). Orders are placed through the CheckoutViewModel; its answer is handled by the fragment.
 */
final class PaymentSheet {

    private static final List<String> E_WALLET = Arrays.asList("ovo", "linkaja", "dana");

    private final Fragment fragment;
    private final CheckoutViewModel vm;
    private final long totalBill;
    private final BottomSheetDialog dialog;
    private final SheetCheckoutPaymentBinding b;
    private final ChannelAdapter adapter;

    private PaymentChannel selected;
    /** Extra data of the channel chosen in the list (tf_bca unique code, "" otherwise); null = not ready. */
    private String moreData;
    private boolean cardStepVisible;
    private boolean tokenizing;
    private boolean ordering;

    PaymentSheet(Fragment fragment, CheckoutViewModel vm, long totalBill) {
        this.fragment = fragment;
        this.vm = vm;
        this.totalBill = totalBill;
        this.dialog = new BottomSheetDialog(fragment.requireContext());
        this.b = SheetCheckoutPaymentBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(b.getRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        adapter = new ChannelAdapter(this::onChannel);
        b.list.setAdapter(adapter);
        b.btnOrder.setOnClickListener(v -> {
            if (selected != null && moreData != null) {
                vm.placeOrder(selected, moreData);
            }
        });
        setupCod();
        setupEwallet();
        setupCard();

        vm.channels(totalBill).observe(fragment.getViewLifecycleOwner(), res -> {
            b.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                adapter.submitList(res.isSuccess() ? res.data : null);
            }
        });
        vm.orderBusy().observe(fragment.getViewLifecycleOwner(), busy -> {
            ordering = Boolean.TRUE.equals(busy);
            b.btnOrder.setEnabled(!ordering);
            b.stepCod.btnOrder.setEnabled(!ordering);
            b.stepEwallet.btnOrder.setEnabled(!ordering);
            updateCardBusy();
        });
    }

    void show() {
        dialog.show();
    }

    void dismiss() {
        dialog.dismiss();
    }

    // ------------------------------------------------------------------ list

    private void onChannel(PaymentChannel ch) {
        String code = ch.channel_code == null ? "" : ch.channel_code.toLowerCase(Locale.ROOT);
        selected = ch;
        moreData = null;
        if (code.equals("kartu_kredit")) {
            showStep(b.stepCard.cardRoot);
            cardStepVisible = true;
        } else if (code.equals("cod")) {
            showStep(b.stepCod.codRoot);
        } else if (E_WALLET.contains(code)) {
            b.stepEwallet.tvName.setText(ch.name == null ? "" : ch.name.toUpperCase(Locale.ROOT));
            showStep(b.stepEwallet.ewalletRoot);
        } else {
            // tf_bca gets a random unique code (1..199); the other channels need no extra data
            moreData = code.equals("tf_bca") ? String.valueOf(1 + new Random().nextInt(199)) : "";
        }
        adapter.setSelected(code);
        b.btnOrder.setVisibility(moreData != null ? View.VISIBLE : View.GONE);
    }

    private void showStep(View step) {
        b.listGroup.setVisibility(View.GONE);
        b.stepCod.codRoot.setVisibility(step == b.stepCod.codRoot ? View.VISIBLE : View.GONE);
        b.stepEwallet.ewalletRoot.setVisibility(step == b.stepEwallet.ewalletRoot ? View.VISIBLE : View.GONE);
        b.stepCard.cardRoot.setVisibility(step == b.stepCard.cardRoot ? View.VISIBLE : View.GONE);
    }

    private void backToList() {
        cardStepVisible = false;
        moreData = null;
        b.stepCod.codRoot.setVisibility(View.GONE);
        b.stepEwallet.ewalletRoot.setVisibility(View.GONE);
        b.stepCard.cardRoot.setVisibility(View.GONE);
        b.listGroup.setVisibility(View.VISIBLE);
        b.btnOrder.setVisibility(View.GONE);
    }

    private static void onText(EditText e, Runnable r) {
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                r.run();
            }
        });
    }

    // ------------------------------------------------------------------ cash

    private void setupCod() {
        b.stepCod.back.setOnClickListener(v -> backToList());
        b.stepCod.tvNote.setText(fragment.getString(R.string.checkout_money_note,
                Currency.formatRupiah(totalBill, 0)));
        onText(b.stepCod.etMoney, () -> {
            boolean show = money() != null && money() >= totalBill;
            b.stepCod.btnOrder.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        b.stepCod.btnOrder.setOnClickListener(v -> {
            Long money = money();
            if (selected != null && money != null) {
                vm.placeOrder(selected, String.valueOf(money));
            }
        });
    }

    private Long money() {
        String value = b.stepCod.etMoney.getText().toString();
        try {
            return value.isEmpty() ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ e-wallet

    private void setupEwallet() {
        b.stepEwallet.back.setOnClickListener(v -> backToList());
        onText(b.stepEwallet.etTelephone, () -> b.stepEwallet.btnOrder.setVisibility(
                b.stepEwallet.etTelephone.length() >= 10 ? View.VISIBLE : View.GONE));
        b.stepEwallet.btnOrder.setOnClickListener(v -> {
            if (selected != null) {
                vm.placeOrder(selected, b.stepEwallet.etTelephone.getText().toString());
            }
        });
    }

    // ------------------------------------------------------------------ credit card

    private void setupCard() {
        Images.asset(b.stepCard.ivCard, "payment/jcb-mastercard.png");
        Images.asset(b.stepCard.ivCvv, "payment/CVV.png");
        Images.asset(b.stepCard.ivLoading, "loading.png");
        b.stepCard.back.setOnClickListener(v -> backToList());
        if (Environment.environment.equals("development")) {
            b.stepCard.etNumber.setText("4000000000000002");
            b.stepCard.etMonth.setText("03");
            b.stepCard.etYear.setText("26");
            b.stepCard.etCvn.setText("266");
        }
        Runnable refresh = () -> b.stepCard.btnVerification.setVisibility(formOkay() ? View.VISIBLE : View.GONE);
        onText(b.stepCard.etNumber, refresh);
        onText(b.stepCard.etMonth, refresh);
        onText(b.stepCard.etYear, refresh);
        onText(b.stepCard.etCvn, refresh);
        refresh.run();
        b.stepCard.btnVerification.setOnClickListener(v -> verification());
    }

    private boolean formOkay() {
        return b.stepCard.etNumber.length() >= 16 && b.stepCard.etMonth.length() >= 2
                && b.stepCard.etYear.length() >= 2 && b.stepCard.etCvn.length() >= 3;
    }

    private void updateCardBusy() {
        b.stepCard.btnVerification.setEnabled(!tokenizing && !ordering);
        b.stepCard.overlay.setVisibility(ordering && cardStepVisible ? View.VISIBLE : View.GONE);
    }

    private void verification() {
        if (selected == null || !formOkay() || tokenizing || ordering) {
            return;
        }
        String number = b.stepCard.etNumber.getText().toString();
        String month = b.stepCard.etMonth.getText().toString();
        String year = "20" + b.stepCard.etYear.getText().toString();
        String cvn = b.stepCard.etCvn.getText().toString();
        tokenizing = true;
        updateCardBusy();
        XenditTokenizer.createToken(fragment.requireActivity(), totalBill, number, month, year, cvn,
                (CardToken token) -> {
                    tokenizing = false;
                    updateCardBusy();
                    if (token == null || !dialog.isShowing()) {
                        return; // token creation failed: nothing else happens (legacy)
                    }
                    String json = AppContainer.from(fragment.requireContext()).apiClient.toJson(token, CardToken.class);
                    vm.placeOrder(selected, json);
                });
    }
}
