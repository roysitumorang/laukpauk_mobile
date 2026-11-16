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

import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.databinding.FragmentMyOrdersBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Json;

/** PesananSaya.dart: the buyer's transaction history (bottom-nav tab). Args: transactionStatus (initial tab 0..5). */
public class MyOrdersFragment extends Fragment {

    private static final int[] TAB_LABELS = {R.string.orders_tab_today, R.string.orders_tab_not_processed,
            R.string.orders_tab_in_process, R.string.orders_tab_in_transit, R.string.orders_tab_completed,
            R.string.orders_tab_cancelled};
    private static final int[] DD_LABELS = {R.string.orders_dd_all, R.string.orders_dd_pending,
            R.string.orders_dd_processing, R.string.orders_dd_otw, R.string.orders_dd_received,
            R.string.orders_dd_rejected};
    private static final String[] DD_VALUES = {"", "pending", "processing", "otw", "received", "rejected"};

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
            public void onOpen(Transaction t) {
                Bundle args = new Bundle();
                args.putString("idTransaksi", t.id);
                nav.navigate(R.id.transactionDetailFragment, args);
            }

            @Override
            public void onOrderAgain(Transaction t) {
                Bundle args = new Bundle();
                args.putString("idTransaksi", t.id);
                nav.navigate(R.id.repeatOrderFragment, args);
            }

            @Override
            public void onReceived(Transaction t) {
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
            public void onConfirmPayment(Transaction t) {
                viewModel.contactWhatsapp(result -> {
                    if (!isAdded() || !result.isSuccess() || result.data == null || Json.isFalse(result.data)) {
                        return;
                    }
                    Object data = result.data;
                    String text = getString(R.string.orders_wa_confirmation_format, OrderUi.nz(t.transactionNumber),
                            OrderUi.nz(t.paymentMethodName));
                    String config = Json.str(Json.obj(data), "contactUsWhatsapp");
                    OrderUi.openUrl(MyOrdersFragment.this,
                            "https://wa.me/" + config + "?text=" + Uri.encode(text));
                });
            }
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
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
        String[] labels = new String[DD_LABELS.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = getString(DD_LABELS[i]);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_orders_spinner,
                labels);
        spinnerAdapter.setDropDownViewResource(R.layout.item_orders_spinner_dropdown);
        binding.statusSpinner.setAdapter(spinnerAdapter);
        int sel = 0;
        for (int i = 0; i < DD_VALUES.length; i++) {
            if (DD_VALUES[i].equals(viewModel.getTodayStatus())) {
                sel = i;
            }
        }
        binding.statusSpinner.setSelection(sel, false);
        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                viewModel.setTodayStatus(DD_VALUES[position]);
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
        boolean today = viewModel.getTab() == 0;
        binding.statusBox.setVisibility(today ? View.VISIBLE : View.GONE);
        binding.dateBox.setVisibility(today ? View.GONE : View.VISIBLE);
        binding.startDate.setText(viewModel.getStartDate());
        binding.endDate.setText(viewModel.getEndDate());
        binding.clearDates.setVisibility(viewModel.hasDateRange() ? View.VISIBLE : View.GONE);
    }

    /** showDatePicker(first 2017, last 2050); the day is not zero-padded ("yyyy-MM-d"), like the legacy app. */
    private void showDatePicker(boolean start) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            int monthNumber = month + 1;
            String formatted = year + "-" + (monthNumber <= 9 ? "0" + monthNumber : String.valueOf(monthNumber)) + "-" + day;
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

    private void render(Resource<List<Transaction>> res) {
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
        List<Transaction> items = res.isSuccess() ? res.data : null;
        adapter.submitList(items);
        binding.empty.getRoot().setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
