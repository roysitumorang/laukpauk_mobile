package id.laukpauk.model;

import android.view.Gravity;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Calendar;
import java.util.List;

import id.laukpauk.core.AppState;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.Prefs;
import id.laukpauk.element.Gift;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.PinGMAPSActivity;
import id.laukpauk.view.pembeli.components.homepage.GiftAlert;

/** Port of lib/models/HeaderModel.dart. */
public final class HeaderModel {
    private HeaderModel() {}

    /** BLOCKING. */
    public static int getJumlahCart() {
        String cartCode = LocalStorage.getCartCode();
        String params = HTTPRequest.getQueryString("cartCode=" + cartCode);

        Http.Response httpResponse = Http.getApi("/User/getJumlahCart?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "jumlahKeranjang");
        }
        return 0;
    }

    /** BLOCKING. */
    public static int getJumlahNotifikasi() {
        int jumlahNotifikasiVariable = NotifikasiModel.getJumlahNotifikasi("unread");
        int jumlahNotifikasiKhususVariable = NotifikasiModel.getJumlahNotifikasiKhusus("unread");
        return jumlahNotifikasiVariable + jumlahNotifikasiKhususVariable;
    }

    /** Dart: showAlamatPengantaran(context, alamatPengantaran) -> bool?. true = "Ubah". */
    public static void showAlamatPengantaran(final BaseActivity activity, String alamatPengantaran,
                                             Async.Done<Boolean> done) {
        String title = "Alamat Pengantaran";
        String subTitle = "Alamat pengantaran yang anda tentukan berada di sekitar : ";

        TextView moreWidgets = Ui.text(activity, alamatPengantaran,
                new Txt.Style(Ui.hb(activity) * 3.25f).color(CustomColors.textMuted));
        moreWidgets.setGravity(Gravity.CENTER);

        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(activity,
                CustomAlert.textButton(activity, "Tutup", () -> handle.close(Boolean.FALSE)),
                CustomAlert.textButton(activity, "Reset", () ->
                        TransaksiModel.removeTransactionSession(activity, v -> handle.close(Boolean.FALSE))),
                CustomAlert.textButton(activity, "Ubah", () -> handle.close(Boolean.TRUE)));
        CustomAlert.<Boolean>textComponent(activity, null, title, subTitle, actions, moreWidgets, false, done);
    }

    /** Mutable locals of Dart's adjd(). */
    private static final class AdjdState {
        String readAbleSelectedLocation;
        String latitude;
        String longitude;
        String kodeJadwalDeliverySelected;
        JsonObject jadwalDeliveryTerpilihMap;
    }

