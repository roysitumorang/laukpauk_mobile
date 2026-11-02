package id.laukpauk.ui.startup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import id.laukpauk.R;
import id.laukpauk.data.repository.StartupRepository;
import id.laukpauk.databinding.FragmentSplashScreenBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.Images;

/**
 * SplashScreen.dart: the launcher screen. Arguments: removeTransactionSession, routeTo (a destination id
 * replacing Dart's {@code Widget? routeTo}) and routeArgs (its arguments as JSON text).
 */
public class SplashScreenFragment extends Fragment {

    private FragmentSplashScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        SplashViewModel viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new SplashViewModel(c.startupRepository, c.cartRepository)))
                .get(SplashViewModel.class);
        Images.asset(binding.logo, "laukpauk-icon.png");

        Bundle args = requireArguments();
        final int routeTo = args.getInt("routeTo", 0);
        final String routeArgs = args.getString("routeArgs");
        viewModel.getDestination().observe(getViewLifecycleOwner(), destination -> {
            if (destination == null) {
                return;
            }
            NavController nav = NavHostFragment.findNavController(this);
            NavOptions leaveSplash = new NavOptions.Builder().setPopUpTo(R.id.splashScreenFragment, true).build();
            switch (destination) {
                case StartupRepository.GO_INTRO:
                    nav.navigate(R.id.introPageFragment, null, leaveSplash);
                    break;
                case StartupRepository.GO_OTP_REGISTRASI:
                    nav.navigate(R.id.oTPRegistrasiFragment, null, leaveSplash);
                    break;
                case SplashViewModel.GO_ROUTE:
                    // legacy pushAndRemoveUntil(routeTo, until HomePage): home stays under the target
                    nav.navigate(R.id.homePageFragment, null, leaveSplash);
                    try {
                        nav.navigate(routeTo, RouteArgs.toBundle(routeArgs));
                    } catch (IllegalArgumentException e) {
                        // unknown destination: stay on home
                    }
                    break;
                default:
                    nav.navigate(R.id.homePageFragment, null, leaveSplash);
                    break;
            }
        });
        viewModel.start(args.getBoolean("removeTransactionSession", false), routeTo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
