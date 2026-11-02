package id.laukpauk.core;

import android.app.Application;
import android.content.Context;

import id.laukpauk.LaukpaukApp;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.BannerModel;
import id.laukpauk.model.CheckoutModel;
import id.laukpauk.model.GiftModel;
import id.laukpauk.model.GiftTransaksiModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.HistoriPencarianModel;
import id.laukpauk.model.HistoriPenjualanModel;
import id.laukpauk.model.HomepageModel;
import id.laukpauk.model.ImagePickerModel;
import id.laukpauk.model.KategoriProdukModel;
import id.laukpauk.model.KodePromoModel;
import id.laukpauk.model.KontenModel;
import id.laukpauk.model.MainModel;
import id.laukpauk.model.NotifikasiModel;
import id.laukpauk.model.PaketModel;
import id.laukpauk.model.PasarModel;
import id.laukpauk.model.PaymentChannelModel;
import id.laukpauk.model.PembayaranModel;
import id.laukpauk.model.PesananSayaModel;
import id.laukpauk.model.PointModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.model.ProdukPenjualModel;
import id.laukpauk.model.ProfilSayaModel;
import id.laukpauk.model.PromoModel;
import id.laukpauk.model.ProsesPesananModel;
import id.laukpauk.model.ROModel;
import id.laukpauk.model.ResepModel;
import id.laukpauk.model.SettingModel;
import id.laukpauk.model.SplashScreenModel;
import id.laukpauk.model.TambahProdukModel;
import id.laukpauk.model.TokoSayaModel;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.model.UpdateHargaModel;
import id.laukpauk.model.UserModel;
import id.laukpauk.model.VoucherModel;
import id.laukpauk.model.XenditModel;

/**
 * Composition root (manual dependency injection).
 *
 * Every service and model is a singleton created exactly once, here, in dependency order, with its
 * dependencies passed to its constructor. Nothing else creates them and nothing looks them up statically.
 * Two constructor cycles (AppState <-> HeaderModel, HeaderModel <-> TransaksiModel) are broken with
 * {@link Lazy}: the dependency is resolved on first use, after the container is fully built.
 *
 * Entry points that Android instantiates reach the container through their Context:
 *   - Activities: BaseActivity#container()
 *   - Views, dialogs, helpers: AppContainer.from(context)
 * and pass the objects they need further down through constructors.
 */
public final class AppContainer {

    public final Application application;
    public final Http http;
    public final Prefs prefs;
    public final LocalStorage localStorage;
    public final AuthModel authModel;
    public final HTTPRequest httpRequest;
    public final Async async;
    public final AppState appState;
    public final ADJDModel adjdModel;
    public final BannerModel bannerModel;
    public final CheckoutModel checkoutModel;
    public final GiftModel giftModel;
    public final GiftTransaksiModel giftTransaksiModel;
    public final NotifikasiModel notifikasiModel;
    public final HeaderModel headerModel;
    public final HistoriPencarianModel historiPencarianModel;
    public final HistoriPenjualanModel historiPenjualanModel;
    public final HomepageModel homepageModel;
    public final ImagePickerModel imagePickerModel;
    public final KategoriProdukModel kategoriProdukModel;
    public final KodePromoModel kodePromoModel;
    public final KontenModel kontenModel;
    public final TransaksiModel transaksiModel;
    public final MainModel mainModel;
    public final PaketModel paketModel;
    public final PasarModel pasarModel;
    public final PaymentChannelModel paymentChannelModel;
    public final PembayaranModel pembayaranModel;
    public final PesananSayaModel pesananSayaModel;
    public final PointModel pointModel;
    public final SettingModel settingModel;
    public final ProdukModel produkModel;
    public final ProdukPenjualModel produkPenjualModel;
    public final ProfilSayaModel profilSayaModel;
    public final PromoModel promoModel;
    public final ProsesPesananModel prosesPesananModel;
    public final ROModel roModel;
    public final ResepModel resepModel;
    public final UserModel userModel;
    public final SplashScreenModel splashScreenModel;
    public final TambahProdukModel tambahProdukModel;
    public final TokoSayaModel tokoSayaModel;
    public final UpdateHargaModel updateHargaModel;
    public final VoucherModel voucherModel;
    public final XenditModel xenditModel;

