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
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.orders.PesananMasukItem;
import id.laukpauk.databinding.FragmentPesananItemTidakTersediaBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.NavResults;

/**
 * PesananItemTidakTersedia.dart: the reason an ordered item is not available. Argument: pesananItem (JSON of
 * PesananMasukItem). Result: {@link #RESULT_KEY} = Boolean (claim saved or not).
 */
public class PesananItemTidakTersediaFragment extends Fragment {

    public static final String RESULT_KEY = "pesanan_item_tidak_tersedia_result";

    private FragmentPesananItemTidakTersediaBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPesananItemTidakTersediaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.seller_pesanan_tidak_tersedia_title));
        AppContainer c = AppContainer.from(requireContext());
        PesananMasukItem item = c.apiClient.gson().fromJson(requireArguments().getString("pesananItem"),
                PesananMasukItem.class);
        PesananItemTidakTersediaViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new PesananItemTidakTersediaViewModel(c.pesananMasukRepository, item.id)))
                .get(PesananItemTidakTersediaViewModel.class);

        binding.keterangan.setText(viewModel.keterangan);
        binding.klaim.setVisibility(viewModel.keterangan.isEmpty() ? View.GONE : View.VISIBLE);
        binding.keterangan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.keterangan = s.toString();
                binding.klaim.setVisibility(viewModel.keterangan.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        binding.klaim.setOnClickListener(v -> viewModel.klaim(r -> {
            if (!isAdded()) {
                return;
            }
            NavResults.setResult(this, RESULT_KEY, r.isSuccess() && Boolean.TRUE.equals(r.data));
            NavHostFragment.findNavController(this).popBackStack();
        }));
        viewModel.getSending().observe(getViewLifecycleOwner(), s -> binding.klaim.setEnabled(!s));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
