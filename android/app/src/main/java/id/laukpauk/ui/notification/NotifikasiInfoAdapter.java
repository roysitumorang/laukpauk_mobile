package id.laukpauk.ui.notification;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import id.laukpauk.R;
import id.laukpauk.data.model.notifikasi.Notifikasi;
import id.laukpauk.databinding.ItemNotifikasiInfoBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Rows of the "Info" tab (NotifikasiGlobalItem.dart). */
public class NotifikasiInfoAdapter extends ListAdapter<Notifikasi, NotifikasiInfoAdapter.Holder> {

    public interface OnClick {
        void onClick(Notifikasi item);
    }

    private final OnClick onClick;

    public NotifikasiInfoAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNotifikasiInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Notifikasi n = getItem(position);
        ItemNotifikasiInfoBinding b = holder.binding;
        b.getRoot().setBackgroundColor(n.isRead ? 0xFFFFFFFF : 0x2566CC99);
        b.judul.setText(n.judul == null ? "" : n.judul.toUpperCase(Locale.ROOT));
        b.judul.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                n.isRead ? R.color.nearly_black : R.color.text_success));
        String pesan = n.pesan == null ? "" : n.pesan;
        b.pesan.setText(pesan.length() >= 75 ? pesan.substring(0, 75) + ".." : pesan);
        b.tanggal.setText(n.createdAt);
        if (n.foto != null && !n.foto.isEmpty()) {
            Images.circle(b.foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarPushNotifikasi + "/compress/" + n.foto);
        } else {
            b.foto.setImageResource(R.drawable.laukpauk_icon);
        }
        b.getRoot().setOnClickListener(v -> onClick.onClick(n));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNotifikasiInfoBinding binding;

        Holder(ItemNotifikasiInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<Notifikasi> DIFF = new DiffUtil.ItemCallback<Notifikasi>() {
        @Override
        public boolean areItemsTheSame(@NonNull Notifikasi a, @NonNull Notifikasi b) {
            return Objects.equals(a.idNotifikasi, b.idNotifikasi);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notifikasi a, @NonNull Notifikasi b) {
            return a.isRead == b.isRead && Objects.equals(a.judul, b.judul) && Objects.equals(a.pesan, b.pesan)
                    && Objects.equals(a.foto, b.foto) && Objects.equals(a.createdAt, b.createdAt);
        }
    };
}
