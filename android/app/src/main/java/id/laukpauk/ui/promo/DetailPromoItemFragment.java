package id.laukpauk.ui.promo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.promo.PromoItemDetail;
import id.laukpauk.databinding.FragmentDetailPromoItemBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.ui.product.ProductActions;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import id.laukpauk.util.ServerPath;

/** DetailPromoItem.dart. Argument: idPromoItem. */
public class DetailPromoItemFragment extends Fragment {

    /** Result key (Boolean) the product edit screen (TambahProdukFragment) sends back after an edit. */
    public static final String RESULT_TAMBAH_PRODUK = id.laukpauk.ui.seller.TambahProdukFragment.RESULT_KEY;

    private static final int WARNING_COLOR = 0xFFE0A800;

    private FragmentDetailPromoItemBinding binding;
    private DetailPromoItemViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailPromoItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        String idPromoItem = requireArguments().getString("idPromoItem", "");
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new DetailPromoItemViewModel(c.promoRepository, idPromoItem)))
                .get(DetailPromoItemViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.storeImage, "store.png");
        Images.asset(binding.yuhu.yuhuImage, "icons/timbangan.png");
        binding.refresh.setOnRefreshListener(viewModel::load);

        NavResults.<Boolean>observe(this, RESULT_TAMBAH_PRODUK, ok -> {
            Messages.snack(this, getString(ok ? R.string.promo_edit_berhasil : R.string.promo_edit_gagal));
            if (ok) {
                viewModel.load();
            }
        });

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::render);
        if (viewModel.getDetail().getValue() == null) {
            viewModel.load();
        }
    }

    private void render(Resource<PromoItemDetail> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        PromoItemDetail item = res.isSuccess() ? res.data : null;
        if (item == null) {
            binding.content.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        binding.notFound.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);

        PromoItemDetail.DetailPromo promo = item.detailPromo != null ? item.detailPromo : new PromoItemDetail.DetailPromo();
        PromoItemDetail.DetailProduk produk = item.detailProduk != null ? item.detailProduk : new PromoItemDetail.DetailProduk();
        PromoItemDetail.DetailSatuan satuan = item.detailSatuan != null ? item.detailSatuan : new PromoItemDetail.DetailSatuan();

        Images.url(binding.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/" + nn(produk.foto));

        binding.namaProduk.setText(nn(produk.nama).toUpperCase(Locale.ROOT));
        binding.kategori.setText(nn(produk.namaKategoriProduk).toUpperCase(Locale.ROOT));
        bindAksi(item, produk);

        String namaToko = nn(produk.namaToko);
        binding.namaToko.setText(namaToko.isEmpty() ? getString(R.string.promo_penjual_tidak_diketahui) : namaToko);
        binding.namaToko.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idPenjual", nn(produk.idPenjual));
            NavHostFragment.findNavController(this).navigate(R.id.penjualFragment, args);
        });

        boolean promoAktif = nn(promo.status).toLowerCase(Locale.ROOT).equals("aktif");
        binding.promoBox.setBackgroundResource(promoAktif ? R.drawable.bg_promo_aktif : R.drawable.bg_promo_nonaktif);
        binding.namaPromo.setText(nn(promo.nama));
        int hargaBersih = Json.parseInt(satuan.harga, 0)
                - (Json.parseInt(satuan.diskon, 0) + item.diskonPromo());
        binding.hargaPromo.setText(Currency.formatRupiah(hargaBersih, 0));
        binding.khusus.setText(getString(R.string.promo_khusus, nn(satuan.nama)));
        binding.deskripsiPromo.setText(nn(promo.deskripsi));
        binding.tidakAktif.setVisibility(promoAktif ? View.GONE : View.VISIBLE);

        setNote(binding.deskripsiProduk, nn(produk.keterangan));
        setNote(binding.catatanPromo, nn(item.catatan));
    }

    private void bindAksi(PromoItemDetail item, PromoItemDetail.DetailProduk produk) {
        boolean aktif = nn(item.statusPromo).toLowerCase(Locale.ROOT).equals("aktif");
        binding.aksi.setVisibility(aktif ? View.VISIBLE : View.GONE);
        if (!aktif) {
            return;
        }
        boolean mine = nn(produk.idPenjual).equals(viewModel.getCurrentUserId());
        binding.aksi.setText(mine ? R.string.promo_edit : R.string.promo_beli);
        binding.aksi.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                mine ? WARNING_COLOR : ContextCompat.getColor(requireContext(), R.color.green)));
        binding.aksi.setOnClickListener(v -> {
            if (mine) {
                Bundle args = new Bundle();
                args.putString("idProduk", nn(produk.id));
                args.putString("jenisProduk", "satuan");
                NavHostFragment.findNavController(this).navigate(R.id.tambahProdukFragment, args);
            } else {
                ProductActions.beli(this, nn(produk.id), nn(produk.nama), nn(item.idPromo));
            }
        });
    }

    /** Muted text, or an italic "Tidak ada deskripsi produk" placeholder. */
    private void setNote(android.widget.TextView view, String text) {
        boolean empty = text.isEmpty();
        view.setText(empty ? getString(R.string.promo_tidak_ada_deskripsi) : text);
        view.setTypeface(null, empty ? android.graphics.Typeface.ITALIC : android.graphics.Typeface.NORMAL);
    }

    private static String nn(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
