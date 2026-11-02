package id.laukpauk.ui.seller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.orders.PesananMasuk;
import id.laukpauk.databinding.FragmentPesananMasukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;

/** PesananMasuk.dart ("Penjualan"): the seller's orders per status. Argument: statusTransaksi (int, 0..3). */
public class PesananMasukFragment extends Fragment {

    private FragmentPesananMasukBinding binding;
    private PesananMasukViewModel viewModel;
    private PesananMasukAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPesananMasukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.seller_pesanan_penjualan));
        final int initial = requireArguments().getInt("statusTransaksi", 0);
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new PesananMasukViewModel(c.pesananMasukRepository, initial)))
                .get(PesananMasukViewModel.class);

        adapter = new PesananMasukAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("idTransaksi", item.id);
            NavHostFragment.findNavController(this).navigate(R.id.prosesPesananFragment, args);
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(() -> viewModel.load());

        int[] labels = {R.string.seller_pesanan_tab_baru, R.string.seller_pesanan_tab_otw,
                R.string.seller_pesanan_tab_completed, R.string.seller_pesanan_tab_dibatalkan};
        for (int label : labels) {
            binding.tabs.addTab(binding.tabs.newTab().setText(getString(label)));
        }
        TabLayout.Tab selected = binding.tabs.getTabAt(viewModel.getTab().getValue() == null ? 0
                : viewModel.getTab().getValue());
        if (selected != null) {
            selected.select();
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.selectTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.tanggalAwal.setText(viewModel.tanggalAwal);
        binding.tanggalAkhir.setText(viewModel.tanggalAkhir);
        binding.tanggalAwal.setOnClickListener(v -> pickDate(binding.tanggalAwal, true));
        binding.tanggalAkhir.setOnClickListener(v -> pickDate(binding.tanggalAkhir, false));
        binding.hapusRentang.setOnClickListener(v -> {
            viewModel.tanggalAwal = "";
            viewModel.tanggalAkhir = "";
            binding.tanggalAwal.setText("");
            binding.tanggalAkhir.setText("");
            updateRentang();
        });

        AuthGate.require(this, this::observe);
    }

    private void observe() {
        viewModel.getTab().observe(getViewLifecycleOwner(), tab -> {
            binding.rentang.setVisibility(tab >= 2 ? View.VISIBLE : View.GONE);
            int[] readable = {R.string.seller_pesanan_status_baru, R.string.seller_pesanan_status_otw,
                    R.string.seller_pesanan_status_received, R.string.seller_pesanan_status_rejected};
            binding.empty.emptyText.setText(getString(R.string.seller_pesanan_kosong,
                    getString(readable[Math.max(0, Math.min(3, tab))])));
            updateRentang();
        });
        viewModel.getList().observe(getViewLifecycleOwner(), this::render);
        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    private void render(Resource<List<PesananMasuk>> res) {
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            adapter.submitList(null);
            binding.empty.getRoot().setVisibility(View.GONE);
            return;
        }
        binding.refresh.setRefreshing(false);
        List<PesananMasuk> items = res.isSuccess() ? res.data : null;
        adapter.submitList(items);
        binding.empty.getRoot().setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateRentang() {
        boolean set = !viewModel.tanggalAwal.isEmpty() && !viewModel.tanggalAkhir.isEmpty();
        binding.hapusRentang.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /** The legacy format: yyyy-MM-d (month padded, day not). */
    private static String format(int year, int month1, int day) {
        return year + "-" + (month1 <= 9 ? "0" + month1 : String.valueOf(month1)) + "-" + day;
    }

    private void pickDate(final EditText field, final boolean awal) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            String formatted = format(year, month + 1, day);
            if (awal) {
                viewModel.tanggalAwal = formatted;
            } else {
                viewModel.tanggalAkhir = formatted;
            }
            field.setText(formatted);
            updateRentang();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        Calendar first = Calendar.getInstance();
        first.clear();
        first.set(2017, Calendar.JANUARY, 1);
        Calendar last = Calendar.getInstance();
        last.clear();
        last.set(2050, Calendar.DECEMBER, 31);
        dialog.getDatePicker().setMinDate(first.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(last.getTimeInMillis());
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
