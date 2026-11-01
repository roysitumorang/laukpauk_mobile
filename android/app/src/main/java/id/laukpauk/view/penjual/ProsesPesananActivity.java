package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.ProsesPesananModel;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.components.prosespesanan.ProsesPesananItem;

/** Port of lib/views/penjual/ProsesPesanan.dart. Args: idTransaksi (String). */
public class ProsesPesananActivity extends BaseActivity {
    private int reload = 0;
    private String idTransaksi;
    private int generation = 0;
    private FrameLayout host;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idTransaksi = Json.str(args(), "idTransaksi");
        render();
    }

    private void snackbar(String text) {
        DetailUi.snackbar(host, text, 14f, 3000L, null, 0, null);
    }

    private void render() {
        final int myGeneration = ++generation;

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(PesananScreenUi.scaffoldBackground);

        host = new FrameLayout(this);
        root.addView(host, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        final FrameLayout content = new FrameLayout(this);
        host.addView(content, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        content.addView(DetailUi.smallSpinner(this, 25), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        async(() -> TransaksiModel.getDetailTransaksi(idTransaksi), (JsonElement detailTransaksi) -> {
            if (myGeneration != generation || detailTransaksi == null || detailTransaksi.isJsonNull()) {
                return; // snapshot.hasData is false: the indicator stays
            }
            content.removeAllViews();
            if (!Json.isFalse(detailTransaksi)) {
                content.addView(found(Json.obj(detailTransaksi), myGeneration),
                        new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            } else {
                content.addView(notFound(), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            }
        });

        setRoot(root);
    }

    private android.view.View notFound() {
        float hb = Ui.hb(this);
        LinearLayout c = Ui.column(this);
        c.setGravity(Gravity.CENTER);
        c.addView(Ui.asset(this, "empty.png", hb * 35.0f));
        c.addView(Ui.spaceH(this, 5.0f));
        c.addView(Ui.text(this, "Transaksi Tidak Ditemukan !", hb * 3.5f, CustomColors.nearlyBlack, true));
        c.addView(Ui.spaceH(this, 2.5f));
        c.addView(Ui.text(this, "Transaksi ini tidak ditemukan dalam sistem Laukpauk", hb * 2.75f,
                CustomColors.textMuted));
        return c;
    }

    private android.view.View found(JsonObject detailTransaksi, final int myGeneration) {
        final float hb = Ui.hb(this);
        String status = Json.str(detailTransaksi, "status");
        String statusLC = status.toLowerCase();
        boolean processing = statusLC.equals("processing");

        LinearLayout column = Ui.column(this);
        column.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, 5));

        if (processing) {
            LinearLayout outer = Ui.column(this);
            Ui.pad(outer, 5, 0, 5, 0);
            column.addView(outer, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            LinearLayout card = PesananScreenUi.card(this);
            Ui.pad(card, 12.5f, 10, 12.5f, 10);
            outer.addView(card);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            LinearLayout texts = Ui.column(this);
            texts.addView(Ui.text(this, "Pesanan Masuk", hb * 4, CustomColors.black, true));
            texts.addView(Ui.spaceH(this, 2.5f));
            texts.addView(Ui.text(this, "Berikut ini adalah pesanan yang masuk ke anda", hb * 3.25f,
                    CustomColors.black));
            row.addView(texts, Ui.weightW(1f));

            row.addView(CustomButton.button(this, "Proses", () -> {
                async(() -> Boolean.valueOf(ProsesPesananModel.prosesPesanan(idTransaksi)),
                        (Boolean statusProsesPesanan) -> {
                            if (statusProsesPesanan != null && statusProsesPesanan) {
                                finish();
                            } else {
                                snackbar("Mohon maaf, pesanan tersebut tidak dapat diproses. Silahkan coba lagi!");
                            }
                        });
            }, new CustomButton.Opts().color(CustomButton.themeColor).pill()));

            card.addView(Ui.spaceH(this, 5.0f));
            card.addView(Ui.text(this,
                    "Secara default produk dianggap ada. Jika produk tidak tersedia, silahkan tekan tombol merah disetiap item pesanan",
                    hb * 2.75f, CustomColors.textInfo));
        } else {
            LinearLayout center = Ui.column(this);
            center.setGravity(Gravity.CENTER);
            center.addView(Ui.asset(this, "pesanan-diterima.png", hb * 30.0f));
            center.addView(Ui.spaceH(this, 5.0f));
            center.addView(Ui.text(this, statusLC.equals("otw") ? "Sedang Dijemput" : "Transaksi Selesai !",
                    hb * 3.5f, CustomColors.nearlyBlack, true));
            center.addView(Ui.spaceH(this, 2.5f));
            center.addView(Ui.text(this,
                    statusLC.equals("otw") ? "Pesanan akan dijemput oleh delivery Laukpauk..."
                            : "Yeay, pesanan sudah diterima oleh pembeli mak ..",
                    hb * 2.75f, CustomColors.textMuted));
            column.addView(center, Ui.weightH(1f));
        }

        if (processing) {
            final FrameLayout listSlot = new FrameLayout(this);
            listSlot.addView(DetailUi.smallSpinner(this, 25), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            column.addView(listSlot, Ui.weightH(1f));
            async(() -> ProsesPesananModel.getPesananMasukItem(idTransaksi), (JsonArray pesanan) -> {
                if (myGeneration != generation || pesanan == null) {
                    return;
                }
                listSlot.removeAllViews();
                if (pesanan.size() >= 1) {
                    LinearLayout list = Ui.column(this);
                    for (JsonElement pesananItem : pesanan) {
                        list.addView(new ProsesPesananItem(this, Json.obj(pesananItem), (Boolean returnedData) -> {
                            if (returnedData != null) {
                                if (returnedData) {
                                    reload++;
                                    render();
                                } else {
                                    snackbar("Mohon maaf, proses pengklaiman produk tidak tersedia gagal dilakukan. Silahkan coba lagi!");
                                }
                            }
                        }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                    }
                    SwipeRefreshLayout refresh = DetailUi.refreshable(this, list, () -> render(), false);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH);
                    lp.setMargins(Ui.dp(this, 10), Ui.dp(this, 5), Ui.dp(this, 10), Ui.dp(this, 5));
                    listSlot.addView(refresh, lp);
                } else {
                    LinearLayout empty = Ui.column(this);
                    empty.setGravity(Gravity.CENTER);
                    Ui.pad(empty, hb * 15.0f, 0, hb * 15.0f, 0);
                    empty.addView(Ui.asset(this, "empty.png", hb * 30.0f));
                    empty.addView(Ui.spaceH(this, 5));
                    TextView t = Ui.text(this, "Tidak ada barang yang dipesan di dalam transaksi ini", 14f,
                            CustomColors.black);
                    t.setGravity(Gravity.CENTER);
                    empty.addView(t, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                    empty.addView(Ui.spaceH(this, 25));
                    empty.addView(CustomButton.button(this, "Kembali", () -> finish(),
                            new CustomButton.Opts().color(CustomButton.secondaryColor).pill()));
                    listSlot.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                }
            });
        }
        return column;
    }
}
