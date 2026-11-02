package id.laukpauk.ui.checkout;

import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

import java.util.Arrays;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.payment.CheckoutResponse;
import id.laukpauk.data.model.payment.ReturnedDataFromCheckout;
import id.laukpauk.data.repository.CheckoutRepository;
import id.laukpauk.databinding.FragmentCheckoutBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.util.Json;

/**
 * Checkout.dart: recipient form, delivery schedule, payment summary and the "Bayar" bar. Needs a signed-in,
 * verified user. State lives in {@link CheckoutViewModel}; the sections are bound by small helper classes.
 */
public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private CheckoutViewModel vm;
    private AppContainer c;
    private SubTotalSection subTotal;
    private JadwalPengirimanSection jadwal;
    private RingkasanPembayaranSection ringkasan;
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
        subTotal = new SubTotalSection(binding.subTotal, this::onBayar);
        jadwal = new JadwalPengirimanSection(binding.jadwal, this::onUbahJadwal);
        ringkasan = new RingkasanPembayaranSection(binding.ringkasan, new RingkasanPembayaranSection.Listener() {
            @Override
            public void onPilihVoucher() {
                pilihVoucher();
            }

            @Override
            public void onBatalVoucher() {
                vm.removeVoucher();
            }

            @Override
            public void onPointUsed(boolean used) {
                vm.setPointUsed(used);
            }

            @Override
            public void onKodePromo(String kode) {
                vm.onKodePromo(kode);
            }
        });
        fillForm();
        watch(binding.etNama, vm::setNama);
        watch(binding.etHp, vm::setHp);
        watch(binding.etAlamat, vm::setAlamat);
        watch(binding.etCatatan, vm::setCatatan);
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(() -> {
            vm.refresh();
            binding.refresh.setRefreshing(false);
        });

        c.deliveryRepository.selection().observe(getViewLifecycleOwner(), sel -> vm.reloadJadwal());
        vm.jadwal().observe(getViewLifecycleOwner(),
                j -> jadwal.render(j, c.deliveryRepository.current().alamat));
        vm.phase().observe(getViewLifecycleOwner(), phase -> {
            boolean ready = phase == CheckoutViewModel.PHASE_READY;
            binding.content.setVisibility(ready ? View.VISIBLE : View.GONE);
            binding.subTotal.getRoot().setVisibility(ready ? View.VISIBLE : View.GONE);
            binding.progress.setVisibility(ready ? View.GONE : View.VISIBLE);
        });
        vm.summary().observe(getViewLifecycleOwner(), s -> {
            subTotal.render(s);
            ringkasan.render(s);
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
            if (e != null && e.consume() != null) {
                info(R.string.checkout_voucher_tdk_title, R.string.checkout_voucher_tdk, null);
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
        setText(binding.etNama, vm.nama());
        setText(binding.etHp, vm.hp());
        setText(binding.etAlamat, vm.alamat());
        setText(binding.etCatatan, vm.catatan());
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
                nav().navigate(R.id.oTPVerifikasiUserFragment);
                break;
            case CheckoutRepository.STEP_OTP_NOT_COMPLETED:
                nav().navigate(R.id.oTPNotCompletedFragment);
                break;
            case CheckoutRepository.STEP_SCHEDULE_CLOSED:
                jadwalDitutup();
                break;
            default:
                info(R.string.checkout_title, R.string.checkout_tidak_cukup, () -> CheckoutNav.goHome(this, false));
                break;
        }
    }

    private void jadwalDitutup() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.checkout_jadwal_ditutup_title)
                .setMessage(R.string.checkout_jadwal_ditutup)
                .setCancelable(false)
                .setPositiveButton(R.string.checkout_pilih_jadwal_lain, (d, w) -> pilihJadwalBaru())
                .setNegativeButton(R.string.checkout_lain_kali, (d, w) -> CheckoutNav.goHome(this, false))
                .show();
    }

    /** Legacy deleteAlamatDanJadwalDelivery + HeaderModel.adjd: clear, ask again, then restart from the splash. */
    private void pilihJadwalBaru() {
        c.deliveryRepository.clear();
        DeliveryFlow.ask(this, saved -> {
            if (saved && isAdded()) {
                CheckoutNav.goHome(this, false);
            }
        });
    }

    private void onUbahJadwal() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.checkout_ubah_alamat_jadwal)
                .setMessage(R.string.checkout_ubah_konfirmasi)
                .setNegativeButton(R.string.batal, null)
                .setPositiveButton(R.string.checkout_lanjutkan, (d, w) -> pilihJadwalBaru())
                .show();
    }

    // ------------------------------------------------------------------ voucher / pay

    private void pilihVoucher() {
        CheckoutSummary s = vm.summary().getValue();
        if (s == null) {
            return;
        }
        VoucherSheet.show(this, vm.vouchers(), s.subTotal, vm.usedVoucher(), vm::chooseVoucher);
    }

    private void onBayar() {
        CheckoutSummary s = vm.summary().getValue();
        if (s == null) {
            return;
        }
        if (!s.isFormCompleted) {
            info(R.string.checkout_title, R.string.checkout_form_belum_lengkap, null);
            return;
        }
        if (vm.jadwal().getValue() == null) {
            return; // legacy: no saved schedule, nothing happens
        }
        paymentSheet = new PaymentSheet(this, vm, s.totalKeseluruhan);
        paymentSheet.show();
    }

    // ------------------------------------------------------------------ after the order

    private static final List<String> SHOW_PAYMENT_MESSAGE =
            Arrays.asList("shopeepay", "dana", "linkaja", "credit_card");

    private void onOrderOutcome(CheckoutViewModel.OrderOutcome o) {
        CheckoutResponse r = o.response;
        String channel = o.channelCodeLC;
        Object payment = r.paymentResponse;
        if (!r.statusCheckout) {
            checkoutFailed(channel, payment);
            return;
        }
        if (channel.equals("dana") || channel.equals("linkaja")) {
            launchExternal(Json.strOrNull(Json.obj(payment), "checkout_url"));
        } else if (channel.equals("shopeepay")) {
            Map<String, Object> actions = Json.object(Json.obj(payment), "actions");
            launchExternal(Json.strOrNull(actions, "mobile_deeplink_checkout_url"));
        } else if (channel.equals("ovo")) {
            closeSheet();
            CheckoutNav.toResult(this, R.id.doPaymentFragment, null);
        } else {
            // cod, tf_bca and kartu_kredit
            closeSheet();
            ReturnedDataFromCheckout data = new ReturnedDataFromCheckout();
            data.responseCheckout = r;
            data.dataCheckout = o.dataCheckout;
            Bundle args = new Bundle();
            args.putString("returnedDataFromCheckout", c.apiClient.toJson(data, ReturnedDataFromCheckout.class));
            if (channel.equals("tf_bca")) {
                CheckoutNav.toResult(this, R.id.transferBankFragment, args);
            } else {
                args.putBoolean("removeTransactionSession", true);
                CheckoutNav.toResult(this, R.id.buatOrderanBerhasilFragment, args);
            }
        }
    }

    private void closeSheet() {
        if (paymentSheet != null) {
            paymentSheet.dismiss();
            paymentSheet = null;
        }
    }

    private void checkoutFailed(String channelLC, Object payment) {
        String content = getString(R.string.checkout_gagal);
        if (SHOW_PAYMENT_MESSAGE.contains(channelLC)) {
            content = Json.str(Json.obj(payment), "message");
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.checkout_gagal_title)
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton(R.string.checkout_coba_lagi, null)
                .show();
    }

    /** E-wallet / ShopeePay: leave the app flow for the payment app or page, and clear the cart session. */
    private void launchExternal(@Nullable String url) {
        Intent intent = null;
        if (url != null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
        }
        if (intent == null || !canLaunch(intent)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.checkout_url_tdk_valid_title)
                    .setMessage(R.string.checkout_url_tdk_valid)
                    .setCancelable(false)
                    .setPositiveButton(R.string.checkout_coba_lagi_seru, null)
                    .show();
            return;
        }
        closeSheet();
        try {
            startActivity(intent);
        } catch (RuntimeException e) {
            // nothing handles it (legacy ignored launch failures)
        }
        c.cartRepository.clearTransactionSession(res -> { });
        CheckoutNav.goHome(this, true);
    }

    private boolean canLaunch(Intent intent) {
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            return true;
        }
        // Package visibility (API 30+) hides handlers not declared in <queries>: custom schemes cannot be
        // probed, so let the launch itself decide.
        String scheme = intent.getData() != null ? intent.getData().getScheme() : null;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && scheme != null
                && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https");
    }
}
