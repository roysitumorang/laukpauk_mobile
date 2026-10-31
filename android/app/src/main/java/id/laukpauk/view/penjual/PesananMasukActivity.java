package id.laukpauk.view.penjual;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Calendar;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.components.pesananmasuk.PesananMasukItem;

/** Port of lib/views/penjual/PesananMasuk.dart. Args: statusTransaksi (int, optional, default 0). */
public class PesananMasukActivity extends BaseActivity {
    private int statusTransaksi = 0;
    private int reload = 0;

    /** Dart: tanggalAwalController.text / tanggalAkhirController.text. */
    private String tanggalAwal = "";
    private String tanggalAkhir = "";
    private int generation = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Json.has(args(), "statusTransaksi") && !Json.isNull(Json.get(args(), "statusTransaksi"))) {
            statusTransaksi = Json.intVal(args(), "statusTransaksi");
        }
        render();
    }

    /** Dart: getPesananMasuk({String? statusTransaksi}). Blocking. */
    private static JsonArray getPesananMasuk(String statusTransaksi) {
        String statusTransaksiQS = "";
        if (statusTransaksi != null) {
            statusTransaksiQS = "statusTransaksi=" + statusTransaksi;
        }
        String params = HTTPRequest.getQueryString(statusTransaksiQS, true);
        Http.Response httpResponse = Http.getApi("/Penjual/getPesananMasuk?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "pesananMasuk");
        }
        return new JsonArray();
    }

    /** Dart: the formatted date: yyyy-MM-d (month padded, day NOT padded). */
    private static String formatTanggal(int year, int month1, int day) {
        String tahunString = String.valueOf(year);
        String bulanString = (month1 <= 9) ? "0" + month1 : String.valueOf(month1);
        String tanggalString = String.valueOf(day);
        return tahunString + "-" + bulanString + "-" + tanggalString;
    }

    private EditText dateField(String hint, String text, final boolean awal) {
        float hb = Ui.hb(this);
        final EditText field = new EditText(this);
        new Txt.Style(hb * 3.25f).color(0xDD000000).apply(field);
        field.setBackground(null);
        field.setHint(hint);
        field.setHintTextColor(0xFF9E9E9E);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setFocusable(false); // onTap opens the date picker
        field.setText(text);
        field.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance(); // selectedDate = DateTime.now() (selectedDateState is never set)
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String formatted = formatTanggal(year, month + 1, dayOfMonth);
                if (awal) {
                    tanggalAwal = formatted;
                } else {
                    tanggalAkhir = formatted;
                }
                field.setText(formatted);
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            Calendar first = Calendar.getInstance();
            first.clear();
            first.set(2017, Calendar.JANUARY, 1);
            Calendar last = Calendar.getInstance();
            last.clear();
            last.set(2050, Calendar.DECEMBER, 31);
            dialog.getDatePicker().setMinDate(first.getTimeInMillis());
            dialog.getDatePicker().setMaxDate(last.getTimeInMillis());
            dialog.show();
        });
        return field;
    }

    private void render() {
        final int myGeneration = ++generation;
        final float hb = Ui.hb(this);

        String statusTransaksiString = null;
        String statusTransaksiStringReadble = null;
        if (statusTransaksi == 0) {
            statusTransaksiString = "processing";
            statusTransaksiStringReadble = "Orderan Baru";
        }
        if (statusTransaksi == 1) {
            statusTransaksiString = "otw";
            statusTransaksiStringReadble = "Sedang Dalam Perjalanan";
        }
        if (statusTransaksi == 2) {
            statusTransaksiString = "received";
            statusTransaksiStringReadble = "Sudah Diterima";
        }
        if (statusTransaksi == 3) {
            statusTransaksiString = "rejected";
            statusTransaksiStringReadble = "Ditolak";
        }

        boolean isRentangWaktuNull = true;
        if (tanggalAwal.length() >= 1 && tanggalAkhir.length() >= 1) {
            isRentangWaktuNull = false;
        }

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(PesananScreenUi.scaffoldBackground);
        root.addView(PesananScreenUi.appBar(this, "Penjualan"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout body = Ui.column(this);
        Ui.pad(body, 5, 5, 5, 5);
        root.addView(body, Ui.weightH(1f));

        LinearLayout card = PesananScreenUi.card(this);
        Ui.pad(card, 15, 10, 15, 10);
        body.addView(card);

        LinearLayout tabBox = Ui.column(this);
        tabBox.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(tabBox, 10, 0, 10, 0);
        card.addView(tabBox, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        tabBox.addView(PesananScreenUi.tabBar(this,
                new String[]{"Orderan Baru", "Dalam Perjalanan", "Completed", "Dibatalkan"}, statusTransaksi,
                hb * 3.25f, (Integer value) -> {
                    statusTransaksi = value;
                    render();
                }));

        if (statusTransaksi >= 2) {
            LinearLayout range = new LinearLayout(this);
            range.setOrientation(LinearLayout.HORIZONTAL);
            range.setGravity(Gravity.CENTER_VERTICAL);
            range.setBackground(Ui.shape(this, 0x5566CC99, 10f));
            Ui.pad(range, 15, 0, 15, 0);
            LinearLayout.LayoutParams rangeLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            rangeLp.topMargin = Ui.dp(this, 10);
            card.addView(range, rangeLp);

            range.addView(dateField("Tanggal Awal", tanggalAwal, true), Ui.weightW(1f));
            range.addView(dateField("Tanggal Akhir", tanggalAkhir, false), Ui.weightW(1f));
            if (!isRentangWaktuNull) {
                ImageView clear = Ui.icon(this, R.drawable.ic_close, hb * 5.5f, CustomColors.textMuted);
                Ui.onTap(clear, () -> {
                    tanggalAwal = "";
                    tanggalAkhir = "";
                    render();
                });
                range.addView(clear);
            }
        }

        final FrameLayout listSlot = new FrameLayout(this);
        FrameLayout.LayoutParams spinnerLp = new FrameLayout.LayoutParams(Ui.dp(this, 15), Ui.dp(this, 15),
                Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        listSlot.addView(DetailUi.smallSpinner(this, 15), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 15),
                Gravity.TOP));
        body.addView(listSlot, Ui.weightH(1f));

        final String status = statusTransaksiString;
        final String readable = String.valueOf(statusTransaksiStringReadble);
        async(() -> getPesananMasuk(status), (JsonArray pesananMasuk) -> {
            if (myGeneration != generation || pesananMasuk == null) {
                return;
            }
            listSlot.removeAllViews();
            if (pesananMasuk.size() >= 1) {
                LinearLayout list = Ui.column(this);
                for (JsonElement riwayat : pesananMasuk) {
                    list.addView(new PesananMasukItem(this, Json.obj(riwayat)),
                            new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
                SwipeRefreshLayout refresh = DetailUi.refreshable(this, list, () -> render(), false);
                listSlot.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            } else {
                LinearLayout empty = Ui.column(this);
                empty.setGravity(Gravity.CENTER);
                Ui.pad(empty, 15, 15, 15, 15);
                empty.addView(Ui.asset(this, "empty.png", hb * 30));
                empty.addView(Ui.spaceH(this, 10));
                android.widget.TextView t = Ui.text(this,
                        "Transaksi dengan status \"" + readable + "\" tidak Ditemukan", hb * 3, CustomColors.black);
                t.setGravity(Gravity.CENTER);
                empty.addView(t, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                SwipeRefreshLayout refresh = DetailUi.refreshable(this, empty, () -> render(), true);
                listSlot.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            }
        });

        setRoot(root);
    }
}
