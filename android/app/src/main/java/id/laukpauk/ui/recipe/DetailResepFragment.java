package id.laukpauk.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.Resep;
import id.laukpauk.databinding.FragmentDetailResepBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** DetailResep.dart. Argument: idResep. */
public class DetailResepFragment extends Fragment {

    private FragmentDetailResepBinding binding;
    private DetailResepViewModel viewModel;
    private AppContainer container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailResepBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        container = AppContainer.from(requireContext());
        String idResep = requireArguments().getString("idResep", "");
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new DetailResepViewModel(container.resepRepository, idResep)))
                .get(DetailResepViewModel.class);

        binding.header.bind(this);
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.bahanKosongImage, "empty.png");
        binding.refresh.setOnRefreshListener(viewModel::load);

        BahanAdapter adapter = new BahanAdapter(new BahanAdapter.Listener() {
            @Override
            public void onPickSatuan(BahanRow row, int satuanIndex) {
                viewModel.pickSatuan(row, satuanIndex);
            }

            @Override
            public void onPickJumlah(BahanRow row, int quantity) {
                viewModel.pickJumlah(row, quantity);
            }

            @Override
            public void onCart(BahanRow row) {
                if (viewModel.isDeliverySet()) {
                    viewModel.toggle(row);
                } else {
                    DeliveryFlow.ensure(DetailResepFragment.this, null,
                            () -> Messages.snack(DetailResepFragment.this, getString(R.string.recipe_adjd_siap)));
                }
            }
        });
        binding.bahan.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.bahan.setAdapter(adapter);

        viewModel.getDetail().observe(getViewLifecycleOwner(), this::renderDetail);
        viewModel.getItems().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.bahanProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            boolean empty = res.isSuccess() && (res.data == null || res.data.isEmpty());
            binding.bahanKosong.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getRows().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            boolean any = total != null && total >= 0;
            binding.summary.setVisibility(any ? View.VISIBLE : View.GONE);
            if (any) {
                binding.total.setText(Currency.formatRupiah(total, 0));
            }
        });
        viewModel.getBusy().observe(getViewLifecycleOwner(), busy -> binding.belanja.setEnabled(!busy));
        binding.belanja.setOnClickListener(v -> viewModel.belanja(result -> {
            if (!isAdded()) {
                return;
            }
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                container.cartRepository.refreshCartCount();
                Messages.cartSnack(this, getString(R.string.recipe_berhasil_keranjang));
            }
        }));

        if (!viewModel.isLoaded()) {
            viewModel.load();
        }
    }

    private void renderDetail(Resource<Resep> res) {
        if (res == null) {
            return;
        }
        boolean loading = res.isLoading();
        binding.progress.setVisibility(loading && !binding.refresh.isRefreshing() ? View.VISIBLE : View.GONE);
        if (loading) {
            return;
        }
        binding.refresh.setRefreshing(false);
        Resep r = res.isSuccess() ? res.data : null;
        if (r == null) {
            binding.content.setVisibility(View.GONE);
            binding.notFound.setVisibility(View.VISIBLE);
            return;
        }
        binding.notFound.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);

        Images.url(binding.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarResep + "/" + r.foto);
        binding.nama.setText(r.nama);
        binding.keterangan.setText(r.keterangan);
        binding.fb.setOnClickListener(v -> LinkOpener.open(this, r.fbLink, R.string.recipe_url_tidak_valid_detail));
        binding.ig.setOnClickListener(v -> LinkOpener.open(this, r.igLink, R.string.recipe_url_tidak_valid_detail));
        binding.caraMasak.setOnClickListener(v ->
                LinkOpener.open(this, r.igLink, R.string.recipe_url_tidak_valid_detail));
        binding.share.setOnClickListener(v -> bagikan(r.nama == null ? "" : r.nama));
    }

    private void bagikan(String namaResep) {
        String nama = namaResep.toUpperCase(Locale.ROOT);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.recipe_share_text, nama));
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.recipe_share_subject, nama));
        startActivity(Intent.createChooser(send, null));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
