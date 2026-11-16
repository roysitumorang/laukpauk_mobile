package id.laukpauk.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.laukpauk.R;
import id.laukpauk.data.model.notification.Notification;
import id.laukpauk.databinding.FragmentGlobalNotificationDetailBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/**
 * DetailNotifikasiGlobal.dart: picture, title, message and date of one "Info" notification.
 * Argument: {@code notificationId} (string).
 */
public class GlobalNotificationDetailFragment extends Fragment {

    private FragmentGlobalNotificationDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGlobalNotificationDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String notificationId = requireArguments().getString("idNotifikasi");
        AppContainer c = AppContainer.from(requireContext());
        GlobalNotificationDetailViewModel viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new GlobalNotificationDetailViewModel(c.notificationRepository)))
                .get(GlobalNotificationDetailViewModel.class);

        viewModel.getDetail(notificationId).observe(getViewLifecycleOwner(), res -> {
            binding.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isLoading()) {
                return;
            }
            Notification n = res.isSuccess() ? res.data : null;
            binding.appBar.getRoot().setVisibility(View.VISIBLE);
            AppBars.setup(this, binding.appBar, n != null ? n.heading : "");
            if (n == null) {
                binding.notFound.setVisibility(View.VISIBLE);
                return;
            }
            binding.content.setVisibility(View.VISIBLE);
            if (n.photo != null && !n.photo.isEmpty()) {
                Images.url(binding.photo, ServerPath.baseURL + "/" + ServerPath.uploadImagePushNotification + "/" + n.photo);
            } else {
                binding.photo.setScaleType(android.widget.ImageView.ScaleType.CENTER);
                binding.photo.setImageResource(R.drawable.laukpauk_icon);
            }
            binding.heading.setText(n.heading);
            binding.message.setText(n.message);
            binding.date.setText(getString(R.string.notification_at, n.createdAt));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
