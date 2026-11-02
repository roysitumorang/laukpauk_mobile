package id.laukpauk.ui.cart;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.databinding.FragmentEditKeranjangItemBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.NavResults;

/** EditKeranjangItem.dart. Argument: dataKeranjangItem (JSON of the cart line). Result: Boolean (saved). */
public class EditKeranjangItemFragment extends Fragment {

    private FragmentEditKeranjangItemBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditKeranjangItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        AppBars.setup(this, binding.appBar, getString(R.string.cart_edit_title));
        CartProduct parsed = c.apiClient.gson().fromJson(requireArguments().getString("dataKeranjangItem"),
                CartProduct.class);
        final CartProduct product = parsed != null ? parsed : new CartProduct();
        final EditKeranjangItemViewModel viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new EditKeranjangItemViewModel(c.cartRepository, product)))
                .get(EditKeranjangItemViewModel.class);

        binding.nama.setText(product.namaProduk);

        String jenisSatuan = product.jenisSatuan == null ? "" : product.jenisSatuan;
        if (jenisSatuan.toLowerCase().equals("tidak pas") && product.idPromo == null) {
            binding.quantityBox.setVisibility(View.VISIBLE);
            final List<String> items = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                items.add(String.valueOf(i));
            }
            if (!items.contains(viewModel.quantity)) {
                items.add(viewModel.quantity);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, items);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.quantitySpinner.setAdapter(adapter);
            binding.quantitySpinner.setSelection(items.indexOf(viewModel.quantity));
            binding.quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                    viewModel.quantity = items.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        binding.note.setText(viewModel.catatan);
        binding.note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.catatan = s.toString();
            }
        });

        viewModel.getSaving().observe(getViewLifecycleOwner(), saving -> {
            boolean busy = Boolean.TRUE.equals(saving);
            binding.save.setEnabled(!busy);
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        });
        binding.save.setOnClickListener(v -> viewModel.save(result -> {
            if (!isAdded()) {
                return;
            }
            NavResults.setResult(this, KeranjangFragment.EDIT_RESULT,
                    result.isSuccess() && Boolean.TRUE.equals(result.data));
            NavHostFragment.findNavController(this).popBackStack();
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
