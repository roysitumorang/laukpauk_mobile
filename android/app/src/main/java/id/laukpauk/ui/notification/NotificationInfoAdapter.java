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
import id.laukpauk.data.model.notification.Notification;
import id.laukpauk.databinding.ItemNotificationInfoBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.ServerPath;

/** Rows of the "Info" tab (NotifikasiGlobalItem.dart). */
public class NotificationInfoAdapter extends ListAdapter<Notification, NotificationInfoAdapter.Holder> {

    public interface OnClick {
        void onClick(Notification item);
    }

    private final OnClick onClick;

    public NotificationInfoAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNotificationInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Notification n = getItem(position);
        ItemNotificationInfoBinding b = holder.binding;
        b.getRoot().setBackgroundColor(n.isRead ? 0xFFFFFFFF : 0x2566CC99);
        b.heading.setText(n.heading == null ? "" : n.heading.toUpperCase(Locale.ROOT));
        b.heading.setTextColor(ContextCompat.getColor(b.getRoot().getContext(),
                n.isRead ? R.color.nearly_black : R.color.text_success));
        String message = n.message == null ? "" : n.message;
        b.message.setText(message.length() >= 75 ? message.substring(0, 75) + ".." : message);
        b.date.setText(n.createdAt);
        if (n.photo != null && !n.photo.isEmpty()) {
            Images.circle(b.photo, ServerPath.baseURL + "/" + ServerPath.uploadImagePushNotification + "/compress/" + n.photo);
        } else {
            b.photo.setImageResource(R.drawable.laukpauk_icon);
        }
        b.getRoot().setOnClickListener(v -> onClick.onClick(n));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNotificationInfoBinding binding;

        Holder(ItemNotificationInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF = new DiffUtil.ItemCallback<Notification>() {
        @Override
        public boolean areItemsTheSame(@NonNull Notification a, @NonNull Notification b) {
            return Objects.equals(a.notificationId, b.notificationId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notification a, @NonNull Notification b) {
            return a.isRead == b.isRead && Objects.equals(a.heading, b.heading) && Objects.equals(a.message, b.message)
                    && Objects.equals(a.photo, b.photo) && Objects.equals(a.createdAt, b.createdAt);
        }
    };
}
