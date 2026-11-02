package id.laukpauk.ui.seller;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

import id.laukpauk.R;

/** Fills a Spinner with (value, label) pairs; the callback fires only when the user picks another entry. */
final class ProdukPenjualSpinners {
    private ProdukPenjualSpinners() {}

    interface OnPick {
        void onPick(String value);
    }

    static void bind(Context ctx, Spinner spinner, final List<String> values, List<String> labels,
                     String selected, final OnPick onPick) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, R.layout.item_seller_produk_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_seller_produk_spinner);
        spinner.setOnItemSelectedListener(null);
        spinner.setAdapter(adapter);
        final int index = Math.max(0, values.indexOf(selected));
        spinner.setSelection(index, false);
        final int[] current = {index};
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == current[0]) {
                    return;
                }
                current[0] = position;
                if (onPick != null) {
                    onPick.onPick(values.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
