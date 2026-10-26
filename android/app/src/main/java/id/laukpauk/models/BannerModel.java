package id.laukpauk.models;

import android.content.Context;
import id.laukpauk.NativeApiClient;
import id.laukpauk.data.NativeApiRepository;
import java.util.Map;

/** Native API port of lib/models/BannerModel.dart. Response key: listBanner. */
public final class BannerModel {
    private BannerModel() { }
    public static void getBanner(Context context, NativeApiClient.Callback callback) {
        NativeApiRepository.get(context, "/Banner/getBanner", false, null, callback);
    }
}
