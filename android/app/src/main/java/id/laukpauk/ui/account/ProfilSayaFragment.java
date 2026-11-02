package id.laukpauk.ui.account;

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

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.databinding.FragmentProfilSayaBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.ImagePicker;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.ServerPath;

/** ProfilSaya.dart: photo, profile data, phone number and password changes, and logout. */
public class ProfilSayaFragment extends Fragment {

    private FragmentProfilSayaBinding binding;
    private ProfilSayaViewModel viewModel;
    private ImagePicker imagePicker;
    private boolean started;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new ProfilSayaViewModel(c.userRepository, c.accountRepository)))
                .get(ProfilSayaViewModel.class);
        imagePicker = new ImagePicker(this, file -> viewModel.setFotoBaru(file));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfilSayaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_profil_saya_title));
        binding.appBar.toolbar.inflateMenu(R.menu.menu_account_profil);
        binding.appBar.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.actionKeluar) {
                logout();
                return true;
            }
            return false;
        });
        started = false;
        AuthGate.require(this, this::start);
    }

    private void start() {
        if (binding == null || started) {
            return;
        }
        started = true;
        bindActions();
        observe();
        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    private void logout() {
        viewModel.logout(result -> {
            if (isAdded() && result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                AccountNav.toHome(this);
            }
        });
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private void watch(android.widget.TextView field, Runnable onChange) {
        field.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                onChange.run();
            }
        });
    }

    private static String text(android.widget.TextView v) {
        return v.getText() == null ? "" : v.getText().toString();
    }

    private void bindActions() {
        binding.gantiFoto.setOnClickListener(v -> imagePicker.show());
        binding.simpanFoto.setOnClickListener(v -> viewModel.saveFoto(result -> { }));
        binding.simpanProfil.setOnClickListener(v -> viewModel.saveProfil(text(binding.namaLengkap),
                text(binding.alamat), text(binding.username)));

        // phone number
        watch(binding.nomorBaru, this::updateNomorButtons);
        watch(binding.kodeVerifikasi, this::updateNomorButtons);
        binding.simpanNomor.setOnClickListener(v -> viewModel.simpanNomorBaru(text(binding.nomorBaru)));
        binding.batalNomor.setOnClickListener(v -> viewModel.batalNomor());
        binding.verifikasiNomor.setOnClickListener(v -> viewModel.verifikasiNomor(text(binding.kodeVerifikasi),
                result -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                        AccountNav.toDashboard(this);
                    } else {
                        Messages.info(this, getString(R.string.account_verifikasi_title),
                                getString(R.string.account_verifikasi_gagal), null);
                    }
                }));

        // password
        Runnable pw = this::updatePasswordButton;
        watch(binding.passwordBaru, pw);
        watch(binding.passwordUlangi, pw);
        binding.simpanPassword.setOnClickListener(v -> viewModel.gantiPassword(text(binding.passwordLama),
                text(binding.passwordBaru), text(binding.passwordUlangi), result -> {
                    if (!isAdded() || !result.isSuccess()) {
                        return;
                    }
                    final boolean ok = Boolean.TRUE.equals(result.data);
                    Messages.info(this, getString(R.string.account_password_title),
                            getString(ok ? R.string.account_password_berhasil : R.string.account_password_gagal),
                            () -> {
                                if (ok && isAdded()) {
                                    AccountNav.toDashboard(this);
                                }
                            });
                }));
    }

    private void updatePasswordButton() {
        String baru = text(binding.passwordBaru);
        String ulangi = text(binding.passwordUlangi);
        boolean complete = baru.length() >= 8 && ulangi.length() >= 8 && baru.equals(ulangi);
        binding.simpanPassword.setVisibility(complete ? View.VISIBLE : View.GONE);
    }

    private void updateNomorButtons() {
        binding.simpanNomor.setVisibility(text(binding.nomorBaru).length() >= 12 ? View.VISIBLE : View.GONE);
        binding.verifikasiNomor.setVisibility(text(binding.kodeVerifikasi).length() >= 6 ? View.VISIBLE : View.GONE);
    }

    private void observe() {
        viewModel.getProfil().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            if (loading && binding.content.getVisibility() == View.VISIBLE) {
                return; // keep showing the form while it is reloaded
            }
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                return;
            }
            UserDetail user = res.isSuccess() ? res.data : null;
            binding.notFound.setVisibility(user == null ? View.VISIBLE : View.GONE);
            binding.content.setVisibility(user != null ? View.VISIBLE : View.GONE);
            if (user != null) {
                showUser(user);
            }
        });
        viewModel.getFotoBaru().observe(getViewLifecycleOwner(), file -> {
            binding.simpanFoto.setVisibility(file != null ? View.VISIBLE : View.GONE);
            if (file != null) {
                Images.circle(binding.foto, file.getAbsolutePath());
            } else {
                UserDetail current = currentUser();
                if (current != null) {
                    showFoto(current);
                }
            }
        });
        viewModel.getSavingFoto().observe(getViewLifecycleOwner(), busy -> {
            boolean b = Boolean.TRUE.equals(busy);
            binding.fotoProgress.setVisibility(b ? View.VISIBLE : View.GONE);
            binding.simpanFoto.setEnabled(!b);
        });
        viewModel.getSavingProfil().observe(getViewLifecycleOwner(),
                busy -> binding.simpanProfil.setEnabled(!Boolean.TRUE.equals(busy)));
        viewModel.getIdPenggantian().observe(getViewLifecycleOwner(), id -> {
            boolean pending = id != null && id != 0;
            binding.nomorForm.setVisibility(pending ? View.GONE : View.VISIBLE);
            binding.nomorVerifikasi.setVisibility(pending ? View.VISIBLE : View.GONE);
            updateNomorButtons();
        });
        viewModel.getJumlahGanti().observe(getViewLifecycleOwner(), jumlah -> {
            binding.nomorCekProgress.setVisibility(jumlah == null ? View.VISIBLE : View.GONE);
            binding.nomorBatas.setVisibility(jumlah != null && jumlah >= 1 ? View.VISIBLE : View.GONE);
            binding.nomorBaruBox.setVisibility(jumlah != null && jumlah < 1 ? View.VISIBLE : View.GONE);
        });
    }

    @Nullable
    private UserDetail currentUser() {
        return viewModel.getProfil().getValue() == null ? null : viewModel.getProfil().getValue().data;
    }

    private void showFoto(UserDetail user) {
        Images.circle(binding.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarUser + "/" + user.foto);
    }

    private void showUser(UserDetail user) {
        if (viewModel.getFotoBaru().getValue() == null) {
            showFoto(user);
        }
        String created = user.createdAt == null ? "" : user.createdAt.toUpperCase(Locale.ROOT);
        binding.nama.setText(user.nama == null ? "" : user.nama.toUpperCase(Locale.ROOT));
        binding.kodeReferal.setText(user.kodeReferal);
        binding.bergabung.setText(getString(R.string.account_bergabung, created));
        binding.diperbarui.setText(getString(R.string.account_diperbarui, created));
        binding.nomorSaatIni.setText(getString(R.string.account_nomor_saat_ini, user.nomorHP == null ? "" : user.nomorHP));
        if (viewModel.consumeRefillFields()) {
            binding.namaLengkap.setText(user.nama);
            binding.alamat.setText(user.alamat);
            binding.username.setText(user.username);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
