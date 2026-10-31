package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Json;
import id.laukpauk.model.NotifikasiModel;
import id.laukpauk.model.UserModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.notifikasi.NotifikasiGlobal;
import id.laukpauk.view.pembeli.components.notifikasi.NotifikasiKhusus;
import id.laukpauk.view.pembeli.components.notifikasi.NotifikasiTab;

/** Port of lib/views/pembeli/Notifikasi.dart. */
public class NotifikasiActivity extends BaseActivity {
    private int jumlahNotifikasiGlobal = 0;
    private int jumlahNotifikasiKhusus = 0;
    private String jenisNotifikasi = "update";

    private String idUser; // null until the active user has been loaded
    private boolean userLoaded = false;
    private boolean unauthenticated = false;

    private FrameLayout bodyHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        AccountUi.requireLogin(this, NotifikasiActivity.class, new JsonObject(), true, this::start);
    }

    private void start() {
        LinearLayout root = Ui.column(this);
        bodyHolder = new FrameLayout(this);
        root.addView(bodyHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        renderBody();
        initialization();

        async(UserModel::getDetailActiveUser, detail -> {
            userLoaded = (detail != null);
            if (detail != null) {
                if (Json.isFalse(detail)) {
                    unauthenticated = true;
                } else {
                    idUser = Json.str(Json.obj(detail), "id");
                }
            }
            renderBody();
        });
    }

    private void initialization() {
        async(() -> new int[]{
                NotifikasiModel.getJumlahNotifikasi("unread"),
                NotifikasiModel.getJumlahNotifikasiKhusus("unread")
        }, jumlah -> {
            if (jumlah == null) {
                return;
            }
            jumlahNotifikasiGlobal = jumlah[0];
            jumlahNotifikasiKhusus = jumlah[1];
            // the counts are only handed to NotifikasiTab, which does not display them
        });
    }

    private void renderBody() {
        bodyHolder.removeAllViews();
        if (!userLoaded) {
            bodyHolder.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }
        if (unauthenticated) {
            bodyHolder.addView(DetailUi.defaultText(this, "Anda tidak terautentikasi!"),
                    new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
            return;
        }

        LinearLayout column = Ui.column(this);
        column.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(new NotifikasiTab(this, jenisNotifikasi, jenisNotifikasiSelected -> {
            jenisNotifikasi = jenisNotifikasiSelected;
            renderBody();
        }, jumlahNotifikasiGlobal, jumlahNotifikasiKhusus), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        View content;
        if (jenisNotifikasi.equals("update")) {
            content = new NotifikasiGlobal(this, idUser, this::initialization);
        } else {
            content = new NotifikasiKhusus(this, idUser, this::initialization);
        }
        column.addView(content, Ui.weightH(1f));
        bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
