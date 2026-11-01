package id.laukpauk.view.pembeli.components.profilsaya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ImagePickerModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.AccountUi;

/** Port of lib/views/pembeli/components/ProfilSaya/GantiFotoProfil.dart. */
public class GantiFotoProfil extends LinearLayout {
    private final BaseActivity activity;
    private final JsonObject dataUser;
    private final Async.Done<Boolean> gantiFotoProfilCallback;

    private File fotoProfilBaruData;
    private boolean isButtonBusyState = false;

    private ImageView photo;
    private LinearLayout saveHolder;

    public GantiFotoProfil(Context ctx, JsonElement detailDataUser, Async.Done<Boolean> gantiFotoProfilCallback) {
        super(ctx);
        setOrientation(VERTICAL);
        this.activity = (BaseActivity) ctx;
        this.dataUser = Json.obj(detailDataUser);
        this.gantiFotoProfilCallback = gantiFotoProfilCallback;
        build();
    }

    private boolean saveFotoProfil() {
        String params = HTTPRequest.getQueryString(null, true);

        Map<String, String> dataFotoProfil = new LinkedHashMap<>();
        dataFotoProfil.put("fotoProfil", AccountUi.fileToBase64(fotoProfilBaruData));

        Map<String, String> body = HTTPRequest.postBody(dataFotoProfil);

        Http.Response httpResponse = Http.postApi("/User/updateFotoProfil?" + params, body);
        if (httpResponse.statusCode == 200) {
            return Json.bool(httpResponse.jsonObject(), "saveFotoProfil");
        }
        return false;
    }

    private String currentPhotoUrl() {
        return ServerPath.baseURL + "/" + ServerPath.uploadGambarUser + "/" + Json.str(dataUser, "foto");
    }

    private void build() {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);

        // Card(elevation: 5, radius 10) > Container(padding: 15)
        setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10f));
        setElevation(Ui.dp(ctx, 5));
        Ui.padAll(this, 15);
        setClipChildren(false);

        addView(Ui.text(ctx, "Foto Profil", hb * 4.0f, 0xDD000000, true));
        addView(Ui.spaceH(ctx, 5));
        addView(Ui.text(ctx, "Anda dapat mengubah foto profil anda dengan pilihan di bawah ini",
                Txt.textXS(ctx)));
        addView(Ui.spaceH(ctx, 15));

        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.TOP);
        row.setClipChildren(false);

        float size = hb * 25f;
        FrameLayout stack = new FrameLayout(ctx);
        stack.setClipChildren(false);
        photo = Ui.network(ctx, currentPhotoUrl(), size, size);
        Ui.clipRounded(photo, size);
        stack.addView(photo, new FrameLayout.LayoutParams(Ui.dp(ctx, size), Ui.dp(ctx, size), Gravity.TOP | Gravity.START));

        FrameLayout camera = new FrameLayout(ctx);
        camera.setBackground(Ui.circle(CustomColors.green));
        camera.addView(Ui.icon(ctx, R.drawable.ic_camera_alt, 20, CustomColors.nearlyWhite),
                new FrameLayout.LayoutParams(Ui.dp(ctx, 20), Ui.dp(ctx, 20), Gravity.CENTER));
        FrameLayout.LayoutParams cameraLp = new FrameLayout.LayoutParams(Ui.dp(ctx, 45), Ui.dp(ctx, 45),
                Gravity.BOTTOM | Gravity.END);
        // Positioned(right: -7.5, bottom: -7.5): the stack is made 7.5dp larger instead
        stack.addView(camera, cameraLp);
        camera.setOnClickListener(v -> pickPhoto());
        row.addView(stack, new LayoutParams(Ui.dp(ctx, size + 7.5f), Ui.dp(ctx, size + 7.5f)));

        row.addView(Ui.spaceW(ctx, 10));

        LinearLayout info = Ui.column(ctx);
        info.addView(Ui.text(ctx, Json.str(dataUser, "nama").toUpperCase(Locale.ROOT),
                new Txt.Style(hb * 4.25f).color(CustomColors.green).bold()));
        info.addView(Ui.spaceH(ctx, 2));
        info.addView(Ui.text(ctx, Json.str(dataUser, "kodeReferal"),
                new Txt.Style(hb * 3.5f).color(CustomColors.textInfo).bold()));
        info.addView(Ui.spaceH(ctx, 7.5f));
        info.addView(Ui.text(ctx, "Bergabung pada " + Json.str(dataUser, "createdAt").toUpperCase(Locale.ROOT),
                Txt.textXS(ctx).color(CustomColors.textMuted)));
        info.addView(Ui.spaceH(ctx, 1.5f));
        info.addView(Ui.text(ctx, "Terakhir kali memperbaharui profil pada "
                + Json.str(dataUser, "createdAt").toUpperCase(Locale.ROOT),
                Txt.textXS(ctx).color(CustomColors.textMuted)));
        row.addView(info, Ui.weightW(1f));

        addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));

        saveHolder = Ui.column(ctx);
        addView(saveHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
        renderSaveButton();
    }

    private void pickPhoto() {
        ImagePickerModel.showImagePicker(activity, imageSource -> {
            ImagePickerModel.takePicture(activity, imageSource, pickedFile -> {
                if (pickedFile != null) {
                    fotoProfilBaruData = pickedFile;
                    Bitmap bmp = BitmapFactory.decodeFile(pickedFile.getPath());
                    if (bmp != null) {
                        photo.setImageBitmap(bmp);
                    }
                    renderSaveButton();
                }
            });
        });
    }

    private void renderSaveButton() {
        Context ctx = getContext();
        saveHolder.removeAllViews();
        if (fotoProfilBaruData == null) {
            return;
        }
        LinearLayout pad = Ui.column(ctx);
        Ui.pad(pad, 0, 15, 0, 0);
        CustomButton.Btn button = CustomButton.button(ctx, "Simpan Perubahan Foto Profil", () -> {
            isButtonBusyState = true;
            renderSaveButton();

            activity.async(this::saveFotoProfil, statusGantiFotoProfil -> {
                boolean ok = statusGantiFotoProfil != null && statusGantiFotoProfil;
                File fotoProfilBaruDataLocalVariable = fotoProfilBaruData;
                if (ok) {
                    fotoProfilBaruDataLocalVariable = null;
                    gantiFotoProfilCallback.run(true);
                }
                isButtonBusyState = false;
                fotoProfilBaruData = fotoProfilBaruDataLocalVariable;
                renderSaveButton();
            });
        }, new CustomButton.Opts().color(CustomButton.themeColor).outline().block().busy(isButtonBusyState));
        button.setBusy(isButtonBusyState);
        pad.addView(button, new LayoutParams(Ui.MATCH, Ui.WRAP));
        saveHolder.addView(pad, new LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
