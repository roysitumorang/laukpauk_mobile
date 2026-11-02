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
import id.laukpauk.data.model.notifikasi.NotifikasiKhusus;
import id.laukpauk.databinding.ItemNotifikasiKhususBinding;
import id.laukpauk.ui.common.Images;

/** Rows of the personal tab (NotifikasiKhususItem.dart). */
public class NotifikasiKhususAdapter extends ListAdapter<NotifikasiKhusus, NotifikasiKhususAdapter.Holder> {

    public interface OnClick {
        void onClick(NotifikasiKhusus item);
    }

    private final OnClick onClick;

    public NotifikasiKhususAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNotifikasiKhususBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotifikasiKhusus n = getItem(position);
        ItemNotifikasiKhususBinding b = holder.binding;
        b.getRoot().setBackgroundColor(n.isRead ? 0xFFFFFFFF : 0x2566CC99);
        Images.asset(b.icon, "notifikasi/" + n.jenisNotifikasi + ".png");
        b.judul.setText(n.judul == null ? "" : n.judul.toUpperCase(Locale.ROOT));
        b.judul.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                n.isRead ? R.color.nearly_black : R.color.text_success));
        b.isi.setText(n.isi);
        b.waktu.setText(n.waktu);
        b.status.setText(n.isRead ? R.string.notifikasi_sudah_dibaca : R.string.notifikasi_belum_dibaca);
        b.getRoot().setOnClickListener(v -> onClick.onClick(n));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNotifikasiKhususBinding binding;

        Holder(ItemNotifikasiKhususBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<NotifikasiKhusus> DIFF = new DiffUtil.ItemCallback<NotifikasiKhusus>() {
        @Override
        public boolean areItemsTheSame(@NonNull NotifikasiKhusus a, @NonNull NotifikasiKhusus b) {
            return Objects.equals(a.id, b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull NotifikasiKhusus a, @NonNull NotifikasiKhusus b) {
            return a.isRead == b.isRead && Objects.equals(a.judul, b.judul) && Objects.equals(a.isi, b.isi)
                    && Objects.equals(a.waktu, b.waktu) && Objects.equals(a.jenisNotifikasi, b.jenisNotifikasi);
        }
    };
}
