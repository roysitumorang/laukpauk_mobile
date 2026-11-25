package id.laukpauk.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.data.model.payment.BuyerTransaction;
import id.laukpauk.util.Currency;
import id.laukpauk.databinding.FragmentOrderSuccessBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;

/**
 * BuatOrderanBerhasil.dart: the order was placed ({@code POST buyer/transactions}). Arguments: {@link #ARG_TRANSACTION}
 * (the transaction as JSON text), {@code removeTransactionSession}.
 */
public class OrderSuccessFragment extends Fragment {

    /** The new transaction ({@link BuyerTransaction}) as JSON text. */
    public static final String ARG_TRANSACTION = "transaction";

    private FragmentOrderSuccessBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        BuyerTransaction t = c.apiClient.fromJson(requireArguments().getString(ARG_TRANSACTION), BuyerTransaction.class);
        binding.paymentInfo.setText(paymentInfo(t));
        if (savedInstanceState == null && requireArguments().getBoolean("removeTransactionSession", false)) {
            c.cartRepository.clearTransactionSession(res -> { });
        }
        Images.asset(binding.image, "buatOrderanBerhasil.png");
        binding.btnShopAgain.setOnClickListener(v -> CheckoutNav.goHome(this, false));
        binding.btnDetail.setOnClickListener(v -> {
            String id = t != null ? t.id : null;
            Bundle args = new Bundle();
            args.putString("idTransaksi", id == null ? "" : id);
            NavHostFragment.findNavController(this).navigate(R.id.transactionDetailFragment, args);
        });
    }

    /** Order number, amount, channel, virtual account and due date (or "already paid"), one per line. */
    private String paymentInfo(@Nullable BuyerTransaction t) {
        if (t == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        line(sb, t.referenceNumber, R.string.checkout_success_reference);
        if (t.paymentAmount <= 0) {
            line(sb, getString(R.string.checkout_success_paid), 0);
            return sb.toString();
        }
        line(sb, Currency.formatRupiah(t.paymentAmount, 0), R.string.checkout_success_amount);
        line(sb, t.paymentChannelName, R.string.checkout_success_channel);
        line(sb, t.paymentVirtualAccountNumber, R.string.checkout_success_virtual_account);
        line(sb, dateTime(t.paymentDueDate), R.string.checkout_success_due);
        return sb.toString();
    }

    private void line(StringBuilder sb, @Nullable String value, int format) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('\n');
        }
        sb.append(format == 0 ? value : getString(format, value));
    }

    /** An RFC 3339 time as "26 November 2026 00:34" in the phone's time zone; the text itself when it can't be read. */
    @Nullable
    static String dateTime(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US);
            java.util.Date d = in.parse(value.replaceFirst("\\.\\d+", ""));
            return new java.text.SimpleDateFormat("d MMMM yyyy HH:mm", new java.util.Locale("id", "ID")).format(d);
        } catch (java.text.ParseException | IllegalArgumentException e) {
            return value;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
