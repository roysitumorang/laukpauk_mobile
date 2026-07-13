import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/global-components/CustomForm.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/models/ADJDModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/KodePromoModel.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/views/pembeli/HomePage.dart';
import 'package:laukpauk/views/pembeli/OTPNotCompleted.dart';

import 'package:laukpauk/views/pembeli/components/Checkout/CheckoutGlobalState.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/SubTotal.dart';
import 'package:laukpauk/models/CheckoutModel.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/JadwalPengiriman.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/RingkasanPembayaran.dart';

import 'package:laukpauk/models/HomepageModel.dart';
import 'package:laukpauk/models/SplashScreenModel.dart';
import 'package:laukpauk/models/SettingModel.dart';
import 'package:laukpauk/models/AuthModel.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/views/pembeli/OTPVerifikasiUser.dart';
import 'package:laukpauk/models/ProfilSayaModel.dart';
import 'package:laukpauk/models/PointModel.dart';

import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:provider/provider.dart';

class Checkout extends StatefulWidget {
  _CheckoutState createState() => _CheckoutState();
}

class _CheckoutState extends State<Checkout> {
  final double _formBorderWidth = .5;
  final Color _formBorderColor = CustomColors.green;
  final TextEditingController _namaPenerimaController = TextEditingController();
  final TextEditingController _nomorHPPenerimaController =
      TextEditingController();
  final TextEditingController _alamatPenerimaController =
      TextEditingController();
  final TextEditingController _catatanTambahanController =
      TextEditingController();

  void dispose() {
    super.dispose();
    _namaPenerimaController.dispose();
    _nomorHPPenerimaController.dispose();
    _alamatPenerimaController.dispose();
    _catatanTambahanController.dispose();
  }

bool? _kodePromoStatusState;

  String cartCode = '';
  String _kodePromoState = ''; 
  String? _kodePromoMessageState;
  int _kodePromoDiskonState = 0;
  Map<String, dynamic>? usedVoucherState, jadwalDeliverySelected;

  Map<String, dynamic>? walletConfigurationState;
  int totalOngkirState = 0, totalBelanjaState = 0, totalDompetSayaState = 0;
  int? jumlahBarangState;
  bool isReadyToShow = false;
  String? penjelasanOngkirState, statusVoucherState;
  Map<String, dynamic>?
      jenisOngkirState; //(bentuknya map karena ada namapasarterjauh, jarakpasarterjauh, banyakpasar dan jenis ongkir)

  bool _isPointUsedState = false;
  int _pointState = 0;
  double _totalDapatPointState = 0;
  int _minimalPointTukarPointState = 0;
  dynamic _diskonKategoriState = false;

  Map<String, dynamic>? _detailUserState;

