package id.laukpauk.ui.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.DetailProdukPenjual;
import id.laukpauk.data.model.seller.products.KategoriPilihan;
import id.laukpauk.databinding.FragmentDetailProdukPenjualBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.ui.common.StatusBar;
import id.laukpauk.util.ServerPath;

/**
 * DetailProdukPenjual.dart: a seller's product or paket - photo, name, "paling laris" switch, category,
 * units/prices and the description. Arguments: idProduk, jenisProduk ("satuan" | "paket").
 */
public class DetailProdukPenjualFragment extends Fragment implements SatuanProdukView.Host {

    private FragmentDetailProdukPenjualBinding binding;
    private DetailProdukPenjualViewModel viewModel;
    private SatuanProdukView pendingView;
    private boolean pendingAdd;
    private DetailProdukPenjual current;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailProdukPenjualBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StatusBar.green(this);
        binding.header.bind(this);
        final String id = requireArguments().getString("idProduk");
        final String jenis = requireArguments().getString("jenisProduk", "satuan");
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new DetailProdukPenjualViewModel(c.produkPenjualRepository, id, jenis)))
                .get(DetailProdukPenjualViewModel.class);

        boolean satuan = viewModel.isSatuan();
        binding.kategoriNama.setVisibility(satuan ? View.VISIBLE : View.GONE);
        binding.dividerTop.setVisibility(satuan ? View.VISIBLE : View.GONE);
        binding.satuan.setVisibility(satuan ? View.VISIBLE : View.GONE);
        binding.dividerBottom.setVisibility(satuan ? View.VISIBLE : View.GONE);
        binding.refresh.setOnRefreshListener(() -> viewModel.load());
        binding.laris.setOnClickListener(v -> {
            // The switch only follows the server: undo the local flip until the server agreed.
            binding.laris.setChecked(!binding.laris.isChecked());
            viewModel.toggleLaris(r -> {
                if (isAdded() && r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                    viewModel.load();
                }
            });
        });
        binding.ubah.setOnClickListener(v -> {
            if (!Boolean.TRUE.equals(viewModel.getEditingDeskripsi().getValue())) {
                binding.deskripsiEdit.setText(current == null ? "" : current.keterangan);
            }
            viewModel.toggleEditingDeskripsi();
        });
        binding.simpanDeskripsi.setOnClickListener(v -> viewModel.ubahDeskripsi(
                binding.deskripsiEdit.getText().toString(), r -> {
                    if (isAdded() && r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                        viewModel.stopEditingDeskripsi();
                        viewModel.load();
                    }
                }));
        viewModel.getEditingDeskripsi().observe(getViewLifecycleOwner(), editing -> {
            boolean e = Boolean.TRUE.equals(editing);
            binding.ubah.setText(e ? R.string.seller_produk_batal : R.string.seller_produk_ubah);
            binding.ubah.setTextColor(requireContext().getColor(e ? R.color.text_danger : R.color.text_primary));
            binding.deskripsi.setVisibility(e ? View.GONE : View.VISIBLE);
            binding.deskripsiEditBox.setVisibility(e ? View.VISIBLE : View.GONE);
        });
        NavResults.<Boolean>observe(this, EditSatuanProdukFragment.RESULT_KEY, ok -> {
            if (!Boolean.TRUE.equals(ok)) {
                SatuanProdukView.alertEditFailure(this, pendingAdd);
            } else if (pendingView != null && pendingView.isAttachedToWindow()) {
                pendingView.reload();
            }
        });
        AuthGate.require(this, this::observe);
    }

    @Override
    public void startEdit(SatuanProdukView view, boolean add, Bundle args) {
        pendingView = view;
        pendingAdd = add;
        NavHostFragment.findNavController(this).navigate(R.id.editSatuanProdukFragment, args);
    }

    private void observe() {
        viewModel.getDetail().observe(getViewLifecycleOwner(), res -> {
            if (res.isLoading()) {
                binding.progress.setVisibility(current == null && res.data == null ? View.VISIBLE : View.GONE);
                return;
            }
            binding.progress.setVisibility(View.GONE);
            binding.refresh.setRefreshing(false);
            render(res);
        });
        if (viewModel.isSatuan()) {
            viewModel.getKategori().observe(getViewLifecycleOwner(), k -> {
                if (!k.isLoading() && current != null) {
                    renderKategori(current, k.isSuccess() ? k.data : null);
                }
            });
        }
    }

    private void render(Resource<DetailProdukPenjual> res) {
        DetailProdukPenjual d = res.isSuccess() ? res.data : null;
        if (d == null) {
            binding.refresh.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        current = d;
        binding.notFound.setVisibility(View.GONE);
        binding.refresh.setVisibility(View.VISIBLE);
        binding.content.setVisibility(View.VISIBLE);
        boolean satuan = viewModel.isSatuan();
        Images.url(binding.foto, Images.server(satuan ? ServerPath.uploadGambarProduk
                : ServerPath.uploadGambarPaket, d.foto == null ? "" : d.foto));
        binding.nama.setText(d.nama == null ? "" : d.nama.toUpperCase(Locale.ROOT));
        binding.laris.setChecked(d.superior);
        binding.deskripsi.setText(d.keterangan);
        if (satuan) {
            boolean unknown = d.namaKategoriProduk == null;
            binding.kategoriNama.setText((unknown ? getString(R.string.seller_produk_kategori_tidak_diketahui)
                    : d.namaKategoriProduk).toUpperCase(Locale.ROOT));
            binding.kategoriNama.setTypeface(binding.kategoriNama.getTypeface(),
                    unknown ? android.graphics.Typeface.ITALIC : android.graphics.Typeface.NORMAL);
            binding.satuan.bind(this, d.id, true, false, true, this);
            Resource<List<KategoriPilihan>> k = viewModel.getKategori().getValue();
            if (k != null && !k.isLoading()) {
                renderKategori(d, k.isSuccess() ? k.data : null);
            }
        }
    }

    private void renderKategori(DetailProdukPenjual d, @Nullable List<KategoriPilihan> list) {
        if (binding == null) {
            return;
        }
        if (list == null || list.isEmpty()) {
            binding.kategoriBox.setVisibility(View.GONE);
            binding.kategoriKosong.setVisibility(View.VISIBLE);
            return;
        }
        binding.kategoriKosong.setVisibility(View.GONE);
        binding.kategoriBox.setVisibility(View.VISIBLE);
        List<String> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        values.add("0");
        labels.add(getString(R.string.seller_produk_pilih_kategori));
        for (KategoriPilihan k : list) {
            values.add(k.id);
            labels.add(k.nama);
        }
        String selected = d.idKategoriProduk != null ? d.idKategoriProduk : "0";
        ProdukPenjualSpinners.bind(requireContext(), binding.kategori, values, labels, selected, picked ->
                viewModel.ubahKategori(picked, r -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                        viewModel.load();
                    } else {
                        renderKategori(d, list);
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        pendingView = null;
        current = null;
    }
}
