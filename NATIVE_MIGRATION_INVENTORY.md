# Native Java migration inventory — Laukpauk Mobile

Branch: migration/native-android-java
Baseline inventory: 212 Dart files under lib/ at commit 7b6ed243ed42f4945deccb50313e6f6d8835a824.

## Status

This is a source-to-target inventory and migration tracker, not a claim that all Dart behavior has been ported. Shared networking/preferences/configuration, several read-side API adapters, basic native navigation, and one native XML screen layout exist. Many Flutter screens and write/payment flows still need complete native equivalents. Keep every Dart file until parity tests pass.

## Source-to-target mapping

| Dart source | Responsibility | Proposed Java/native target | Current status |
|---|---|---|---|
| lib/element-class/Gift.dart | Data object | android/app/src/main/java/id/laukpauk/element/Gift.java | Partial Java POJO exists |
| lib/element-class/GiftTransaksi.dart | Data object | android/app/src/main/java/id/laukpauk/element/GiftTransaksi.java | Partial Java POJO exists |
| lib/element-class/Keranjang.dart | Data object | android/app/src/main/java/id/laukpauk/element/Keranjang.java | Partial Java POJO exists |
| lib/global-components/CustomAlert.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/CustomButton.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/CustomForm.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/Footer.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/Header.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/HeaderSearch.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/HistoriPencarian.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/Loading.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/Pencarian.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/SearchResult.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/global-components/Yuhu.dart | Shared UI component | Native shared view/helper + XML layout where applicable | Full native screen/component conversion pending; feature shell is partial |
| lib/libraries/ClientPath.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/libraries/Currency.dart | Shared utility/infrastructure | android/app/src/main/java/id/laukpauk/RupiahFormatter.java | Native equivalent exists; audit/parity pending |
| lib/libraries/CustomColors.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/libraries/CustomText.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/libraries/DefaultImage.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/libraries/Environment.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/libraries/HTTPRequest.dart | Shared utility/infrastructure | android/app/src/main/java/id/laukpauk/data/NativeApiRepository.java | Native equivalent exists; audit/parity pending |
| lib/libraries/LocalStorage.dart | Shared utility/infrastructure | android/app/src/main/java/id/laukpauk/NativePreferences.java | Native equivalent exists; audit/parity pending |
| lib/libraries/Routing.dart | Shared utility/infrastructure | android/app/src/main/java/id/laukpauk/NativeFeatureActivity.java | Native equivalent exists; audit/parity pending |
| lib/libraries/SizeConfig.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/libraries/TanyaJadwal.dart | Shared utility/infrastructure | Native utility/theme/resource equivalent | Pending native utility/state conversion |
| lib/main.dart | App bootstrap/navigation | android/app/src/main/java/id/laukpauk/MainActivity.java | Native equivalent exists; audit/parity pending |
| lib/models/ADJDModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ADJDModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/AuthModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/AuthModel.java | Pending Java port |
| lib/models/BannerModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/BannerModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/CheckoutModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/CheckoutModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/GiftModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/GiftModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/GiftTransaksiModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/GiftTransaksiModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/HeaderModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/HeaderModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/HistoriPencarianModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/HistoriPencarianModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/HistoriPenjualanModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/HistoriPenjualanModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/HomepageModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/HomepageModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/ImagePickerModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ImagePickerModel.java | Pending Java port |
| lib/models/KategoriProdukModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/KategoriProdukModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/KodePromoModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/KodePromoModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/KontenModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/KontenModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/MainModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/MainModel.java | Pending Java port |
| lib/models/NotifikasiModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/NotifikasiModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/PaketModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PaketModel.java | Pending Java port |
| lib/models/PasarModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PasarModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/PaymentChannelModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PaymentChannelModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/PembayaranModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PembayaranModel.java | Pending Java port |
| lib/models/PesananSayaModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PesananSayaModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/PointModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PointModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/ProdukModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ProdukModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/ProdukPenjualModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ProdukPenjualModel.java | Pending Java port |
| lib/models/ProfilSayaModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ProfilSayaModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/PromoModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/PromoModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/ProsesPesananModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ProsesPesananModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/ROModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ROModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/ResepModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/ResepModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/SettingModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/SettingModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/SplashScreenModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/SplashScreenModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/TambahProdukModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/TambahProdukModel.java | Pending Java port |
| lib/models/TokoSayaModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/TokoSayaModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/TransaksiModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/TransaksiModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/UpdateHargaModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/UpdateHargaModel.java | Pending Java port |
| lib/models/UserModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/UserModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/models/VoucherModel.dart | Data/API model | android/app/src/main/java/id/laukpauk/models/VoucherModel.java | Partial Java API/data port exists; UI/business-flow parity pending |
| lib/view-models/HeaderViewModel.dart | State management | Native ViewModel/repository state | Pending native utility/state conversion |
| lib/views/IntroPage.dart | App screen | android/app/src/main/java/id/laukpauk/buyer/IntroPageActivity.java + res/layout/activity_intropage.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/Penjual.dart | App screen | android/app/src/main/java/id/laukpauk/buyer/PenjualActivity.java + res/layout/activity_penjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/SplashScreen.dart | App screen | android/app/src/main/java/id/laukpauk/buyer/SplashScreenActivity.java + res/layout/activity_splashscreen.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/components/IntroPage/IntroPageIndicator.dart | Reusable UI component | Native custom view/adapter + res/layout/item_intropageindicator.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/components/IntroPage/IntroPageItem.dart | Reusable UI component | Native custom view/adapter + res/layout/item_intropageitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/components/Penjual/PaketPenjual.dart | Reusable UI component | Native custom view/adapter + res/layout/item_paketpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/components/Penjual/PaketPenjualCard.dart | Reusable UI component | Native custom view/adapter + res/layout/item_paketpenjualcard.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/components/Penjual/ProdukPenjual.dart | Reusable UI component | Native custom view/adapter + res/layout/item_produkpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/components/Penjual/ProdukPenjualCard.dart | Reusable UI component | Native custom view/adapter + res/layout/item_produkpenjualcard.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/BuatOrderanBerhasil.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/BuatOrderanBerhasilActivity.java + res/layout/activity_buatorderanberhasil.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Checkout.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/CheckoutActivity.java + res/layout/activity_checkout.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DetailPaket.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DetailPaketActivity.java + res/layout/activity_detailpaket.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DetailProduk.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DetailProdukActivity.java + res/layout/activity_detailproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DetailPromoItem.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DetailPromoItemActivity.java + res/layout/activity_detailpromoitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DetailResep.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DetailResepActivity.java + res/layout/activity_detailresep.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DetailTransaksi.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DetailTransaksiActivity.java + res/layout/activity_detailtransaksi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DetailVoucher.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DetailVoucherActivity.java + res/layout/activity_detailvoucher.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/DoPayment.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/DoPaymentActivity.java + res/layout/activity_dopayment.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/EditKeranjangItem.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/EditKeranjangItemActivity.java + res/layout/activity_editkeranjangitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/HistoriDompet.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/HistoriDompetActivity.java + res/layout/activity_historidompet.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/HistoriPoin.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/HistoriPoinActivity.java + res/layout/activity_historipoin.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/HomePage.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/HomePageActivity.java + res/layout/activity_homepage.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/HubungiKami.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/HubungiKamiActivity.java + res/layout/activity_hubungikami.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Keranjang.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/KeranjangActivity.java + res/layout/activity_keranjang.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/KirimUlangOTP.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/KirimUlangOTPActivity.java + res/layout/activity_kirimulangotp.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/KodeReferal.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/KodeReferalActivity.java + res/layout/activity_kodereferal.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/ListProduk.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/ListProdukActivity.java + res/layout/activity_listproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Login.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/LoginActivity.java + res/layout/activity_login.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/LupaPassword.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/LupaPasswordActivity.java + res/layout/activity_lupapassword.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Notifikasi.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/NotifikasiActivity.java + res/layout/activity_notifikasi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/OTPNotCompleted.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/OTPNotCompletedActivity.java + res/layout/activity_otpnotcompleted.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/OTPRegistrasi.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/OTPRegistrasiActivity.java + res/layout/activity_otpregistrasi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/OTPVerifikasiUser.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/OTPVerifikasiUserActivity.java + res/layout/activity_otpverifikasiuser.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/PesananSaya.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/PesananSayaActivity.java + res/layout/activity_pesanansaya.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/PinGMAPS.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/PinGMAPSActivity.java + res/layout/activity_pingmaps.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/ProdukDiDalamPasar.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/ProdukDiDalamPasarActivity.java + res/layout/activity_produkdidalampasar.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/ProfilSaya.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/ProfilSayaActivity.java + res/layout/activity_profilsaya.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Promo.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/PromoActivity.java + res/layout/activity_promo.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Register.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/RegisterActivity.java + res/layout/activity_register.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/RepeatOrder.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/RepeatOrderActivity.java + res/layout/activity_repeatorder.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/RequestJadiPenjual.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/RequestJadiPenjualActivity.java + res/layout/activity_requestjadipenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Resep.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/ResepActivity.java + res/layout/activity_resep.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/ResponseRPD.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/ResponseRPDActivity.java + res/layout/activity_responserpd.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/SemuaPasar.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/SemuaPasarActivity.java + res/layout/activity_semuapasar.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/TransferBank.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/TransferBankActivity.java + res/layout/activity_transferbank.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/UserAndDashboard.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/UserAndDashboardActivity.java + res/layout/activity_useranddashboard.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/Voucher.dart | Buyer screen | android/app/src/main/java/id/laukpauk/buyer/VoucherActivity.java + res/layout/activity_voucher.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/CheckoutElement.dart | Feature UI component | Native custom view/adapter + res/layout/item_checkoutelement.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/CheckoutGlobalState.dart | Feature UI component | Native custom view/adapter + res/layout/item_checkoutglobalstate.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/CreditCardData.dart | Feature UI component | Native custom view/adapter + res/layout/item_creditcarddata.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/JadwalPengiriman.dart | Feature UI component | Native custom view/adapter + res/layout/item_jadwalpengiriman.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/NomorTelepon.dart | Feature UI component | Native custom view/adapter + res/layout/item_nomortelepon.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/PilihanMetodePembayaran.dart | Feature UI component | Native custom view/adapter + res/layout/item_pilihanmetodepembayaran.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/RingkasanPembayaran.dart | Feature UI component | Native custom view/adapter + res/layout/item_ringkasanpembayaran.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/SubTotal.dart | Feature UI component | Native custom view/adapter + res/layout/item_subtotal.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/UangYangDipersiapkan.dart | Feature UI component | Native custom view/adapter + res/layout/item_uangyangdipersiapkan.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Checkout/VoucherItemCheckout.dart | Feature UI component | Native custom view/adapter + res/layout/item_voucheritemcheckout.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailProduk/PaketPenjualLainnya.dart | Feature UI component | Native custom view/adapter + res/layout/item_paketpenjuallainnya.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailProduk/PilihanSatuanProduk.dart | Feature UI component | Native custom view/adapter + res/layout/item_pilihansatuanproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailProduk/PilihanSatuanProdukItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_pilihansatuanprodukitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailProduk/ProdukPenjualLainnya.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkpenjuallainnya.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailProduk/ShowSatuanProduk.dart | Feature UI component | Native custom view/adapter + res/layout/item_showsatuanproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailPromo/NamaPenjual.dart | Feature UI component | Native custom view/adapter + res/layout/item_namapenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailPromo/NamaProduk.dart | Feature UI component | Native custom view/adapter + res/layout/item_namaproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailPromo/NamaPromo.dart | Feature UI component | Native custom view/adapter + res/layout/item_namapromo.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailTransaksi/GiftTransaksiPembeli.dart | Feature UI component | Native custom view/adapter + res/layout/item_gifttransaksipembeli.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailTransaksi/ItemTransaksi.dart | Feature UI component | Native custom view/adapter + res/layout/item_itemtransaksi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/DetailTransaksi/StatusTransaksi.dart | Feature UI component | Native custom view/adapter + res/layout/item_statustransaksi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/EditKeranjangItem/KeranjangItemInput.dart | Feature UI component | Native custom view/adapter + res/layout/item_keranjangiteminput.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HistoriDompet/HistoriDompetItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_historidompetitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/Artikel.dart | Feature UI component | Native custom view/adapter + res/layout/item_artikel.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/CariGoogleMaps.dart | Feature UI component | Native custom view/adapter + res/layout/item_carigooglemaps.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/GiftAlert.dart | Feature UI component | Native custom view/adapter + res/layout/item_giftalert.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/HeroSlider.dart | Feature UI component | Native custom view/adapter + res/layout/item_heroslider.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/JadwalPengantaran.dart | Feature UI component | Native custom view/adapter + res/layout/item_jadwalpengantaran.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/Kategori.dart | Feature UI component | Native custom view/adapter + res/layout/item_kategori.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/Konten.dart | Feature UI component | Native custom view/adapter + res/layout/item_konten.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/KontenAplikasi.dart | Feature UI component | Native custom view/adapter + res/layout/item_kontenaplikasi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/KontenItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_kontenitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/KontenItemBanner.dart | Feature UI component | Native custom view/adapter + res/layout/item_kontenitembanner.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/Pasar.dart | Feature UI component | Native custom view/adapter + res/layout/item_pasar.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/PasarSekitarSaya.dart | Feature UI component | Native custom view/adapter + res/layout/item_pasarsekitarsaya.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/ProdukPasarSekitar.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkpasarsekitar.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/SemuaKategoriProduk.dart | Feature UI component | Native custom view/adapter + res/layout/item_semuakategoriproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/SemuaKategoriProdukItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_semuakategoriprodukitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/HomePage/WalletAndVoucher.dart | Feature UI component | Native custom view/adapter + res/layout/item_walletandvoucher.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Keranjang/ListKeranjang.dart | Feature UI component | Native custom view/adapter + res/layout/item_listkeranjang.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Keranjang/MyGift.dart | Feature UI component | Native custom view/adapter + res/layout/item_mygift.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Keranjang/PenjualCard.dart | Feature UI component | Native custom view/adapter + res/layout/item_penjualcard.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Keranjang/ProductPenjual.dart | Feature UI component | Native custom view/adapter + res/layout/item_productpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ListProduk/ListPenjualItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_listpenjualitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ListProduk/ListProdukItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_listprodukitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Notifikasi/DetailNotifikasiGlobal.dart | Feature UI component | Native custom view/adapter + res/layout/item_detailnotifikasiglobal.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Notifikasi/NotifikasiGlobal.dart | Feature UI component | Native custom view/adapter + res/layout/item_notifikasiglobal.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Notifikasi/NotifikasiGlobalItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_notifikasiglobalitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Notifikasi/NotifikasiKhusus.dart | Feature UI component | Native custom view/adapter + res/layout/item_notifikasikhusus.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Notifikasi/NotifikasiKhususItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_notifikasikhususitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Notifikasi/NotifikasiTab.dart | Feature UI component | Native custom view/adapter + res/layout/item_notifikasitab.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/OTPVerifikasiUser/ListPasar.dart | Feature UI component | Native custom view/adapter + res/layout/item_listpasar.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/PesananSaya/PesananSayaItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_pesanansayaitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProdukDiDalamPasar/PasarLain.dart | Feature UI component | Native custom view/adapter + res/layout/item_pasarlain.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProdukDiDalamPasar/PasarLainDalamCA.dart | Feature UI component | Native custom view/adapter + res/layout/item_pasarlaindalamca.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProdukDiDalamPasar/PasarLainItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_pasarlainitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProdukDiDalamPasar/PasarLainLuarCA.dart | Feature UI component | Native custom view/adapter + res/layout/item_pasarlainluarca.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProdukDiDalamPasar/ProdukDiDalamPasarItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkdidalampasaritem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProfilSaya/GantiFotoProfil.dart | Feature UI component | Native custom view/adapter + res/layout/item_gantifotoprofil.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProfilSaya/GantiNomorHP.dart | Feature UI component | Native custom view/adapter + res/layout/item_gantinomorhp.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProfilSaya/GantiPassword.dart | Feature UI component | Native custom view/adapter + res/layout/item_gantipassword.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/ProfilSaya/RMP.dart | Feature UI component | Native custom view/adapter + res/layout/item_rmp.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Promo/ProdukPromo.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkpromo.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Promo/PromoItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_promoitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/RepeatOrder/ItemTransaksiRO.dart | Feature UI component | Native custom view/adapter + res/layout/item_itemtransaksiro.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/RepeatOrder/RepeatOrderItemTransaksi.dart | Feature UI component | Native custom view/adapter + res/layout/item_repeatorderitemtransaksi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/RepeatOrder/RepeatOrderTransaksiItemPerPenjual.dart | Feature UI component | Native custom view/adapter + res/layout/item_repeatordertransaksiitemperpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Resep/ResepCard.dart | Feature UI component | Native custom view/adapter + res/layout/item_resepcard.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Resep/ResepItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_resepitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Resep/ResepItemItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_resepitemitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Resep/ResepLangkah.dart | Feature UI component | Native custom view/adapter + res/layout/item_reseplangkah.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/UserAndDashboard/Transaksi.dart | Feature UI component | Native custom view/adapter + res/layout/item_transaksi.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/UserAndDashboard/UserMenu.dart | Feature UI component | Native custom view/adapter + res/layout/item_usermenu.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/pembeli/components/Voucher/VoucherItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_voucheritem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/DetailProdukPenjual.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/DetailProdukPenjualActivity.java + res/layout/activity_detailprodukpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/HistoriPenjualan.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/HistoriPenjualanActivity.java + res/layout/activity_historipenjualan.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/ListProdukPenjual.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/ListProdukPenjualActivity.java + res/layout/activity_listprodukpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/PesananItemTidakTersedia.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/PesananItemTidakTersediaActivity.java + res/layout/activity_pesananitemtidaktersedia.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/PesananMasuk.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/PesananMasukActivity.java + res/layout/activity_pesananmasuk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/ProsesPesanan.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/ProsesPesananActivity.java + res/layout/activity_prosespesanan.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/RequestPencairanDana.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/RequestPencairanDanaActivity.java + res/layout/activity_requestpencairandana.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/TambahProduk.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/TambahProdukActivity.java + res/layout/activity_tambahproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/TokoSaya.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/TokoSayaActivity.java + res/layout/activity_tokosaya.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/UbahNamaToko.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/UbahNamaTokoActivity.java + res/layout/activity_ubahnamatoko.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/UpdateHarga.dart | Seller screen | android/app/src/main/java/id/laukpauk/seller/UpdateHargaActivity.java + res/layout/activity_updateharga.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/HistoriPenjualan/HistoriPenjualanItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_historipenjualanitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/ListProdukPenjual/ListProdukPenjualItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_listprodukpenjualitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/ListProdukPenjual/ProdukPaketPenjual.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkpaketpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/ListProdukPenjual/ProdukPaketPenjualItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkpaketpenjualitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/PesananMasuk/PesananMasukItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_pesananmasukitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/ProsesPesanan/ProsesPesananItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_prosespesananitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/TokoSaya/JamDelivery.dart | Feature UI component | Native custom view/adapter + res/layout/item_jamdelivery.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/TokoSaya/PenjualanDanTransferDana.dart | Feature UI component | Native custom view/adapter + res/layout/item_penjualandantransferdana.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/TokoSaya/ProdukPenjual.dart | Feature UI component | Native custom view/adapter + res/layout/item_produkpenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/TokoSaya/TagihanToko.dart | Feature UI component | Native custom view/adapter + res/layout/item_tagihantoko.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/TokoSaya/TransaksiPenjual.dart | Feature UI component | Native custom view/adapter + res/layout/item_transaksipenjual.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/TokoSaya/UpdateLokasiBerjualan.dart | Feature UI component | Native custom view/adapter + res/layout/item_updatelokasiberjualan.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/UpdateHarga/EditSatuanProduk.dart | Feature UI component | Native custom view/adapter + res/layout/item_editsatuanproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/UpdateHarga/SatuanProduk.dart | Feature UI component | Native custom view/adapter + res/layout/item_satuanproduk.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/UpdateHarga/SatuanProdukItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_satuanprodukitem.xml | Full native screen/component conversion pending; feature shell is partial |
| lib/views/penjual/components/UpdateHarga/UpdateHargaItem.dart | Feature UI component | Native custom view/adapter + res/layout/item_updatehargaitem.xml | Full native screen/component conversion pending; feature shell is partial |

