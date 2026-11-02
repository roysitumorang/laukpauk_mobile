package id.laukpauk.ui.auth;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentOTPVerifikasiUserBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.SearchBox;

/** OTPVerifikasiUser.dart (+ ListPasar): verification code, and the market choice for old-app sellers. */
public class OTPVerifikasiUserFragment extends Fragment {

    private FragmentOTPVerifikasiUserBinding binding;
    private OTPVerifikasiUserViewModel viewModel;
    private PasarAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOTPVerifikasiUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new OTPVerifikasiUserViewModel(c.authRepository)))
                .get(OTPVerifikasiUserViewModel.class);
        Images.asset(binding.logo, "laukpauk.png");
        Images.asset(binding.pasarEmptyImage, "empty.png");

        adapter = new PasarAdapter(viewModel::select);
        binding.pasarList.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.pasarList.setAdapter(adapter);
        SearchBox.bind(binding.search, getString(R.string.auth_pasar_hint), viewModel.getQuery(),
                viewModel::search, () -> viewModel.search(""));
        binding.lokasiSendiri.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.pinGMAPSFragment));

        binding.verificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateForm();
            }
        });

        viewModel.getSeller().observe(getViewLifecycleOwner(), s -> updateForm());
        viewModel.getSelected().observe(getViewLifecycleOwner(), id -> {
            adapter.setSelected(id);
            updateForm();
        });
        viewModel.getPasar().observe(getViewLifecycleOwner(), res -> {
            binding.pasarProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isSuccess()) {
                adapter.submitList(res.data);
                boolean empty = res.data == null || res.data.isEmpty();
                binding.pasarEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.pasarList.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });
        viewModel.getBusy().observe(getViewLifecycleOwner(),
                busy -> AuthFlow.setBusy(binding.verifikasiButton, binding.verifikasiSpinner, busy));
        binding.verifikasiButton.setOnClickListener(v -> viewModel.verify(
                binding.verificationCode.getText().toString(), r -> {
                    if (binding == null) {
                        return;
                    }
                    if (!r.isSuccess()) {
                        Messages.snack(this, getString(R.string.auth_gagal_koneksi));
                    } else if (Boolean.TRUE.equals(r.data)) {
                        AuthFlow.finish(this);
                    } else {
                        AuthFlow.alert(this, R.string.auth_verifikasi_judul, getString(R.string.auth_verifikasi_gagal),
                                R.string.auth_ok, null, 0, null);
                    }
                }));
        viewModel.start();
        updateForm();
    }

    /** The parts of build() that depend on the state. */
    private void updateForm() {
        if (binding == null) {
            return;
        }
        boolean seller = Boolean.TRUE.equals(viewModel.getSeller().getValue());
        binding.dan.setText(getString(R.string.auth_verifikasi_pesan, getString(seller
                ? R.string.auth_verifikasi_pesan_penjual : R.string.auth_verifikasi_pesan_pembeli)));
        binding.sellerBox.setVisibility(seller ? View.VISIBLE : View.GONE);
        boolean complete = viewModel.isFormComplete(binding.verificationCode.getText().length());
        binding.buttonHolder.setVisibility(complete ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
