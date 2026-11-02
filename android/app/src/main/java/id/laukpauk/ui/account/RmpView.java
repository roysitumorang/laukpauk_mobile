package id.laukpauk.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.databinding.ViewAccountRmpBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.util.Json;

/**
 * Legacy RMP component ("Response Menjadi Penjual"): shown on Toko Saya while a seller request is pending or
 * rejected. Use in a layout as {@code <id.laukpauk.ui.account.RmpView .../>} and call
 * {@link #bind(Fragment, JsonObject)} with the "dataPenjual" object (namaToko, status, statusReason).
 */
public class RmpView extends LinearLayout {

    private final ViewAccountRmpBinding binding;

    public RmpView(android.content.Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        int pad = (int) (15 * context.getResources().getDisplayMetrics().density);
        setPadding(pad, pad, pad, pad);
        binding = ViewAccountRmpBinding.inflate(LayoutInflater.from(context), this);
    }

    public void bind(Fragment owner, JsonObject dataPenjual) {
        String status = Json.str(dataPenjual, "status").toLowerCase(Locale.ROOT);
        boolean pending = status.equals("pending");
        Images.asset(binding.image, pending ? "RMP_Pending.png" : "RMP_Rejected.png");
        Images.asset(binding.gagalImage, "empty.png");
        binding.hai.setText(getContext().getString(R.string.account_hai_penjual, Json.str(dataPenjual, "namaToko")));
        binding.pesan.setText(pending ? R.string.account_rmp_pending : R.string.account_rmp_ditolak);
        binding.pesan.setTextColor(ContextCompat.getColor(getContext(),
                pending ? R.color.text_muted : R.color.text_danger));
        boolean rejected = status.equals("rejected");
        binding.alasan.setVisibility(rejected ? View.VISIBLE : View.GONE);
        binding.alasan.setText(rejected ? Json.str(dataPenjual, "statusReason") : "");

        binding.progress.setVisibility(View.VISIBLE);
        binding.whatsapp.setVisibility(View.GONE);
        binding.gagal.setVisibility(View.GONE);
        AppContainer.from(getContext()).settingRepository.getContactUsWhatsappConfiguration(result -> {
            if (!owner.isAdded()) {
                return;
            }
            binding.progress.setVisibility(View.GONE);
            if (result.isSuccess() && result.data != null && result.data.isJsonObject()) {
                final String number = Json.str(result.data.getAsJsonObject(), "contactUsWhatsapp");
                binding.whatsapp.setVisibility(View.VISIBLE);
                binding.whatsapp.setOnClickListener(v -> {
                    try {
                        owner.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number)));
                    } catch (RuntimeException e) {
                        Messages.snack(owner, getContext().getString(R.string.account_rmp_whatsapp_gagal));
                    }
                });
            } else {
                binding.gagal.setVisibility(View.VISIBLE);
            }
        });
    }
}
