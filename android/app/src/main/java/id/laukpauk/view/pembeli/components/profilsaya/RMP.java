package id.laukpauk.view.pembeli.components.profilsaya;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.SettingModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/ProfilSaya/RMP.dart (Response Menjadi Penjual). */
public class RMP extends LinearLayout {

    public RMP(final Context ctx, JsonObject dataPenjual) {
        super(ctx);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        float hb = Ui.hb(ctx);
        Ui.padXY(this, 15, 15);

        String namaToko = Json.str(dataPenjual, "namaToko");
        String statusApprove = Json.str(dataPenjual, "status");
        String statusApproveLC = statusApprove.toLowerCase();
        boolean pending = statusApproveLC.equals("pending");
        String imgURL = pending ? "RMP_Pending.png" : "RMP_Rejected.png";
        String text = pending
                ? "Harap tunggu sampai admin menyetujui request anda menjadi penjual"
                : "Mohon maaf, permintaan anda menjadi penjual tidak diizinkan oleh admin karena hal tertentu.";

        String statusReason = statusApproveLC.equals("rejected") ? Json.str(dataPenjual, "statusReason") : "";

        Txt.Style textStyle = new Txt.Style(hb * 3.50f)
                .color(pending ? CustomColors.textMuted : CustomColors.textDanger);

        addView(Ui.asset(ctx, imgURL, hb * 30f));
        addView(Ui.spaceH(ctx, 25));
        addView(Ui.text(ctx, "Hai, Penjual " + namaToko,
                new Txt.Style(hb * 5f).color(CustomColors.nearlyBlack).bold()));
        addView(Ui.spaceH(ctx, 10));
        TextView t = Ui.text(ctx, text, textStyle);
        t.setGravity(Gravity.CENTER);
        addView(t, new LayoutParams(Ui.WRAP, Ui.WRAP));
        if (statusApproveLC.equals("rejected")) {
            TextView reason = Ui.text(ctx, statusReason, hb * 3f, CustomColors.textMuted);
            reason.setGravity(Gravity.CENTER);
            LayoutParams lp = new LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.topMargin = Ui.dp(ctx, 5);
            addView(reason, lp);
        }
        addView(Ui.spaceH(ctx, 25));

        final FrameLayout holder = new FrameLayout(ctx);
        addView(holder, new LayoutParams(Ui.WRAP, Ui.WRAP));
        View spinner = id.laukpauk.view.pembeli.DetailUi.smallSpinner(ctx, 15f);
        holder.addView(spinner, new FrameLayout.LayoutParams(Ui.dp(ctx, 15), Ui.dp(ctx, 15), Gravity.CENTER));

        final BaseActivity a = (BaseActivity) ctx;
        a.async(SettingModel::getContactUsWhatsappConfiguration, config -> {
            if (config == null) {
                return; // Dart: snapshot without data keeps the spinner
            }
            holder.removeAllViews();
            if (!Json.isFalse(config)) {
                final String contactUsWhatsapp = Json.str(Json.obj(config), "contactUsWhatsapp");
                holder.addView(CustomButton.button(a, "Tanya Kami Melalui Whatsapp", () -> {
                    try {
                        a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + contactUsWhatsapp)));
                    } catch (ActivityNotFoundException e) {
                        a.toast("Tidak dapat membuka Whatsapp");
                    }
                }, new CustomButton.Opts().color(CustomButton.infoColor).outline().pill()));
            } else {
                LinearLayout column = Ui.column(a);
                column.setGravity(Gravity.CENTER_HORIZONTAL);
                column.addView(Ui.asset(a, "empty.png", hb * 30f));
                column.addView(Ui.spaceH(a, 10));
                column.addView(Ui.text(a,
                        "Mohon maaf, server kami gagal mengambil data nomor telepon yang dapat anda gunakan untuk menghubungi Laukpauk. Silahkan cari informasi di laman web kami.",
                        hb * 3.5f, CustomColors.textDanger));
                column.addView(Ui.spaceH(a, 10));
                holder.addView(column);
            }
        });
    }
}
