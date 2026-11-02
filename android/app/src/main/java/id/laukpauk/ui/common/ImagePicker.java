package id.laukpauk.ui.common;

import androidx.fragment.app.Fragment;

import java.io.File;

/**
 * Camera / gallery picker (legacy ImagePickerModel): a small sheet "Kamera" / "Galeri", the system photo
 * picker (no storage permission) or ACTION_IMAGE_CAPTURE into the FileProvider cache dir with the CAMERA
 * permission. Create it in the fragment's {@code onCreate} (it registers activity-result launchers, which
 * must happen before the fragment is STARTED) and call {@link #show()} from a click.
 *
 * PLACEHOLDER (contract only) - implemented by the account slice. Keep these signatures.
 */
public final class ImagePicker {

    public interface Callback {
        /** The picked image as a file in the app cache (copied from the content URI). */
        void onPicked(File image);
    }

    public ImagePicker(Fragment fragment, Callback callback) {
    }

    /** Shows the source chooser. */
    public void show() {
    }
}
