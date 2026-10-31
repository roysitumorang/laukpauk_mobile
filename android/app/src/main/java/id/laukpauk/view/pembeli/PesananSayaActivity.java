package id.laukpauk.view.pembeli;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Calendar;

import id.laukpauk.R;
import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;
import id.laukpauk.view.pembeli.components.pesanansaya.PesananSayaItem;

/**
 * Port of lib/views/pembeli/PesananSaya.dart (Footer tab "Pesanan").
 * Args: statusTransaksi (int, optional; initial tab 0..5).
 */
public class PesananSayaActivity extends BaseActivity {
    private static final String[] TAB_LABELS = {"Hari Ini", "Belum di Proses", "Dalam Proses", "Dalam Perjalanan",
            "Completed", "Dibatalkan"};
    private static final String[] DD_LABELS = {"Semua Status Transaksi", "Belum di Proses", "Sedang di proses",
            "Sedang dalam perjalanan", "Sudah diterima", "Dibatalkan"};
    private static final String[] DD_VALUES = {"", "pending", "processing", "otw", "received", "rejected"};

    private int statusTransaksiState = 0;
    private int reload = 0;
    private String statusTransaksiHariIni = "";

    private String tanggalAwal = "";
    private String tanggalAkhir = "";

    private FrameLayout bodyHost;
    private LinearLayout tabRow;
    private FrameLayout filterHolder;
    private FrameLayout contentFrame;
    private int requestSeq = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Json.has(args(), "statusTransaksi")) {
            statusTransaksiState = Json.intVal(args(), "statusTransaksi");
        }

        // FutureBuilder(AuthModel.isLogin()) -> Scaffold(body: Loading())
        setRoot(Loading.view(this));
        async(AuthModel::isLogin, isLogin -> {
            if (isLogin) {
                buildScreen();
                load(true);
            } else {
                // Login(routeTo: PesananSaya()) embedded in the Scaffold -> the Login screen with a routeTo.
                Nav.pushReplacement(this, LoginActivity.class,
                        Json.of("routeToClass", PesananSayaActivity.class.getName(),
                                "routeToArgs", new JsonObject()));
            }
        });
    }

    // ------------------------------------------------------------------ screen

    private View appBar() {
        if (!isTaskRoot()) {
            return DetailUi.appBar(this, "Riwayat Transaksi");
        }
        // AppBar without a back button (the route cannot be popped).
        LinearLayout bar = Ui.column(this);
        bar.setBackgroundColor(CustomColors.green);
        bar.setElevation(Ui.dp(this, 4));
        Layouts.tintStatusBar(this, CustomColors.green);
        TextView title = Ui.text(this, "Riwayat Transaksi", 20f, CustomColors.white);
        title.setSingleLine(true);
        title.setGravity(Gravity.CENTER);
        bar.addView(title, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 56)));
        return bar;
    }

    private void buildScreen() {
        float hb = Ui.hb(this);
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.white);
        root.addView(appBar(), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        bodyHost = new FrameLayout(this);
        root.addView(bodyHost, Ui.weightH(1f));

        // Padding(h 5, v 5) > Column
        LinearLayout column = Ui.column(this);
        Ui.padXY(column, 5, 5);
        bodyHost.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        // Card(elevation 5, radius 10) > Padding(h 15, v 10) > Column(TabBar, filter)
        LinearLayout card = TransaksiUi.card(this, 15, 10);

        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        tabRow = Ui.row(this);
        hs.addView(tabRow, new ViewGroup.LayoutParams(Ui.WRAP, Ui.WRAP));
        LinearLayout tabPad = Ui.column(this);
        Ui.padXY(tabPad, 10, 0);
        tabPad.addView(hs, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(tabPad, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        buildTabs();

        filterHolder = new FrameLayout(this);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        flp.topMargin = Ui.dp(this, 10);
        card.addView(filterHolder, flp);
        buildFilter();

        column.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        contentFrame = new FrameLayout(this);
        column.addView(contentFrame, Ui.weightH(1f));

        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
    }

    // ------------------------------------------------------------------ tabs

    private void buildTabs() {
        tabRow.removeAllViews();
        float hb = Ui.hb(this);
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int index = i;
            boolean selected = i == statusTransaksiState;
            LinearLayout tab = Ui.column(this);
            TextView label = Ui.text(this, TAB_LABELS[i], hb * 3.25f,
                    selected ? CustomColors.green : CustomColors.textMuted);
            label.setGravity(Gravity.CENTER);
            Ui.padXY(label, 16, 0);
            tab.addView(label, new LinearLayout.LayoutParams(Ui.WRAP, 0, 1f));
            View indicator = new View(this);
            indicator.setBackgroundColor(selected ? CustomColors.green : CustomColors.transparent);
            tab.addView(indicator, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 2)));
            Layouts.ripple(tab);
            tab.setOnClickListener(v -> {
                statusTransaksiState = index;
                buildTabs();
                buildFilter();
                load(true);
            });
            tabRow.addView(tab, new LinearLayout.LayoutParams(Ui.WRAP, Ui.dp(this, 48)));
        }
    }

    // ------------------------------------------------------------------ filter

    private void buildFilter() {
        float hb = Ui.hb(this);
        filterHolder.removeAllViews();
        LinearLayout box = Ui.row(this);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setBackground(Ui.shape(this, 0x5566CC99, 10f));
        Ui.pad(box, 15, 0, 15, 0);

        if (statusTransaksiState == 0) {
            final Spinner spinner = new Spinner(this);
            spinner.setBackground(null);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, DD_LABELS) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView tv = (TextView) super.getView(position, convertView, parent);
                    tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, hb * 3.25f);
                    tv.setTextColor(TransaksiUi.BLACK87);
                    tv.setPadding(0, 0, 0, 0);
                    return tv;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                    tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, hb * 3.25f);
                    tv.setTextColor(TransaksiUi.BLACK87);
                    return tv;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            int sel = 0;
            for (int i = 0; i < DD_VALUES.length; i++) {
                if (DD_VALUES[i].equals(statusTransaksiHariIni)) {
                    sel = i;
                }
            }
            spinner.setSelection(sel, false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newValue = DD_VALUES[position];
                    if (!newValue.equals(statusTransaksiHariIni)) {
                        statusTransaksiHariIni = newValue;
                        load(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
            box.addView(spinner, slp);
            box.addView(Ui.icon(this, R.drawable.ic_arrow_drop_down, 24, CustomColors.textMuted));
        } else {
            box.addView(dateField(true, "Tanggal Awal"), new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f));
            box.addView(dateField(false, "Tanggal Akhir"), new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f));
            boolean isRentangWaktuNull = true;
            if (tanggalAwal.length() >= 1 && tanggalAkhir.length() >= 1) {
                isRentangWaktuNull = false;
            }
            if (!isRentangWaktuNull) {
                View close = Ui.icon(this, R.drawable.ic_close, hb * 5.5f, CustomColors.textMuted);
                Ui.onTap(close, () -> {
                    tanggalAwal = "";
                    tanggalAkhir = "";
                    buildFilter();
                    load(true);
                });
                box.addView(close);
            }
        }
        filterHolder.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private EditText dateField(final boolean awal, String hint) {
        EditText et = new EditText(this);
        et.setBackground(null);
        et.setSingleLine(true);
        et.setHint(hint);
        et.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, Ui.hb(this) * 3.25f);
        et.setTextColor(TransaksiUi.BLACK87);
        et.setText(awal ? tanggalAwal : tanggalAkhir);
        et.setFocusable(false);
        et.setOnClickListener(v -> showDatePicker(awal));
        return et;
    }

    /** showDatePicker(initialDate: now, firstDate: 2017, lastDate: 2050) then 'yyyy-MM-d' (day not padded). */
    private void showDatePicker(final boolean awal) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, dayOfMonth) -> {
            int bulan = month + 1;
            String tahunString = String.valueOf(year);
            String bulanString = (bulan <= 9) ? "0" + bulan : String.valueOf(bulan);
            String tanggalString = String.valueOf(dayOfMonth);
            String formatted = tahunString + "-" + bulanString + "-" + tanggalString;
            if (awal) {
                tanggalAwal = formatted;
            } else {
                tanggalAkhir = formatted;
            }
            buildFilter();
            load(true);
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        Calendar min = Calendar.getInstance();
        min.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar max = Calendar.getInstance();
        max.set(2050, Calendar.JANUARY, 1, 0, 0, 0);
        dialog.getDatePicker().setMinDate(min.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());
        dialog.show();
    }

    // ------------------------------------------------------------------ data

    private String statusTransaksiString() {
        String s = null;
        if (statusTransaksiState == 1) {
            s = "pending";
        }
        if (statusTransaksiState == 2) {
            s = "processing";
        }
        if (statusTransaksiState == 3) {
            s = "otw";
        }
        if (statusTransaksiState == 4) {
            s = "received";
        }
        if (statusTransaksiState == 5) {
            s = "rejected";
        }
        return s;
    }

    /** BLOCKING: GET /User/getRiwayatTransaksi. */
    private static JsonArray getRiwayatTransaksi(String statusTransaksi, String statusTransaksiHariIni,
                                                 String tglAwal, String tglAkhir) {
        String statusTransaksiQS = (statusTransaksi == null) ? "" : "statusTransaksi=" + statusTransaksi;
        String statusTransaksiHariIniQS = (statusTransaksi == null && !statusTransaksiHariIni.equals(""))
                ? "&statusTransaksiHariIni=" + statusTransaksiHariIni : "";

        String rentangWaktuQS = "";
        if (statusTransaksi != null) {
            String rentangWaktuAwal = (tglAwal.length() >= 1) ? "&rentangAwal=" + tglAwal : "";
            String rentangWaktuAkhir = (tglAkhir.length() >= 1) ? "&rentangAkhir=" + tglAkhir : "";
            rentangWaktuQS = rentangWaktuAwal + rentangWaktuAkhir;
        }

        String params = HTTPRequest.getQueryString(
                statusTransaksiQS + statusTransaksiHariIniQS + rentangWaktuQS, true);

        Http.Response httpResponse = Http.getApi("/User/getRiwayatTransaksi?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "riwayatTransaksi");
        }

        return new JsonArray();
    }

    /** @param showLoading true = replace the list with a spinner while fetching (tab / filter change). */
    private void load(boolean showLoading) {
        if (showLoading) {
            contentFrame.removeAllViews();
            contentFrame.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
        final int seq = ++requestSeq;
        final String st = statusTransaksiString();
        final String hariIni = statusTransaksiHariIni;
        final String awal = tanggalAwal;
        final String akhir = tanggalAkhir;
        async(() -> getRiwayatTransaksi(st, hariIni, awal, akhir), list -> {
            if (seq != requestSeq) {
                return;
            }
            showList(list);
        });
    }

    private void showList(JsonArray riwayatTransaksi) {
        contentFrame.removeAllViews();
        final Runnable onRefresh = () -> load(false);
        SwipeRefreshLayout refresh;
        if (riwayatTransaksi.size() >= 1) {
            LinearLayout list = Ui.column(this);
            for (int i = 0; i < riwayatTransaksi.size(); i++) {
                JsonObject riwayat = Json.objAt(riwayatTransaksi, i);
                list.addView(new PesananSayaItem(this, riwayat, statusPesananDiterima -> {
                    String message = "Gagal menandai pesanan sebagai SUDAH DITERIMA. Silahkan coba lagi!";
                    if (statusPesananDiterima != null && statusPesananDiterima) {
                        reload++;
                        load(false);
                        message = "Berhasil menanda pesanan sebagai SUDAH DITERIMA !";
                    }
                    DetailUi.snackbar(bodyHost, message, 14f, 3000, null, 0, null);
                }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            final SwipeRefreshLayout[] holder = new SwipeRefreshLayout[1];
            holder[0] = DetailUi.refreshable(this, list, onRefresh, false);
            refresh = holder[0];
        } else {
            LinearLayout empty = Ui.column(this);
            empty.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            Ui.pad(empty, 0, 15, 0, 15);
            empty.addView(Ui.asset(this, "empty.png", Ui.hb(this) * 30));
            empty.addView(Ui.spaceH(this, 10));
            empty.addView(TransaksiUi.hbText(this, "Riwayat Transaksi tidak Ditemukan", 3f, TransaksiUi.BLACK87,
                    false));
            refresh = DetailUi.refreshable(this, empty, onRefresh, true);
        }
        contentFrame.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
