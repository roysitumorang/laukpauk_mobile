package id.laukpauk.ui.product;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.databinding.FragmentDetailPaketBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Currency;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/** DetailPaket.dart. Arg: idPaket. */
public class DetailPaketFragment extends Fragment {

    private FragmentDetailPaketBinding binding;
    private DetailPaketViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailPaketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        final String idPaket = requireArguments().getString("idPaket", "");
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new DetailPaketViewModel(c.paketRepository, idPaket))).get(DetailPaketViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.yuhu.yuhuImage, "icons/timbangan.png");
        Images.asset(binding.storeIcon, "store.png");
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.lainnyaEmptyImage, "empty.png");

        LainnyaAdapter lainnyaAdapter = new LainnyaAdapter(ServerPath.uploadGambarPaket, true, 12f, 10,
                item -> {
                    Bundle args = new Bundle();
                    args.putString("idPaket", item.id);
                    NavHostFragment.findNavController(this).navigate(R.id.detailPaketFragment, args,
                            new NavOptions.Builder().setPopUpTo(R.id.detailPaketFragment, true).build());
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
        NavResults.<Boolean>observe(this, DetailProdukFragment.EDIT_RESULT_KEY, ok -> {
            Messages.snack(this, getString(ok ? R.string.product_edit_paket_berhasil : R.string.product_edit_paket_gagal));
            if (ok) {
                viewModel.load();
            }
        });
    }

    private void render(Resource<Paket> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.content.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        Paket p = res.isSuccess() ? res.data : null;
        binding.notFound.setVisibility(p == null ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(p == null ? View.GONE : View.VISIBLE);
        if (p == null) {
            return;
        }
        boolean isDefault = DefaultImage.defaultPaket.equalsIgnoreCase(p.foto);
        binding.image.setScaleType(isDefault ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Images.url(binding.image, Images.server(ServerPath.uploadGambarPaket, p.foto == null ? "" : p.foto));
        binding.nama.setText(p.nama == null ? "" : p.nama.toUpperCase());

        double persen = p.diskon >= 1 && p.harga >= 1 ? (double) p.diskon / p.harga * 100 : 0;
        binding.diskonRow.setVisibility(persen >= 0.1 ? View.VISIBLE : View.GONE);
        binding.hargaCoret.setText(Currency.formatRupiah(p.harga, 0));
        binding.hargaCoret.setPaintFlags(binding.hargaCoret.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        binding.diskon.setText(getString(R.string.product_diskon, String.format(Locale.US, "%.1f", persen)));
        binding.harga.setText(Currency.formatRupiah(p.harga - p.diskon, 0));

        binding.toko.setText(p.namaToko != null && !p.namaToko.isEmpty() ? p.namaToko
                : getString(R.string.product_penjual_tidak_diketahui));
        binding.toko.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idPenjual", p.idPenjual);
            NavHostFragment.findNavController(this).navigate(R.id.penjualFragment, args);
        });
        boolean ada = p.keterangan != null && !p.keterangan.isEmpty();
        binding.deskripsi.setText(ada ? p.keterangan : getString(R.string.product_deskripsi_paket_kosong));
        binding.deskripsi.setTypeface(null, ada ? Typeface.NORMAL : Typeface.ITALIC);

        boolean mine = p.idPenjual != null && p.idPenjual.equals(viewModel.currentUserId());
        binding.beli.setText(mine ? R.string.product_edit : R.string.product_beli);
        binding.beli.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), mine ? R.color.product_warning : R.color.green)));
        binding.beli.setOnClickListener(v -> {
            if (mine) {
                Bundle args = new Bundle();
                args.putString("idProduk", p.id);
                args.putString("jenisProduk", "paket");
                NavHostFragment.findNavController(this).navigate(R.id.tambahProdukFragment, args);
            } else {
                ProductActions.beliPaket(this, p.id, p.nama);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
