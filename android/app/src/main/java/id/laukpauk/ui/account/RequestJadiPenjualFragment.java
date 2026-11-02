package id.laukpauk.ui.account;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.Pasar;
import id.laukpauk.databinding.FragmentRequestJadiPenjualBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.location.PinGMAPSFragment;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Json;

/** RequestJadiPenjual.dart: apply to become a seller. */
public class RequestJadiPenjualFragment extends Fragment {

    private FragmentRequestJadiPenjualBinding binding;
    private RequestJadiPenjualViewModel viewModel;
    private PasarAdapter pasarAdapter;
    private boolean started;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestJadiPenjualBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_rjp_app_bar));
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new RequestJadiPenjualViewModel(c.accountRepository)))
                .get(RequestJadiPenjualViewModel.class);
        started = false;
        AuthGate.require(this, this::start);
    }

    private static String text(TextView v) {
        return v.getText() == null ? "" : v.getText().toString();
    }

    private void start() {
        if (binding == null || started) {
            return;
        }
        started = true;

        pasarAdapter = new PasarAdapter(p -> viewModel.setPasarSelected(String.valueOf(p.id)));
        binding.pasarList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.pasarList.setAdapter(pasarAdapter);

        TextWatcher completeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateAjukan();
            }
        };
        binding.namaToko.addTextChangedListener(completeWatcher);
        binding.alamatToko.addTextChangedListener(completeWatcher);
        binding.bio.addTextChangedListener(completeWatcher);
        binding.nomorRekening.addTextChangedListener(completeWatcher);
        binding.cariPasar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setPencarian(s.toString().toLowerCase(Locale.ROOT));
            }
        });

        binding.lokasiDropdown.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), binding.lokasiDropdown);
            menu.getMenu().add(0, 0, 0, R.string.account_rjp_lokasi_berjualan);
            menu.getMenu().add(0, 1, 1, R.string.account_rjp_pasar_tersedia);
            menu.getMenu().add(0, 2, 2, R.string.account_rjp_lokasi_tersendiri);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    viewModel.setLokasiBerjualan(RequestJadiPenjualViewModel.PASAR_TERSEDIA);
                } else if (item.getItemId() == 2) {
                    String[] ll = viewModel.getLokasiTersendiri().getValue();
                    if (ll == null) {
                        pilihLokasi();
                    } else {
                        viewModel.setLokasiBerjualan(RequestJadiPenjualViewModel.LOKASI_TERSENDIRI);
                    }
                }
                return true;
            });
            menu.show();
        });
        binding.pilihLain.setOnClickListener(v -> pilihLokasi());
        NavResults.<String>observe(this, PinGMAPSFragment.RESULT_KEY, json -> {
            JsonObject result = Json.parseObject(json);
            if (result != null) {
                viewModel.setLokasiTersendiri(Json.str(result, "lat"), Json.str(result, "lng"));
            }
        });

        binding.ajukan.setOnClickListener(v -> ajukan());

        viewModel.getLokasiBerjualan().observe(getViewLifecycleOwner(), lokasi -> {
            boolean tersendiri = RequestJadiPenjualViewModel.LOKASI_TERSENDIRI.equals(lokasi);
            binding.lokasiLabel.setText(tersendiri ? R.string.account_rjp_lokasi_tersendiri
                    : R.string.account_rjp_pasar_tersedia);
            binding.lokasiBox.setVisibility(tersendiri ? View.VISIBLE : View.GONE);
            binding.pasarBox.setVisibility(tersendiri ? View.GONE : View.VISIBLE);
        });
        viewModel.getLokasiTersendiri().observe(getViewLifecycleOwner(), ll -> {
            if (ll != null) {
                binding.lokasiKoordinat.setText(getString(R.string.account_rjp_koordinat, ll[0], ll[1]));
            }
        });
        viewModel.getPasarSelected().observe(getViewLifecycleOwner(), id -> pasarAdapter.setSelected(id));
        viewModel.getBusy().observe(getViewLifecycleOwner(), busy -> binding.ajukan.setEnabled(!Boolean.TRUE.equals(busy)));
        viewModel.getPasar().observe(getViewLifecycleOwner(), res -> showPasar());
        viewModel.getPencarian().observe(getViewLifecycleOwner(), q -> showPasar());
        updateAjukan();
    }

    private void showPasar() {
        if (binding == null) {
            return;
        }
        Resource<List<Pasar>> res = viewModel.getPasar().getValue();
        if (res == null || res.isLoading()) {
            binding.pasarProgress.setVisibility(View.VISIBLE);
            binding.pasarList.setVisibility(View.GONE);
            binding.pasarEmpty.setVisibility(View.GONE);
            return;
        }
        binding.pasarProgress.setVisibility(View.GONE);
        String q = viewModel.getPencarian().getValue() == null ? "" : viewModel.getPencarian().getValue();
        List<Pasar> filtered = new ArrayList<>();
        if (res.data != null) {
            for (Pasar p : res.data) {
                if (p.nama != null && p.nama.toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(p);
                }
            }
        }
        pasarAdapter.submitList(filtered);
        binding.pasarList.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        binding.pasarEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void pilihLokasi() {
        Bundle args = new Bundle();
        args.putString("appBarText", getString(R.string.account_rjp_pilih_lokasi));
        NavHostFragment.findNavController(this).navigate(R.id.pinGMAPSFragment, args);
    }

    private void updateAjukan() {
        boolean complete = text(binding.nomorRekening).length() >= 1 && text(binding.namaToko).length() >= 1
                && text(binding.alamatToko).length() >= 1 && text(binding.bio).length() >= 1;
        binding.ajukan.setVisibility(complete ? View.VISIBLE : View.GONE);
    }

    private void ajukan() {
        viewModel.ajukan(text(binding.namaToko), text(binding.alamatToko), text(binding.bio),
                text(binding.nomorRekening), result -> {
                    if (!isAdded() || !result.isSuccess()) {
                        return; // legacy: nothing happens when the request does not answer 200
                    }
                    final boolean ok = Boolean.TRUE.equals(result.data);
                    Messages.info(this, getString(R.string.account_rjp_pengajuan),
                            getString(ok ? R.string.account_rjp_berhasil : R.string.account_rjp_gagal), () -> {
                                if (ok && isAdded()) {
                                    AccountNav.toDashboard(this);
                                }
                            });
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
