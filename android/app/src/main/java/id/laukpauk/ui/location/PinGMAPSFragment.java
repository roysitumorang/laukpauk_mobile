package id.laukpauk.ui.location;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.delivery.PlaceSuggestion;
import id.laukpauk.databinding.FragmentPinGMAPSBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Json;

/**
 * PinGMAPS.dart: pick a delivery address on a map (long-press, search box or the given start position).
 * Args: currentLatitude, currentLongitude (default Merdeka Walk Medan), appBarText.
 * Result: {@link #RESULT_KEY} = JSON text {formattedAddress, lat, lng} (strings) via NavResults on the previous
 * screen; no result when the buyer just goes back.
 */
public class PinGMAPSFragment extends Fragment {

    public static final String RESULT_KEY = "pin_gmaps_result";

    private static final double DEFAULT_LAT = 3.5889242;
    private static final double DEFAULT_LNG = 98.6790958;

    private FragmentPinGMAPSBinding binding;
    private PinGMAPSViewModel viewModel;
    private GoogleMap googleMap;
    private PinGMAPSViewModel.Position shown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPinGMAPSBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this, ViewModelFactory.of(() -> new PinGMAPSViewModel(c.deliveryRepository)))
                .get(PinGMAPSViewModel.class);
        Bundle args = getArguments();
        String lat = args != null && args.getString("currentLatitude") != null
                ? args.getString("currentLatitude") : String.valueOf(DEFAULT_LAT);
        String lng = args != null && args.getString("currentLongitude") != null
                ? args.getString("currentLongitude") : String.valueOf(DEFAULT_LNG);
        String title = args != null && args.getString("appBarText") != null
                ? args.getString("appBarText") : getString(R.string.delivery_pin_title_default);
        AppBars.setup(this, binding.appBar, title);
        Images.asset(binding.emptyImage, "empty.png");

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(map -> {
            googleMap = map;
            map.addMarker(new MarkerOptions().position(new LatLng(DEFAULT_LAT, DEFAULT_LNG)));
            map.setOnMapLongClickListener(p -> viewModel.moveTo(p.latitude, p.longitude));
            if (viewModel.position().getValue() != null) {
                showPosition(viewModel.position().getValue(), false);
            }
        });

        SuggestionAdapter adapter = new SuggestionAdapter(s -> {
            binding.searchField.clearFocus();
            hideKeyboard();
            viewModel.hideSuggestions();
            viewModel.select(s.formattedAddress, s.lat, s.lng);
        });
        binding.suggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.suggestions.setAdapter(adapter);
        binding.searchField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                viewModel.queryNow(binding.searchField.getText().toString());
            } else {
                viewModel.hideSuggestions();
            }
        });
        binding.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding != null && binding.searchField.hasFocus()) {
                    viewModel.query(s.toString());
                }
            }
        });
        viewModel.suggestions().observe(getViewLifecycleOwner(), list -> renderSuggestions(adapter, list));
        viewModel.position().observe(getViewLifecycleOwner(), p -> {
            if (p != null) {
                binding.tetapkan.setVisibility(View.VISIBLE);
                showPosition(p, true);
            }
        });
        binding.tetapkan.setOnClickListener(v -> {
            PinGMAPSViewModel.Position p = viewModel.position().getValue();
            if (p == null) {
                return;
            }
            JsonObject result = Json.of("formattedAddress", p.formattedAddress,
                    "lat", String.valueOf(p.lat), "lng", String.valueOf(p.lng));
            NavResults.setResult(this, RESULT_KEY, result.toString());
            NavHostFragment.findNavController(this).popBackStack();
        });
        viewModel.init(lat, lng);
    }

    private void renderSuggestions(SuggestionAdapter adapter, @Nullable List<PlaceSuggestion> list) {
        if (binding == null) {
            return;
        }
        if (list == null) {
            binding.suggestionsCard.setVisibility(View.GONE);
            return;
        }
        adapter.submitList(list);
        binding.suggestions.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        binding.suggestionsEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        binding.suggestionsCard.setVisibility(View.VISIBLE);
    }

    /** Legacy pindahLokasi: animate to the position (zoom 16) and replace the marker. */
    private void showPosition(PinGMAPSViewModel.Position p, boolean animate) {
        if (googleMap == null || p == shown) {
            return;
        }
        shown = p;
        LatLng ll = new LatLng(p.lat, p.lng);
        CameraPosition cp = new CameraPosition(ll, 16.0f, 0f, 0f);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(ll));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && binding != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }
    }

    // ------------------------------------------------------------------ MapView lifecycle

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        binding.mapView.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            binding.mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        binding.mapView.onDestroy();
        googleMap = null;
        shown = null;
        binding = null;
        super.onDestroyView();
    }
}
