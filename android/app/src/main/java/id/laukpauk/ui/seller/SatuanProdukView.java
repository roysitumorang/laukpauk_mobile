package id.laukpauk.ui.seller;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import id.laukpauk.R;
import id.laukpauk.data.model.seller.products.SatuanProdukData;
import id.laukpauk.data.repository.ProdukPenjualRepository;
import id.laukpauk.databinding.ViewSellerProdukSatuanBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.ui.common.Messages;

/**
 * SatuanProduk.dart: "Daftar Satuan Produk" with the list of a product's units/prices, "Tambah Harga Baru", and
 * (optionally) edit / delete per row. Call {@link #bind} once; it loads its own list.
 * Editing opens EditSatuanProdukFragment through the {@link Host}, which must forward the result with
 * {@link #onEditResult}.
 */
public class SatuanProdukView extends LinearLayout {

    /** The screen that owns the views: opens the edit screen and remembers who asked for the result. */
    public interface Host {
        void startEdit(SatuanProdukView view, boolean add, Bundle args);
    }

    private final ViewSellerProdukSatuanBinding binding;
    private Fragment fragment;
    private ProdukPenjualRepository repository;
    private String idProduk;
    private Host host;
    private SatuanProdukAdapter adapter;
    private int token;

    public SatuanProdukView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        binding = ViewSellerProdukSatuanBinding.inflate(LayoutInflater.from(context), this);
    }

    public void bind(Fragment fragment, String idProduk, boolean showNomor, boolean updateAble,
                     boolean showOptions, Host host) {
        this.fragment = fragment;
        this.repository = AppContainer.from(getContext()).produkPenjualRepository;
        this.idProduk = idProduk;
        this.host = host;
        adapter = new SatuanProdukAdapter(showNomor, updateAble, showOptions, new SatuanProdukAdapter.Listener() {
            @Override
            public void onEdit(SatuanProdukData item) {
                Bundle args = new Bundle();
                args.putString("dataSatuan", AppContainer.from(getContext()).apiClient.toJson(item, SatuanProdukData.class));
                args.putString("idProduk", item.idProduk);
                args.putBoolean("isUpdate", true);
                SatuanProdukView.this.host.startEdit(SatuanProdukView.this, false, args);
            }

            @Override
            public void onHapus(SatuanProdukData item) {
                hapus(item);
            }
        });
        binding.list.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.list.setAdapter(adapter);
        binding.tambahHarga.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("idProduk", SatuanProdukView.this.idProduk);
            args.putString("appBarTitle", getContext().getString(R.string.seller_produk_satuan_baru_title));
            args.putBoolean("isUpdate", false);
            SatuanProdukView.this.host.startEdit(SatuanProdukView.this, true, args);
        });
        reload();
    }

    /** Result of the edit screen when the view survived: reload on success, otherwise the failure alert. */
    public void onEditResult(boolean add, boolean success) {
        if (success) {
            reload();
        } else {
            alertEditFailure(fragment, add);
        }
    }

    /** The legacy alert after a failed add / update of a unit. */
    public static void alertEditFailure(Fragment fragment, boolean add) {
        if (fragment == null || !fragment.isAdded()) {
            return;
        }
        Messages.info(fragment,
                fragment.getString(add ? R.string.seller_produk_tambah_satuan_title
                        : R.string.seller_produk_update_satuan_title),
                fragment.getString(add ? R.string.seller_produk_tambah_satuan_gagal
                        : R.string.seller_produk_update_satuan_gagal), null);
    }

    public void reload() {
        final int mine = ++token;
        if (adapter.getItemCount() == 0) {
            binding.progress.setVisibility(View.VISIBLE);
            binding.empty.setVisibility(View.GONE);
        }
        repository.getSatuanProduk(idProduk, r -> {
            if (mine != token || fragment == null || !fragment.isAdded()) {
                return;
            }
            binding.progress.setVisibility(View.GONE);
            List<SatuanProdukData> list = r.isSuccess() ? r.data : null;
            adapter.submitList(list);
            binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void hapus(SatuanProdukData item) {
        Messages.confirm(fragment, getContext().getString(R.string.seller_produk_konfirmasi_satuan_title),
                getContext().getString(R.string.seller_produk_konfirmasi_satuan_msg, item.nama),
                getContext().getString(R.string.seller_produk_ya), () -> repository.hapusSatuan(item.id, r -> {
                    if (!fragment.isAdded()) {
                        return;
                    }
                    if (r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                        reload();
                    } else {
                        Messages.info(fragment, getContext().getString(R.string.seller_produk_hapus_satuan_title),
                                getContext().getString(R.string.seller_produk_hapus_satuan_gagal), null);
                    }
                }));
    }
}
