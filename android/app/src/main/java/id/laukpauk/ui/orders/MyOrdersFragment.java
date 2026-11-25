package id.laukpauk.ui.orders;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.databinding.FragmentMyOrdersBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Json;

/**
 * PesananSaya.dart: the buyer's transactions ({@code GET buyer/transactions}): "Hari Ini" (created today, with a
 * status dropdown) or a status tab with creation dates. Args: statusTransaksi (initial tab 0..7).
 */
public class MyOrdersFragment extends Fragment {

    /** Statuses 0..8 of {@code buyer/transactions}. */
    private static final int STATUSES = 9;

    /** The tabs; their statuses are {@link MyOrdersViewModel#TAB_STATUS}. */
    private static final int[] TAB_LABELS = {R.string.orders_tab_today, R.string.orders_tab_all, R.string.orders_tab_unpaid,
            R.string.orders_tab_paid, R.string.orders_tab_in_process, R.string.orders_tab_in_transit,
            R.string.orders_tab_completed, R.string.orders_tab_cancelled};

    private FragmentMyOrdersBinding binding;
    private MyOrdersViewModel viewModel;
    private MyOrdersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        int initialTab = requireArguments().getInt("statusTransaksi", 0);
        if (initialTab < 0 || initialTab >= TAB_LABELS.length) {
            initialTab = 0;
        }
        final int startTab = initialTab;
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new MyOrdersViewModel(c.transactionRepository, c.settingRepository, startTab)))
                .get(MyOrdersViewModel.class);

        AppBars.setup(this, binding.appBar, getString(R.string.orders_transaction_records));
        NavController nav = NavHostFragment.findNavController(this);
        if (nav.getPreviousBackStackEntry() == null) {
            // a tab root: the legacy AppBar had no back button when the route could not be popped
            binding.appBar.toolbar.setNavigationIcon(null);
        }

        adapter = new MyOrdersAdapter(new MyOrdersAdapter.Listener() {
            @Override
            public void onOpen(BuyerTransaction t) {
                Bundle args = new Bundle();
                args.putString("idTransaksi", t.id);
                nav.navigate(R.id.transactionDetailFragment, args);
            }

            @Override
            public void onOrderAgain(BuyerTransaction t) {
                Bundle args = new Bundle();
                args.putString("idTransaksi", t.id);
                nav.navigate(R.id.repeatOrderFragment, args);
            }

            @Override
            public void onReceived(BuyerTransaction t) {
                OrderUi.confirmReceived(MyOrdersFragment.this, () ->
                        viewModel.orderReceived(t.id, result -> {
                            if (!isAdded()) {
                                return;
                            }
                            boolean ok = result.isSuccess() && Boolean.TRUE.equals(result.data);
                            if (ok) {
                                viewModel.load();
                            }
                            Messages.snack(MyOrdersFragment.this, getString(ok
                                    ? R.string.orders_received_success : R.string.orders_received_failed));
                        }));
            }

            @Override
            public void onConfirmPayment(BuyerTransaction t) {
                viewModel.contactWhatsapp(result -> {
                    if (!isAdded() || !result.isSuccess() || result.data == null || Json.isFalse(result.data)) {
                        return;
                    }
                    Object data = result.data;
                    String text = getString(R.string.orders_wa_confirmation_format, OrderUi.nz(t.referenceNumber),
                            OrderUi.nz(t.paymentChannelName));
                    String config = Json.str(Json.obj(data), "contactUsWhatsapp");
                    OrderUi.openUrl(MyOrdersFragment.this,
                            "https://wa.me/" + config + "?text=" + Uri.encode(text));
                });
            }
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0 && !rv.canScrollVertically(1)) {
                    viewModel.loadMore();
                }
            }
        });
        binding.empty.emptyText.setText(R.string.orders_records_empty);
        binding.refresh.setOnRefreshListener(() -> viewModel.load());

        setupTabs();
        setupFilter();
        AuthGate.require(this, this::observe);
    }

    private void setupTabs() {
        for (int label : TAB_LABELS) {
            binding.tabs.addTab(binding.tabs.newTab().setText(getString(label)));
        }
        TabLayout.Tab selected = binding.tabs.getTabAt(viewModel.getTab());
        if (selected != null) {
            selected.select();
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectTab(tab.getPosition());
                renderFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupFilter() {
        // "Hari Ini": all statuses, or one of them
        String[] labels = new String[STATUSES + 1];
        labels[0] = getString(R.string.orders_status_all);
        for (int i = 0; i < STATUSES; i++) {
            labels[i + 1] = OrderUi.statusLabel(requireContext(), i);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_orders_spinner,
                labels);
        spinnerAdapter.setDropDownViewResource(R.layout.item_orders_spinner_dropdown);
        binding.statusSpinner.setAdapter(spinnerAdapter);
        Integer chosen = viewModel.getTodayStatus();
        binding.statusSpinner.setSelection(chosen == null ? 0 : chosen + 1, false);
        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                viewModel.setTodayStatus(position == 0 ? null : position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.startDate.setOnClickListener(v -> showDatePicker(true));
        binding.endDate.setOnClickListener(v -> showDatePicker(false));
        binding.clearDates.setOnClickListener(v -> {
            viewModel.clearDates();
            renderFilter();
        });
        renderFilter();
    }

    private void renderFilter() {
        boolean today = viewModel.getTab() == MyOrdersViewModel.TAB_TODAY;
        binding.statusBox.setVisibility(today ? View.VISIBLE : View.GONE);
        binding.dateBox.setVisibility(today ? View.GONE : View.VISIBLE);
        binding.startDate.setText(viewModel.getStartDate());
        binding.endDate.setText(viewModel.getEndDate());
        binding.clearDates.setVisibility(viewModel.hasDateRange() ? View.VISIBLE : View.GONE);
    }

    /** showDatePicker(first 2017, last 2050): "yyyy-MM-dd", sent as created_at_start / created_at_end. */
    private void showDatePicker(boolean start) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            int monthNumber = month + 1;
            String formatted = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, monthNumber, day);
            if (start) {
                viewModel.setStartDate(formatted);
            } else {
                viewModel.setEndDate(formatted);
            }
            renderFilter();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        Calendar min = Calendar.getInstance();
        min.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar max = Calendar.getInstance();
        max.set(2050, Calendar.JANUARY, 1, 0, 0, 0);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());
        dialog.show();
    }

    private void observe() {
        viewModel.getList().observe(getViewLifecycleOwner(), this::render);
        viewModel.start();
    }

    private void render(Resource<List<BuyerTransaction>> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            binding.empty.getRoot().setVisibility(View.GONE);
            adapter.submitList(null);
            return;
        }
        binding.refresh.setRefreshing(false);
        List<BuyerTransaction> items = res.isSuccess() ? res.data : null;
        adapter.submitList(items);
        binding.empty.getRoot().setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
