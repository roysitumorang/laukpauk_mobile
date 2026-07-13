import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';

import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/models/ADJDModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/SettingModel.dart';
import 'package:laukpauk/views/pembeli/HomePage.dart';
import 'package:laukpauk/views/pembeli/TransferBank.dart';

import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/models/TransaksiModel.dart';
import 'package:laukpauk/views/pembeli/RepeatOrder.dart';

import 'package:laukpauk/models/PesananSayaModel.dart';
import 'package:laukpauk/views/pembeli/components/DetailTransaksi/StatusTransaksi.dart';
import 'package:laukpauk/views/pembeli/components/DetailTransaksi/ItemTransaksi.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class DetailTransaksi extends StatefulWidget {
  final String idTransaksi;
  final String? idPenjual;
  DetailTransaksi(this.idTransaksi, {this.idPenjual});
  _DetailTransaksiState createState() => _DetailTransaksiState();
}

class _DetailTransaksiState extends State<DetailTransaksi> {
  int reload = 0;
  Map<String, dynamic>? _rekeningData;


  bool _isButtonBelanjaUlangBusy = false;

  TextStyle titleSize(BuildContext context, {Color? color}) {
    SizeConfig().initSize(context);

    return TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.75,
        fontWeight: FontWeight.w700,
        color: (color == null) ? CustomColors.nearlyBlack : color);
  }

  TextStyle contentSize(BuildContext context,
      {Color? color,
      FontStyle? fontStyle,
      double? fontSize,
      FontWeight? fontWeight}) {
    SizeConfig().initSize(context);

    return TextStyle(
        fontWeight: (fontWeight == null) ? FontWeight.normal : fontWeight,
        fontStyle: (fontStyle == null) ? FontStyle.normal : fontStyle,
        fontSize:
            (fontSize == null) ? SizeConfig.horizontalBlock * 3.25 : fontSize,
        color: (color == null) ? CustomColors.nearlyBlack : color);
  }

  void initState() {
    super.initState();
    _initialization();
  }

  Future<void> _initialization() async {
    String _nomorRekeningLP =
        await SettingModel.getSettingValue('nomorRekeningLP');
    String _pemilikNomorRekeningLP =
        await SettingModel.getSettingValue('pemilikNomorRekeningLP');

    setState(() {
      _rekeningData = {
        'nomorRekening': _nomorRekeningLP,
        'pemilikNomorRekening': _pemilikNomorRekeningLP
      };
    });
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idTransaksi = widget.idTransaksi;
    String? idPenjual  = widget.idPenjual;

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        appBar: AppBar(
          title: Text('Detail Transaksi'),
          backgroundColor: CustomColors.green,
        ),
        body: FutureBuilder(
            future: TransaksiModel.getDetailTransaksi(idTransaksi),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                dynamic detailTransaksi = snapshot.data;
                if (detailTransaksi != false) {
                  int totalBelanja = int.parse(detailTransaksi['totalBelanja']);
                  int ongkir = int.parse(detailTransaksi['ongkir']);
                  int nilaiVoucher = int.parse(detailTransaksi['nilaiVoucher']);
                  String jenisVoucher = detailTransaksi['jenisVoucher'];
                  String _jenisVoucherLC = jenisVoucher.toLowerCase();

                  int _totalBelanja =
                      int.parse(detailTransaksi['totalBelanja']);
                  int _ongkir = int.parse(detailTransaksi['ongkir']);

                  int _nilaiVoucher =
                      int.parse(detailTransaksi['nilaiVoucher']);
                  int _nilaiPotonganWallet =
                      int.parse(detailTransaksi['potonganWallet']);
                  int _nilaiPointOut = int.parse(detailTransaksi['pointOut']);
                  int _nilaiDiskonKategori =
                      int.parse(detailTransaksi['diskonKategori']);
                  int _nilaiDiskonKodePromo =
                      int.parse(detailTransaksi['diskonPromo']);

                  String status = detailTransaksi['status'];

                  String formattedTanggalDelivery =
                      detailTransaksi['formattedTanggalDelivery'];
                  String jamDelivery = detailTransaksi['jadwalDelivery'];
                  String _metodePembayaran =
                      detailTransaksi['metodePembayaran'];
                  String namaMetodePembayaran =
                      detailTransaksi['namaMetodePembayaran'];
                  String nomorTransaksi = detailTransaksi['nomorTransaksi'];
                  String catatan = detailTransaksi['catatan'];
                  String statusReason = detailTransaksi['statusReason'];
                  String alamatDelivery = detailTransaksi['alamatDelivery'];
                  String? alamatDetailLengkap =
                      detailTransaksi['alamatDetailLengkap'];

                  String statusLC = status.toLowerCase();

                  bool isRejected = (statusLC == 'rejected');

                  String statusReadable =
                      PesananSayaModel.readAbleStatusForUser(status)
                          .toUpperCase();

                  int _refundedOngkir =
                      int.parse(detailTransaksi['refundedOngkir']);
                  int _refundedUangBelanja =
                      int.parse(detailTransaksi['refundedUangBelanja']);

                  int _totalKeseluruhan = (_totalBelanja + _ongkir) -
                      (_nilaiVoucher +
                          _nilaiPotonganWallet +
                          _nilaiPointOut +
                          _nilaiDiskonKategori + _nilaiDiskonKodePromo);

                  bool _isBCA = false;
                  int _moreDataMetodePembayaran = 0;
                  Map<String, dynamic>? _transferBankData;
                  if (_metodePembayaran.toLowerCase() == 'tf_bca') {
                    _moreDataMetodePembayaran =
                        int.parse(detailTransaksi['moreDataMetodePembayaran']);
                    _isBCA = true;

                    Map<String, dynamic> _responseCheckout = {
                      'idTransaksi': idTransaksi,
                      'paymentResponse': {
                        'totalBayar': _totalKeseluruhan,
                        'kodeUnik': _moreDataMetodePembayaran,
                        'nomorRekening': (_rekeningData != null)
                            ? _rekeningData!['nomorRekening']
                            : '-- Nomor Rekenig --',
                        'pemilikNomorRekening': (_rekeningData != null)
                            ? _rekeningData!['pemilikNomorRekening']
                            : ' -- Nama Pemilik --',
                        'idTransaksi': idTransaksi,
                        'nomorTransaksi': nomorTransaksi
                      }
                    };
                    _transferBankData = {'responseCheckout': _responseCheckout};

                    _totalKeseluruhan += _moreDataMetodePembayaran;
                  }

                  // bool _isEWallet = true;
                  // bool _isEWallet = false;
                  // List<String> _eWallets = [
                  //   'OVO',
                  //   'DANA',
                  //   'LINKAJA',
                  //   'SHOPEEPAY'
                  // ];
                  // if (_eWallets.contains(namaMetodePembayaran.toUpperCase())) {
                  //   _isEWallet = true;
                  // }
                  //aku lupa kenapa kok hanya diperbolehkan untuk eWallet aja, jadi supaya aman aku hilangkan aja kondisi itu

                  String _cartCode  = detailTransaksi['cartCode'];

                  return RefreshIndicator(
                      onRefresh: () async {
                        setState(() {});
                      },
                      child: Padding(
                            padding: EdgeInsets.symmetric(
                                vertical: 5.0, horizontal: 5.0),
                            child: ListView(children: [
                              Card(
                                  shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(10)),
                                  child: Padding(
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 15, vertical: 10),
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.spaceBetween,
                                              children: <Widget>[
                                                Text('Detail Transaksi',
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            4.25,
                                                        fontWeight:
                                                            FontWeight.bold)),
                                                GestureDetector(
                                                  onTap : (() async {
                                                    String _url = ServerPath
                                                            .baseURL +
                                                        '/transaksi/invoice/$idTransaksi';
                                                    Uri _uri = Uri.parse(_url);

                                                    if (await canLaunchUrl(_uri)) {
                                                      await launchUrl(_uri, mode: LaunchMode.externalApplication);
                                                    } else {
                                                      SnackBar snackBar = SnackBar(
                                                          content: Text(
                                                              'URL Tidak Valid !'),
                                                          duration:
                                                              Duration(seconds: 1));
                                                      ScaffoldMessenger.of(context)
                                                          .showSnackBar(snackBar);
                                                    }
                                                  }),
                                                  child: Text('Lihat Invoice',
                                                      style: contentSize(context,
                                                          color: CustomColors
                                                              .textPrimary)),
                                                ),
                                              ]),
                                          SizedBox(height: 15.0),
                                          (!isRejected)
                                              ? Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                      StatusTransaksi(status,
                                                          iconWidth: SizeConfig
                                                                  .horizontalBlock *
                                                              10.5,
                                                          iconSize: SizeConfig
                                                                  .horizontalBlock *
                                                              4),
                                                      (statusLC == 'received')
                                                          ? Padding(
                                                              padding:
                                                                  EdgeInsets.only(
                                                                      left: 5),
                                                              child: CustomButton.button(
                                                                  context,
                                                                  'Pesan Lagi', () {
                                                                Navigator.push(
                                                                    context,
                                                                    MaterialPageRoute(
                                                                        builder: (_) =>
                                                                            RepeatOrder(
                                                                                idTransaksi)));
                                                              },
                                                                  color: CustomButton
                                                                      .themeColor,
                                                                  isPill: true),
                                                            )
                                                          : SizedBox(),
                                                      (statusLC == 'otw')
                                                          ? Padding(
                                                              padding:
                                                                  EdgeInsets.only(
                                                                      left: 5),
                                                              child: CustomButton.button(
                                                                  context,
                                                                  'Diterima',
                                                                  () async {
                                                                String _title =
                                                                    'Pesanan Diterima';
                                                                String _subTitle =
                                                                    'Lakukan konfirmasi jika pesanan sudah diterima ya mak ...';

                                                                bool? konfirmasi =
                                                                    await CustomAlert
                                                                        .konfirmasi(
                                                                            context,
                                                                            title:
                                                                                _title,
                                                                            subTitle:
                                                                                _subTitle);

                                                                if (konfirmasi !=
                                                                        null &&
                                                                    konfirmasi ==
                                                                        true) {
                                                                  bool
                                                                      statusPesananDiterima =
                                                                      await PesananSayaModel
                                                                          .pesananDiterima(
                                                                              idTransaksi);
                                                                  String message =
                                                                      'Gagal menandai pesanan sebagai SUDAH DITERIMA. Silahkan coba lagi!';
                                                                  if (statusPesananDiterima) {
                                                                    setState(() {
                                                                      reload++;
                                                                    });
                                                                    message =
                                                                        'Berhasil menanda pesanan sebagai SUDAH DITERIMA !';
                                                                  }
                                                                  SnackBar snackBar = SnackBar(
                                                                      duration:
                                                                          Duration(
                                                                              seconds:
                                                                                  3),
                                                                      content: Text(
                                                                          message));

                                                                  ScaffoldMessenger
                                                                          .of(
                                                                              context)
                                                                      .showSnackBar(
                                                                          snackBar);
                                                                }
                                                              },
                                                                  outline: true,
                                                                  color: CustomButton
                                                                      .themeColor,
                                                                  isPill: true),
                                                            )
                                                          : SizedBox()
                                                    ])
                                              : SizedBox(),
                                          (!isRejected)
                                              ? SizedBox(height: 10)
                                              : SizedBox(),
                                          (!isRejected)
                                              ? Text('Status : ' + statusReadable,
                                                  style: TextStyle(
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3.25))
                                              : SizedBox(),
                                          (!isRejected)
                                              ? Container(
                                                  margin: EdgeInsets.symmetric(
                                                      vertical: 10.0),
                                                  height: 0.5,
                                                  width:
                                                      SizeConfig.horizontalBlock *
                                                          100,
                                                  decoration: BoxDecoration(
                                                      color:
                                                          CustomColors.textMuted))
                                              : SizedBox(),
                                          (statusLC == 'otw')
                                              ? Container(
                                                  margin: EdgeInsets.symmetric(
                                                      vertical: 5.0),
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 10.0,
                                                      vertical: 10.0),
                                                  decoration: BoxDecoration(
                                                      color: Color(0x3366CC99),
                                                      borderRadius:
                                                          BorderRadius.circular(
                                                              10.0)),
                                                  child: Column(
                                                      crossAxisAlignment:
                                                          CrossAxisAlignment.start,
                                                      children: [
                                                        Text('Note!',
                                                            style: TextStyle(
                                                                fontWeight:
                                                                    FontWeight.bold,
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    3.5)),
                                                        SizedBox(height: 2.5),
                                                        Text(
                                                            'Mak, jangan lupa tekan tombol DITERIMA untuk bisa pesan ulang produk yang sama ya ...',
                                                            style: TextStyle(
                                                                fontStyle: FontStyle
                                                                    .italic,
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    3.0))
                                                      ]))
                                              : SizedBox(),
                                          SizedBox(height: 5.0),
                                          Column(
                                              crossAxisAlignment:
                                                  CrossAxisAlignment.start,
                                              children: [
                                                Text('Nomor Order',
                                                    style: titleSize(context)),
                                                SizedBox(height: 5.0),
                                                Text(nomorTransaksi,
                                                    style: contentSize(context))
                                              ]),
                                          SizedBox(height: 10),
                                          Column(
                                              crossAxisAlignment:
                                                  CrossAxisAlignment.start,
                                              children: [
                                                Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Metode Pembayaran',
                                                        style: titleSize(context)),
                                                    (_isBCA)
                                                        ? GestureDetector(
                                                            child: Text(
                                                                'Cara Pembayaran',
                                                                style: contentSize(
                                                                        context)
                                                                    .copyWith(
                                                                        color: CustomColors
                                                                            .textDanger)),
                                                            onTap: () => Navigator.push(
                                                                context,
                                                                MaterialPageRoute(
                                                                    builder: (_) =>
                                                                        TransferBank(
                                                                            _transferBankData!))),
                                                          )
                                                        : SizedBox()
                                                  ],
                                                ),
                                                SizedBox(height: 5.0),
                                                Text(namaMetodePembayaran,
                                                    style: contentSize(context)),
                                              ]),
                                          (catatan.length >= 1)
                                              ? SizedBox(height: 10)
                                              : SizedBox(),
                                          (catatan.length >= 1)
                                              ? Text('Catatan Tambahan',
                                                  style: titleSize(context))
                                              : SizedBox(),
                                          (catatan.length >= 1)
                                              ? Text(catatan,
                                                  style: contentSize(context))
                                              : SizedBox(),
                                          SizedBox(
                                              height: (statusLC == 'rejected')
                                                  ? 10
                                                  : 0),
                                          (statusLC == 'rejected')
                                              ? Container(
                                                  margin:
                                                      EdgeInsets.only(bottom: 5),
                                                  width:
                                                      SizeConfig.horizontalBlock *
                                                          100,
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 15, vertical: 10),
                                                  decoration: BoxDecoration(
                                                      color: Color(0x25dc3545),
                                                      borderRadius:
                                                          BorderRadius.circular(
                                                              10)),
                                                  child: Column(
                                                    crossAxisAlignment:
                                                        CrossAxisAlignment.start,
                                                    children: [
                                                      Text('Alasan Pembatalan',
                                                          style: contentSize(
                                                              context,
                                                              color: CustomColors
                                                                  .textDanger)),
                                                      SizedBox(height: 5),
                                                      (statusReason.length >= 1)
                                                          ? Text(statusReason,
                                                              style: contentSize(
                                                                  context))
                                                          : Text('Tanpa Keterangan',
                                                              style: contentSize(
                                                                  context,
                                                                  fontStyle:
                                                                      FontStyle
                                                                          .italic,
                                                                  color: CustomColors
                                                                      .textMuted)),
                                                    ],
                                                  ),
                                                )
                                              : SizedBox(),
                                          SizedBox(height: 5.0),
                                          (statusLC == 'rejected')
                                              ? FutureBuilder<int?>(
                                                  future: TransaksiModel
                                                      .jumlahItemDiantar(
                                                          idTransaksi: idTransaksi),
                                                  builder: (context,
                                                      _jumlahItemDiantarSnapshot) {
                                                    if (_jumlahItemDiantarSnapshot
                                                        .hasData) {
                                                      if (_jumlahItemDiantarSnapshot
                                                              .data !=
                                                          null) {
                                                        int _jumlahItemDiantar =
                                                            _jumlahItemDiantarSnapshot
                                                                .data!;
                                                        if (_jumlahItemDiantar <=
                                                            0) {
                                                          return Container(
                                                            margin: EdgeInsets.only(
                                                                bottom: 5),
                                                            width: SizeConfig
                                                                    .horizontalBlock *
                                                                100,
                                                            padding: EdgeInsets
                                                                .symmetric(
                                                                    horizontal: 15,
                                                                    vertical: 10),
                                                            decoration: BoxDecoration(
                                                                color: Color(
                                                                    0x25dc3545),
                                                                borderRadius:
                                                                    BorderRadius
                                                                        .circular(
                                                                            10)),
                                                            child: Column(
                                                              crossAxisAlignment:
                                                                  CrossAxisAlignment
                                                                      .start,
                                                              children: [
                                                                Text(
                                                                    'Pengembalian Dana (' +
                                                                        Currency.formatRupiah(
                                                                            _refundedOngkir +
                                                                                _refundedUangBelanja,
                                                                            0) +
                                                                        ')',
                                                                    style: contentSize(
                                                                        context,
                                                                        color: CustomColors
                                                                            .textDanger)),
                                                                SizedBox(height: 5),
                                                                Text(
                                                                    'Ongkos Kirim ' +
                                                                        Currency.formatRupiah(
                                                                            _refundedOngkir,
                                                                            0),
                                                                    style: contentSize(
                                                                        context,
                                                                        fontStyle:
                                                                            FontStyle
                                                                                .italic,
                                                                        color: CustomColors
                                                                            .textMuted)),
                                                                Text(
                                                                    'Uang Belanja ' +
                                                                        Currency.formatRupiah(
                                                                            _refundedUangBelanja,
                                                                            0),
                                                                    style: contentSize(
                                                                        context,
                                                                        fontStyle:
                                                                            FontStyle
                                                                                .italic,
                                                                        color: CustomColors
                                                                            .textMuted)),
                                                                SizedBox(
                                                                    height: 15.0),
                                                                CustomButton.button(
                                                                    context,
                                                                    'Belanja Ulang',
                                                                    () async {
                                                                  bool _isADJDSet =
                                                                      await LocalStorage
                                                                          .isAlamatDeliveryDanJadwalDeliverySet();

                                                                  if (_isADJDSet) {
                                                                    setState(() {
                                                                      _isButtonBelanjaUlangBusy =
                                                                          true;
                                                                    });

                                                                    String
                                                                        _adjdTitle =
                                                                        'Jadwal dan Alamat Antar';
                                                                    String
                                                                        _adjdSubTitle =
                                                                        'Gunakan alamat dan jadwal pengantaran yang sudah di atur sebelumnya?';
                                                                    Widget
                                                                        _adjdActions =
                                                                        Container(
                                                                            padding: EdgeInsets.symmetric(
                                                                                horizontal: SizeConfig.horizontalBlock *
                                                                                    15.0),
                                                                            child: Column(
                                                                                mainAxisSize:
                                                                                    MainAxisSize.min,
                                                                                mainAxisAlignment: MainAxisAlignment.center,
                                                                                children: [
                                                                                  Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                                                                                    Expanded(
                                                                                      child: CustomButton.button(context, 'Ya', () {
                                                                                        Navigator.pop(context, true);
                                                                                      }, color: CustomColors.green),
                                                                                    ),
                                                                                    SizedBox(width: 5.0),
                                                                                    Expanded(
                                                                                      child: CustomButton.button(context, 'Tidak', () {
                                                                                        Navigator.pop(context, false);
                                                                                      }, color: CustomColors.textDanger),
                                                                                    )
                                                                                  ]),
                                                                                  SizedBox(height: 5.0),
                                                                                  CustomButton.button(context, 'Tutup', () {
                                                                                    Navigator.pop(context);
                                                                                  }, isBlock: true, color: CustomColors.textMuted, outline: true)
                                                                                ]));
                                                                    bool?
                                                                        _useSameADJD =
                                                                        await CustomAlert.textComponent(
                                                                            context,
                                                                            title:
                                                                                _adjdTitle,
                                                                            subTitle:
                                                                                _adjdSubTitle,
                                                                            actions:
                                                                                _adjdActions);
                                                                    if (_useSameADJD !=
                                                                        null) {
                                                                      if (!_useSameADJD) {
                                                                        await ADJDModel
                                                                            .deleteAlamatDanJadwalDelivery();
                                                                        await HeaderModel
                                                                            .adjd(
                                                                                context);
                                                                      }

                                                                      Map<String,
                                                                              dynamic>?
                                                                          _responseBelanjaUlang =
                                                                          await TransaksiModel.belanjaUlang(
                                                                              idTransaksi:
                                                                                  idTransaksi);
                                                                      if (_responseBelanjaUlang !=
                                                                          null) {
                                                                        await TransaksiModel.responseBelanjaUlang(
                                                                            context,
                                                                            _responseBelanjaUlang);
                                                                      }
                                                                    }

                                                                    setState(() {
                                                                      _isButtonBelanjaUlangBusy =
                                                                          false;
                                                                    });
                                                                  } else {
                                                                    Widget _actions = Row(
                                                                        mainAxisAlignment:
                                                                            MainAxisAlignment
                                                                                .center,
                                                                        children: [
                                                                          CustomButton.button(
                                                                              context,
                                                                              'OK',
                                                                              () {
                                                                            Navigator.push(
                                                                                context,
                                                                                MaterialPageRoute(builder: (_) => HomePage()));
                                                                          },
                                                                              color:
                                                                                  CustomColors.green)
                                                                        ]);
                                                                    await CustomAlert.textComponent(
                                                                        context,
                                                                        title:
                                                                            'Alamat dan Jadwal',
                                                                        subTitle:
                                                                            'Alamat dan jadwal pengantaran belum ditentukan, tentukan dulu ya mak...',
                                                                        actions:
                                                                            _actions);
                                                                  }
                                                                },
                                                                    color:
                                                                        CustomColors
                                                                            .green,
                                                                    isPill: true,
                                                                    isBusy:
                                                                        _isButtonBelanjaUlangBusy,
                                                                    isBlock: true)
                                                              ],
                                                            ),
                                                          );
                                                        }
                                                      }

                                                      return SizedBox();
                                                    }
                                                    return Loading();
                                                  })
                                              : SizedBox()
                                        ],
                                      )),
                                  elevation: 5),
                              ItemTransaksi(idTransaksi,
                                  idPenjual: idPenjual, cartCode: _cartCode),
                              
                              Card(
                                  elevation: 5,
                                  shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(10)),
                                  child: Container(
                                      width: double.infinity,
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 15, vertical: 10),
                                      child: Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Text('Alamat Pengantaran',
                                                style: titleSize(context)),
                                            SizedBox(height: 5),
                                            Text(alamatDelivery,
                                                style: contentSize(context)),
                                            SizedBox(height: 15),
                                            (alamatDetailLengkap != null)? Column(
                                              children: [
                                                Text('Alamat Detail Pengantaran',
                                                style: titleSize(context)),
                                            SizedBox(height: 5),
                                            Text(alamatDetailLengkap,
                                                style: contentSize(context)),
                                            SizedBox(height: 15),
                                              ]
                                            ) : SizedBox(),
                                            Text('Jadwal Delivery',
                                                style: titleSize(context)),
                                            SizedBox(height: 5),
                                            Text(
                                                formattedTanggalDelivery +
                                                    ', ETA Jam ' +
                                                    jamDelivery,
                                                style: contentSize(context)),
                                            SizedBox(height: 15),
                                            Container(
                                                padding: EdgeInsets.symmetric(
                                                    vertical: 10),
                                                color: CustomColors.green,
                                                height: 1),
                                            SizedBox(height: 15),
                                            Padding(
                                              padding: EdgeInsets.only(bottom: 7),
                                              child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Potongan Dompet',
                                                        style:
                                                            contentSize(context)),
                                                    Text(
                                                        '(' +
                                                            Currency.formatRupiah(
                                                                _nilaiPotonganWallet,
                                                                0) +
                                                            ')',
                                                        textAlign: TextAlign.right,
                                                        style: contentSize(context,
                                                            color: CustomColors
                                                                .nearlyBlack))
                                                  ]),
                                            ),
                                            Padding(
                                              padding: EdgeInsets.only(bottom: 7),
                                              child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Potongan Kode Promo',
                                                        style:
                                                            contentSize(context)),
                                                    Text(
                                                        '(' +
                                                            Currency.formatRupiah(
                                                                _nilaiDiskonKodePromo,
                                                                0) +
                                                            ')',
                                                        textAlign: TextAlign.right,
                                                        style: contentSize(context,
                                                            color: CustomColors
                                                                .nearlyBlack))
                                                  ]),
                                            ),
                                            Padding(
                                              padding: EdgeInsets.only(bottom: 7),
                                              child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Potongan Poin',
                                                        style:
                                                            contentSize(context)),
                                                    Text(
                                                        '(' +
                                                            Currency.formatRupiah(
                                                                _nilaiPointOut, 0) +
                                                            ')',
                                                        textAlign: TextAlign.right,
                                                        style: contentSize(context,
                                                            color: CustomColors
                                                                .nearlyBlack))
                                                  ]),
                                            ),
                                            Padding(
                                              padding: EdgeInsets.only(bottom: 7),
                                              child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Ongkos Kirim',
                                                        style:
                                                            contentSize(context)),
                                                    Text(
                                                      Currency.formatRupiah(
                                                          ongkir, 0),
                                                      textAlign: TextAlign.right,
                                                      style: contentSize(context,
                                                          color: CustomColors
                                                              .nearlyBlack),
                                                    )
                                                  ]),
                                            ),
                                            (nilaiVoucher >= 1 &&
                                                    _jenisVoucherLC == 'ongkir')
                                                ? Padding(
                                                    padding:
                                                        EdgeInsets.only(bottom: 7),
                                                    child: Row(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .spaceBetween,
                                                        children: [
                                                          Text(
                                                              'Potongan Voucher Ongkir',
                                                              style: contentSize(
                                                                  context)),
                                                          Text(
                                                            '(' +
                                                                Currency
                                                                    .formatRupiah(
                                                                        nilaiVoucher,
                                                                        0) +
                                                                ')',
                                                            textAlign:
                                                                TextAlign.right,
                                                            style: contentSize(
                                                                context,
                                                                color: CustomColors
                                                                    .nearlyBlack),
                                                          )
                                                        ]),
                                                  )
                                                : SizedBox(),
                                            Padding(
                                              padding: EdgeInsets.only(bottom: 7),
                                              child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Total Belanja',
                                                        style:
                                                            contentSize(context)),
                                                    Text(
                                                        Currency.formatRupiah(
                                                            totalBelanja, 0),
                                                        textAlign: TextAlign.right,
                                                        style: contentSize(context,
                                                            color: CustomColors
                                                                .nearlyBlack))
                                                  ]),
                                            ),
                                            (_jenisVoucherLC == 'belanja' &&
                                                    nilaiVoucher >= 1)
                                                ? Padding(
                                                    padding:
                                                        EdgeInsets.only(bottom: 7),
                                                    child: Row(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .spaceBetween,
                                                        children: [
                                                          Text(
                                                              'Potongan Voucher Belanja',
                                                              style: contentSize(
                                                                  context)),
                                                          Text(
                                                              '(' +
                                                                  Currency
                                                                      .formatRupiah(
                                                                          nilaiVoucher,
                                                                          0) +
                                                                  ')',
                                                              textAlign:
                                                                  TextAlign.right,
                                                              style: contentSize(
                                                                  context,
                                                                  color: CustomColors
                                                                      .nearlyBlack))
                                                        ]),
                                                  )
                                                : SizedBox(),
                                            (_nilaiDiskonKategori >= 1)
                                                ? Padding(
                                                    padding:
                                                        EdgeInsets.only(bottom: 7),
                                                    child: Row(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .spaceBetween,
                                                        children: [
                                                          Text('Diskon',
                                                              style: contentSize(
                                                                  context)),
                                                          Text(
                                                              '(' +
                                                                  Currency.formatRupiah(
                                                                      _nilaiDiskonKategori,
                                                                      0) +
                                                                  ')',
                                                              textAlign:
                                                                  TextAlign.right,
                                                              style: contentSize(
                                                                  context,
                                                                  color: CustomColors
                                                                      .nearlyBlack))
                                                        ]),
                                                  )
                                                : SizedBox(),
                                            (_isBCA)
                                                ? Padding(
                                                    padding: EdgeInsets.only(
                                                        bottom: 7.0),
                                                    child: Row(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .spaceBetween,
                                                        children: [
                                                          Text('Kode Unik',
                                                              style: contentSize(
                                                                  context)),
                                                          Text(
                                                              Currency.formatRupiah(
                                                                  _moreDataMetodePembayaran,
                                                                  0),
                                                              textAlign:
                                                                  TextAlign.right,
                                                              style: contentSize(
                                                                  context,
                                                                  color: CustomColors
                                                                      .nearlyBlack))
                                                        ]),
                                                  )
                                                : SizedBox(),
                                            Padding(
                                              padding: EdgeInsets.only(bottom: 7),
                                              child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    Text('Total Yang Harus Dibayar',
                                                        style: contentSize(context,
                                                            fontWeight:
                                                                FontWeight.w700)),
                                                    Text(
                                                        Currency.formatRupiah(
                                                            _totalKeseluruhan, 0),
                                                        textAlign: TextAlign.right,
                                                        style: contentSize(context,
                                                            fontWeight:
                                                                FontWeight.w700,
                                                            color: CustomColors
                                                                .nearlyBlack))
                                                  ]),
                                            )
                                          ])))
                            ]),
                          ));
                } else {
                  return RefreshIndicator(
                    onRefresh: () async {
                      setState(() {});
                    },
                    child: LayoutBuilder(builder: (context, constraints) {
                      return ListView(children: [
                        Container(
                            constraints: BoxConstraints(
                                minHeight: constraints.maxHeight),
                            child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Image.asset(
                                      ClientPath.assetsImg + '/empty.png',
                                      width: SizeConfig.horizontalBlock * 35),
                                  SizedBox(height: 15),
                                  Text('Detail transaksi tidak ditemukan',
                                      style: TextStyle(
                                          color: CustomColors.textMuted,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.5))
                                ]))
                      ]);
                    }),
                  );
                }
              } else {
                return Loading();
              }
            }));
  }
}
