package id.laukpauk.push;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import id.laukpauk.di.AppContainer;

/**
 * Receives Firebase Cloud Messaging events.
 * <ul>
 *   <li>Messages with a {@code notification} payload are shown by the system while the app is in the
 *       background; tapping one opens MainActivity with the {@code data} payload as extras.</li>
 *   <li>Everything else (data-only messages, and any message while the app is in the foreground) is shown
 *       here with the same tap behaviour.</li>
 * </ul>
 * Either way the notification bell badge is refreshed.
 */
public class PushMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PushMessagingService";

    @Override
    public void onNewToken(@NonNull String token) {
        AppContainer.from(this).pushRepository.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Map<String, String> data = new HashMap<>(message.getData());
        RemoteMessage.Notification n = message.getNotification();
        String title = n != null && n.getTitle() != null ? n.getTitle() : first(data, "title", "judul");
        String body = n != null && n.getBody() != null ? n.getBody() : first(data, "body", "pesan", "isi");
        String imageUrl = n != null && n.getImageUrl() != null ? n.getImageUrl().toString() : first(data, "image", "foto");

        PushNotifications.show(this, notificationId(message), title, body, loadImage(imageUrl), data);

        final AppContainer c = AppContainer.from(this);
        new Handler(Looper.getMainLooper()).post(c.notifikasiRepository::refreshUnreadCount);
    }

    private static int notificationId(RemoteMessage message) {
        String id = message.getMessageId();
        return id != null ? id.hashCode() : (int) (System.currentTimeMillis() & 0x7fffffff);
    }

    @Nullable
    private Bitmap loadImage(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            // onMessageReceived runs on a background thread and may take a few seconds
            return Glide.with(getApplicationContext()).asBitmap().load(url).submit().get(8, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.w(TAG, "notification image " + url, e);
            return null;
        }
    }

    @Nullable
    private static String first(Map<String, String> data, String... keys) {
        for (String k : keys) {
            String v = data.get(k);
            if (v != null && !v.isEmpty()) {
                return v;
            }
        }
        return null;
    }
}
