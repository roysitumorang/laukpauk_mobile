import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/CheckoutModel.dart';

import 'package:laukpauk/views/pembeli/components/Checkout/CheckoutGlobalState.dart';

class SubTotal extends StatelessWidget {
  final BuildContext checkoutContext;
  final CheckoutGlobalState checkoutGlobalState;
  SubTotal({required this.checkoutContext, required this.checkoutGlobalState});
  Widget build(BuildContext context) {
    CheckoutGlobalState _cgs = checkoutGlobalState;
    int _totalKeseluruhan = _cgs.totalKeseluruhan;

    String _catatanTambahan = _cgs.catatanTambahan;
    String _cartCode = _cgs.cartCode;

    bool _isFormCheckoutCompleted = _cgs.isCheckoutFormCompleted;
    bool _isWalletOn = _cgs.isWalletOn;
    bool _isPointUsed = _cgs.isPointUsed;

    Map<String, dynamic>? _usedVoucher = _cgs.usedVoucher;
    Map<String, dynamic>? _jadwalDeliverySelected = _cgs.jadwalDelivery;
    Map<String, dynamic> _detailUser = _cgs.detailUser;
    String _kodePromo = _cgs.kodePromo;

    dynamic _diskonKategori = _cgs.diskonKategori;

    return Container(
      decoration: BoxDecoration(
          borderRadius: BorderRadius.only(
              bottomLeft: Radius.circular(12.5),
              bottomRight: Radius.circular(12.5)),
          color: CustomColors.nearlyWhite,
          boxShadow: [
            BoxShadow(
                color: Color(0X22222222),
                offset: Offset(0, 4.5),
                blurRadius: 50.0,
                spreadRadius: 50.0)
          ]),
      padding: EdgeInsets.all(20.0),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('TOTAL',
                          style: TextStyle(
                              color: Colors.black,
                              fontSize: SizeConfig.horizontalBlock * 5,
                              fontWeight: FontWeight.w500),
                          textAlign: TextAlign.left),
                      Text(Currency.formatRupiah(_totalKeseluruhan, 0),
                          style: TextStyle(
                              color: CustomColors.green,
                              fontSize: SizeConfig.horizontalBlock * 4,
                              fontWeight: FontWeight.w700))
                    ]),
              ),
              Opacity(
                opacity: (_isFormCheckoutCompleted) ? 1 : .35,
                child: CustomButton.button(context, 'Bayar', () async {
                  if (_isFormCheckoutCompleted) {
                    Map<String, dynamic> savedADJD =
                        await LocalStorage.getSavedLatitudeAndLongitude();
                    String latDelivery = savedADJD['latitude'];
                    String longDelivery = savedADJD['longitude'];

                    String usedVoucherID =
                        (_usedVoucher == null) ? '0' : _usedVoucher['id'];

                    Map<String, dynamic> checkoutData = {
                      'namaPenerima': _detailUser['nama'],
                      'nomorHPPenerima': _detailUser['nomorHP'],
                      'alamatDetailLengkap': _detailUser['alamat'],
                      'catatanTambahan': _catatanTambahan,
                      'jadwalPengiriman': _jadwalDeliverySelected!['day'] +
                          ' | ' +
                          _jadwalDeliverySelected['rentangWaktu'],
                      'cartCode': _cartCode,
                      'voucher': usedVoucherID,
                      'latDelivery': latDelivery,
                      'longDelivery': longDelivery,
                      'jadwalDeliveryCode': _jadwalDeliverySelected['kode'],
                      'isPointUsed': (_isPointUsed) ? 'true' : 'false',
                      'diskonKategori': (_diskonKategori != false)
                          ? jsonEncode(_diskonKategori)
                          : '',
                          'kodePromo' : _kodePromo
                    };
                    CheckoutModel.showMetodePembayaran(
                        checkoutContext, checkoutData, _totalKeseluruhan);
                  } else {
                    String _title = 'Checkout';
                    String _subTitle =
                        'Anda harus melengkapi terlebih dahulu data - data yang diperlukan';
                    Widget _actions = Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          TextButton(
                              onPressed: () {
                                Navigator.pop(context);
                              },
                              child: Text('OK'))
                        ]);

                    CustomAlert.textComponent(context,
                        title: _title, subTitle: _subTitle, actions: _actions);
                  }
                }, color: CustomButton.themeColor, isPill: true),
              )
            ],
          ),
          (_isWalletOn)
              ? FutureBuilder(
                  future: CheckoutModel.getWalletConfiguration(),
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      dynamic walletConfiguration = snapshot.data;

                      if (walletConfiguration != false) {
                        int walletDiscount =
                            int.parse(walletConfiguration['walletDiscount']);
                        int minGrandTotalTransactionForWallet = int.parse(
                            walletConfiguration[
                                'minGrandTotalTransactionForWallet']);

                        return Padding(
                          padding: EdgeInsets.only(top: 15),
                          child: Text(
                              "Potongan langsung dari dompet sebesar " +
                                  Currency.formatRupiah(walletDiscount, 0) +
                                  " untuk setiap transaksi minimal " +
                                  Currency.formatRupiah(
                                      minGrandTotalTransactionForWallet, 0),
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                  fontStyle: FontStyle.italic,
                                  fontSize: SizeConfig.horizontalBlock * 3.25)),
                        );
                      } else {
                        return Padding(
                          padding: EdgeInsets.only(top: 15),
                          child: Text(
                              'Gagal mengambil data konfigurasi Dompet/Wallet'),
                        );
                      }
                    } else {
                      return Center(
                          child: SizedBox(
                              child:
                                  CircularProgressIndicator(strokeWidth: 1.5),
                              width: 15,
                              height: 15));
                    }
                  })
              : SizedBox()
        ],
      ),
    );
  }
}
