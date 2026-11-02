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

import com.google.gson.JsonElement;

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
        ReturnedDataFromCheckout data = c.apiClient.gson().fromJson(
                requireArguments().getString("returnedDataFromCheckout"), ReturnedDataFromCheckout.class);
        final String idTransaksi = data != null && data.responseCheckout != null
                && data.responseCheckout.idTransaksi != null ? data.responseCheckout.idTransaksi : "";
        JsonElement pr = data != null && data.responseCheckout != null ? data.responseCheckout.paymentResponse : null;
        TransferBankPayment p = pr != null && pr.isJsonObject()
                ? c.apiClient.gson().fromJson(pr, TransferBankPayment.class) : new TransferBankPayment();
        final String nomorRekening = p.nomorRekening == null ? "" : p.nomorRekening;

        Images.asset(binding.image, "bca.png");
        binding.tvRekening.setText(nomorRekening);
        binding.tvPemilik.setText(getString(R.string.checkout_an, p.pemilikNomorRekening == null ? "" : p.pemilikNomorRekening));
        binding.tvBesarDana.setText(Currency.formatRupiah(p.totalBayar + p.kodeUnik, 0));
        binding.tvRekening.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("nomorRekening", nomorRekening));
            }
            Messages.snack(this, getString(R.string.checkout_tersalin));
        });
        binding.btnBelanjaLagi.setOnClickListener(v -> CheckoutNav.goHome(this, false));
        binding.btnDetail.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idTransaksi", idTransaksi);
            NavHostFragment.findNavController(this).navigate(R.id.detailTransaksiFragment, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
