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
import id.laukpauk.data.model.home.Article;
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
    private ProductSectionViewModel productViewModel;
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
        productViewModel = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new ProductSectionViewModel(c.homeRepository, c.deliveryRepository)))
                .get("produk", ProductSectionViewModel.class);

        binding.header.bind(this);
        DeliveryFlow.homeChecks(this);

        binding.refresh.setColorSchemeColors(requireContext().getColor(R.color.green));
        binding.refresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            productViewModel.reload();
            binding.refresh.setRefreshing(false);
        });

        setupHero();
        setupWallet();
        new ProductSection(this, binding.product, productViewModel);
        setupContent();
        setupArticle();
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
        Images.asset(binding.wallet.walletIcon, "icons/pembeli/dompet.png");
        Images.asset(binding.wallet.voucherIcon, "icons/pembeli/voucher.png");
        Images.asset(binding.wallet.pointsIcon, "poin.png");
        Images.asset(binding.wallet.referralIcon, "referal.png");
        binding.wallet.wallet.setOnClickListener(v -> go(R.id.walletHistoryFragment));
        binding.wallet.voucher.setOnClickListener(v -> go(R.id.voucherFragment));
        binding.wallet.points.setOnClickListener(v -> go(R.id.pointsHistoryFragment));
        binding.wallet.referral.setOnClickListener(v -> go(R.id.referralCodeFragment));
        viewModel.getLoggedIn().observe(getViewLifecycleOwner(), login -> {
            int vis = Boolean.TRUE.equals(login) ? View.VISIBLE : View.GONE;
            binding.wallet.walletValue.setVisibility(vis);
            binding.wallet.voucherValue.setVisibility(vis);
            binding.wallet.pointsValue.setVisibility(vis);
            if (vis == View.VISIBLE) {
                binding.wallet.walletValue.setText(R.string.home_wallet_fetching);
                binding.wallet.voucherValue.setText(R.string.home_wallet_fetching);
                binding.wallet.pointsValue.setText(R.string.home_wallet_fetching);
            }
        });
        viewModel.getWallet().observe(getViewLifecycleOwner(), res -> {
            if (res == null || !res.isSuccess() || res.data == null) {
                return;
            }
            WalletInfo w = res.data;
            binding.wallet.walletValue.setText(Currency.formatRupiah(w.walletTotal, 0));
            binding.wallet.voucherValue.setText(w.voucherCount >= 1
                    ? getString(R.string.home_wallet_voucher_count, Currency.formatUnit(w.voucherCount))
                    : getString(R.string.home_wallet_no_voucher));
            binding.wallet.pointsValue.setText(Currency.formatRupiah(w.points, 0));
        });
    }

    private void go(int destination) {
        NavHostFragment.findNavController(this).navigate(destination);
    }

    private void setupContent() {
        ContentAdapter adapter = new ContentAdapter(c.homeRepository::getLowestPrice, ProductListener.navigating(this),
                new ContentAdapter.Listener() {
                    @Override
                    public void onViewAll(id.laukpauk.data.model.home.Content content) {
                        HomeNav.openContentAll(HomePageFragment.this, content);
                    }

                    @Override
                    public void onBanner(id.laukpauk.data.model.home.Content content,
                                         id.laukpauk.data.model.home.ContentItem item) {
                        HomeNav.openBanner(HomePageFragment.this, content, item);
                    }
                });
        binding.contentList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.contentList.setAdapter(adapter);
        viewModel.getContent().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.contentProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (!res.isLoading()) {
                adapter.submitList(res.data);
            }
        });
    }

    private void setupArticle() {
        SlideAdapter slides = new SlideAdapter(slide -> openUrl((String) slide.tag));
        binding.articlePager.setAdapter(slides);
        AutoPager.attach(binding.articlePager);
        binding.articleEmpty.emptyText.setText(R.string.home_article_empty);
        binding.articleView.setOnClickListener(v -> c.settingRepository.getSettingValue("seeAllArticleURL", result -> {
            if (!isAdded()) {
                return;
            }
            String url = SettingRepository.asString(result.data);
            if (url != null) {
                launch(url);
            }
        }));
        viewModel.getArticle().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            binding.articleProgress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isLoading()) {
                return;
            }
            List<SlideAdapter.Slide> list = new ArrayList<>();
            if (res.isSuccess() && res.data != null) {
                for (Article a : res.data) {
                    String heading = a.heading == null ? "" : a.heading;
                    if (heading.length() >= 31) {
                        heading = getString(R.string.home_article_long_heading, heading.substring(0, 30));
                    }
                    list.add(new SlideAdapter.Slide(Images.server(ServerPath.uploadImageArticle, a.photo), heading, a.link));
                }
            }
            slides.submit(list);
            binding.articlePager.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            binding.articleEmpty.getRoot().setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    /** Opens an article link; the "URL tidak bisa diakses" dialog when nothing can show it. */
    private void openUrl(@Nullable String url) {
        if (!launch(url)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.home_app_name)
                    .setMessage(R.string.home_url_cannot)
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
