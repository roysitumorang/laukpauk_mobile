package id.laukpauk.ui.common;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import id.laukpauk.databinding.SheetAccountImageSourceBinding;
import id.laukpauk.di.AppContainer;

/**
 * Camera / gallery picker (legacy ImagePickerModel): a small sheet "Dari Kamera" / "Dari Galery Handphone", the
 * system photo picker (no storage permission) or the camera into the FileProvider cache dir with the CAMERA
 * permission. Create it in the fragment's {@code onCreate} (it registers activity-result launchers, which
 * must happen before the fragment is STARTED) and call {@link #show()} from a click.
 */
public final class ImagePicker {

    public interface Callback {
        /** The picked image as a file in the app cache (copied from the content URI). */
        void onPicked(File image);
    }

    private final Fragment fragment;
    private final Callback callback;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickLauncher;
    private final ActivityResultLauncher<Uri> takeLauncher;
    private final ActivityResultLauncher<String> permissionLauncher;

    /** The file the camera is currently writing to. */
    private File cameraTarget;

    public ImagePicker(Fragment fragment, Callback callback) {
        this.fragment = fragment;
        this.callback = callback;
        pickLauncher = fragment.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                this::onGalleryResult);
        takeLauncher = fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            File target = cameraTarget;
            cameraTarget = null;
            if (Boolean.TRUE.equals(ok) && target != null && target.exists() && target.length() > 0) {
                callback.onPicked(target);
            }
        });
        permissionLauncher = fragment.registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (Boolean.TRUE.equals(granted)) {
                        launchCamera();
                    }
                });
    }

    /** Shows the source chooser. */
    public void show() {
        if (!fragment.isAdded()) {
            return;
        }
        final BottomSheetDialog dialog = new BottomSheetDialog(fragment.requireContext());
        SheetAccountImageSourceBinding sheet = SheetAccountImageSourceBinding.inflate(android.view.LayoutInflater.from(fragment.requireContext()));
        sheet.optionCamera.setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });
        sheet.optionGallery.setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });
        dialog.setContentView(sheet.getRoot());
        dialog.show();
    }

    // ------------------------------------------------------------------ camera

    private void openCamera() {
        Context ctx = fragment.requireContext();
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        if (!fragment.isAdded()) {
            return;
        }
        try {
            Context ctx = fragment.requireContext();
            cameraTarget = new File(imagesDir(ctx), "IMG_" + System.currentTimeMillis() + ".jpg");
            Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", cameraTarget);
            takeLauncher.launch(uri);
        } catch (RuntimeException e) {
            cameraTarget = null;
        }
    }

    // ------------------------------------------------------------------ gallery

    private void openGallery() {
        pickLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void onGalleryResult(Uri uri) {
        if (uri == null || !fragment.isAdded()) {
            return;
        }
        final Context ctx = fragment.requireContext().getApplicationContext();
        final AppContainer container = AppContainer.from(ctx);
        container.executors.diskIO().execute(() -> {
            File copy;
            try {
                copy = copyToCache(ctx, uri);
            } catch (IOException e) {
                copy = null;
            }
            final File result = copy;
            if (result != null) {
                container.executors.mainThread().execute(() -> callback.onPicked(result));
            }
        });
    }

    private static File imagesDir(Context ctx) {
        File dir = new File(ctx.getCacheDir(), "images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /** Copies the picked content Uri into cacheDir/images (uploads need a File). Runs on a background thread. */
    private static File copyToCache(Context ctx, Uri uri) throws IOException {
        ContentResolver resolver = ctx.getContentResolver();
        String type = resolver.getType(uri);
        String extension = "image/png".equals(type) ? ".png" : ".jpg";
        File target = new File(imagesDir(ctx), "PICK_" + System.currentTimeMillis() + extension);
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