  void _initializeState() async {
    bool _isLogin = await AuthModel.isLogin();
    if (_isLogin) {
      bool _isUserVerified = await LocalStorage.isUserVerified();
      if (_isUserVerified) {
        bool _hasOTPConfirmed = await SplashScreenModel.hasOTPConfirmed();
        if (_hasOTPConfirmed) {
          bool _jadwalDeliveryIsOpened =
              await CheckoutModel.isJadwalDeliveryOpened();
          if (_jadwalDeliveryIsOpened) {
            await _dataInitialization();
          } else {
            String _title = 'Jadwal Pengantaran';
            String _subTitle =
                'Yah, jadwal pengantarannya sudah ditutup mak! Pilih jadwal pengantaran lainnya yuk ...';
            Widget _actions =
                Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              TextButton(
                  onPressed: () => Navigator.pop(context, true),
                  child: Text('Pilih Jadwal Lain',
                      style: TextStyle(color: CustomColors.textSuccess))),
              TextButton(
                  onPressed: () => Navigator.pop(context, false),
                  child: Text('Lain Kali',
                      style: TextStyle(color: CustomColors.textMuted)))
            ]);
            bool? _konfirmasi = await CustomAlert.textComponent(context,
                title: _title, subTitle: _subTitle, actions: _actions);

            if (_konfirmasi != null && _konfirmasi == true) {
              await ADJDModel.deleteAlamatDanJadwalDelivery();
              Map<String, dynamic> _newADJD = await HeaderModel.adjd(context);

              String? _latitude = _newADJD['latitude'];
              String? _longitude = _newADJD['longitude'];
              String? _kodeJadwalDelivery = _newADJD['kodeJadwalDelivery'];

              if (_latitude != null &&
                  _longitude != null &&
                  _kodeJadwalDelivery != null) {
                Provider.of<HeaderViewModel>(context, listen: false).isADJDSet =
                    true;
                Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (_) => SplashScreen()),
                    (route) => false);
              }
            } else {
              await Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(builder: ((context) => SplashScreen())),
                  (route) => false);
            }
          }
        } else {
          await Navigator.pushAndRemoveUntil(
              context,
              MaterialPageRoute(
                  builder: (_) => OTPNotCompleted(routeTo: Checkout())),
              (route) => false);
        }
      } else {
        await Navigator.pushAndRemoveUntil(
            context,
            MaterialPageRoute(
                builder: (_) => OTPVerifikasiUser(routeTo: Checkout())),
            (route) => false);
      }
    } else {
      await Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (_) => Login(routeTo: Checkout())),
          (route) => false);
    }
  }

  Future<void> _dataInitialization() async {
    int totalBelanja = await CheckoutModel.getTotalKeranjang();

    if (totalBelanja >= 1) {
      Map<String, dynamic> walletConfiguration =
          await CheckoutModel.getWalletConfiguration();
      int totalOngkir = await CheckoutModel.getTotalOngkir();
      // int totalBelanja = 2801;
      int jumlahBarang = await CheckoutModel.getJumlahCart();
      Map<String, dynamic>? jenisOngkir = await CheckoutModel.getJenisOngkir();
      dynamic _diskonKategori = await CheckoutModel.getDiskonKategori();
      String penjelasanOngkir = '';

      int totalDompetSaya = await HomepageModel.getTotalDompet();

      Map<String, dynamic> _detailUser =
          await ProfilSayaModel.getDetailActiveUser(
              select: 'nama, nomorHP, alamat');

      String cartCodeVariable = await LocalStorage.getCartCode();

      if (jenisOngkir != null) {
        if (jenisOngkir['jenisOngkir'] == 'perpasar') {
          if (jenisOngkir['banyakPasar'] >= 2) {
            String tarifOngkirPerPasarString =
                await SettingModel.getSettingValue('costDelivery');
            int tarifOngkirPerPasar = int.parse(tarifOngkirPerPasarString);
            int banyakPasar = jenisOngkir['banyakPasar'];

            penjelasanOngkir = 'Anda dikenakan  ' +
                Currency.formatSatuan(banyakPasar) +
                'x ongkos kirim karena belanja dari ' +
                Currency.formatSatuan(banyakPasar, satuan: 'Pasar') +
                '. Tarif ongkir perpasar adalah ' +
                Currency.formatRupiah(tarifOngkirPerPasar, 0) +
                '. Disarankan belanja dari 1 pasar.';
          }
        } else {
          String namaPasarTerjauh = (jenisOngkir['namaPasarTerjauh'] != null)
              ? jenisOngkir['namaPasarTerjauh']
              : 'Pasar Tidak Dikenal';

          String tarifOngkirPerKMString =
              await SettingModel.getSettingValue('costPerKm');
          int tarifOngkirPerKM = int.parse(tarifOngkirPerKMString);
          penjelasanOngkir =
              'Anda dikenakan ongkir PERKM karena anda sudah berbelanja di luar Coverage Area anda. Jarak yang harus ditempuh kurir adalah ' +
                  Currency.formatSatuan(jenisOngkir['jarakPasarTerjauh'],
                      satuan: 'KM') +
                  ' (' +
                  namaPasarTerjauh.toUpperCase() +
                  ')'
                      '. Tarif ongkir per km adalah ' +
                  Currency.formatRupiah(tarifOngkirPerKM, 0);
        }

        dynamic _getPoint =
            await PointModel.getPoint(queryString: 'point=true');
        int _point = (_getPoint != false) ? _getPoint['point'] : 0;

        dynamic _minimalPointTukarPoint =
            await SettingModel.getSettingValue('minimalPointTukarPoint');

        setState(() {
          cartCode = cartCodeVariable;
          totalDompetSayaState = totalDompetSaya;
          walletConfigurationState = walletConfiguration;
          totalOngkirState = totalOngkir;
          totalBelanjaState = totalBelanja;
          jumlahBarangState = jumlahBarang;
          isReadyToShow = true;
          jenisOngkirState = jenisOngkir;
          penjelasanOngkirState = penjelasanOngkir;

          _pointState = _point;
          _detailUserState = _detailUser;

          _namaPenerimaController.text = _detailUser['nama'];
          _nomorHPPenerimaController.text = _detailUser['nomorHP'];
          _alamatPenerimaController.text = _detailUser['alamat'];

          if (_minimalPointTukarPoint != false) {
            if (_minimalPointTukarPoint.runtimeType == String) {
              _minimalPointTukarPointState = int.parse(_minimalPointTukarPoint);
            } else {
              _minimalPointTukarPointState = _minimalPointTukarPoint;
            }
          }

          _diskonKategoriState = _diskonKategori;
        });
      }
    } else {
      String _title = 'Checkout';
      String _subTitle =
          'Anda tidak dapat chekout karena total belanjaan anda kurang dari pembelian minimal!';
      Widget _actions =
          Row(mainAxisAlignment: MainAxisAlignment.center, children: [
        CustomButton.button(context, 'OK', () {
          Navigator.pushAndRemoveUntil(context,
              MaterialPageRoute(builder: (_) => HomePage()), (route) => false);
        })
      ]);
      await CustomAlert.textComponent(context,
          title: _title, subTitle: _subTitle, actions: _actions);
    }
  }

  void initState() {
    super.initState();
    _initializeState();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);
    bool _isFormCheckoutCompleted = false;
    Map<String, dynamic> dataRingkasanPembayaran;

    if (isReadyToShow) {
      String _namaPenerima = _namaPenerimaController.text.trim();
      String _nomorHPPenerima = _nomorHPPenerimaController.text.trim();
      String _alamatDetailPenerima = _alamatPenerimaController.text.trim();

      _isFormCheckoutCompleted = _namaPenerima.length >= 1 &&
          _nomorHPPenerima.length >= 1 &&
          _alamatDetailPenerima.length >= 1;

      int _totalBelanja = totalBelanjaState;
      int _totalOngkir = totalOngkirState;
      int totalOngkirNormal = totalOngkirState;

      bool isSelectedVoucherNull = (usedVoucherState == null);
      bool isWalletConfigurationNull = (walletConfigurationState == null);

      int _nilaiVoucher = 0;
      if (!isSelectedVoucherNull) {
        if (statusVoucherState != null) {
          if (statusVoucherState!.toLowerCase() == 'aktif') {
            _nilaiVoucher = int.parse(usedVoucherState!['nilai']);
          }
        }
      }

      int _nilaiDiskonKategori = (_diskonKategoriState != false)
          ? int.parse(_diskonKategoriState['diskon'])
          : 0;

      int _potonganWallet = 0;
      int minGrandTotalTransactionForWallet = 0;
      bool isWalletOn = false;
      bool walletDibatalkan = false;
      if (!isWalletConfigurationNull) {
        isWalletOn = walletConfigurationState!['isWalletOn'];

        if (isWalletOn) {
          minGrandTotalTransactionForWallet = int.parse(
              walletConfigurationState!['minGrandTotalTransactionForWallet']);
          if (totalBelanjaState >= minGrandTotalTransactionForWallet) {
            int potonganDompetConfig =
                int.parse(walletConfigurationState!['walletDiscount']);
            if (totalDompetSayaState >= potonganDompetConfig) {
              _potonganWallet = potonganDompetConfig;
            } else {
              walletDibatalkan = true;
            }
          }
        }
      }

      int _subTotal =
          totalBelanjaState; //belum termasuk potongan wallet, ongkir dan potongan voucher
      int _subTotalBersih =
          _subTotal - _potonganWallet - _nilaiVoucher - _nilaiDiskonKategori;

      int _nilaiPointOut = (_isPointUsedState) ? _pointState : 0;
      if (_nilaiPointOut >= _subTotalBersih) {
        _nilaiPointOut = _subTotalBersih;
      }

      int _totalKeseluruhan = (_totalBelanja + _totalOngkir) -
          (_nilaiVoucher +
              _potonganWallet +
              _nilaiPointOut +
              _nilaiDiskonKategori + _kodePromoDiskonState);

              print(_kodePromoDiskonState);

      dataRingkasanPembayaran = {
        'isReadyToShow': isReadyToShow,
        'usedVoucher': usedVoucherState,
        'walletConfigurationState': walletConfigurationState,
        'jumlahBarang': jumlahBarangState,
        'totalBelanja': _subTotal,
        'totalOngkir': _totalOngkir,
        'totalOngkirNormal': totalOngkirNormal,
        'jenisOngkir': jenisOngkirState,
        'penjelasanOngkir': penjelasanOngkirState,
        'statusVoucher': statusVoucherState,
        'isWalletOn': isWalletOn,
        'potonganWallet': _potonganWallet,
        'walletDibatalkan': walletDibatalkan,
        'totalKeseluruhan': _totalKeseluruhan
      };

      CheckoutGlobalState _cgs = CheckoutGlobalState(
          catatanTambahan: _catatanTambahanController.text.trim(),
          detailUser: _detailUserState!,
          ongkir: _totalOngkir,
          usedVoucher: usedVoucherState,
          cartCode: cartCode,
          isWalletOn: isWalletOn,
          isCheckoutFormCompleted: _isFormCheckoutCompleted,
          totalKeseluruhan: _totalKeseluruhan,
          isPointUsed: _isPointUsedState,
          point: _pointState,
          subTotal: _subTotal,
          potonganDompet: _potonganWallet,
          potonganVoucher: _nilaiVoucher,
          jadwalDelivery: jadwalDeliverySelected,
          totalDapatPoint: _totalDapatPointState,
          minimalPointTukarPoint: _minimalPointTukarPointState,
          diskonKategori: _diskonKategoriState,
          nilaiPointOut: _nilaiPointOut, kodePromo: _kodePromoState, kodePromoStatus: _kodePromoStatusState, kodePromoMessage: _kodePromoMessageState, kodePromoDiskon: _kodePromoDiskonState);

      double _formCheckoutSpace = 12.5;

      Color _textFieldBackgroundColor = Color(0x3366CC99);

      return Scaffold(
          resizeToAvoidBottomInset: true,
          backgroundColor: CustomColors.nearlyWhite,
          appBar: AppBar(
            backgroundColor: CustomColors.green,
            title: Text('Checkout'),
            leading: IconButton(
                icon: Icon(FontAwesomeIcons.arrowLeft),
                onPressed: () {
                  Navigator.pop(context);
                }),
          ),
          body: Column(children: [
            SubTotal(checkoutContext: context, checkoutGlobalState: _cgs),
            Expanded(
              child: RefreshIndicator(
                onRefresh: () async {
                  _initializeState();
                },
                child: LayoutBuilder(builder: (context, constraints) {
                  return ListView(
                    shrinkWrap: true,
                    children: [
                      Padding(
                        padding: EdgeInsets.only(
                            right: SizeConfig.horizontalBlock * 1.75,
                            left: SizeConfig.horizontalBlock * 1.75,
                            bottom: SizeConfig.horizontalBlock * 5.0),
                        child: Card(
                          elevation: 5.0,
                          shape: RoundedRectangleBorder(
                              borderRadius:
                                  BorderRadius.all(Radius.circular(10.0))),
                          child: Container(
                            constraints: BoxConstraints(
                                minHeight: constraints.maxHeight),
                            padding: EdgeInsets.all(20.0),
                            child: Column(
                                mainAxisAlignment: MainAxisAlignment.start,
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('Checkout',
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 4,
                                          fontWeight: FontWeight.w700)),
                                  SizedBox(height: 5),
                                  Text(
                                      'Silahkan lengkapi data - data berikut ini untuk memproses orderan',
                                      style: CustomText.textXS()),
                                  Container(
                                      margin:
                                          EdgeInsets.symmetric(vertical: 15.0),
                                      height: .5,
                                      color: CustomColors.green),
                                  CustomForm.textField(context,
                                      backgroundColor:
                                          _textFieldBackgroundColor,
                                      hintText: 'Nama Lengkap Penerima',
                                      useLabel: true,
                                      controller: _namaPenerimaController,
                                      borderWidth: _formBorderWidth,
                                      borderColor: _formBorderColor),
                                  SizedBox(height: _formCheckoutSpace),
                                  CustomForm.textField(context,
                                      backgroundColor:
                                          _textFieldBackgroundColor,
                                      useLabel: true,
                                      hintText: 'Nomor HP Penerima',
                                      controller: _nomorHPPenerimaController,
                                      borderWidth: _formBorderWidth,
                                      borderColor: _formBorderColor),
                                  SizedBox(height: _formCheckoutSpace),
                                  CustomForm.textField(context,
                                      backgroundColor:
                                          _textFieldBackgroundColor,
                                      hintText: 'Alamat Lengkap Penerima',
                                      useLabel: true,
                                      controller: _alamatPenerimaController,
                                      borderWidth: _formBorderWidth,
                                      borderColor: _formBorderColor),
                                  SizedBox(height: _formCheckoutSpace),
                                  CustomForm.textField(context,
                                      backgroundColor:
                                          _textFieldBackgroundColor,
                                      hintText: 'Catatan Tambahan',
                                      useLabel: true,
                                      controller: _catatanTambahanController,
                                      borderWidth: _formBorderWidth,
                                      borderColor: _formBorderColor),
                                  SizedBox(height: 20),
                                  Container(height: 1, color: Colors.grey[300]),
                                  SizedBox(height: 20),
                                  JadwalPengiriman(jadwalPengirimanTerpilih:
                                      (jadwalPengiriman) {
                                    setState(() {
                                      jadwalDeliverySelected = jadwalPengiriman;
                                    });
                                  }),
                                  SizedBox(height: 20),
                                  RingkasanPembayaran(dataRingkasanPembayaran,
                                  onKodePromoFilled: (kodePromo) async {
                                    Map<String, dynamic>? _checkKodePromo  = await KodePromoModel.cekKodePromo(kodePromo: kodePromo);

                                    if(_checkKodePromo != null){
                                      bool _status = _checkKodePromo['status'];
                                      String? _message = _checkKodePromo['message'];
                                      Map<String, dynamic>? _data = _checkKodePromo['data'];

                                      int _kodePromoDiskon  = 0;
                                      if(_data != null){
                                        Map<String, dynamic> _promoDetail = _data['promo'];
                                        _kodePromoDiskon = int.parse(_promoDetail['diskon']);
                                      }

                                      setState(() {
                                        _kodePromoState = kodePromo;
                                        _kodePromoStatusState = _status;
                                        _kodePromoMessageState  = _message;
                                        _kodePromoDiskonState = _kodePromoDiskon;
                                      });
                                    }else{
                                      
                                      setState(() {
                                      _kodePromoState   = kodePromo;
                                    });
                                    }
                                  },
                                      checkoutGlobalState: _cgs,
                                      onVoucherSelected:
                                          (voucherSelected, statusVoucher) {
                                    int _nilaiVoucher =
                                        int.parse(voucherSelected['nilai']);
                                    if (_nilaiVoucher <= _totalKeseluruhan) {
                                      setState(() {
                                        usedVoucherState = voucherSelected;
                                        statusVoucherState = statusVoucher;
                                      });
                                    } else {
                                      String _title =
                                          'Voucher Tidak Dapat Digunakan';
                                      String _subTitle =
                                          'Voucher tidak dapat digunakan karena potongan voucher lebih besar dari total belanjaan anda!';
                                      Widget _actions = Row(
                                          mainAxisAlignment:
                                              MainAxisAlignment.center,
                                          children: [
                                            CustomButton.button(context, 'OK',
                                                () {
                                              Navigator.pop(context);
                                            }, color: CustomColors.green)
                                          ]);

                                      CustomAlert.textComponent(context,
                                          title: _title,
                                          subTitle: _subTitle,
                                          actions: _actions);
                                    }
                                  }, onVoucherRemoved: () async {
                                    await LocalStorage.clearUsedVoucher();
                                    setState(() {
                                      usedVoucherState = null;
                                      statusVoucherState = null;
                                    });
                                  }, onPointUsed: (checked) {
                                    setState(() {
                                      _isPointUsedState = checked;
                                    });
                                  }),
                                  SizedBox(height: 5)
                                ]),
                          ),
                        ),
                      ),
                    ],
                  );
                }),
              ),
            )
          ]));
    } else {
      return Scaffold(body: Loading());
    }
  }
}
