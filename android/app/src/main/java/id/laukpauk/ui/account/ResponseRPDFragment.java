package id.laukpauk.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.R;
import id.laukpauk.databinding.FragmentResponseRPDBinding;
import id.laukpauk.ui.common.AppBars;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;

/**
 * ResponseRPD.dart: the admin's answer to a fund disbursement request. Arguments: dataPenjual and dataRPD as JSON
 * text (genuinely dynamic payloads from the notification route, so they are read as JsonObject).
 */
public class ResponseRPDFragment extends Fragment {

    private FragmentResponseRPDBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResponseRPDBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppBars.setup(this, binding.appBar, getString(R.string.account_rpd_title));
        Bundle args = requireArguments();
        JsonObject dataPenjual = orEmpty(Json.parseObject(args.getString("dataPenjual", "{}")));
        JsonObject dataRPD = orEmpty(Json.parseObject(args.getString("dataRPD", "{}")));

        String status = Json.str(dataRPD, "status").toLowerCase(Locale.ROOT);
        boolean approved = status.equals("approved");
        long nominal = Json.asLong(Json.get(dataRPD, "nominal"), 0);
        String nominalText = Currency.formatRupiah(nominal, 0);

        Images.asset(binding.image, status.equals("pending") ? "RPD_Pending.png" : "RPD_Rejected.png");
        binding.hai.setText(getString(R.string.account_hai_penjual, Json.str(dataPenjual, "namaToko")));
        binding.nominal.setText(nominalText);
        if (approved) {
            binding.pesan.setText(R.string.account_rpd_disetujui);
            binding.transfer.setVisibility(View.VISIBLE);
            binding.transfer.setText(getString(R.string.account_rpd_transfer, nominalText,
                    Json.str(dataRPD, "nomorRekening"), Json.str(dataRPD, "namaPenerima")));
            binding.alasan.setVisibility(View.GONE);
        } else {
            binding.pesan.setText(R.string.account_rpd_ditolak);
            binding.transfer.setVisibility(View.GONE);
            binding.alasan.setVisibility(View.VISIBLE);
            binding.alasan.setText(status.equals("rejected") ? Json.str(dataRPD, "rejectionReason") : "");
        }
        binding.halamanAwal.setOnClickListener(v -> AccountNav.toHome(this));
        binding.tokoSaya.setOnClickListener(v -> AccountNav.toTokoSaya(this));
    }

    private static JsonObject orEmpty(JsonObject o) {
        return o == null ? new JsonObject() : o;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
