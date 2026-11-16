package id.laukpauk.ui.checkout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import id.laukpauk.R;
import id.laukpauk.data.model.payment.ReturnedDataFromCheckout;
import id.laukpauk.data.model.payment.TransferBankPayment;
import id.laukpauk.databinding.FragmentTransferBankBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;

/** TransferBank.dart. Argument: {@code returnedDataFromCheckout} (JSON text). */
public class TransferBankFragment extends Fragment {

    private FragmentTransferBankBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransferBankBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        if (savedInstanceState == null) {
            c.cartRepository.clearTransactionSession(res -> { });
        }
        ReturnedDataFromCheckout data = c.apiClient.fromJson(
                requireArguments().getString("returnedDataFromCheckout"), ReturnedDataFromCheckout.class);
        final String transactionId = data != null && data.responseCheckout != null
                && data.responseCheckout.transactionId != null ? data.responseCheckout.transactionId : "";
        Object pr = data != null && data.responseCheckout != null ? data.responseCheckout.paymentResponse : null;
        TransferBankPayment parsed = pr instanceof java.util.Map ? c.apiClient.fromTree(pr, TransferBankPayment.class) : null;
        TransferBankPayment p = parsed != null ? parsed : new TransferBankPayment();
        final String bankAccountNumber = p.bankAccountNumber == null ? "" : p.bankAccountNumber;

        Images.asset(binding.image, "bca.png");
        binding.tvBankAccount.setText(bankAccountNumber);
        binding.tvOwner.setText(getString(R.string.checkout_an, p.bankAccountOwner == null ? "" : p.bankAccountOwner));
        binding.tvFundAmount.setText(Currency.formatRupiah(p.totalPayment + p.uniqueCode, 0));
        binding.tvBankAccount.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("nomorRekening", bankAccountNumber));
            }
            Messages.snack(this, getString(R.string.checkout_copied));
        });
        binding.btnShopAgain.setOnClickListener(v -> CheckoutNav.goHome(this, false));
        binding.btnDetail.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idTransaksi", transactionId);
            NavHostFragment.findNavController(this).navigate(R.id.transactionDetailFragment, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
