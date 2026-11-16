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
import id.laukpauk.databinding.FragmentMyProfileBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.ImagePicker;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.ServerPath;

/** ProfilSaya.dart: photo, profile data, phone number and password changes, and logout. */
public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding binding;
    private MyProfileViewModel viewModel;
    private ImagePicker imagePicker;
    private boolean started;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new MyProfileViewModel(c.userRepository, c.accountRepository)))
                .get(MyProfileViewModel.class);
        imagePicker = new ImagePicker(this, file -> viewModel.setPhotoNew(file));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_my_profile_title));
        binding.appBar.toolbar.inflateMenu(R.menu.menu_account_profile);
        binding.appBar.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.actionLogout) {
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
        binding.changePhoto.setOnClickListener(v -> imagePicker.show());
        binding.photoSave.setOnClickListener(v -> viewModel.savePhoto(result -> { }));
        binding.profileSave.setOnClickListener(v -> viewModel.saveProfile(text(binding.fullName),
                text(binding.address), text(binding.username)));

        // phone number
        watch(binding.newNumber, this::updateNumberButtons);
        watch(binding.verificationCode, this::updateNumberButtons);
        binding.numberSave.setOnClickListener(v -> viewModel.newNumberSave(text(binding.newNumber)));
        binding.cancelNumber.setOnClickListener(v -> viewModel.cancelNumber());
        binding.numberVerification.setOnClickListener(v -> viewModel.numberVerification(text(binding.verificationCode),
                result -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                        AccountNav.toDashboard(this);
                    } else {
                        Messages.info(this, getString(R.string.account_verification_title),
                                getString(R.string.account_verification_failed), null);
                    }
                }));

        // password
        Runnable pw = this::updatePasswordButton;
        watch(binding.newPassword, pw);
        watch(binding.repeatPassword, pw);
        binding.passwordSave.setOnClickListener(v -> viewModel.changePassword(text(binding.oldPassword),
                text(binding.newPassword), text(binding.repeatPassword), result -> {
                    if (!isAdded() || !result.isSuccess()) {
                        return;
                    }
                    final boolean ok = Boolean.TRUE.equals(result.data);
                    Messages.info(this, getString(R.string.account_password_title),
                            getString(ok ? R.string.account_password_success : R.string.account_password_failed),
                            () -> {
                                if (ok && isAdded()) {
                                    AccountNav.toDashboard(this);
                                }
                            });
                }));
    }

    private void updatePasswordButton() {
        String newPassword = text(binding.newPassword);
        String retry = text(binding.repeatPassword);
        boolean complete = newPassword.length() >= 8 && retry.length() >= 8 && newPassword.equals(retry);
        binding.passwordSave.setVisibility(complete ? View.VISIBLE : View.GONE);
    }

    private void updateNumberButtons() {
        binding.numberSave.setVisibility(text(binding.newNumber).length() >= 12 ? View.VISIBLE : View.GONE);
        binding.numberVerification.setVisibility(text(binding.verificationCode).length() >= 6 ? View.VISIBLE : View.GONE);
    }

    private void observe() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), res -> {
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
        viewModel.getPhotoNew().observe(getViewLifecycleOwner(), file -> {
            binding.photoSave.setVisibility(file != null ? View.VISIBLE : View.GONE);
            if (file != null) {
                Images.circle(binding.photo, file.getAbsolutePath());
            } else {
                UserDetail current = currentUser();
                if (current != null) {
                    showPhoto(current);
                }
            }
        });
        viewModel.getSavingPhoto().observe(getViewLifecycleOwner(), busy -> {
            boolean b = Boolean.TRUE.equals(busy);
            binding.photoProgress.setVisibility(b ? View.VISIBLE : View.GONE);
            binding.photoSave.setEnabled(!b);
        });
        viewModel.getSavingProfile().observe(getViewLifecycleOwner(),
                busy -> binding.profileSave.setEnabled(!Boolean.TRUE.equals(busy)));
        viewModel.getChangeId().observe(getViewLifecycleOwner(), id -> {
            boolean pending = id != null && id != 0;
            binding.numberForm.setVisibility(pending ? View.GONE : View.VISIBLE);
            binding.verificationNumber.setVisibility(pending ? View.VISIBLE : View.GONE);
            updateNumberButtons();
        });
        viewModel.getChangeCount().observe(getViewLifecycleOwner(), quantity -> {
            binding.numberCheckProgress.setVisibility(quantity == null ? View.VISIBLE : View.GONE);
            binding.numberLimit.setVisibility(quantity != null && quantity >= 1 ? View.VISIBLE : View.GONE);
            binding.newNumberBox.setVisibility(quantity != null && quantity < 1 ? View.VISIBLE : View.GONE);
        });
    }

    @Nullable
    private UserDetail currentUser() {
        return viewModel.getProfile().getValue() == null ? null : viewModel.getProfile().getValue().data;
    }

    private void showPhoto(UserDetail user) {
        Images.circle(binding.photo, ServerPath.baseURL + "/" + ServerPath.uploadImageUser + "/" + user.photo);
    }

    private void showUser(UserDetail user) {
        if (viewModel.getPhotoNew().getValue() == null) {
            showPhoto(user);
        }
        String created = user.createdAt == null ? "" : user.createdAt.toUpperCase(Locale.ROOT);
        binding.name.setText(user.name == null ? "" : user.name.toUpperCase(Locale.ROOT));
        binding.referralCode.setText(user.referralCode);
        binding.join.setText(getString(R.string.account_join, created));
        binding.updated.setText(getString(R.string.account_updated, created));
        binding.currentNumber.setText(getString(R.string.account_current_number, user.phoneNumber == null ? "" : user.phoneNumber));
        if (viewModel.consumeRefillFields()) {
            binding.fullName.setText(user.name);
            binding.address.setText(user.address);
            binding.username.setText(user.username);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
