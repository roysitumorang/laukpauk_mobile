package id.laukpauk.ui.checkout;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.checkout.Voucher;
import id.laukpauk.databinding.SheetCheckoutVoucherBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;

/** Bottom sheet "Voucher Kamu" (legacy CheckoutModel.showVoucher). */
final class VoucherSheet {
    private VoucherSheet() {}

    static void show(Fragment fragment, LiveData<Resource<List<Voucher>>> data, long totalBelanja,
                     @Nullable Voucher used, VoucherAdapter.OnVoucher onSelected) {
        final BottomSheetDialog dialog = new BottomSheetDialog(fragment.requireContext());
        final SheetCheckoutVoucherBinding b = SheetCheckoutVoucherBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(b.getRoot());
        b.subtitle.setText(fragment.getString(R.string.checkout_voucher_aktif, Currency.formatRupiah(totalBelanja, 0)));
        Images.asset(b.emptyImage, "empty.png");
        VoucherAdapter adapter = new VoucherAdapter(used != null ? used.id : null, v -> {
            dialog.dismiss();
            onSelected.onVoucher(v);
        });
        b.list.setAdapter(adapter);
        data.observe(fragment.getViewLifecycleOwner(), res -> {
            b.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
            if (res.isLoading()) {
                return;
            }
            List<Voucher> list = res.isSuccess() ? res.data : null;
            boolean has = list != null && !list.isEmpty();
            adapter.submitList(list);
            b.list.setVisibility(has ? View.VISIBLE : View.GONE);
            b.empty.setVisibility(has ? View.GONE : View.VISIBLE);
        });
        dialog.show();
    }
}
