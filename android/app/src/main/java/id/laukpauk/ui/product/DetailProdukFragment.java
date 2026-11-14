package id.laukpauk.ui.product;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.ImageView;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.databinding.FragmentDetailProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** DetailProduk.dart. Arg: idProduk. */
public class DetailProdukFragment extends Fragment {

    private FragmentDetailProdukBinding binding;
    private DetailProdukViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailProdukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String idProduk = requireArguments().getString("idProduk", "");
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new DetailProdukViewModel(c.produkRepository, idProduk))).get(DetailProdukViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.yuhu.yuhuImage, "icons/timbangan.png");
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.lainnyaEmptyImage, "empty.png");
        binding.refresh.setColorSchemeResources(R.color.green);
        binding.refresh.setOnRefreshListener(viewModel::load);

        LainnyaAdapter lainnyaAdapter = new LainnyaAdapter(ServerPath.uploadGambarProduk, false, 10f, 7,
                item -> {
                    Bundle args = new Bundle();
                    args.putString("idProduk", item.id);
                    NavHostFragment.findNavController(this).navigate(R.id.detailProdukFragment, args,
                            new NavOptions.Builder().setPopUpTo(R.id.detailProdukFragment, true).build());
                });
        binding.lainnya.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.lainnya.setAdapter(lainnyaAdapter);

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLainnya().observe(getViewLifecycleOwner(), res -> {
            binding.lainnyaProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                boolean empty = !res.isSuccess() || res.data == null || res.data.isEmpty();
                lainnyaAdapter.submitList(empty ? null : res.data);
                binding.lainnyaEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void render(Resource<Produk> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.refresh.isRefreshing() || binding.content.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        binding.refresh.setRefreshing(false);
        Produk p = res.isSuccess() ? res.data : null;
        binding.notFound.setVisibility(p == null ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(p == null ? View.GONE : View.VISIBLE);
        if (p == null) {
            return;
        }
        boolean isDefault = DefaultImage.defaultProduk.equalsIgnoreCase(p.foto);
        binding.image.setScaleType(isDefault ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(binding.image, Images.server(ServerPath.uploadGambarProduk, p.foto == null ? "" : p.foto));
        binding.nama.setText(p.nama == null ? "" : p.nama.toUpperCase());
        boolean hasKategori = p.namaKategoriProduk != null;
        binding.kategori.setText((hasKategori ? p.namaKategoriProduk : getString(R.string.product_kategori_tidak_diketahui)).toUpperCase());
        binding.kategori.setTypeface(null, hasKategori ? Typeface.NORMAL : Typeface.ITALIC);
        boolean ada = p.keterangan != null && !p.keterangan.isEmpty();
        binding.deskripsi.setText(ada ? p.keterangan : getString(R.string.product_deskripsi_kosong));
        binding.deskripsi.setTypeface(null, ada ? Typeface.NORMAL : Typeface.ITALIC);

        binding.beli.setText(R.string.product_beli);
        binding.beli.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.green)));
        binding.beli.setOnClickListener(v -> ProductActions.beli(this, p.id, p.nama));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
