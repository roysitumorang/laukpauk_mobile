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
import id.laukpauk.data.repository.NotificationRepository;
import id.laukpauk.databinding.FragmentNotificationBinding;
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
public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private NotificationViewModel viewModel;
    private NotificationInfoAdapter infoAdapter;
    private SpecialNotificationAdapter specialAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new NotificationViewModel(c.notificationRepository)))
                .get(NotificationViewModel.class);

        infoAdapter = new NotificationInfoAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("idNotifikasi", item.notificationId);
            NavHostFragment.findNavController(this).navigate(R.id.globalNotificationDetailFragment, args);
        });
        specialAdapter = new SpecialNotificationAdapter(item -> viewModel.open(item, result -> {
            if (!isAdded()) {
                return;
            }
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                MainRouter.route(NavHostFragment.findNavController(this), item.routeName, item.routeParams);
            } else {
                Toast.makeText(requireContext(), R.string.notification_status_change_failed, Toast.LENGTH_SHORT).show();
            }
        }));
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.notification_tab_info))
                .setTag(NotificationRepository.INFO_TYPE));
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.notification_tab_special))
                .setTag(NotificationRepository.SPECIAL_TYPE));
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectType((String) tab.getTag());
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
        viewModel.getType().observe(getViewLifecycleOwner(), type -> {
            boolean info = NotificationRepository.INFO_TYPE.equals(type);
            binding.list.setAdapter(info ? infoAdapter : specialAdapter);
            binding.empty.emptyText.setText(info ? R.string.notification_info_empty : R.string.special_notification_empty);
            TabLayout.Tab tab = binding.tabs.getTabAt(info ? 0 : 1);
            if (tab != null && binding.tabs.getSelectedTabPosition() != tab.getPosition()) {
                tab.select();
            }
        });
        viewModel.getInfo().observe(getViewLifecycleOwner(), res -> {
            if (NotificationRepository.INFO_TYPE.equals(viewModel.getType().getValue())) {
                render(res, infoAdapter::submitList);
            }
        });
        viewModel.getSpecial().observe(getViewLifecycleOwner(), res -> {
            if (NotificationRepository.SPECIAL_TYPE.equals(viewModel.getType().getValue())) {
                render(res, specialAdapter::submitList);
            }
        });
        if (viewModel.getInfo().getValue() == null && viewModel.getSpecial().getValue() == null) {
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