## API and dependency audit highlights

- AuthModel: isLogin calls /Auth/cekToken; isOTPConfirmed calls /Auth/isOTPConfirmed. NativeAuthService is a partial port.
- BannerModel: getBanner calls /Banner/getBanner. Java read adapter exists.
- HomepageModel: getArtikel calls /Artikel/getArtikel; getJumlahVoucher calls /User/getJumlahVoucher; getTotalDompet calls /User/getTotalDompet; market lists use /Pasar/getPasar and /Pasar/getPasarAroundOfThisLatLong. Java adapter is partial.
- HeaderModel: /User/getJumlahCart plus notification counts; delivery-address/schedule dialogs and gift flow remain unported.
- UserModel: /User/getDetailUser and /User/getRiwayatPoint. Java read adapter exists.
- ProdukModel: /Keranjang/saveCart, /Produk/isThisProductInCA, /Produk/isProductSellerAvailable, /Produk/detailProduk, /Produk/getLowestPriceOfThisProduct/{id}. Java read-side subset exists; cart mutation is pending.
- KategoriProdukModel: /KategoriProduk/getKategoriProduk and /Produk/getProductsInThisCategory/{id}. Java adapter exists.
- PasarModel: /Pasar/getProdukInThisPasar/{id}. Java adapter exists.
- PromoModel: /Promo/getPromoAroundOfThisLatLong and /Promo/getPromoItem/{id}. Java adapter exists.
- ResepModel: /Resep/getResep, /Resep/getResepItemFromThisResep/{id}, /Keranjang/belanjaResep. Read-side adapter exists; recipe purchase is pending.
- CheckoutModel: /User/getVoucher, /User/getTotalKeranjang, /User/getTotalOngkir, /User/getJumlahCart, /User/getDiskonKategori and /JadwalDelivery/isJadwalDeliveryOpened. Java read-side subset exists; checkout transaction is pending.
- PembayaranModel: /api-v2/Transaksi/checkout and payment hand-off. Not ported; do not use native scaffold for real payments.
- NotifikasiModel: /User/getNotifikasi, /User/getNotifikasiKhusus, notification-count endpoints, /Notifikasi/getNotifikasi/{id}, /Notifikasi/setAs. Java read-side subset exists; mutation pending.
- TransaksiModel: /Transaksi/getTransaksi/{id}, /Transaksi/getJumlahItemDiantar/{id}, /Transaksi/getLastTransactionID and /Transaksi/belanjaUlang/{id}. Java read-side subset exists; session side effects pending.
- ProsesPesananModel: /Transaksi/prosesPesanan/{id} and /Penjual/getPesananMasukItem/{id}. Java read-side subset exists; order mutations pending.
- ProfilSayaModel: /User/getDetailUser and /User/updateProfil. Java read-side subset exists; profile updates pending.
- PaymentChannelModel: /Xendit/getPaymentChannels. API adapter exists, but native payment SDK/checkout is not integrated.
- PointModel: /Point/getPoint. API adapter exists.
- SettingModel: /Pengaturan/getSettingValue/{key} and WhatsApp contact configuration. API adapter exists.
- TokoSayaModel and HistoriPenjualanModel have only partial read-side adapters; seller updates and actions are pending.
- HTTPRequest adds versionCode, optional authToken, and merges query/body parameters. NativeApiRepository exists; form-encoded POST compatibility still needs endpoint-by-endpoint verification.
- LocalStorage uses SharedPreferences, cart/session keys, saved address/schedule, voucher and verification state. NativePreferences is partial; legacy key-by-key tests are pending.
- ServerPath.dart is absent from the tracked source. ApiConfiguration requires LAUKPAUK_API_BASE_URL; do not guess the API base URL.
- Identified platform dependencies include Google Maps/geolocation/geocoding, image picker, URL launcher/share, OneSignal, Firebase Dynamic Links/Messaging and Xendit. These are not all integrated in native UI yet.
- CI previously failed before Java compilation because the Xendit dependency pulled a CardinalCommerce artifact that returned HTTP 401 from JFrog. The current scaffold temporarily defers that SDK; payment parity is therefore incomplete.

## Completion gates

1. For every row, verify exact methods, dependencies, endpoint parameters, payload encoding, response keys, error behavior, and side effects.
2. Implement native Java models/repositories and native layouts/Activities for every screen/component.
3. Port login/register/OTP, cart mutation, checkout/payment, seller product/order mutations, notifications, maps/location, image upload, push/deep links, and all local persistence.
4. Add tests for JSON mapping, URL/query/form encoding, state migration, currency and business rules; add instrumented buyer/seller flow tests.
5. Run debug and release builds with the configured API and payment dependencies; verify on Android devices.
6. Only then remove Flutter/Dart files and Flutter build configuration in a separate final commit.
