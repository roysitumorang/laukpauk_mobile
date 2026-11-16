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

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.account.WalletRecords;
import id.laukpauk.databinding.FragmentWalletHistoryBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;

/** HistoriDompet.dart: wallet history. Argument: walletType ("referral" / "potongantransaksi", optional). */
public class WalletHistoryFragment extends Fragment {

    private FragmentWalletHistoryBinding binding;
    private WalletHistoryViewModel viewModel;
    private boolean started;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_wallet_title));
        AppContainer c = AppContainer.from(requireContext());
        String arg = requireArguments().getString("jenisDompet");
        int initial = 0;
        if (arg != null) {
            String lc = arg.toLowerCase(Locale.ROOT);
            initial = lc.equals("referral") ? 1 : lc.equals("potongantransaksi") ? 2 : 0;
        }
        final int initialType = initial;
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new WalletHistoryViewModel(c.userRepository, initialType)))
                .get(WalletHistoryViewModel.class);
        WalletHistoryAdapter adapter = new WalletHistoryAdapter();
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        int[] labels = {R.string.account_wallet_tab_all, R.string.account_wallet_tab_referral,
                R.string.account_wallet_tab_deduction};
        for (int label : labels) {
            binding.tabs.addTab(binding.tabs.newTab().setText(getString(label)));
        }
        TabLayout.Tab selected = binding.tabs.getTabAt(viewModel.getType().getValue() == null ? 0
                : viewModel.getType().getValue());
        if (selected != null) {
            selected.select();
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectType(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        binding.refresh.setOnRefreshListener(viewModel::load);
        started = false;
        AuthGate.require(this, () -> start(adapter));
    }

    private void start(WalletHistoryAdapter adapter) {
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
            if (loading) {
                adapter.submitList(null);
                binding.empty.setVisibility(View.GONE);
            } else {
                binding.refresh.setRefreshing(false);
                List<WalletRecords> items = res.data;
                adapter.submitList(items);
                binding.empty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
