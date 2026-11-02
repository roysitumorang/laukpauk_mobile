package id.laukpauk.ui.delivery;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import id.laukpauk.MainActivity;
import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.JadwalDelivery;
import id.laukpauk.databinding.DialogDeliveryConfirmAddressBinding;
import id.laukpauk.databinding.DialogDeliveryJadwalBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.location.PinGMAPSFragment;

/**
 * Headless fragment (added to the activity) that runs {@link DeliveryFlow#ask}: it owns the permission launcher and
 * the dialogs; the state lives in {@link DeliveryFlowViewModel}. One instance per activity, kept for reuse.
 */
public class DeliveryFlowFragment extends Fragment {
    static final String TAG = "delivery_flow";
    private static WeakReference<DeliveryFlowFragment> instance;

    private final List<DeliveryFlow.Callback> pending = new ArrayList<>();
    private DeliveryFlowViewModel vm;
    private Dialog dialog;
    private NavController listenedNav;

    private final ActivityResultLauncher<String[]> permission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), r -> locate());

    private final NavController.OnDestinationChangedListener pinListener = (controller, dest, args) -> {
        if (vm == null || vm.step.getValue() != DeliveryFlowViewModel.Step.PIN) {
            return;
        }
        if (dest.getId() == R.id.pinGMAPSFragment) {
            vm.pinSeen = true;
            return;
        }
        if (!vm.pinSeen) {
            return;
        }
        vm.pinSeen = false;
        String result = null;
        NavBackStackEntry entry = controller.getCurrentBackStackEntry();
        if (entry != null) {
            result = entry.getSavedStateHandle().remove(PinGMAPSFragment.RESULT_KEY);
        }
        vm.pinResult(result);
    };

    static void request(FragmentActivity activity, @Nullable DeliveryFlow.Callback cb) {
        DeliveryFlowFragment f = (DeliveryFlowFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (f == null && instance != null) {
            DeliveryFlowFragment i = instance.get();
            if (i != null && i.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
                f = i;
            }
        }
        if (f == null) {
            f = new DeliveryFlowFragment();
            instance = new WeakReference<>(f);
            activity.getSupportFragmentManager().beginTransaction().add(f, TAG).commitAllowingStateLoss();
        }
        f.enqueue(cb);
    }

    private void enqueue(@Nullable DeliveryFlow.Callback cb) {
        if (vm != null) {
            vm.begin(cb);
        } else if (cb != null) {
            pending.add(cb);
        } else {
            pending.add(ok -> { });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContainer c = AppContainer.from(requireContext());
        vm = new ViewModelProvider(this, ViewModelFactory.of(
                () -> new DeliveryFlowViewModel(c.deliveryRepository, c.cartRepository)))
                .get(DeliveryFlowViewModel.class);
        vm.step.observe(this, this::render);
        List<DeliveryFlow.Callback> toStart = new ArrayList<>(pending);
        pending.clear();
        for (DeliveryFlow.Callback cb : toStart) {
            vm.begin(cb);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (vm.step.getValue() == DeliveryFlowViewModel.Step.PIN) {
            listenPin();
        }
    }

    @Override
    public void onStop() {
        unlistenPin();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }

    private void listenPin() {
        if (listenedNav == null && getActivity() instanceof MainActivity) {
            listenedNav = ((MainActivity) getActivity()).getNavController();
            listenedNav.addOnDestinationChangedListener(pinListener);
        }
    }

    private void unlistenPin() {
        if (listenedNav != null) {
            listenedNav.removeOnDestinationChangedListener(pinListener);
            listenedNav = null;
        }
    }

    private void dismissDialog() {
        if (dialog != null) {
            Dialog d = dialog;
            dialog = null;
            d.setOnDismissListener(null);
            d.dismiss();
        }
    }

    private void render(DeliveryFlowViewModel.Step step) {
        dismissDialog();
        if (step != DeliveryFlowViewModel.Step.PIN) {
            unlistenPin();
        }
        switch (step) {
            case DETECT:
                detect();
                break;
            case GPS_OFF:
                notice(R.string.delivery_laukpauk, R.string.delivery_gps_off, vm::gpsOk);
                break;
            case GEOCODE_FAILED:
                notice(R.string.delivery_deteksi_gagal_title, R.string.delivery_deteksi_gagal, vm::geocodeFailedOk);
                break;
            case CONFIRM:
                showConfirm();
                break;
            case PIN:
                openPin();
                break;
            case JADWAL:
                showJadwal();
                break;
            default:
                break;
        }
    }

    private void notice(int title, int message, Runnable ok) {
        dialog = new MaterialAlertDialogBuilder(requireActivity()).setTitle(title).setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.delivery_ok, (d, w) -> ok.run()).show();
    }

    // ------------------------------------------------------------------ location

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        if (lm == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        }
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void detect() {
        if (vm.detecting) {
            return;
        }
        vm.detecting = true;
        if (hasPermission()) {
            locate();
        } else {
            permission.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void locate() {
        final DeliveryFlowViewModel model = vm;
        if (model == null || model.step.getValue() != DeliveryFlowViewModel.Step.DETECT) {
            return;
        }
        if (!hasPermission() || !isLocationServiceEnabled()) {
            model.onLocation(null, null);
            return;
        }
        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(requireActivity());
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener(task -> {
                android.location.Location loc = null;
                try {
                    if (task.isSuccessful()) {
                        loc = task.getResult();
                    }
                } catch (RuntimeException e) {
                    loc = null;
                }
                if (loc == null) {
                    model.onLocation(null, null);
                } else {
                    model.onLocation(String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()));
                }
            });
        } catch (RuntimeException e) {
            model.onLocation(null, null);
        }
    }

    // ------------------------------------------------------------------ dialogs / navigation

    private void showConfirm() {
        DialogDeliveryConfirmAddressBinding b = DialogDeliveryConfirmAddressBinding.inflate(android.view.LayoutInflater.from(requireContext()));
        b.message.setText(R.string.delivery_alamat_content);
        b.address.setText(vm.alamat);
        dialog = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.delivery_alamat_title).setView(b.getRoot()).setCancelable(false)
                .setNeutralButton(R.string.delivery_tutup, (d, w) -> vm.confirmTutup())
                .setNegativeButton(R.string.delivery_alamat_lain, (d, w) -> vm.confirmAlamatLain())
                .setPositiveButton(R.string.delivery_ya, (d, w) -> vm.confirmYa())
                .show();
    }

    private void openPin() {
        if (!(getActivity() instanceof MainActivity)) {
            vm.pinResult(null);
            return;
        }
        listenPin();
        if (vm.pinOpened) {
            return;
        }
        vm.pinOpened = true;
        Bundle args = new Bundle();
        args.putString("currentLatitude", vm.latitude);
        args.putString("currentLongitude", vm.longitude);
        ((MainActivity) getActivity()).getNavController().navigate(R.id.pinGMAPSFragment, args);
    }

    private void showJadwal() {
        DialogDeliveryJadwalBinding b = DialogDeliveryJadwalBinding.inflate(android.view.LayoutInflater.from(requireContext()));
        b.alamat.setText(vm.alamat);
        JadwalAdapter adapter = new JadwalAdapter(vm::selectJadwal);
        b.list.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        b.list.setAdapter(adapter);
        AlertDialog d = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.delivery_jadwal_title).setView(b.getRoot()).setCancelable(false)
                .setNegativeButton(R.string.delivery_jadwal_nanti_saja, (x, w) -> vm.jadwalNantiSaja())
                .setPositiveButton(R.string.delivery_ok, (x, w) -> vm.jadwalOk())
                .create();
        d.show();
        d.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(android.view.View.GONE);
        Observer<Resource<List<JadwalDelivery>>> listObs = res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            List<JadwalDelivery> items = res.data != null ? res.data : new ArrayList<JadwalDelivery>();
            b.progress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
            b.list.setVisibility(!loading && !items.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            b.none.setVisibility(!loading && items.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            adapter.submitList(items);
        };
        Observer<JadwalDelivery> selObs = j -> {
            adapter.setSelected(j);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(j != null ? android.view.View.VISIBLE : android.view.View.GONE);
        };
        vm.jadwal.observe(this, listObs);
        vm.selected.observe(this, selObs);
        d.setOnDismissListener(x -> {
            vm.jadwal.removeObserver(listObs);
            vm.selected.removeObserver(selObs);
        });
        dialog = d;
    }
}
