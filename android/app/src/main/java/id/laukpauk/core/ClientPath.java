package id.laukpauk.core;

/**
 * Port of lib/libraries/ClientPath.dart. Flutter bundled images as `assets/img/...`;
 * in this project they live in app/src/main/assets/img/..., read through Ui.asset(...)
 * with paths relative to that folder, e.g. Ui.asset(ctx, "laukpauk-icon.png", 50).
 */
public final class ClientPath {
    private ClientPath() {}

    /** Dart: ClientPath.assetsImg ('assets/img'). Android asset folder name. */
    public static final String assetsImg = "img";
}
