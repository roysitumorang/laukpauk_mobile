package id.laukpauk.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.repository.NotifikasiRepository;
import id.laukpauk.databinding.FragmentNotifikasiBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.startup.MainRouter;

/**
 * Notifikasi.dart: the Info and personal notification lists (signed-in users only).
 *
 * Reference implementation of the app's screen pattern: ViewBinding layout, a ViewModel built by
 * {@link ViewModelFactory} with its repository injected from {@link AppContainer}, LiveData observed with the
 * view's lifecycle, ListAdapters for lists, and navigation through the NavController.
 */
public class NotifikasiFragment extends Fragment {

    private FragmentNotifikasiBinding binding;
    private NotifikasiViewModel viewModel;
    private NotifikasiInfoAdapter infoAdapter;
    private NotifikasiKhususAdapter khususAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotifikasiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new NotifikasiViewModel(c.notifikasiRepository)))
                .get(NotifikasiViewModel.class);

        infoAdapter = new NotifikasiInfoAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("idNotifikasi", item.idNotifikasi);
            NavHostFragment.findNavController(this).navigate(R.id.detailNotifikasiGlobalFragment, args);
        });
        khususAdapter = new NotifikasiKhususAdapter(item -> viewModel.open(item, result -> {
            if (!isAdded()) {
                return;
            }
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                MainRouter.route(NavHostFragment.findNavController(this), item.routeName, item.routeParams);
            } else {
                Toast.makeText(requireContext(), R.string.notifikasi_gagal_ubah_status, Toast.LENGTH_SHORT).show();
            }
        }));
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.notifikasi_tab_info))
                .setTag(NotifikasiRepository.JENIS_INFO));
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.notifikasi_tab_khusus))
                .setTag(NotifikasiRepository.JENIS_KHUSUS));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectJenis((String) tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.refresh.setOnRefreshListener(viewModel::load);

        AuthGate.require(this, this::observe);
    }

    private void observe() {
        viewModel.getJenis().observe(getViewLifecycleOwner(), jenis -> {
            boolean info = NotifikasiRepository.JENIS_INFO.equals(jenis);
            binding.list.setAdapter(info ? infoAdapter : khususAdapter);
            binding.empty.emptyText.setText(info ? R.string.notifikasi_info_kosong : R.string.notifikasi_khusus_kosong);
            TabLayout.Tab tab = binding.tabs.getTabAt(info ? 0 : 1);
            if (tab != null && binding.tabs.getSelectedTabPosition() != tab.getPosition()) {
                tab.select();
            }
        });
        viewModel.getInfo().observe(getViewLifecycleOwner(), res -> {
            if (NotifikasiRepository.JENIS_INFO.equals(viewModel.getJenis().getValue())) {
                render(res, infoAdapter::submitList);
            }
        });
        viewModel.getKhusus().observe(getViewLifecycleOwner(), res -> {
            if (NotifikasiRepository.JENIS_KHUSUS.equals(viewModel.getJenis().getValue())) {
                render(res, khususAdapter::submitList);
            }
        });
        if (viewModel.getInfo().getValue() == null && viewModel.getKhusus().getValue() == null) {
            viewModel.load();
        }
    }

    private interface Submit<T> {
        void submit(List<T> list);
    }

    private <T> void render(Resource<List<T>> res, Submit<T> submit) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (!loading) {
            binding.refresh.setRefreshing(false);
        }
        if (res.isSuccess()) {
            List<T> items = res.data;
            submit.submit(items);
            binding.empty.getRoot().setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
