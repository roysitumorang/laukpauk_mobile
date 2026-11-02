package id.laukpauk.ui.seller;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.products.DetailProdukPenjual;
import id.laukpauk.data.model.seller.products.KategoriPilihan;
import id.laukpauk.databinding.FragmentTambahProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.ImagePicker;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.DefaultImage;
import id.laukpauk.util.ServerPath;

/**
 * TambahProduk.dart: add or update a product / paket. Arguments: idProduk (null = new), jenisProduk.
 * Result: {@link #RESULT_KEY} = Boolean (saved or not); nothing when left without saving.
 */
public class TambahProdukFragment extends Fragment {

    public static final String RESULT_KEY = "tambah_produk_result";

    private FragmentTambahProdukBinding binding;
    private TambahProdukViewModel viewModel;
    private ImagePicker imagePicker;
    private boolean binding_;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = new ImagePicker(this, file -> {
            if (viewModel != null) {
                viewModel.setImage(file);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTambahProdukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final String idProduk = requireArguments().getString("idProduk");
        final String jenisArg = requireArguments().getString("jenisProduk", "satuan");
        AppContainer c = AppContainer.from(requireContext());
        AuthGate.require(this, () -> start(c, idProduk, jenisArg));
    }

    private void start(AppContainer c, String idProduk, String jenisArg) {
        if (binding == null) {
            return;
        }
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new TambahProdukViewModel(c.produkPenjualRepository, idProduk, jenisArg)))
                .get(TambahProdukViewModel.class);
        boolean isNew = viewModel.isNew();
        AppBars.setup(this, binding.appBar, getString(isNew ? R.string.seller_produk_tambah_title
                : R.string.seller_produk_update_title));
        binding.title.setText(isNew ? R.string.seller_produk_tambah_title : R.string.seller_produk_update_title);
        binding.desc.setText(isNew ? R.string.seller_produk_tambah_desc : R.string.seller_produk_update_desc);
        Images.asset(binding.fotoIcon, "upload-icon.png");
        binding.fotoHolder.setOnClickListener(v -> imagePicker.show());

        binding_ = true;
        fillFields();
        binding_ = false;
        binding.nama.addTextChangedListener(watcher(s -> viewModel.nama = s));
        binding.keterangan.addTextChangedListener(watcher(s -> viewModel.keterangan = s));
        binding.harga.addTextChangedListener(watcher(s -> viewModel.harga = s));
        binding.diskon.addTextChangedListener(watcher(s -> viewModel.diskon = s));
        binding.simpan.setOnClickListener(v -> viewModel.simpan(r -> {
            if (!isAdded()) {
                return;
            }
            NavResults.setResult(this, RESULT_KEY, r.isSuccess() && Boolean.TRUE.equals(r.data));
            NavHostFragment.findNavController(this).popBackStack();
        }));

        viewModel.getKategori().observe(getViewLifecycleOwner(), r -> renderSpinners());
        viewModel.getDraftVersion().observe(getViewLifecycleOwner(), v -> {
            binding_ = true;
            fillFields();
            binding_ = false;
            renderSpinners();
            renderFoto();
            refresh();
        });
        viewModel.getImage().observe(getViewLifecycleOwner(), f -> renderFoto());
        viewModel.getBusy().observe(getViewLifecycleOwner(), busy -> binding.simpan.setEnabled(!busy));
        renderSpinners();
        renderFoto();
        refresh();
    }

    private void fillFields() {
        binding.nama.setText(viewModel.nama);
        binding.keterangan.setText(viewModel.keterangan);
        binding.harga.setText(viewModel.harga);
        binding.diskon.setText(viewModel.diskon);
    }

    private interface Sink {
        void put(String s);
    }

    private TextWatcher watcher(Sink sink) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!binding_) {
                    sink.put(s.toString());
                    refresh();
                }
            }
        };
    }

    private void renderSpinners() {
        if (binding == null || viewModel == null) {
            return;
        }
        List<String> jv = Arrays.asList("", "satuan", "paket");
        List<String> jl = Arrays.asList(getString(R.string.seller_produk_pilih_jenis),
                getString(R.string.seller_produk_tab_satuan), getString(R.string.seller_produk_tab_paket));
        ProdukPenjualSpinners.bind(requireContext(), binding.jenis, jv, jl, viewModel.jenis, v -> {
            viewModel.jenis = v;
            renderSpinners();
            refresh();
        });
        List<String> kv = new ArrayList<>();
        List<String> kl = new ArrayList<>();
        kv.add("");
        kl.add(getString(R.string.seller_produk_pilih_kategori));
        List<KategoriPilihan> list = viewModel.getKategori().getValue() == null ? null
                : viewModel.getKategori().getValue().data;
        if (list != null) {
            for (KategoriPilihan k : list) {
                kv.add(k.id);
                kl.add(k.nama);
            }
        }
        ProdukPenjualSpinners.bind(requireContext(), binding.kategori, kv, kl, viewModel.kategoriSelected, v -> {
            viewModel.kategoriSelected = v;
            refresh();
        });
        boolean satuan = viewModel.jenis.equals("satuan");
        boolean paket = viewModel.jenis.equals("paket");
        binding.kategoriBox.setVisibility(satuan ? View.VISIBLE : View.GONE);
        binding.noteHarga.setVisibility(satuan ? View.VISIBLE : View.GONE);
        binding.paketBox.setVisibility(paket ? View.VISIBLE : View.GONE);
    }

    /** uploadFotoWidget(): picked image, else (edit) the server image, else the upload icon. */
    private void renderFoto() {
        if (binding == null || viewModel == null) {
            return;
        }
        File picked = viewModel.getImage().getValue();
        DetailProdukPenjual d = viewModel.getDetail().getValue();
        boolean custom = false;
        boolean fit = false;
        if (picked != null) {
            binding.foto.setImageURI(Uri.fromFile(picked));
            custom = true;
        } else if (!viewModel.isNew() && d != null) {
            boolean satuan = id.laukpauk.data.repository.ProdukPenjualRepository.isSatuan(viewModel.getJenisArg());
            String foto = d.foto == null ? "" : d.foto;
            String file = satuan ? foto.toLowerCase(Locale.ROOT) : foto;
            Images.url(binding.foto, Images.server(satuan ? ServerPath.uploadGambarProduk
                    : ServerPath.uploadGambarPaket, file));
            fit = (satuan ? DefaultImage.defaultProduk : DefaultImage.defaultPaket)
                    .equalsIgnoreCase(foto);
            custom = true;
        }
        binding.foto.setScaleType(fit ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        binding.foto.setVisibility(custom ? View.VISIBLE : View.GONE);
        binding.fotoIcon.setVisibility(custom ? View.GONE : View.VISIBLE);
    }

    private void refresh() {
        if (binding != null && viewModel != null) {
            binding.simpan.setVisibility(viewModel.isFormCompleted() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
