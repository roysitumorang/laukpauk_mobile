package id.laukpauk.model;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Port of lib/models/PesananSayaModel.dart. */
public final class PesananSayaModel {
    private PesananSayaModel() {}

    /** Material (M2) default text colour of dialog title / content: Colors.black87. */
    private static final int BLACK87 = 0xDD000000;

    /** POST /Transaksi/pesananDiterima/{id} (no form body). BLOCKING (network). */
    public static boolean pesananDiterima(String idTransaksi) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.postApi(
                "/Transaksi/pesananDiterima/" + idTransaksi + "?" + params, null);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "pesananDiterima");
        }

        return false;
    }

    /**
     * Dart: konfirmasii(context, title, content, {noText, yesText}) -> Future&lt;bool?&gt;.
     * A Material AlertDialog with a 'Tidak' (false) and a 'Ya, lanjutkan !' (true) button; the
     * default labels are used when noText / yesText are null. `done` gets true / false, or null when
     * the dialog is dismissed by tapping outside / back.
     */
    public static void konfirmasii(final BaseActivity activity, final String title, final String content,
                                   final String noText, final String yesText, final Async.Done<Boolean> done) {
        Modal.<Boolean>dialog(activity, true, false, new Modal.Content<Boolean>() {
            @Override
            public View build(final Modal.Handle<Boolean> handle) {
                float hb = Ui.hb(activity);

                // AlertDialog: shape radius 4, title padding (24,24,24,0), content padding (24,20,24,24),
                // actions padding 8, insetPadding horizontal 40 (16 from the Modal window + 24 here).
                LinearLayout box = Ui.column(activity);
                box.setBackground(Ui.shape(activity, CustomColors.white, 4f));

                TextView titleView = Ui.text(activity, title, new Txt.Style(hb * 4).color(BLACK87));
                Ui.pad(titleView, 24, 24, 24, 0);
                box.addView(titleView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

                TextView contentView = Ui.text(activity, content, new Txt.Style(hb * 3.5f).color(BLACK87));
                Ui.pad(contentView, 24, 20, 24, 24);
                box.addView(contentView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

                LinearLayout actions = Ui.row(activity);
                actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                Ui.padAll(actions, 8);
                TextView no = CustomAlert.textButton(activity, (noText != null) ? noText : "Tidak",
                        new Runnable() {
                            @Override
                            public void run() {
                                handle.close(Boolean.FALSE);
                            }
                        });
                TextView yes = CustomAlert.textButton(activity, (yesText != null) ? yesText : "Ya, lanjutkan !",
                        new Runnable() {
                            @Override
                            public void run() {
                                handle.close(Boolean.TRUE);
                            }
                        });
                actions.addView(no, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
                LinearLayout.LayoutParams yesLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
                yesLp.leftMargin = Ui.dp(activity, 8);
                actions.addView(yes, yesLp);
                box.addView(actions, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

                FrameLayout holder = new FrameLayout(activity);
                holder.setPadding(Ui.dp(activity, 24), 0, Ui.dp(activity, 24), 0);
                holder.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                return holder;
            }
        }, done);
    }

    /** Dart: konfirmasii(context, title, content) with the default button labels. */
    public static void konfirmasii(BaseActivity activity, String title, String content, Async.Done<Boolean> done) {
        konfirmasii(activity, title, content, null, null, done);
    }

    /** Status text shown to the buyer ('' for an unknown status). */
    public static String readAbleStatusForUser(String status) {
        String statusLC = status.toLowerCase();
        String readAbleStatusForUserVariable = "";
        if (statusLC.equals("pending")) {
            readAbleStatusForUserVariable = "Sudah Diterima Penjual";
        }
        if (statusLC.equals("processing")) {
            readAbleStatusForUserVariable = "Sedang diproses";
        }
        if (statusLC.equals("otw")) {
            readAbleStatusForUserVariable = "Dalam Perjalanan";
        }
        if (statusLC.equals("received")) {
            readAbleStatusForUserVariable = "Diterima";
        }
        if (statusLC.equals("rejected")) {
            readAbleStatusForUserVariable = "Dibatalkan";
        }

        return readAbleStatusForUserVariable;
    }
}
