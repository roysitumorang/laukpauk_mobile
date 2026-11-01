package id.laukpauk.model;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResult;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.R;

/**
 * Port of lib/models/ImagePickerModel.dart (image_picker): a chooser sheet (camera / gallery) and the picker itself.
 * Gallery = the system photo picker (no storage permission), camera = ACTION_IMAGE_CAPTURE into a FileProvider file.
 */
public final class ImagePickerModel {
    private ImagePickerModel() {}

    /** Dart ImageSource.camera / ImageSource.gallery. */
    public static final String CAMERA = "camera";
    public static final String GALLERY = "gallery";

    /** Dart: showImagePicker(context) -> ImageSource?. `done` gets CAMERA, GALLERY or null (sheet dismissed). */
    public static void showImagePicker(final BaseActivity activity, Async.Done<String> done) {
        Modal.<String>bottomSheet(activity, true, handle -> {
            LinearLayout column = Ui.column(activity);
            column.addView(tile(activity, R.drawable.ic_camera_alt, "Dari Kamera", () -> handle.close(CAMERA)),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            column.addView(tile(activity, R.drawable.ic_photo_library, "Dari Galery Handphone",
                    () -> handle.close(GALLERY)), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return column;
        }, done);
    }

    /** A ListTile(leading: icon, title: text, onTap). */
    private static View tile(Activity a, int iconRes, String title, final Runnable onTap) {
        LinearLayout row = Ui.row(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        Ui.pad(row, 16, 0, 16, 0);
        row.setMinimumHeight(Ui.dp(a, 56));
        row.addView(Ui.icon(a, iconRes, 24, 0x8A000000));
        row.addView(Ui.spaceW(a, 32));
        row.addView(Ui.text(a, title, new Txt.Style(16f).color(0xDD000000)));
        Layouts.ripple(row, 0x22000000, 0f);
        row.setOnClickListener(v -> onTap.run());
        return row;
    }

    /**
     * Dart: takePicture(context, imageSource) -> XFile?. `done` gets the picked image as a local file (ready for
     * Http.postMultipartApi) or null when cancelled / denied / failed.
     */
    public static void takePicture(final BaseActivity activity, String imageSource, final Async.Done<File> done) {
        try {
            if (imageSource == null) {
                finish(done, null);
            } else if (imageSource.equals(CAMERA)) {
                fromCamera(activity, done);
            } else {
                fromGallery(activity, done);
            }
        } catch (RuntimeException e) {
            finish(done, null);
        }
    }

    private static void finish(Async.Done<File> done, File file) {
        if (done != null) {
            done.run(file);
        }
    }

    private static File imagesDir(Activity a) {
        File dir = new File(a.getCacheDir(), "images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static void fromCamera(final BaseActivity activity, final Async.Done<File> done) {
        activity.askPermissions(new String[]{Manifest.permission.CAMERA}, (Map<String, Boolean> granted) -> {
            Boolean ok = granted == null ? null : granted.get(Manifest.permission.CAMERA);
            if (ok == null || !ok) {
                finish(done, null);
                return;
            }
            try {
                final File target = new File(imagesDir(activity), "IMG_" + System.currentTimeMillis() + ".jpg");
                Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", target);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startForRawResult(intent, (ActivityResult result) -> {
                    if (result != null && result.getResultCode() == Activity.RESULT_OK
                            && target.exists() && target.length() > 0) {
                        finish(done, target);
                    } else {
                        finish(done, null);
                    }
                });
            } catch (RuntimeException e) {
                finish(done, null);
            }
        });
    }

    private static void fromGallery(final BaseActivity activity, final Async.Done<File> done) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        activity.startForRawResult(intent, (ActivityResult result) -> {
            if (result == null || result.getResultCode() != Activity.RESULT_OK || result.getData() == null
                    || result.getData().getData() == null) {
                finish(done, null);
                return;
            }
            final Uri uri = result.getData().getData();
            Async.run(() -> copyToCache(activity, uri), (File file) -> finish(done, file));
        });
    }

    /** BLOCKING. Copies the picked content Uri into cacheDir/images (the upload code needs a File). */
    private static File copyToCache(Activity a, Uri uri) throws java.io.IOException {
        ContentResolver resolver = a.getContentResolver();
        String type = resolver.getType(uri);
        String extension = (type != null && type.equals("image/png")) ? ".png" : ".jpg";
        File target = new File(imagesDir(a), "PICK_" + System.currentTimeMillis() + extension);
        try (InputStream in = resolver.openInputStream(uri); OutputStream out = new FileOutputStream(target)) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return target;
    }
}
