package id.laukpauk.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.account.PointRecords;
import id.laukpauk.databinding.FragmentPointsHistoryBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;

/** HistoriPoin.dart: the points history of the signed-in user. */
public class PointsHistoryFragment extends Fragment {

    private FragmentPointsHistoryBinding binding;
    private PointsHistoryViewModel viewModel;
    private boolean started;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPointsHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_points_title));
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new PointsHistoryViewModel(c.userRepository)))
                .get(PointsHistoryViewModel.class);
        PointsHistoryAdapter adapter = new PointsHistoryAdapter();
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(viewModel::load);
        started = false;
        AuthGate.require(this, () -> start(adapter));
    }

    private void start(PointsHistoryAdapter adapter) {
        if (binding == null || started) {
            return;
        }
        started = true;
        viewModel.getHistory().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
            if (!loading) {
                binding.refresh.setRefreshing(false);
                List<PointRecords> items = res.data;
                adapter.submitList(items);
                boolean empty = items == null || items.isEmpty();
                binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });
        if (viewModel.getHistory().getValue() == null) {
            viewModel.load();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