    /**
     * Dart: adjd(context, {callback, nantiSajaCallback, okSajaCallback}) -> {latitude, longitude, kodeJadwalDelivery}.
     * The three callbacks are never invoked by the Dart code either.
     */
    public static void adjd(final BaseActivity activity, Async.Done<JsonObject> callback, Runnable nantiSajaCallback,
                            Runnable okSajaCallback, final Async.Done<JsonObject> done) {
        final AdjdState st = new AdjdState();

        boolean isLocationAndTimeOfDeliverySet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();

        if (!isLocationAndTimeOfDeliverySet) {
            ADJDModel.detectMyLocation(activity, (JsonObject myLocation) -> {
                if (myLocation != null) {
                    st.latitude = Json.strOrNull(myLocation, "latitude");
                    st.longitude = Json.strOrNull(myLocation, "longitude");

                    ADJDModel.askAlamatPengantaran(activity, st.latitude, st.longitude, null, null,
                            (JsonObject diantarKeLokasiSekarangMap) -> {
                        if (diantarKeLokasiSekarangMap != null) {
                            st.readAbleSelectedLocation = Json.strOrNull(diantarKeLokasiSekarangMap, "address");
                            String diantarKeLokasiSekarang = Json.str(diantarKeLokasiSekarangMap, "answer");
                            if (diantarKeLokasiSekarang.equals("tidak")) {
                                Nav.pushForResult(activity, PinGMAPSActivity.class,
                                        Json.of("currentLatitude", st.latitude, "currentLongitude", st.longitude),
                                        (JsonElement returned) -> {
                                    if (returned != null && returned.isJsonObject()) {
                                        JsonObject returnedDataFromPinGMAPS = returned.getAsJsonObject();
                                        st.latitude = Json.asStr(Json.get(returnedDataFromPinGMAPS, "lat"), "null");
                                        st.longitude = Json.asStr(Json.get(returnedDataFromPinGMAPS, "lng"), "null");
                                        st.readAbleSelectedLocation =
                                                Json.strOrNull(returnedDataFromPinGMAPS, "formattedAddress");
                                    }
                                    askJadwal(activity, st, done);
                                });
                            } else {
                                askJadwal(activity, st, done);
                            }
                        } else {
                            setNewADJDAndFinish(activity, st, false, done);
                        }
                    });
                } else {
                    String title = "Laukpauk";
                    String subTitle = "Supaya mamak bisa belanja, aktifkan GPSnya dulu ya mak :)";

                    Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(activity,
                            CustomAlert.textButton(activity, "OK", () -> handle.close()));

                    CustomAlert.<Object>textComponent(activity, null, title, subTitle, actions, null, false,
                            r -> finishAdjd(st, done));
                }
            });
        } else {
            JsonObject getSavedLatitudeAndLongitudeMap = LocalStorage.getSavedLatitudeAndLongitude();

            st.latitude = Json.strOrNull(getSavedLatitudeAndLongitudeMap, "latitude");
            st.longitude = Json.strOrNull(getSavedLatitudeAndLongitudeMap, "longitude");
            st.kodeJadwalDeliverySelected = Json.strOrNull(getSavedLatitudeAndLongitudeMap, "jadwalDeliveryCode");
            finishAdjd(st, done);
        }
    }

    private static void askJadwal(final BaseActivity activity, final AdjdState st, final Async.Done<JsonObject> done) {
        ADJDModel.askJadwalDelivery(activity, st.latitude, st.longitude, st.readAbleSelectedLocation,
                (JsonObject jadwalDeliveryMap) -> {
            boolean adjdSelectionCompleted = false;
            if (jadwalDeliveryMap != null) {
                st.kodeJadwalDeliverySelected = Json.strOrNull(jadwalDeliveryMap, "jadwalDeliverySelected");
                st.jadwalDeliveryTerpilihMap = Json.objectOrNull(jadwalDeliveryMap, "jadwalDeliveryTerpilih");
                if (st.kodeJadwalDeliverySelected != null && st.jadwalDeliveryTerpilihMap != null) {
                    adjdSelectionCompleted = true;
                }
            }
            setNewADJDAndFinish(activity, st, adjdSelectionCompleted, done);
        });
    }

    private static void setNewADJDAndFinish(BaseActivity activity, final AdjdState st, boolean completed,
                                            final Async.Done<JsonObject> done) {
        // jsonEncode(null) is the text "null" in Dart
        String jadwalDeliveryMap = st.jadwalDeliveryTerpilihMap != null
                ? st.jadwalDeliveryTerpilihMap.toString() : "null";
        setNewADJD(activity, completed, st.latitude, st.longitude, st.kodeJadwalDeliverySelected,
                st.readAbleSelectedLocation, jadwalDeliveryMap, v -> finishAdjd(st, done));
    }

    private static void finishAdjd(AdjdState st, Async.Done<JsonObject> done) {
        JsonObject adjdSet = new JsonObject();
        adjdSet.addProperty("latitude", st.latitude);
        adjdSet.addProperty("longitude", st.longitude);
        adjdSet.addProperty("kodeJadwalDelivery", st.kodeJadwalDeliverySelected);
        if (done != null) {
            done.run(adjdSet);
        }
    }

