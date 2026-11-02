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
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.data.repository.TokoSayaRepository;
import id.laukpauk.databinding.FragmentUbahNamaTokoFormBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.NavResults;

/** UbahNamaToko.dart (form): sends the result (Boolean) back to the dashboard. */
public class UbahNamaTokoFormFragment extends Fragment {

    private FragmentUbahNamaTokoFormBinding binding;
    private TokoSayaRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUbahNamaTokoFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = AppContainer.from(requireContext()).tokoSayaRepository;
        AppBars.setup(this, binding.appBar, getString(R.string.seller_toko_ubah_nama_toko));
        binding.namaToko.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.simpan.setVisibility(s.length() >= 1 ? View.VISIBLE : View.GONE);
            }
        });
        binding.simpan.setOnClickListener(v -> {
            binding.simpan.setEnabled(false);
            repository.ubahNamaToko(binding.namaToko.getText().toString(), result -> {
                if (!isAdded()) {
                    return;
                }
                NavResults.setResult(this, TokoSayaFragment.RESULT_UBAH_NAMA, Boolean.TRUE.equals(result.data));
                NavHostFragment.findNavController(this).popBackStack();
            });
        });
        AuthGate.require(this, () -> { });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
