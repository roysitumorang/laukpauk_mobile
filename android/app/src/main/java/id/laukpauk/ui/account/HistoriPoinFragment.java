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
import id.laukpauk.data.model.account.RiwayatPoint;
import id.laukpauk.databinding.FragmentHistoriPoinBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;

/** HistoriPoin.dart: the points history of the signed-in user. */
public class HistoriPoinFragment extends Fragment {

    private FragmentHistoriPoinBinding binding;
    private HistoriPoinViewModel viewModel;
    private boolean started;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoriPoinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_poin_title));
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new HistoriPoinViewModel(c.userRepository)))
                .get(HistoriPoinViewModel.class);
        HistoriPoinAdapter adapter = new HistoriPoinAdapter();
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(viewModel::load);
        started = false;
        AuthGate.require(this, () -> start(adapter));
    }

    private void start(HistoriPoinAdapter adapter) {
        if (binding == null || started) {
            return;
        }
        started = true;
        viewModel.getHistori().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
            if (!loading) {
                binding.refresh.setRefreshing(false);
                List<RiwayatPoint> items = res.data;
                adapter.submitList(items);
                boolean empty = items == null || items.isEmpty();
                binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });
        if (viewModel.getHistori().getValue() == null) {
            viewModel.load();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