    /**
     * Dart: setNewADJD(context, isADJDSelectionCompleted, {latitude, longitude, jadwalDeliveryCode, alamat,
     * jadwalDeliveryMap}). jadwalDeliveryMap is the JSON text of the chosen schedule.
     */
    public static void setNewADJD(final BaseActivity activity, boolean isADJDSelectionCompleted, String latitude,
                                  String longitude, String jadwalDeliveryCode, String alamat,
                                  String jadwalDeliveryMap, final Async.Done<Void> done) {
        boolean clearKeranjang = isADJDSelectionCompleted;
        boolean isAlamatDeliveryDanJadwalDeliverySet;

        JsonObject coppiedADJD = ADJDModel.getCoppiedADJD();

        String coppiedLatitude = Json.strOrNull(coppiedADJD, "coppiedLatitude");
        String coppiedLongitude = Json.strOrNull(coppiedADJD, "coppiedLongitude");
        String coppiedJadwalDeliveryCode = Json.strOrNull(coppiedADJD, "coppiedJadwalDeliveryCode");
        String coppiedAlamat = Json.strOrNull(coppiedADJD, "coppiedAlamat");
        String coppiedJadwalDeliveryMap = Json.strOrNull(coppiedADJD, "coppiedJadwalDeliveryMap");

        boolean isCoppiedADJDThereIsNotNull = (coppiedLatitude != null && coppiedLongitude != null
                && coppiedJadwalDeliveryCode != null && coppiedAlamat != null && coppiedJadwalDeliveryMap != null);

        if (isADJDSelectionCompleted) {
            isAlamatDeliveryDanJadwalDeliverySet = true;
        } else {
            // cek dulu coppied adjdnya, kalau tidak ada yang null, maka set true, else set false
            isAlamatDeliveryDanJadwalDeliverySet = isCoppiedADJDThereIsNotNull;
        }

        if (!isADJDSelectionCompleted) {
            latitude = coppiedLatitude;
            longitude = coppiedLongitude;
            jadwalDeliveryCode = coppiedJadwalDeliveryCode;
            alamat = coppiedAlamat;
            jadwalDeliveryMap = coppiedJadwalDeliveryMap;
            if (isCoppiedADJDThereIsNotNull) {
                isADJDSelectionCompleted = true;
            }
            // khusus untuk jadwal delivery yang gak lengkap, jadi nanti klo gak lengkap maka dicopy dari adjd yang dicopy sebelum dihapus
        }

        if (isADJDSelectionCompleted) {
            Prefs.setBool("isAlamatDeliveryDanJadwalDeliverySet", isAlamatDeliveryDanJadwalDeliverySet);

            if (latitude != null) {
                Prefs.setString("savedLatitude", latitude);
            }
            if (longitude != null) {
                Prefs.setString("savedLongitude", longitude);
            }
            if (jadwalDeliveryCode != null) {
                Prefs.setString("savedJadwalDeliveryCode", jadwalDeliveryCode);
            }
            if (jadwalDeliveryMap != null) {
                Calendar now = Calendar.getInstance();

                String tahun = String.valueOf(now.get(Calendar.YEAR));
                String bulan = String.valueOf(now.get(Calendar.MONTH) + 1);
                String tanggal = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

                String jam = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
                String menit = String.valueOf(now.get(Calendar.MINUTE));
                String detik = String.valueOf(now.get(Calendar.SECOND));

                String currentDateTime = tahun + "-" + bulan + "-" + tanggal + " " + jam + ":" + menit + ":" + detik;

                JsonObject jadwalDelivery = Json.parseObject(jadwalDeliveryMap);
                jadwalDelivery.addProperty("setOn", currentDateTime);

                jadwalDeliveryMap = jadwalDelivery.toString();

                Prefs.setString("savedJadwalDeliveryMap", jadwalDeliveryMap);
            }
            if (alamat != null) {
                Prefs.setString("savedAlamat", alamat);
            }

            JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();

            AppState state = AppState.get();
            state.setADJDSet(isAlamatDeliveryDanJadwalDeliverySet);
            state.setAlamatDelivery(alamat == null ? "" : alamat);
            state.setSavedADJD(savedADJD);

            if (clearKeranjang) {
                Async.run(() -> CheckoutModel.bersihkanKeranjang(), ok -> {
                    AppState.get().setJumlahCart(0);
                    if (done != null) {
                        done.run(null);
                    }
                });
                return;
            }
        }
        if (done != null) {
            done.run(null);
        }
    }

