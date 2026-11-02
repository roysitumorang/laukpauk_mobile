package id.laukpauk.data.repository;

import android.util.Base64;

import androidx.lifecycle.LiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.DetailProdukPenjual;
import id.laukpauk.data.model.seller.products.KategoriPilihan;
import id.laukpauk.data.model.seller.products.ProdukPenjual;
import id.laukpauk.data.model.seller.products.ProdukPenjualResponses;
import id.laukpauk.data.model.seller.products.SatuanProdukData;
import id.laukpauk.data.remote.ProdukPenjualApi;
import retrofit2.Call;

/**
 * Seller product management (ListProdukPenjual, DetailProdukPenjual, TambahProduk, UpdateHarga):
 * products, paket, units/prices, categories. Write actions report {@code true} when the server's status flag is set.
 */
public class ProdukPenjualRepository extends BaseRepository {

    public static final String JENIS_SATUAN = "satuan";
    public static final String JENIS_PAKET = "paket";

    private static final String PRODUK_ENTITIES = "id, nama, foto, namaKategoriProduk, keterangan";
    private static final String UPDATE_HARGA_ENTITIES = "id, foto, nama, namaKategoriProduk";
    private static final String SATUAN_ENTITIES = "id, nama, harga, diskon, jenisHarga, keterangan, idProduk";

    private final ProdukPenjualApi api;

