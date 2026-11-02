package id.laukpauk.ui.seller;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.ProdukPenjual;
import id.laukpauk.databinding.FragmentUpdateHargaBinding;
import id.laukpauk.databinding.ItemSellerProdukUpdateHargaBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.ServerPath;

/** UpdateHarga.dart: search your products and manage the prices (units) of each result. */
public class UpdateHargaFragment extends Fragment implements SatuanProdukView.Host {

    private FragmentUpdateHargaBinding binding;
    private UpdateHargaViewModel viewModel;
    private SatuanProdukView pendingView;
    private boolean pendingAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdateHargaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.seller_produk_update_harga_title));
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new UpdateHargaViewModel(c.produkPenjualRepository))).get(UpdateHargaViewModel.class);

        binding.cari.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                cari();
                return true;
            }
            return false;
        });
        binding.cariButton.setOnClickListener(v -> cari());
        NavResults.<Boolean>observe(this, EditSatuanProdukFragment.RESULT_KEY, ok -> {
            if (!Boolean.TRUE.equals(ok)) {
                SatuanProdukView.alertEditFailure(this, pendingAdd);
            } else if (pendingView != null && pendingView.isAttachedToWindow()) {
                pendingView.reload();
            }
        });
        AuthGate.require(this, this::observe);
    }

    private void cari() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.cari.getWindowToken(), 0);
        }
        viewModel.search(binding.cari.getText().toString());
    }

    @Override
    public void startEdit(SatuanProdukView view, boolean add, Bundle args) {
        pendingView = view;
        pendingAdd = add;
        NavHostFragment.findNavController(this).navigate(R.id.editSatuanProdukFragment, args);
    }

    private void observe() {
        viewModel.getResult().observe(getViewLifecycleOwner(), this::render);
        if (viewModel.getResult().getValue() == null) {
            viewModel.search("");
        }
    }

    private void render(Resource<java.util.List<ProdukPenjual>> res) {
        binding.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
        if (res.isLoading()) {
            binding.results.removeAllViews();
            binding.empty.setVisibility(View.GONE);
            return;
        }
        binding.results.removeAllViews();
        java.util.List<ProdukPenjual> list = res.isSuccess() ? res.data : null;
        if (list == null || list.isEmpty()) {
            binding.emptyText.setText(getString(R.string.seller_produk_cari_kosong, viewModel.getTerm()));
            binding.empty.setVisibility(View.VISIBLE);
            return;
        }
        binding.empty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ProdukPenjual p : list) {
            ItemSellerProdukUpdateHargaBinding item = ItemSellerProdukUpdateHargaBinding.inflate(inflater,
                    binding.results, false);
            Images.circle(item.foto, Images.server(ServerPath.uploadGambarProduk + "/compress",
                    p.foto == null ? "" : p.foto));
            item.nama.setText(p.nama == null ? "" : p.nama.toUpperCase(Locale.ROOT));
            item.kategori.setText(p.namaKategoriProduk);
            item.satuan.bind(this, p.id, false, true, false, this);
            binding.results.addView(item.getRoot());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        pendingView = null;
    }
}
