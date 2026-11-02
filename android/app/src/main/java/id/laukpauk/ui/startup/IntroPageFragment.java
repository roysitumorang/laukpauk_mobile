package id.laukpauk.ui.startup;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentIntroPageBinding;
import id.laukpauk.di.AppContainer;

/** IntroPage.dart + IntroPageIndicator.dart: the intro pager with its animated dots. */
public class IntroPageFragment extends Fragment {

    private static final float ACTIVE_DP = 15f;
    private static final float INACTIVE_DP = 10f;

    private FragmentIntroPageBinding binding;
    private final List<View> dots = new ArrayList<>();
    private int activePage = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIntroPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        binding.pager.setAdapter(new IntroPageAdapter(() -> {
            c.startupRepository.markAppStarted();
            NavHostFragment.findNavController(this).navigate(R.id.splashScreenFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.introPageFragment, true).build());
        }));

        dots.clear();
        activePage = -1;
        for (int i = 0; i < IntroPageAdapter.IMAGES.length; i++) {
            View dot = LayoutInflater.from(requireContext()).inflate(R.layout.view_startup_dot, binding.indicator, false);
            binding.indicator.addView(dot);
            dots.add(dot);
        }
        setActivePage(0, false);
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setActivePage(position, true);
            }
        });
    }

    private void setActivePage(int page, boolean animate) {
        if (page == activePage) {
            return;
        }
        activePage = page;
        float density = getResources().getDisplayMetrics().density;
        int active = ContextCompat.getColor(requireContext(), R.color.green);
        int inactive = ContextCompat.getColor(requireContext(), R.color.nearly_white);
        for (int i = 0; i < dots.size(); i++) {
            final View dot = dots.get(i);
            boolean isActive = i == page;
            dot.setBackgroundTintList(ColorStateList.valueOf(isActive ? active : inactive));
            float to = (isActive ? ACTIVE_DP : INACTIVE_DP) * density;
            if (!animate) {
                resize(dot, to);
                continue;
            }
            ValueAnimator anim = ValueAnimator.ofFloat(dot.getLayoutParams().width, to);
            anim.setDuration(250);
            anim.addUpdateListener(a -> resize(dot, (Float) a.getAnimatedValue()));
            anim.start();
        }
    }

    private static void resize(View dot, float px) {
        ViewGroup.LayoutParams lp = dot.getLayoutParams();
        lp.width = Math.round(px);
        lp.height = Math.round(px);
        dot.setLayoutParams(lp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dots.clear();
        binding = null;
    }
}
