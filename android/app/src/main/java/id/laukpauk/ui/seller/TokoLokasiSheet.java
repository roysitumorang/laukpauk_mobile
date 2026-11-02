package id.laukpauk.ui.seller;

import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.Pasar;
import id.laukpauk.databinding.SheetSellerTokoLokasiBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.SearchBox;

/** The bottom sheet of UpdateLokasiBerjualan.dart: pick a market or an own location, then confirm. */
class TokoLokasiSheet {

    interface Host {
        /** "Lokasi Tersendiri": the sheet closed and the pin map should open. */
        void pickOnMap();

        /** "Tetapkan Lokasi Baru Berjualan" tapped. */
        void confirm(Runnable onFinished);
    }

    private final Fragment fragment;
    private final TokoSayaViewModel viewModel;
    private final Host host;

    private BottomSheetDialog dialog;
    private SheetSellerTokoLokasiBinding b;
    private TokoPasarAdapter adapter;
    private List<Pasar> all = new ArrayList<>();
    private String query = "";
    private Observer<Resource<List<Pasar>>> observer;

    TokoLokasiSheet(Fragment fragment, TokoSayaViewModel viewModel, Host host) {
        this.fragment = fragment;
        this.viewModel = viewModel;
        this.host = host;
    }

    void show() {
        b = SheetSellerTokoLokasiBinding.inflate(LayoutInflater.from(fragment.requireContext()));
        dialog = new BottomSheetDialog(fragment.requireContext());
        dialog.setContentView(b.getRoot());
        Images.asset(b.pasarKosongImage, "empty.png");

        adapter = new TokoPasarAdapter(pasar -> {
            viewModel.lokasi.idPasar = pasar.id;
            viewModel.lokasi.jenis = TokoLokasiState.PASAR_TERSEDIA;
            render();
        });
        b.listPasar.setLayoutManager(new LinearLayoutManager(fragment.requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        b.listPasar.setAdapter(adapter);

        SearchBox.bind(b.search, fragment.getString(R.string.seller_toko_type_here), query, q -> {
            query = q.toLowerCase();
            filter();
        }, () -> {
            query = "";
            filter();
        });

        b.tabPasar.setOnClickListener(v -> {
            viewModel.lokasi.reset();
            render();
        });
        b.tabTersendiri.setOnClickListener(v -> {
            dialog.dismiss();
            host.pickOnMap();
        });
        b.tetapkan.setOnClickListener(v -> {
            b.tetapkan.setEnabled(false);
            host.confirm(() -> {
                if (b != null) {
                    b.tetapkan.setEnabled(true);
                }
            });
        });

        observer = res -> {
            if (res == null || b == null) {
                return;
            }
            b.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                all = res.isSuccess() && res.data != null ? res.data : new ArrayList<>();
                filter();
            }
        };
        viewModel.getPasar().observe(fragment.getViewLifecycleOwner(), observer);
        dialog.setOnDismissListener(d -> {
            viewModel.getPasar().removeObserver(observer);
            b = null;
        });
        viewModel.loadPasarIfNeeded();
        render();
        dialog.show();
    }

    void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void filter() {
        List<Pasar> out = new ArrayList<>();
        for (Pasar p : all) {
            if (p.nama != null && p.nama.toLowerCase().contains(query)) {
                out.add(p);
            }
        }
        adapter.submitList(out);
        b.pasarKosong.setVisibility(out.isEmpty() && b.progress.getVisibility() != View.VISIBLE
                ? View.VISIBLE : View.GONE);
    }

    private void render() {
        TokoLokasiState s = viewModel.lokasi;
        boolean pasar = TokoLokasiState.PASAR_TERSEDIA.equals(s.jenis);
        b.pasarBox.setVisibility(pasar ? View.VISIBLE : View.GONE);
        b.lokasiBaru.setVisibility(pasar ? View.GONE : View.VISIBLE);
        if (!pasar) {
            b.lokasiBaru.setText(fragment.getString(R.string.seller_toko_lokasi_baru,
                    s.alamat == null ? "" : s.alamat));
        }
        adapter.setSelectedId(s.idPasar);
        b.tetapkan.setVisibility(s.hasSelection() ? View.VISIBLE : View.GONE);
    }
}
