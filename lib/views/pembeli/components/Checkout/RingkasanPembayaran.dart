import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomForm.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/models/CheckoutModel.dart';

import 'package:laukpauk/views/pembeli/components/Checkout/CheckoutGlobalState.dart';

class RingkasanPembayaran extends StatefulWidget {
  final CheckoutGlobalState checkoutGlobalState;
  final Map<String, dynamic> dataRingkasanPembayaran;
  final void Function(Map<String, dynamic> dataVoucher, String statusVoucher)
      onVoucherSelected;
  final void Function() onVoucherRemoved;
  final void Function(bool checked) onPointUsed;
  final void Function(String kodePromo) onKodePromoFilled;

  RingkasanPembayaran(this.dataRingkasanPembayaran,
      {required this.checkoutGlobalState,
      required this.onVoucherSelected,
      required this.onVoucherRemoved,
      required this.onPointUsed,
      required this.onKodePromoFilled});
  _RingkasanPembayaranState createState() => _RingkasanPembayaranState();
}

class _RingkasanPembayaranState extends State<RingkasanPembayaran> {
  TextEditingController _kodePromoController = TextEditingController();

  void dispose() {
    super.dispose();
    _kodePromoController.dispose();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> dataRingkasanPembayaran =
        widget.dataRingkasanPembayaran;
    bool isReadyToShow = dataRingkasanPembayaran['isReadyToShow'];

    if (isReadyToShow) {
      CheckoutGlobalState _cgs = widget.checkoutGlobalState;

      Map<String, dynamic>? usedVoucher = _cgs.usedVoucher;
      Map<String, dynamic>? walletConfigurationState =
          dataRingkasanPembayaran['walletConfigurationState'];
      String? statusVoucher = dataRingkasanPembayaran['statusVoucher'];
      int jumlahBarang = dataRingkasanPembayaran['jumlahBarang'];
      int totalBelanja = _cgs.subTotal;
      int totalOngkir = _cgs.ongkir;
      int totalOngkirNormal = dataRingkasanPembayaran['totalOngkirNormal'];
      Map<String, dynamic>? jenisOngkir =
          dataRingkasanPembayaran['jenisOngkir'];
      String penjelasanOngkir = dataRingkasanPembayaran['penjelasanOngkir'];

      String jumlahBarangString = jumlahBarang.toString();

      bool isSelectedVoucherNull = usedVoucher == null;
      bool isWalletConfigurationNull = walletConfigurationState == null;

      String kodeVoucher = (!isSelectedVoucherNull) ? usedVoucher['kode'] : '';
      String namaVoucher = (!isSelectedVoucherNull) ? usedVoucher['nama'] : '';
      String jenisVoucher =
          (!isSelectedVoucherNull) ? usedVoucher['jenis'] : '';

      int nilaiVoucher = 0;

      if (!isSelectedVoucherNull) {
        if (statusVoucher != null) {
          if (statusVoucher.toLowerCase() == 'aktif') {
            nilaiVoucher = int.parse(usedVoucher['nilai']);
          }
        }
      }

      if (jenisVoucher.toLowerCase() == 'ongkir') {
        totalOngkir = totalOngkir - nilaiVoucher;
      }

      // bool walletDibatalkan = dataRingkasanPembayaran['walletDibatalkan'];
      // bool walletDibatalkan = false; //supaya tidak muncul (keinginan  client)

      double _iconWidth = SizeConfig.horizontalBlock * 5.75;

      bool _isPointUsed = _cgs.isPointUsed;
      int _jumlahPoint = _cgs.point;
      int _totalKeseluruhan = _cgs.totalKeseluruhan;
      int potonganWallet = _cgs.potonganDompet;
      bool isWalletOn = _cgs.isWalletOn;
      dynamic _diskonKategori = _cgs.diskonKategori;
      int _nilaiPointOut = _cgs.nilaiPointOut;

      int _dkDiskonKategori = 0;
      if (_diskonKategori != false) {
        _dkDiskonKategori = int.parse(_diskonKategori['diskon']);
      }

      bool _minimalPointTerpenuhi = _jumlahPoint >= _cgs.minimalPointTukarPoint;

      if (_nilaiPointOut >= totalBelanja) {
        _nilaiPointOut = totalBelanja;
      }

      int _absoluteNilaiPointOut = _nilaiPointOut;
      if (_nilaiPointOut <= -1) {
        _absoluteNilaiPointOut = _nilaiPointOut * -1;
      }

      bool? _kodePromoStatus = _cgs.kodePromoStatus;
      String? _kodePromoMessage = _cgs.kodePromoMessage;

      Color _kodePromoMessageColor  = CustomColors.textMuted;
      if(_kodePromoStatus != null){
          _kodePromoMessageColor  = CustomColors.textDanger;
          
        if(_kodePromoStatus){
          _kodePromoMessageColor  = CustomColors.textSuccess;
        }
      }

      return Container(
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text('Ringkasan Pembayaran',
            style: TextStyle(
                fontSize: SizeConfig.horizontalBlock * 4,
                fontWeight: FontWeight.w700)),
        SizedBox(height: 5),
        Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          Text('Total Harga ($jumlahBarangString Barang)',
              style: TextStyle(
                  color: CustomColors.grey,
                  fontSize: SizeConfig.horizontalBlock * 3.5)),
          Text(Currency.formatRupiah(totalBelanja, 0),
              textAlign: TextAlign.right,
              style: TextStyle(
                  color: CustomColors.grey,
                  fontSize: SizeConfig.horizontalBlock * 3.5)),
        ]),
        SizedBox(height: 20),
        Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(10),
              border: Border.all(width: 1, color: CustomColors.green)),
          padding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Image.asset(ClientPath.assetsImg + '/icons/pembeli/voucher.png',
                width: _iconWidth),
            SizedBox(width: 10),
            Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                  Text('Voucher'),
                  (!isSelectedVoucherNull) ? SizedBox(height: 2.5) : SizedBox(),
                  (!isSelectedVoucherNull)
                      ? Row(children: [
                          Text(kodeVoucher,
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 2.75)),
                          SizedBox(width: 5),
                          Text(namaVoucher,
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 2.75))
                        ])
                      : SizedBox(),
                  (!isSelectedVoucherNull)
                      ? Container(
                          margin: EdgeInsets.only(top: 5),
                          child: InkWell(
                              onTap: () {
                                widget.onVoucherRemoved();
                              },
                              child: Text('Batal Pakai Voucher',
                                  style: TextStyle(
                                      color: CustomColors.textMuted,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3))),
                        )
                      : SizedBox()
                ])),
            InkWell(
                onTap: () {
                  CheckoutModel.showVoucher(context, totalBelanja, usedVoucher,
                      (selectedVoucher) async {
                    String idSelectedVoucher = selectedVoucher['id'];
                    String statusVoucher =
                        await LocalStorage.getStatusVoucher(idSelectedVoucher);
                    setState(() {
                      usedVoucher = selectedVoucher;
                      statusVoucher = statusVoucher;
                    });
                    LocalStorage.setUsedVoucher(idSelectedVoucher);
                    widget.onVoucherSelected(selectedVoucher, statusVoucher);
                  });
                },
                child: Text(
                    (kodeVoucher.length >= 1 && namaVoucher.length >= 1)
                        ? 'Ganti'
                        : 'Gunakan Voucher',
                    style: TextStyle(
                        color: CustomColors.textPrimary,
                        fontSize: SizeConfig.horizontalBlock * 3)))
          ]),
        ),
        SizedBox(height: 10.0),
        Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(10),
              border: Border.all(width: 1, color: CustomColors.green)),
          padding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            SizedBox(
              width: 15.0,
              height: 15.0,
              child: Checkbox(
                  activeColor: CustomColors.green,
                  value: _isPointUsed,
                  onChanged: (_minimalPointTerpenuhi)
                      ? (isChecked) {
                          if (isChecked != null) {
                            widget.onPointUsed(isChecked);
                          }
                        }
                      : null),
            ),
            SizedBox(width: 10),
            Expanded(
                child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Gunakan Poin'),
                (!_minimalPointTerpenuhi)
                    ? Padding(
                        padding: EdgeInsets.only(top: 2.5),
                        child: Text(
                            'Minimal penukaran poin ' +
                                Currency.formatRupiah(
                                    _cgs.minimalPointTukarPoint, 0),
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 2.5)))
                    : SizedBox()
              ],
            )),
            SizedBox(width: 5.0),
            Column(crossAxisAlignment: CrossAxisAlignment.end, children: [
              Text(Currency.formatRupiah(_jumlahPoint, 0),
                  style: TextStyle(
                      color: CustomColors.green,
                      fontWeight: FontWeight.bold,
                      fontSize: SizeConfig.horizontalBlock * 3.5)),
              (_isPointUsed && _nilaiPointOut < _jumlahPoint)
                  ? Text(
                      'Terpakai Hanya ' +
                          Currency.formatRupiah(_absoluteNilaiPointOut, 0),
                      style:
                          TextStyle(fontSize: SizeConfig.horizontalBlock * 2.5))
                  : SizedBox()
            ])
          ]),
        ),
        SizedBox(height: 10.0),
        Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          Text('Kode Promo'),
          SizedBox(width: 15.0),
          Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  CustomForm.textField(context,
                      hintText: 'Kode Promo',
                      controller: _kodePromoController,
                      borderWidth: 1.0,
                      borderColor: CustomColors.green,
                      onChange: (value){
                        String _kodePromo = value;
                          widget.onKodePromoFilled(_kodePromo);
    }),(_kodePromoMessage != null)
              ? Padding(
                padding: const EdgeInsets.only(top: 7.5),
                child: Text(_kodePromoMessage, style: CustomText.textXS().copyWith(color: _kodePromoMessageColor)),
              )
              : SizedBox()
                ],
              ))
          
        ]),
        SizedBox(height: 15.0),
        (_minimalPointTerpenuhi) ? SizedBox(height: 10.0) : SizedBox(),
        (!isWalletConfigurationNull && isWalletOn)
            ? SizedBox(height: 5)
            : SizedBox(),
        (!isWalletConfigurationNull && isWalletOn)
            ? Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: EdgeInsets.only(top: 5.0),
                    child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text('Potongan Dompet',
                              style: TextStyle(
                                  color: CustomColors.grey,
                                  fontSize: SizeConfig.horizontalBlock * 3.5)),
                          Text(
                              '(' +
                                  Currency.formatRupiah(potonganWallet, 0) +
                                  ')',
                              textAlign: TextAlign.right,
                              style: TextStyle(
                                  fontWeight: FontWeight.w500,
                                  color: CustomColors.grey,
                                  fontSize: SizeConfig.horizontalBlock * 3.5))
                        ]),
                  ),
                  // (walletDibatalkan)
                  //     ? Container(
                  //         margin: EdgeInsets.only(bottom: 5.0, top: 2.5),
                  //         padding: EdgeInsets.symmetric(
                  //             horizontal: 10.0, vertical: 7.5),
                  //         decoration: BoxDecoration(
                  //           color: Color(0x33dc3545),
                  //           borderRadius: BorderRadius.circular(10.0),
                  //         ),
                  //         child: Text(
                  //             'Potongan Dompet dibatalkan karena saldo dompet Anda tidak mencukupi...',
                  //             style: TextStyle(
                  //                 fontSize: SizeConfig.horizontalBlock * 3.0)))
                  //     : SizedBox(),
                  (_isPointUsed)
                      ? Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                              Text('Potongan Poin',
                                  style: TextStyle(
                                      color: CustomColors.grey,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5)),
                              Text(
                                  '(' +
                                      Currency.formatRupiah(_nilaiPointOut, 0) +
                                      ')',
                                  textAlign: TextAlign.right,
                                  style: TextStyle(
                                      fontWeight: FontWeight.w500,
                                      color: CustomColors.grey,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5))
                            ])
                      : SizedBox(),
                ],
              )
            : SizedBox(),
        (jenisVoucher.toLowerCase() == 'ongkir')
            ? Padding(
                padding: EdgeInsets.only(top: 5.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Ongkir Normal ',
                        style: TextStyle(
                            color: CustomColors.grey,
                            fontSize: SizeConfig.horizontalBlock * 3.5)),
                    Text(Currency.formatRupiah(totalOngkirNormal, 0),
                        style: TextStyle(
                            color: CustomColors.grey,
                            fontSize: SizeConfig.horizontalBlock * 3.5))
                  ],
                ),
              )
            : SizedBox(),
        (!isSelectedVoucherNull)
            ? Padding(
                padding: EdgeInsets.only(top: 5.0),
                child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Potongan Voucher',
                          style: TextStyle(
                              color: CustomColors.grey,
                              fontSize: SizeConfig.horizontalBlock * 3.5)),
                      Column(
                          crossAxisAlignment: CrossAxisAlignment.end,
                          children: [
                            Text(
                                '(' +
                                    Currency.formatRupiah(nilaiVoucher, 0) +
                                    ')',
                                textAlign: TextAlign.right,
                                style: TextStyle(
                                    fontWeight: FontWeight.w500,
                                    color: CustomColors.grey,
                                    fontSize:
                                        SizeConfig.horizontalBlock * 3.5)),
                            Text(
                                'Potongan ' +
                                    jenisVoucher +
                                    ' (Voucher ' +
                                    statusVoucher!.toUpperCase() +
                                    ')',
                                textAlign: TextAlign.right,
                                style: TextStyle(
                                    fontWeight: FontWeight.w500,
                                    color: CustomColors.grey,
                                    fontSize: SizeConfig.horizontalBlock * 2.5))
                          ])
                    ]),
              )
            : SizedBox(),
        (_diskonKategori != false)
            ? (_dkDiskonKategori >= 1)
                ? Padding(
                    padding: EdgeInsets.only(top: 5.0),
                    child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Discount',
                                  style: TextStyle(
                                      color: CustomColors.grey,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5))
                            ],
                          ),
                          Text(
                              '(' +
                                  Currency.formatRupiah(_dkDiskonKategori, 0) +
                                  ')',
                              textAlign: TextAlign.right,
                              style: TextStyle(
                                  fontWeight: FontWeight.w500,
                                  color: CustomColors.grey,
                                  fontSize: SizeConfig.horizontalBlock * 3.5))
                        ]),
                  )
                : SizedBox()
            : SizedBox(),
        Padding(
          padding: EdgeInsets.only(top: 5.0),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Text('Total Ongkos Kirim',
                style: TextStyle(
                    color: CustomColors.grey,
                    fontSize: SizeConfig.horizontalBlock * 3.5)),
            Text(Currency.formatRupiah(totalOngkir, 0),
                textAlign: TextAlign.right,
                style: TextStyle(
                    fontWeight: FontWeight.w500,
                    color: CustomColors.grey,
                    fontSize: SizeConfig.horizontalBlock * 3.5))
          ]),
        ),
        (jenisOngkir != null)
            ? Padding(
                padding: EdgeInsets.only(top: 10.0, bottom: 15.0),
                child: Text(penjelasanOngkir,
                    style: TextStyle(
                        fontStyle: FontStyle.italic,
                        color: CustomColors.textDanger,
                        fontSize: SizeConfig.horizontalBlock * 2.75)),
              )
            : SizedBox(),
        Container(
            height: 1,
            width: SizeConfig.horizontalBlock * 100,
            color: CustomColors.green),
        SizedBox(height: 20.0),
        Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
          Text('Total Yang Harus Dibayar',
              style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: CustomColors.grey,
                  fontSize: SizeConfig.horizontalBlock * 3.5)),
          Text(Currency.formatRupiah(_totalKeseluruhan, 0),
              textAlign: TextAlign.right,
              style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: CustomColors.darkGreen,
                  fontSize: SizeConfig.horizontalBlock * 4.5))
        ]),
      ]));
    } else {
      return Loading();
    }
  }
}
