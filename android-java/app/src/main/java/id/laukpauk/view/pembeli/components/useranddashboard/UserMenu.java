package id.laukpauk.view.pembeli.components.useranddashboard;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;

import id.laukpauk.R;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.SettingModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.HubungiKamiActivity;
import id.laukpauk.view.pembeli.KodeReferalActivity;

/** Port of lib/views/pembeli/components/UserAndDashboard/UserMenu.dart. */
public class UserMenu extends FrameLayout {
    private final BaseActivity activity;

    public UserMenu(Context ctx) {
        super(ctx);
        this.activity = (BaseActivity) ctx;
        addView(Loading.view(ctx), new LayoutParams(Ui.MATCH, Ui.WRAP));

        activity.async(UserMenu::getNilaiDompetReferral, nilaiDompet -> {
            if (nilaiDompet == null) {
                return;
            }
            removeAllViews();
            addView(build(nilaiDompet), new LayoutParams(Ui.MATCH, Ui.WRAP));
        });
    }

    private static Integer getNilaiDompetReferral() {
        JsonElement settingValue = SettingModel.getSettingValue("referralWallet");
        if (settingValue != null && !Json.isFalse(settingValue)) {
            int nilaiDompetReferral;
            if (settingValue.isJsonPrimitive() && settingValue.getAsJsonPrimitive().isString()) {
                nilaiDompetReferral = Json.parseInt(settingValue.getAsString(), 0);
            } else {
                nilaiDompetReferral = Json.asInt(settingValue, 0);
            }
            return nilaiDompetReferral;
        }

        return 0;
    }

    private View arrow(final Runnable onTap) {
        View icon = Ui.icon(activity, R.drawable.ic_arrow_forward_ios, 20f, 0xDD000000);
        Ui.onTap(icon, onTap);
        return icon;
    }

    private View build(int nilaiDompet) {
        Context ctx = activity;
        float hb = Ui.hb(ctx);
        Txt.Style title = Txt.textSM(ctx).bold().color(CustomColors.grey);
        Txt.Style subTitle = Txt.textXS(ctx);

        LinearLayout box = Ui.column(ctx);
        Ui.padAll(box, 15);
        box.addView(Ui.text(ctx, "Menu Lainnya", Txt.textMD(ctx).bold()));
        box.addView(Ui.spaceH(ctx, 15));

        LinearLayout row1 = Ui.row(ctx);
        LinearLayout kode = Ui.column(ctx);
        kode.addView(Ui.text(ctx, "Kode Referal", title));
        kode.addView(Ui.spaceH(ctx, 3));
        kode.addView(Ui.text(ctx, "Bagikan kode ini kepada siapa saja dan dapatkan uang belanja gratis "
                + Currency.formatRupiah(nilaiDompet, 0), subTitle));
        row1.addView(kode, Ui.weightW(1f));
        row1.addView(Ui.spaceW(ctx, 15));
        row1.addView(arrow(() -> Nav.push(activity, KodeReferalActivity.class)));
        box.addView(row1, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        box.addView(Ui.spaceH(ctx, 10));

        LinearLayout row2 = Ui.row(ctx);
        row2.addView(Ui.text(ctx, "Privasi dan Kebijakan", title), Ui.weightW(1f));
        row2.addView(Ui.spaceW(ctx, 15));
        row2.addView(arrow(this::openKebijakanPrivasi));
        box.addView(row2, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        box.addView(Ui.spaceH(ctx, 10));

        LinearLayout row3 = Ui.row(ctx);
        row3.addView(Ui.text(ctx, "Hubungi Kami", title), Ui.weightW(1f));
        row3.addView(Ui.spaceW(ctx, 15));
        row3.addView(arrow(() -> Nav.push(activity, HubungiKamiActivity.class)));
        box.addView(row3, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        return box;
    }

    private void openKebijakanPrivasi() {
        activity.async(() -> {
            JsonElement v = SettingModel.getSettingValue("kebijakanPrivasiURL");
            return (v == null) ? "" : Json.asStr(v, "");
        }, url -> {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException | SecurityException e) {
                CustomAlert.urlCantBeLaunched(activity, url);
            }
        });
    }
}
