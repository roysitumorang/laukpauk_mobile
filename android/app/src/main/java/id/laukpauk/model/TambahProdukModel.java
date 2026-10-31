package id.laukpauk.model;

import android.net.Uri;
import android.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.Async;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;

/** Port of lib/models/TambahProdukModel.dart. */
public final class TambahProdukModel {
    private TambahProdukModel() {}

    /**
     * Dart: saveProduk(context, {idProduk = '', required Map dataProdukMap}) -> Future&lt;bool&gt;.
     *
     * `dataProdukMap` keys (as built by TambahProduk): nama, jenis ('satuan' | 'paket'),
     * keterangan, imageProdukData and either kategori (satuan) or harga + diskon (paket).
     * Dart held a PickedFile / XFile in `imageProdukData`; here it is the picked image's file
     * path (a JSON string, or an object with a "path" member; a content:// URI also works),
     * or null / absent for "no image". `done` gets the `saveProduk` flag (false on failure).
     */
    public static void saveProduk(final BaseActivity activity, final String idProduk,
                                  final JsonObject dataProdukMap, final Async.Done<Boolean> done) {
        activity.async(() -> {
            String idProdukPath = (idProduk == null) ? "" : idProduk;
            if (!idProdukPath.isEmpty()) {
                idProdukPath = "/" + idProdukPath;
            }

            String nama = Json.str(dataProdukMap, "nama");
            String jenis = Json.str(dataProdukMap, "jenis");
            String jenisLC = jenis.toLowerCase();
            String keterangan = Json.str(dataProdukMap, "keterangan");
            String imageProdukPath = imagePath(Json.get(dataProdukMap, "imageProdukData"));

            Map<String, String> dataProduk = new LinkedHashMap<>();
            dataProduk.put("nama", nama);
            dataProduk.put("jenisProduk", jenis);
            dataProduk.put("keterangan", keterangan);
            if (imageProdukPath != null) {
                byte[] imageProdukDataListInt = readBytes(activity, imageProdukPath);
                if (imageProdukDataListInt == null) {
                    return false; // Dart: readAsBytes() threw
                }
                dataProduk.put(jenisLC.equals("satuan") ? "produkImg" : "paketImg",
                        Base64.encodeToString(imageProdukDataListInt, Base64.NO_WRAP));
            }

            if (jenis.equals("satuan")) {
                String kategori = Json.str(dataProdukMap, "kategori");
                dataProduk.put("kategori", kategori);
            } else {
                String harga = Json.str(dataProdukMap, "harga");
                String diskon = Json.str(dataProdukMap, "diskon");

                dataProduk.put("harga", harga);
                dataProduk.put("diskon", diskon);
            }

            String params = HTTPRequest.getQueryString(null, true);
            Map<String, String> body = HTTPRequest.postBody(dataProduk);

            Http.Response httpResponse = Http.postApi("/Produk/saveProduk" + idProdukPath + "?" + params, body);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.bool(dataObject, "saveProduk");
            }

            return false;
        }, saveProduk -> {
            if (done != null) {
                done.run(saveProduk != null && saveProduk);
            }
        });
    }

    /** The path out of `imageProdukData` (string or {"path": ...}); null when there is no image. */
    private static String imagePath(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        if (e.isJsonObject()) {
            String p = Json.strOrNull(e.getAsJsonObject(), "path");
            return (p == null || p.isEmpty()) ? null : p;
        }
        if (e.isJsonPrimitive()) {
            String p = Json.asStr(e, "");
            return p.isEmpty() ? null : p;
        }
        return null;
    }

    /** XFile.readAsBytes(): the whole file, or null when it cannot be read. */
    private static byte[] readBytes(BaseActivity activity, String path) {
        InputStream in = null;
        try {
            if (path.startsWith("content://")) {
                in = activity.getContentResolver().openInputStream(Uri.parse(path));
            } else {
                in = new FileInputStream(new File(path));
            }
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // nothing to do
                }
            }
        }
    }
}