    /** Dart: makeSureADJD(context): after 3 s asks (once) to confirm the delivery location. */
    public static void makeSureADJD(final BaseActivity activity, final Async.Done<Void> done) {
        Async.mainDelayed(() -> {
            boolean show = showMakeSure();
            if (show && activity.isAlive()) {
                String subTitle = "Mak, pastikan lokasi pengantaran sebelum belanjaannya diantar ya...";
                Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(activity,
                        CustomButton.button(activity, "OK", () -> {
                            Prefs.setBool("showMakeSureADJD", false);

                            handle.close();
                        }, new CustomButton.Opts().color(CustomColors.green)));

                CustomAlert.<Object>textComponent(activity, null, "Laukpauk", subTitle, actions, null, false,
                        r -> {
                            if (done != null) {
                                done.run(null);
                            }
                        });
            } else if (done != null) {
                done.run(null);
            }
        }, 3000);
    }

    /**
     * Dart: _showGift. GiftAlert.show must dismiss its own dialog and then run `onClose`
     * (Dart: GiftAlert(onClose: () { setBool('showAlertGift', false); onClose(); }) with onClose = Navigator.pop).
     */
    private static void showGift(BaseActivity activity, List<Gift> gifts, final Runnable onClose) {
        if (gifts.size() >= 1) {
            GiftAlert.show(activity, gifts, () -> {
                Prefs.setBool("showAlertGift", false);
                onClose.run();
            });
        } else {
            onClose.run();
        }
    }

    /** Dart: giftChecking(context): after 3 s shows the gifts dialog (once). */
    public static void giftChecking(final BaseActivity activity, final Async.Done<Void> done) {
        final Runnable finish = () -> {
            if (done != null) {
                done.run(null);
            }
        };
        Async.mainDelayed(() -> {
            boolean show = showAlertGift();
            if (show && activity.isAlive()) {
                Async.run(() -> GiftModel.getGifts(), (List<Gift> gift) -> {
                    if (gift == null || !activity.isAlive()) {
                        finish.run();
                        return;
                    }
                    showGift(activity, gift, finish);
                });
            } else {
                finish.run();
            }
        }, 3000);
    }

    public static boolean showMakeSure() {
        return Prefs.getBool("showMakeSureADJD", true);
    }

    public static boolean showAlertGift() {
        return Prefs.getBool("showAlertGift", true);
    }

    /** Dart: alertADJDKadaluarsa(context). `done` runs when the dialog is closed. */
    public static void alertADJDKadaluarsa(final BaseActivity activity, Async.Done<Void> done) {
        android.view.View icon = Ui.asset(activity, "laukpauk-icon.png", 50f);
        String subTitle = "Mak, jadwal pengantaran yang mamak pilih sudah kadaluarsa. Keranjang belanja dikosongkan "
                + "dan mamak harus tentukan alamat dan jadwal pengantaran lagi ya mak.....";
        Modal.Content<Void> actions = handle -> CustomAlert.actionsRow(activity,
                CustomButton.button(activity, "OK", () -> handle.close(),
                        new CustomButton.Opts().color(CustomColors.green)));

        CustomAlert.<Void>textComponent(activity, icon, "Jadwal Pengantaran Kadaluarsa", subTitle, actions, null,
                false, done);
    }
}
