package id.laukpauk.ui.seller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import id.laukpauk.R;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.DetailPenjual;
import id.laukpauk.data.model.seller.shop.TokoSayaResponses;
import id.laukpauk.databinding.FragmentTokoSayaBinding;
import id.laukpauk.di.AppContainer;
import id.laukpauk.di.ViewModelFactory;
import id.laukpauk.ui.common.AuthGate;
import id.laukpauk.ui.common.Images;
import id.laukpauk.ui.common.Messages;
import id.laukpauk.ui.common.NavResults;
import id.laukpauk.util.Currency;
import id.laukpauk.util.ServerPath;

/** TokoSaya.dart: the seller dashboard. */
public class TokoSayaFragment extends Fragment {

    /** Result (Boolean) sent by TambahProdukFragment: product saved or not. */
    public static final String RESULT_SAVE_PRODUK = TambahProdukFragment.RESULT_KEY;
    /** Result (Boolean) sent by UbahNamaTokoFormFragment. */
    public static final String RESULT_UBAH_NAMA = "ubah_nama_toko_result";
    /** Result of PinGMAPSFragment: a JSON string {"lat": .., "lng": ..}. */
    public static final String RESULT_PIN = "pin_gmaps_result";

    private FragmentTokoSayaBinding binding;
    private TokoSayaViewModel viewModel;
    private TokoLokasiSheet sheet;
    private DetailPenjual toko;
    private boolean whatsappLoaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTokoSayaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AppContainer c = AppContainer.from(requireContext());
        viewModel = new ViewModelProvider(this,
                ViewModelFactory.of(() -> new TokoSayaViewModel(c.tokoSayaRepository, c.settingRepository)))
                .get(TokoSayaViewModel.class);
        binding.header.bind(this);
        binding.refresh.setOnRefreshListener(viewModel::refresh);
        bindStaticImages();
        bindClicks();
        observeResults();
        AuthGate.require(this, this::observe);
    }

    private NavController nav() {
        return NavHostFragment.findNavController(this);
    }

    private void bindStaticImages() {
        Images.asset(binding.iconPenjualan, "icons/penjualan.png");
        Images.asset(binding.iconTransfer, "icons/transfer-dana.png");
        Images.asset(binding.iconOrderanBaru, "icons/orderan-baru.png");
        Images.asset(binding.iconDelivery, "icons/delivery.png");
        Images.asset(binding.iconCompleted, "icons/completed.png");
        Images.asset(binding.iconHistori, "icons/penjualan.png");
        Images.asset(binding.iconAkun, "user-icon.png");
        Images.asset(binding.notFoundImage, "empty.png");
        Images.asset(binding.rmpErrorImage, "empty.png");
    }

    private void bindClicks() {
        binding.penjualan.setOnClickListener(v -> nav().navigate(R.id.historiPenjualanFragment));
        binding.transferDana.setOnClickListener(v -> nav().navigate(R.id.requestPencairanDanaFragment));
        binding.orderanBaru.setOnClickListener(v -> pesananMasuk(0));
        binding.dalamPerjalanan.setOnClickListener(v -> pesananMasuk(1));
        binding.pesananDiterima.setOnClickListener(v -> pesananMasuk(2));
        binding.historiPenjualan.setOnClickListener(v -> nav().navigate(R.id.historiPenjualanFragment));
        binding.akunSaya.setOnClickListener(v -> SellerShopNav.toAkunSaya(this));
        binding.updateHarga.setOnClickListener(v -> nav().navigate(R.id.updateHargaFragment));
        binding.lihatProduk.setOnClickListener(v -> nav().navigate(R.id.listProdukPenjualFragment));
        binding.tambahProduk.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("jenisProduk", "satuan");
            nav().navigate(R.id.tambahProdukFragment, args);
        });
        binding.ubahNamaToko.setOnClickListener(v -> nav().navigate(R.id.ubahNamaTokoFormFragment));
        binding.namaToko.setOnClickListener(v -> openToko(null));
        binding.lihatToko.setOnClickListener(v -> openToko(null));
        binding.jumlahSatuan.setOnClickListener(v -> openToko("satuan"));
        binding.jumlahPaket.setOnClickListener(v -> openToko("paket"));
        binding.pinLokasi.setOnClickListener(v -> openLokasiSheet());
        bindJadwal(binding.jadwalPagi, 0, R.string.seller_toko_pagi);
        bindJadwal(binding.jadwalSiang, 1, R.string.seller_toko_siang);
        bindJadwal(binding.jadwalSore, 2, R.string.seller_toko_sore);
    }

    private void pesananMasuk(int status) {
        Bundle args = new Bundle();
        args.putInt("statusTransaksi", status);
        nav().navigate(R.id.pesananMasukFragment, args);
    }

    private void openToko(@Nullable String jenisProdukSelected) {
        if (toko == null || toko.id == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("idPenjual", toko.id);
        if (jenisProdukSelected != null) {
            args.putString("jenisProdukSelected", jenisProdukSelected);
        }
        nav().navigate(R.id.penjualFragment, args);
    }

    /** The checkbox only changes once the server accepted the toggle. */
    private void bindJadwal(CheckBox box, int index, int nameRes) {
        box.setOnClickListener(v -> {
            box.setChecked(!box.isChecked());
            viewModel.toggleJadwal(index, result -> {
                if (!isAdded()) {
                    return;
                }
                if (!Boolean.TRUE.equals(result.data)) {
                    Messages.info(this, getString(R.string.seller_toko_jadwal_judul),
                            getString(R.string.seller_toko_jadwal_gagal, getString(nameRes)), null);
                }
            });
        });
    }

    private void observeResults() {
        NavResults.<Boolean>observe(this, RESULT_SAVE_PRODUK, ok -> {
            Messages.snack(this, getString(ok ? R.string.seller_toko_produk_berhasil
                    : R.string.seller_toko_produk_gagal_simpan));
            if (ok) {
                viewModel.refresh();
            }
        });
        NavResults.<Boolean>observe(this, RESULT_UBAH_NAMA, ok -> {
            Messages.snack(this, getString(ok ? R.string.seller_toko_nama_berhasil
                    : R.string.seller_toko_nama_gagal));
            if (ok) {
                viewModel.refresh();
            }
        });
        NavResults.<String>observe(this, RESULT_PIN, this::onPinned);
    }

    private void observe() {
        viewModel.getDetail().observe(getViewLifecycleOwner(), this::renderDetail);
        viewModel.getTotalPenjualan().observe(getViewLifecycleOwner(), res -> {
            boolean done = res != null && !res.isLoading();
            binding.totalPenjualanProgress.setVisibility(done ? View.GONE : View.VISIBLE);
            binding.totalPenjualan.setVisibility(done ? View.VISIBLE : View.GONE);
            if (done) {
                binding.totalPenjualan.setText(Currency.formatRupiah(res.isSuccess() && res.data != null
                        ? res.data : 0, 0));
            }
        });
        viewModel.getTagihan().observe(getViewLifecycleOwner(), res -> {
            boolean done = res != null && !res.isLoading();
            binding.tagihanProgress.setVisibility(done ? View.GONE : View.VISIBLE);
            binding.tagihan.setVisibility(done ? View.VISIBLE : View.GONE);
            if (done) {
                binding.tagihan.setText(Currency.formatRupiah(res.isSuccess() && res.data != null
                        ? res.data : 0, 0));
            }
        });
        viewModel.getJumlah().observe(getViewLifecycleOwner(), res -> {
            if (res == null) {
                return;
            }
            boolean loading = res.isLoading();
            boolean ok = res.isSuccess() && res.data != null;
            binding.jumlahProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.jumlahGagal.setVisibility(!loading && !ok ? View.VISIBLE : View.GONE);
            binding.jumlahRow.setVisibility(ok ? View.VISIBLE : View.GONE);
            if (ok) {
                binding.jumlahSatuan.setText(Currency.formatSatuan(res.data.produk, ""));
                binding.jumlahPaket.setText(Currency.formatSatuan(res.data.paket, ""));
            }
        });
        viewModel.getJadwalAktif().observe(getViewLifecycleOwner(), aktif -> {
            binding.jadwalPagi.setChecked(aktif[0]);
            binding.jadwalSiang.setChecked(aktif[1]);
            binding.jadwalSore.setChecked(aktif[2]);
        });
        viewModel.start();
    }

    private void renderDetail(Resource<DetailPenjual> res) {
        if (res == null) {
            return;
        }
        if (res.isLoading()) {
            binding.progress.setVisibility(binding.refresh.isRefreshing() ? View.GONE : View.VISIBLE);
            return;
        }
        binding.progress.setVisibility(View.GONE);
        binding.refresh.setRefreshing(false);
        toko = res.isSuccess() ? res.data : null;
        binding.content.setVisibility(View.GONE);
        binding.rmp.setVisibility(View.GONE);
        binding.notFound.setVisibility(View.GONE);
        if (toko == null) {
            binding.notFound.setVisibility(View.VISIBLE);
        } else if (!toko.isApproved()) {
            renderRmp(toko);
        } else {
            renderToko(toko);
        }
    }

    private void renderToko(DetailPenjual d) {
        binding.content.setVisibility(View.VISIBLE);
        Images.circle(binding.avatar, Images.server(ServerPath.uploadGambarBannerPenjual,
                "compress/" + d.banner));
        binding.namaToko.setText(d.namaToko);
        boolean adaAlamat = d.alamatToko != null && !d.alamatToko.isEmpty();
        binding.alamatToko.setText(adaAlamat ? d.alamatToko : getString(R.string.seller_toko_alamat_belum));
        binding.alamatToko.setTypeface(null, adaAlamat ? Typeface.NORMAL : Typeface.ITALIC);
        if (d.namaPasar != null) {
            String nama = "0".equals(d.pasarUmum) ? getString(R.string.seller_toko_lokasi_tersendiri) : d.namaPasar;
            binding.lokasi.setText(getString(R.string.seller_toko_berjualan_di, nama));
            binding.lokasi.setVisibility(View.VISIBLE);
        } else {
            binding.lokasi.setVisibility(View.GONE);
        }
    }

    private void renderRmp(DetailPenjual d) {
        binding.rmp.setVisibility(View.VISIBLE);
        boolean pending = "pending".equals(d.status == null ? "" : d.status.toLowerCase());
        boolean rejected = "rejected".equals(d.status == null ? "" : d.status.toLowerCase());
        Images.asset(binding.rmpImage, pending ? "RMP_Pending.png" : "RMP_Rejected.png");
        binding.rmpHai.setText(getString(R.string.seller_toko_hai_penjual, d.namaToko == null ? "" : d.namaToko));
        binding.rmpText.setText(pending ? R.string.seller_toko_rmp_pending : R.string.seller_toko_rmp_rejected);
        binding.rmpText.setTextColor(requireContext().getColor(pending ? R.color.text_muted : R.color.text_danger));
        binding.rmpReason.setVisibility(rejected ? View.VISIBLE : View.GONE);
        binding.rmpReason.setText(d.statusReason);
        if (!whatsappLoaded) {
            whatsappLoaded = true;
            viewModel.loadWhatsapp(result -> {
                if (binding == null || !result.isSuccess()) {
                    whatsappLoaded = false;
                    return; // legacy: no data keeps the spinner
                }
                binding.rmpProgress.setVisibility(View.GONE);
                JsonElement config = result.data;
                if (config != null && config.isJsonObject()) {
                    JsonObject o = config.getAsJsonObject();
                    String nomor = o.has("contactUsWhatsapp") && !o.get("contactUsWhatsapp").isJsonNull()
                            ? o.get("contactUsWhatsapp").getAsString() : "";
                    binding.rmpWhatsapp.setVisibility(View.VISIBLE);
                    binding.rmpWhatsapp.setOnClickListener(v -> {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + nomor)));
                        } catch (ActivityNotFoundException e) {
                            Messages.snack(this, getString(R.string.seller_toko_whatsapp_gagal));
                        }
                    });
                } else {
                    binding.rmpError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // ------------------------------------------------------------------ lokasi berjualan

    private void openLokasiSheet() {
        if (toko == null) {
            return;
        }
        if (sheet == null) {
            sheet = new TokoLokasiSheet(this, viewModel, new TokoLokasiSheet.Host() {
                @Override
                public void pickOnMap() {
                    nav().navigate(R.id.pinGMAPSFragment);
                }

                @Override
                public void confirm(Runnable onFinished) {
                    viewModel.tetapkanLokasi(toko.id, result -> {
                        onFinished.run();
                        if (!isAdded()) {
                            return;
                        }
                        boolean ok = Boolean.TRUE.equals(result.data);
                        sheet.dismiss();
                        viewModel.lokasi.reset();
                        Messages.snack(TokoSayaFragment.this, getString(ok ? R.string.seller_toko_lokasi_berhasil
                                : R.string.seller_toko_lokasi_gagal));
                        if (ok) {
                            viewModel.refresh();
                        }
                    });
                }
            });
        }
        sheet.show();
    }

    /** Result of the pin map: reverse-geocode it, then reopen the sheet with the own location selected. */
    private void onPinned(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            String lat = o.get("lat").getAsString();
            String lng = o.get("lng").getAsString();
            viewModel.reverseGeocoding(lat, lng, result -> {
                if (!isAdded()) {
                    return;
                }
                TokoLokasiState s = viewModel.lokasi;
                s.jenis = TokoLokasiState.LOKASI_TERSENDIRI;
                s.latitude = lat;
                s.longitude = lng;
                s.alamat = result.data;
                openLokasiSheet();
            });
        } catch (RuntimeException e) {
            // not a location: ignore
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sheet != null) {
            sheet.dismiss();
            sheet = null;
        }
        binding = null;
        whatsappLoaded = false;
    }
}
