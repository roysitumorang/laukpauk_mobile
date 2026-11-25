package id.laukpauk.util;

import id.laukpauk.BuildConfig;

/**
 * Port of lib/libraries/ServerPath.dart (which is git-ignored in the Flutter repo).
 * Values come from laukpauk.properties through BuildConfig - see laukpauk.properties.example.
 *
 * Image URLs are built as: baseURL + "/" + uploadGambarX + "/" + fileName
 */
public final class ServerPath {
    private ServerPath() {}

    public static final String apiBaseURL = BuildConfig.API_BASE_URL;
    public static final String baseURL = BuildConfig.BASE_URL;

    public static final String uploadImageProductCategory = BuildConfig.UPLOAD_IMAGE_PRODUCT_CATEGORY;
    public static final String uploadImageContentItem = BuildConfig.UPLOAD_IMAGE_CONTENT_ITEM;
    public static final String uploadImagePack = BuildConfig.UPLOAD_IMAGE_PACK;
    public static final String uploadImageProduct = BuildConfig.UPLOAD_IMAGE_PRODUCT;
    public static final String uploadImagePushNotification = BuildConfig.UPLOAD_IMAGE_PUSH_NOTIFICATION;
    public static final String uploadImageUser = BuildConfig.UPLOAD_IMAGE_USER;
    public static final String uploadImageVoucher = BuildConfig.UPLOAD_IMAGE_VOUCHER;
}
