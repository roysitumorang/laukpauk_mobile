package id.laukpauk.ui.seller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentRequestPencairanDanaBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Currency;

/** RequestPencairanDana.dart: ask the admin to transfer the seller's balance. */
public class RequestPencairanDanaFragment extends Fragment {

    private FragmentRequestPencairanDanaBinding binding;
    private RequestPencairanDanaViewModel viewModel;
    private int saldo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestPencairanDanaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new RequestPencairanDanaViewModel(c.tokoSayaRepository)))
                .get(RequestPencairanDanaViewModel.class);
        AppBars.setup(this, binding.appBar, getString(R.string.seller_toko_rpd_judul));
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshForm();
            }
        };
        binding.nomorRekening.addTextChangedListener(watcher);
        binding.namaPenerima.addTextChangedListener(watcher);
        binding.nominal.addTextChangedListener(watcher);
        binding.ajukan.setOnClickListener(v -> ajukan());
        refreshForm();
        AuthGate.require(this, this::observe);
    }

    private void observe() {
        viewModel.start();
        viewModel.getSaldo().observe(getViewLifecycleOwner(), value -> {
            saldo = value == null ? 0 : value;
            refreshForm();
        });
        viewModel.getRekening().observe(getViewLifecycleOwner(), res -> {
            if (res != null && res.isSuccess() && res.data != null && !viewModel.prefilled) {
                viewModel.prefilled = true;
                binding.nomorRekening.setText(res.data.nomorRekening);
                binding.namaPenerima.setText(res.data.namaUser);
            }
        });
    }

    private void refreshForm() {
        if (binding == null) {
            return;
        }
        String nominalText = binding.nominal.getText().toString();
        long nominal = 0;
        if (nominalText.length() >= 1) {
            try {
                nominal = Long.parseLong(nominalText);
            } catch (NumberFormatException e) {
                nominal = 0;
            }
        }
        boolean cukup = nominal <= saldo;
        boolean lengkap = binding.nomorRekening.getText().length() >= 1
                && binding.namaPenerima.getText().length() >= 1
                && nominalText.length() >= 1 && cukup && nominal >= 1;
        binding.saldo.setText(getString(R.string.seller_toko_rpd_saldo, Currency.formatRupiah(saldo, 0)));
        binding.saldoTidakCukup.setVisibility(cukup ? View.GONE : View.VISIBLE);
        binding.ajukan.setVisibility(lengkap ? View.VISIBLE : View.GONE);
    }

    private void ajukan() {
        binding.ajukan.setEnabled(false);
        viewModel.ajukan(binding.nomorRekening.getText().toString(), binding.namaPenerima.getText().toString(),
                binding.nominal.getText().toString(), binding.keterangan.getText().toString(), result -> {
                    if (!isAdded()) {
                        return;
                    }
                    binding.ajukan.setEnabled(true);
                    if (!result.isSuccess()) {
                        return; // legacy: a failed request shows nothing
                    }
                    boolean ok = Boolean.TRUE.equals(result.data);
                    Messages.info(this, getString(R.string.seller_toko_rpd_dialog_judul),
                            getString(ok ? R.string.seller_toko_rpd_berhasil : R.string.seller_toko_rpd_gagal),
                            ok ? () -> SellerShopNav.toAkunSaya(this) : null);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