    public ProdukPenjualRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(ProdukPenjualApi.class);
    }

    public static boolean isSatuan(String jenisProduk) {
        return jenisProduk == null || JENIS_SATUAN.equalsIgnoreCase(jenisProduk);
    }

    // ------------------------------------------------------------------ reads

    /** Detail of a product or paket (SUCCESS with null data when it doesn't exist). */
    public LiveData<Resource<DetailProdukPenjual>> getDetail(String id, String jenisProduk) {
        if (isSatuan(jenisProduk)) {
            return load(api.getHargaProduk(id, null), body -> body == null ? null : body.detailProduk);
        }
        return load(api.getDetailPaket(id), body -> body == null ? null : body.detailPaket);
    }

    /** Same detail, via a one-shot callback (TambahProduk edit mode uses Produk/detailProduk). */
    public void getDetailForEdit(String id, String jenisProduk, ResultCallback<DetailProdukPenjual> callback) {
        if (isSatuan(jenisProduk)) {
            enqueue(api.getDetailProduk(id), body -> body == null ? null : body.detailProduk, callback);
        } else {
            enqueue(api.getDetailPaket(id), body -> body == null ? null : body.detailPaket, callback);
        }
    }

    public LiveData<Resource<List<KategoriPilihan>>> getKategoriProduk() {
        return load(api.getKategoriProduk(), body -> body == null || body.kategoriProduk == null
                ? Collections.<KategoriPilihan>emptyList() : body.kategoriProduk);
    }

    /** The seller's single products (the "Produk Satuan" tab). */
    public LiveData<Resource<List<ProdukPenjual>>> getProdukPenjual() {
        return load(api.getProdukPenjual(authToken(), PRODUK_ENTITIES, null), ProdukPenjualRepository::listOf);
    }

    /** The seller's paket (the "Produk Paketan" tab). */
    public LiveData<Resource<List<ProdukPenjual>>> getPaketPenjual() {
        return load(api.getPaketPenjual(authToken()), body -> body == null || body.paketPenjual == null
                ? Collections.<ProdukPenjual>emptyList() : body.paketPenjual);
    }

    /** UpdateHarga search; an empty term gives an empty list without a request (legacy behaviour). */
    public void searchProduk(String term, ResultCallback<List<ProdukPenjual>> callback) {
        if (term == null || term.isEmpty()) {
            callback.onResult(Resource.success(Collections.<ProdukPenjual>emptyList()));
            return;
        }
        enqueue(api.getProdukPenjual(authToken(), UPDATE_HARGA_ENTITIES, term), ProdukPenjualRepository::listOf,
                callback);
    }

    /** Units/prices of a product. */
    public void getSatuanProduk(String idProduk, ResultCallback<List<SatuanProdukData>> callback) {
        enqueue(api.getHargaProduk(idProduk, SATUAN_ENTITIES), body -> body == null || body.hargaProduk == null
                ? Collections.<SatuanProdukData>emptyList() : body.hargaProduk, callback);
    }

    // ------------------------------------------------------------------ actions

    public void toggleProdukPalingLaris(String id, String jenisProduk, ResultCallback<Boolean> callback) {
        if (isSatuan(jenisProduk)) {
            enqueue(api.toggleProdukPalingLaris(id, authToken()),
                    body -> body != null && body.statusToggleProdukPalingLaris, callback);
        } else {
            enqueue(api.togglePaketPalingLaris(id, authToken()),
                    body -> body != null && body.statusTogglePaketPalingLaris, callback);
        }
    }

    public void ubahKategoriProduk(String idProduk, String idKategori, ResultCallback<Boolean> callback) {
        enqueue(api.ubahKategoriProduk(idProduk, authToken(), idKategori),
                body -> body != null && body.statusUbahKategoriProduk, callback);
    }

    public void ubahDeskripsi(String id, String jenisProduk, String deskripsi, ResultCallback<Boolean> callback) {
        if (isSatuan(jenisProduk)) {
            enqueue(api.ubahDeskripsiProduk(id, authToken(), deskripsi),
                    body -> body != null && body.statusUbahDeskripsiProduk, callback);
        } else {
            enqueue(api.ubahDeskripsiPaket(id, authToken(), deskripsi),
                    body -> body != null && body.statusUbahDeskripsiPaket, callback);
        }
    }

    public void hapusProduk(String id, ResultCallback<Boolean> callback) {
        enqueue(api.hapusProduk(id, authToken()), body -> body != null && body.statusHapusProduk, callback);
    }

    public void hapusPaket(String id, ResultCallback<Boolean> callback) {
        enqueue(api.hapusPaket(id, authToken()), body -> body != null && body.statusHapusPaket, callback);
    }

    public void hapusSatuan(String idSatuanProduk, ResultCallback<Boolean> callback) {
        enqueue(api.hapusHargaProduk(idSatuanProduk, authToken()),
                body -> body != null && body.statusHapusHargaProduk, callback);
    }

    /** Creates ({@code idSatuan} null) or updates a unit/price. */
    public void saveSatuan(String idSatuan, String idProduk, String nama, String harga, String diskon,
                           String jenisHarga, String keterangan, ResultCallback<Boolean> callback) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("idProduk", idProduk);
        fields.put("nama", nama);
        fields.put("harga", harga);
        fields.put("diskon", diskon);
        fields.put("jenisHarga", jenisHarga);
        fields.put("keterangan", keterangan);
        Call<ProdukPenjualResponses.Status> call = idSatuan != null
                ? api.saveHargaProduk(idSatuan, authToken(), fields)
                : api.saveHargaProduk(authToken(), fields);
        enqueue(call, body -> body != null && body.statusSaveHargaProduk, callback);
    }

    /**
     * Saves a product or paket ({@code idProduk} null/empty = new). {@code jenis} is "satuan" or "paket";
     * {@code kategori} is used for satuan, {@code harga}/{@code diskon} for paket. The image (optional) is read
     * off the main thread and sent base64-encoded like the legacy app did.
     */
    public void saveProduk(String idProduk, String jenis, String nama, String keterangan, String kategori,
                           String harga, String diskon, File image, ResultCallback<Boolean> callback) {
        final Map<String, String> fields = new LinkedHashMap<>();
        fields.put("nama", nama);
        fields.put("jenisProduk", jenis);
        fields.put("keterangan", keterangan);
        if ("satuan".equals(jenis)) {
            fields.put("kategori", kategori);
        } else {
            fields.put("harga", harga);
            fields.put("diskon", diskon);
        }
        final String id = idProduk == null || idProduk.isEmpty() ? null : idProduk;
        if (image == null) {
            send(id, fields, callback);
            return;
        }
        executors.diskIO().execute(() -> {
            byte[] bytes = readBytes(image);
            executors.mainThread().execute(() -> {
                if (bytes == null) {
                    callback.onResult(Resource.success(false)); // legacy: readAsBytes() failed -> false
                    return;
                }
                fields.put(jenis.equalsIgnoreCase("satuan") ? "produkImg" : "paketImg",
                        Base64.encodeToString(bytes, Base64.NO_WRAP));
                send(id, fields, callback);
            });
        });
    }

    private void send(String id, Map<String, String> fields, ResultCallback<Boolean> callback) {
        Call<ProdukPenjualResponses.Status> call = id != null
                ? api.saveProduk(id, authToken(), fields)
                : api.saveProduk(authToken(), fields);
        enqueue(call, body -> body != null && body.saveProduk, callback);
    }

    private static List<ProdukPenjual> listOf(ProdukPenjualResponses.ProdukList body) {
        return body == null || body.produkPenjual == null ? Collections.<ProdukPenjual>emptyList()
                : body.produkPenjual;
    }

    private static byte[] readBytes(File file) {
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
