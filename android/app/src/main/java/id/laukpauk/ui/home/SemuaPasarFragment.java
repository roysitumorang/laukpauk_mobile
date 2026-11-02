package id.laukpauk.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentSemuaPasarBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;

/** SemuaPasar.dart: every market with a name search. */
public class SemuaPasarFragment extends Fragment {

    private FragmentSemuaPasarBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSemuaPasarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        AppBars.setup(this, binding.appBar, getString(R.string.home_semua_pasar_title));
        PasarViewModel vm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new PasarViewModel(c.homeRepository, c.deliveryRepository, null, false)))
                .get(PasarViewModel.class);
        new PasarSection(this, binding.pasar, vm, false, true, false, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