    public AppContainer(Application application) {
        this.application = application;
        final Context context = application;
        this.http = new Http();
        this.prefs = new Prefs(context);
        this.localStorage = new LocalStorage(context, this.http, this.prefs);
        this.authModel = new AuthModel(this.http, this.localStorage);
        this.httpRequest = new HTTPRequest(this.authModel, this.localStorage);
        this.async = new Async();
        this.appState = new AppState(this.async, this.authModel, this.localStorage, this::lazyHeaderModel);
        this.adjdModel = new ADJDModel(this.async, this.http, this.localStorage, this.prefs);
        this.bannerModel = new BannerModel(this.http);
        this.checkoutModel = new CheckoutModel(this.async, this.httpRequest, this.http, this.localStorage, this.prefs);
        this.giftModel = new GiftModel(this.httpRequest, this.http);
        this.giftTransaksiModel = new GiftTransaksiModel(this.httpRequest, this.http, this.localStorage);
        this.notifikasiModel = new NotifikasiModel(this.authModel, this.httpRequest, this.http);
        this.headerModel = new HeaderModel(this.appState, this.async, this.httpRequest, this.http, this.localStorage, this.prefs, this.adjdModel, this.checkoutModel, this.giftModel, this.notifikasiModel, this::lazyTransaksiModel);
        this.historiPencarianModel = new HistoriPencarianModel(this.prefs);
        this.historiPenjualanModel = new HistoriPenjualanModel(this.httpRequest, this.http);
        this.homepageModel = new HomepageModel(this.httpRequest, this.http);
        this.imagePickerModel = new ImagePickerModel(this.async);
        this.kategoriProdukModel = new KategoriProdukModel(this.httpRequest, this.http);
        this.kodePromoModel = new KodePromoModel(this.httpRequest, this.http);
        this.kontenModel = new KontenModel(this.httpRequest, this.http);
        this.transaksiModel = new TransaksiModel(this.appState, this.async, this.httpRequest, this.http, this.localStorage, this.adjdModel, this.checkoutModel, this.headerModel);
        this.mainModel = new MainModel(this.prefs, this.headerModel, this.transaksiModel);
        this.paketModel = new PaketModel(this.httpRequest, this.http, this.localStorage);
        this.pasarModel = new PasarModel(this.httpRequest, this.http, this.localStorage);
        this.paymentChannelModel = new PaymentChannelModel(this.httpRequest, this.http);
        this.pembayaranModel = new PembayaranModel(this.httpRequest, this.http, this.localStorage);
        this.pesananSayaModel = new PesananSayaModel(this.httpRequest, this.http);
        this.pointModel = new PointModel(this.httpRequest, this.http);
        this.settingModel = new SettingModel(this.httpRequest, this.http);
        this.produkModel = new ProdukModel(this.appState, this.async, this.authModel, this.httpRequest, this.http, this.localStorage, this.headerModel, this.settingModel);
        this.produkPenjualModel = new ProdukPenjualModel(this.httpRequest, this.http);
        this.profilSayaModel = new ProfilSayaModel(this.httpRequest, this.http);
        this.promoModel = new PromoModel(this.httpRequest, this.http, this.localStorage);
        this.prosesPesananModel = new ProsesPesananModel(this.httpRequest, this.http);
        this.roModel = new ROModel(this.httpRequest, this.http, this.localStorage);
        this.resepModel = new ResepModel(this.httpRequest, this.http);
        this.userModel = new UserModel(this.httpRequest, this.http);
        this.splashScreenModel = new SplashScreenModel(this.prefs, this.userModel);
        this.tambahProdukModel = new TambahProdukModel(this.httpRequest, this.http);
        this.tokoSayaModel = new TokoSayaModel(this.httpRequest, this.http);
        this.updateHargaModel = new UpdateHargaModel(this.httpRequest, this.http);
        this.voucherModel = new VoucherModel(this.httpRequest, this.http);
        this.xenditModel = new XenditModel(this.async);
    }

    // Lazy<T> targets for the two constructor cycles: read through a method, so they resolve on first use
    // (after the constructor has finished) instead of while the fields are still unassigned.
    private HeaderModel lazyHeaderModel() {
        return headerModel;
    }

    private TransaksiModel lazyTransaksiModel() {
        return transaksiModel;
    }

    /** The container of the running app, from any Context (Activity, View, Dialog, Application). */
    public static AppContainer from(Context context) {
        return ((LaukpaukApp) context.getApplicationContext()).container();
    }
}
