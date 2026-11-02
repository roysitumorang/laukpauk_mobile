package id.laukpauk.ui.seller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.products.SatuanProdukData;
import id.laukpauk.databinding.FragmentEditSatuanProdukBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.NavResults;

/**
 * EditSatuanProduk.dart: add or edit one unit/price of a product.
 * Arguments: idProduk, dataSatuan (JSON of SatuanProdukData, optional), appBarTitle, isUpdate.
 * Result: {@link #RESULT_KEY} = Boolean (saved or not).
 */
public class EditSatuanProdukFragment extends Fragment {

    public static final String RESULT_KEY = "edit_satuan_produk_result";

    private FragmentEditSatuanProdukBinding binding;
    private EditSatuanProdukViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditSatuanProdukBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        final String idProduk = args.getString("idProduk");
        String json = args.getString("dataSatuan");
        String title = args.getString("appBarTitle");
        final boolean isUpdate = args.getBoolean("isUpdate", false);
        AppContainer c = AppContainer.from(requireContext());
        SatuanProdukData data = json == null ? null : c.apiClient.gson().fromJson(json, SatuanProdukData.class);
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new EditSatuanProdukViewModel(c.produkPenjualRepository, idProduk, data)))
                .get(EditSatuanProdukViewModel.class);

        AppBars.setup(this, binding.appBar, title != null ? title
                : getString(R.string.seller_produk_edit_satuan_title));
        binding.title.setText(isUpdate ? R.string.seller_produk_edit_satuan_title
                : R.string.seller_produk_satuan_baru_title);
        binding.desc.setText(isUpdate ? R.string.seller_produk_edit_satuan_desc
                : R.string.seller_produk_tambah_satuan_desc);

        binding.nama.setText(viewModel.nama);
        binding.harga.setText(viewModel.harga);
        binding.diskon.setText(viewModel.diskon);
        binding.keterangan.setText(viewModel.keterangan);

        List<String> values = Arrays.asList("", "pas", "tidak pas");
        List<String> labels = Arrays.asList(getString(R.string.seller_produk_jenis_harga),
                getString(R.string.seller_produk_harga_pas), getString(R.string.seller_produk_harga_tidak_pas));
        ProdukPenjualSpinners.bind(requireContext(), binding.jenisHarga, values, labels, viewModel.jenisHarga,
                selected -> {
                    viewModel.jenisHarga = selected;
                    refresh();
                });

        binding.nama.addTextChangedListener(watcher(s -> viewModel.nama = s));
        binding.harga.addTextChangedListener(watcher(s -> viewModel.harga = s));
        binding.diskon.addTextChangedListener(watcher(s -> viewModel.diskon = s));
        binding.keterangan.addTextChangedListener(watcher(s -> viewModel.keterangan = s));
        binding.simpan.setOnClickListener(v -> {
            binding.simpan.setEnabled(false);
            viewModel.simpan(r -> {
                boolean ok = r.isSuccess() && Boolean.TRUE.equals(r.data);
                if (!isAdded()) {
                    return;
                }
                NavResults.setResult(this, RESULT_KEY, ok);
                NavHostFragment.findNavController(this).popBackStack();
            });
        });
        refresh();
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
                sink.put(s.toString());
                refresh();
            }
        };
    }

    private void refresh() {
        if (binding == null) {
            return;
        }
        String jenis = viewModel.jenisHarga.toLowerCase(Locale.ROOT);
        binding.note.setTextColor(requireContext().getColor(jenis.isEmpty() ? R.color.text_muted
                : R.color.text_info));
        int note = R.string.seller_produk_note_jenis_harga;
        if (jenis.equals("pas")) {
            note = R.string.seller_produk_note_pas;
        } else if (jenis.equals("tidak pas")) {
            note = R.string.seller_produk_note_tidak_pas;
        }
        binding.note.setText(note);
        binding.simpan.setVisibility(viewModel.isComplete() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
