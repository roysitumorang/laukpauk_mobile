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
import id.laukpauk.data.model.notification.SpecialNotification;
import id.laukpauk.databinding.ItemSpecialNotificationBinding;
import id.laukpauk.ui.common.Images;

/** Rows of the personal tab (NotifikasiKhususItem.dart). */
public class SpecialNotificationAdapter extends ListAdapter<SpecialNotification, SpecialNotificationAdapter.Holder> {

    public interface OnClick {
        void onClick(SpecialNotification item);
    }

    private final OnClick onClick;

    public SpecialNotificationAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemSpecialNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SpecialNotification n = getItem(position);
        ItemSpecialNotificationBinding b = holder.binding;
        b.getRoot().setBackgroundColor(n.isRead ? 0xFFFFFFFF : 0x2566CC99);
        Images.asset(b.icon, "notifikasi/" + n.notificationType + ".png");
        b.heading.setText(n.heading == null ? "" : n.heading.toUpperCase(Locale.ROOT));
        b.heading.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                n.isRead ? R.color.nearly_black : R.color.text_success));
        b.body.setText(n.body);
        b.time.setText(n.time);
        b.status.setText(n.isRead ? R.string.notification_read : R.string.notification_unread);
        b.getRoot().setOnClickListener(v -> onClick.onClick(n));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemSpecialNotificationBinding binding;

        Holder(ItemSpecialNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<SpecialNotification> DIFF = new DiffUtil.ItemCallback<SpecialNotification>() {
        @Override
        public boolean areItemsTheSame(@NonNull SpecialNotification a, @NonNull SpecialNotification b) {
            return Objects.equals(a.id, b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull SpecialNotification a, @NonNull SpecialNotification b) {
            return a.isRead == b.isRead && Objects.equals(a.heading, b.heading) && Objects.equals(a.body, b.body)
                    && Objects.equals(a.time, b.time) && Objects.equals(a.notificationType, b.notificationType);
        }
    };
}
