package id.laukpauk.ui.home;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.home.Artikel;
import id.laukpauk.data.model.home.WalletInfo;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.databinding.FragmentHomePageBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.delivery.DeliveryFlow;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** HomePage.dart: header, hero slider, wallet, the first products, konten blocks and articles. */
public class HomePageFragment extends Fragment {

    private FragmentHomePageBinding binding;
    private HomeViewModel viewModel;
    private ProdukSectionViewModel produkViewModel;
    private AppContainer c;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new HomeViewModel(c.homeRepository))).get(HomeViewModel.class);
        produkViewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ProdukSectionViewModel(c.homeRepository, c.deliveryRepository)))
                .get("produk", ProdukSectionViewModel.class);

        binding.header.bind(this);
        DeliveryFlow.homeChecks(this);

        binding.refresh.setColorSchemeColors(requireContext().getColor(R.color.green));
        binding.refresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            produkViewModel.reload();
            binding.refresh.setRefreshing(false);
        });

        setupHero();
        setupWallet();
        new ProdukSection(this, binding.produk, produkViewModel, true);
        setupKonten();
        setupArtikel();
        viewModel.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.checkLogin();
        }
    }

    private void setupHero() {
        HeroAdapter hero = new HeroAdapter();
        binding.heroPager.setAdapter(hero);
        AutoPager.attach(binding.heroPager);
        viewModel.getBanners().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.heroProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                hero.submit(res.isSuccess() && res.data != null ? res.data : new ArrayList<>());
            }
        });
    }

    private void setupWallet() {
        Images.asset(binding.wallet.dompetIcon, "icons/pembeli/dompet.png");
        Images.asset(binding.wallet.voucherIcon, "icons/pembeli/voucher.png");
        Images.asset(binding.wallet.poinIcon, "poin.png");
        Images.asset(binding.wallet.referalIcon, "referal.png");
        binding.wallet.dompet.setOnClickListener(v -> go(R.id.historiDompetFragment));
        binding.wallet.voucher.setOnClickListener(v -> go(R.id.voucherFragment));
        binding.wallet.poin.setOnClickListener(v -> go(R.id.historiPoinFragment));
        binding.wallet.referal.setOnClickListener(v -> go(R.id.kodeReferalFragment));
        viewModel.getLoggedIn().observe(getViewLifecycleOwner(), login -> {
            int vis = Boolean.TRUE.equals(login) ? View.VISIBLE : View.GONE;
            binding.wallet.dompetValue.setVisibility(vis);
            binding.wallet.voucherValue.setVisibility(vis);
            binding.wallet.poinValue.setVisibility(vis);
            if (vis == View.VISIBLE) {
                binding.wallet.dompetValue.setText(R.string.home_wallet_mengambil);
                binding.wallet.voucherValue.setText(R.string.home_wallet_mengambil);
                binding.wallet.poinValue.setText(R.string.home_wallet_mengambil);
            }
        });
        viewModel.getWallet().observe(getViewLifecycleOwner(), res -> {
            if (res == null || !res.isSuccess() || res.data == null) {
                return;
            }
            WalletInfo w = res.data;
            binding.wallet.dompetValue.setText(Currency.formatRupiah(w.totalDompet, 0));
            binding.wallet.voucherValue.setText(w.jumlahVoucher >= 1
                    ? getString(R.string.home_wallet_voucher_jumlah, Currency.formatSatuan(w.jumlahVoucher))
                    : getString(R.string.home_wallet_belum_voucher));
            binding.wallet.poinValue.setText(Currency.formatRupiah(w.poin, 0));
        });
    }

    private void go(int destination) {
        NavHostFragment.findNavController(this).navigate(destination);
    }

    private void setupKonten() {
        KontenAdapter adapter = new KontenAdapter(c.homeRepository::getLowestPrice, ProdukListener.navigating(this),
                new KontenAdapter.Listener() {
                    @Override
                    public void onLihatSemua(id.laukpauk.data.model.home.Konten konten) {
                        HomeNav.openKontenSemua(HomePageFragment.this, konten);
                    }

                    @Override
                    public void onBanner(id.laukpauk.data.model.home.Konten konten,
                                         id.laukpauk.data.model.home.KontenItem item) {
                        HomeNav.openBanner(HomePageFragment.this, konten, item);
                    }
                });
        binding.kontenList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.kontenList.setAdapter(adapter);
        viewModel.getKonten().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.kontenProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                adapter.submitList(res.data);
            }
        });
    }

    private void setupArtikel() {
        SlideAdapter slides = new SlideAdapter(slide -> openUrl((String) slide.tag));
        binding.artikelPager.setAdapter(slides);
        AutoPager.attach(binding.artikelPager);
        binding.artikelEmpty.emptyText.setText(R.string.home_artikel_kosong);
        binding.artikelLihat.setOnClickListener(v -> c.settingRepository.getSettingValue("seeAllArticleURL", result -> {
            if (!isAdded()) {
                return;
            }
            String url = SettingRepository.asString(result.data);
            if (url != null) {
                launch(url);
            }
        }));
        viewModel.getArtikel().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.artikelProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isLoading()) {
                return;
            }
            List<SlideAdapter.Slide> list = new ArrayList<>();
            if (res.isSuccess() && res.data != null) {
                for (Artikel a : res.data) {
                    String judul = a.judul == null ? "" : a.judul;
                    if (judul.length() >= 31) {
                        judul = getString(R.string.home_artikel_judul_panjang, judul.substring(0, 30));
                    }
                    list.add(new SlideAdapter.Slide(Images.server(ServerPath.uploadGambarArtikel, a.foto), judul, a.link));
                }
            }
            slides.submit(list);
            binding.artikelPager.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            binding.artikelEmpty.getRoot().setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    /** Opens an article link; the "URL tidak bisa diakses" dialog when nothing can show it. */
    private void openUrl(@Nullable String url) {
        if (!launch(url)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.home_app_name)
                    .setMessage(R.string.home_url_tidak_bisa)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private boolean launch(@Nullable String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } catch (ActivityNotFoundException | SecurityException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
