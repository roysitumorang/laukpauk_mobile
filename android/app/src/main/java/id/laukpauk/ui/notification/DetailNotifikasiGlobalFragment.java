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
import id.laukpauk.data.model.notifikasi.Notifikasi;
import id.laukpauk.databinding.FragmentDetailNotifikasiGlobalBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/**
 * DetailNotifikasiGlobal.dart: picture, title, message and date of one "Info" notification.
 * Argument: {@code idNotifikasi} (string).
 */
public class DetailNotifikasiGlobalFragment extends Fragment {

    private FragmentDetailNotifikasiGlobalBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailNotifikasiGlobalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String idNotifikasi = requireArguments().getString("idNotifikasi");
        AppContainer c = AppContainer.from(requireContext());
        DetailNotifikasiGlobalViewModel viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new DetailNotifikasiGlobalViewModel(c.notifikasiRepository)))
                .get(DetailNotifikasiGlobalViewModel.class);

        viewModel.getDetail(idNotifikasi).observe(getViewLifecycleOwner(), res -> {
            binding.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isLoading()) {
                return;
            }
            Notifikasi n = res.isSuccess() ? res.data : null;
            binding.appBar.getRoot().setVisibility(View.VISIBLE);
            AppBars.setup(this, binding.appBar, n != null ? n.judul : "");
            if (n == null) {
                binding.notFound.setVisibility(View.VISIBLE);
                return;
            }
            binding.content.setVisibility(View.VISIBLE);
            if (n.foto != null && !n.foto.isEmpty()) {
                Images.url(binding.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarPushNotifikasi + "/" + n.foto);
            } else {
                binding.foto.setScaleType(android.widget.ImageView.ScaleType.CENTER);
                binding.foto.setImageResource(R.drawable.laukpauk_icon);
            }
            binding.judul.setText(n.judul);
            binding.pesan.setText(n.pesan);
            binding.tanggal.setText(getString(R.string.notifikasi_pada, n.createdAt));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
