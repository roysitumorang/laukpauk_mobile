package id.laukpauk.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Map;

import id.laukpauk.MainActivity;
import id.laukpauk.R;
import id.laukpauk.ui.startup.MainRouter;

/**
 * Shows push notifications and builds the intent that opens them. The data keys are the ones the backend used
 * with OneSignal's {@code additionalData} (see {@link MainRouter}): {@code notifikasi} ("update" |
 * "khususuntukkamu"), {@code idNotifikasi}, {@code routeName}, {@code routeParams}; plus {@code title},
 * {@code body} and an optional {@code image} URL for data-only messages.
 */
public final class PushNotifications {
    private PushNotifications() {}

    /** Creates the notification channel (API 26+). Safe to call repeatedly. */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.push_channel_id),
                    context.getString(R.string.push_channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.push_channel_description));
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    /** The intent MainRouter.handle routes: MainActivity with the message data as String extras. */
    public static Intent openIntent(Context context, Map<String, String> data) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (Map.Entry<String, String> e : data.entrySet()) {
            intent.putExtra(e.getKey(), e.getValue());
        }
        return intent;
    }

    /** Posts a notification (no-op when the user disabled notifications or denied POST_NOTIFICATIONS). */
    public static void show(Context context, int id, @Nullable String title, @Nullable String body,
                            @Nullable Bitmap image, Map<String, String> data) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        if (!nm.areNotificationsEnabled()) {
            return;
        }
        PendingIntent open = PendingIntent.getActivity(context, id, openIntent(context, data),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, context.getString(R.string.push_channel_id))
                .setSmallIcon(R.drawable.ic_stat_icon_apps)
                .setColor(ContextCompat.getColor(context, R.color.green))
                .setContentTitle(title != null && !title.isEmpty() ? title : context.getString(R.string.app_name))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(open);
        if (image != null) {
            b.setLargeIcon(image).setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image)
                    .setSummaryText(body));
        }
        try {
            nm.notify(id, b.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS revoked between the check and the call
        }
    }
}
