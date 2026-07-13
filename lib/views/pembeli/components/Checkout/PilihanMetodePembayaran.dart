import 'dart:math';

import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/Checkout/UangYangDipersiapkan.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/CreditCardData.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/NomorTelepon.dart';

import 'package:laukpauk/models/PembayaranModel.dart';
import 'package:laukpauk/models/PaymentChannelModel.dart';

class PilihanMetodePembayaran extends StatefulWidget {
  final int totalTagihan;
  final BuildContext context;
  final Map<String, dynamic> checkoutData;
  final void Function(Map<String, dynamic> returnedData) checkoutCallback;
  PilihanMetodePembayaran(this.context, this.checkoutData, this.totalTagihan,
      {required this.checkoutCallback});
  _PilihanMetodePembayaranState createState() =>
      _PilihanMetodePembayaranState();
}

class _PilihanMetodePembayaranState extends State<PilihanMetodePembayaran> {
  String? _selectedChannelCode, _moreData;
  Map<String, dynamic>? _metodePembayaranTerpilihMap;

  List<String> _eWallet = ['ovo', 'linkaja', 'dana'];
  List<String> _channelCodeNeedsMoreData = [
    'cod',
    'ovo',
    'linkaja',
    'dana',
    'kartu_kredit'
  ];

  bool _isButtonBuatOrderBusy = false;
  bool _switchCollectData = false;

  Widget _collectDataUI = SizedBox();

  Future<void> _onChannelCodeSelected(
      BuildContext widgetContext,
      BuildContext pilihanMetodePembayaranContext,
      String metodePembayaranSelected,
      Map<String, dynamic> detailMetodePembayaran) async {
    int _totalTagihan = widget.totalTagihan;

    String _selectedChannelCodeLC = metodePembayaranSelected.toLowerCase();
    bool _doesThisChannelCodeNeedsMoreData =
        _channelCodeNeedsMoreData.contains(_selectedChannelCodeLC);

    if (_doesThisChannelCodeNeedsMoreData) {
      if (_selectedChannelCodeLC == 'kartu_kredit') {
        Widget _ui = CreditCardData(
            channelCodeTerpilihLC: _selectedChannelCodeLC,
            checkoutCallback: widget.checkoutCallback,
            checkoutData: widget.checkoutData,
            metodePembayaranTerpilihMap: detailMetodePembayaran,
            totalTagihan: _totalTagihan,
            backToChannelPayment: () {
              setState(() {
                _switchCollectData = false;
                _collectDataUI = SizedBox();
              });
            });

        setState(() {
          _switchCollectData = true;
          _collectDataUI = _ui;
          _selectedChannelCode = _selectedChannelCodeLC;
          _metodePembayaranTerpilihMap = detailMetodePembayaran;
        });
      }
      if (_selectedChannelCodeLC == 'cod') {
        Widget _collectDataUILocalVariable = UangYangDipersiapkan(
            backToChannelPayment: () {
              setState(() {
                _switchCollectData = false;
                _collectDataUI = SizedBox();
              });
            },
            channelCodeTerpilihLC: _selectedChannelCodeLC,
            checkoutCallback: widget.checkoutCallback,
            checkoutData: widget.checkoutData,
            metodePembayaranTerpilihMap: detailMetodePembayaran,
            totalBelanja: _totalTagihan);

        setState(() {
          _switchCollectData = true;
          _collectDataUI = _collectDataUILocalVariable;
          _selectedChannelCode = _selectedChannelCodeLC;
          _metodePembayaranTerpilihMap = detailMetodePembayaran;
        });
      }
      if (_eWallet.contains(_selectedChannelCodeLC)) {
        Widget _collectDataUILocalVariable = NomorTelepon(
          backToChannelPayment: () {
            setState(() {
              _switchCollectData = false;
              _collectDataUI = SizedBox();
            });
          },
          channelCodeTerpilihLC: _selectedChannelCodeLC,
          checkoutCallback: widget.checkoutCallback,
          metodePembayaranTerpilihMap: detailMetodePembayaran,
          checkoutData: widget.checkoutData,
        );

        setState(() {
          _switchCollectData = true;
          _collectDataUI = _collectDataUILocalVariable;
          _selectedChannelCode = _selectedChannelCodeLC;
          _metodePembayaranTerpilihMap = detailMetodePembayaran;
        });
      }
    } else {
      bool _isTFBCA = (_selectedChannelCodeLC == 'tf_bca');

      int? _uniqueCode;
      if (_isTFBCA) {
        _uniqueCode = _random(1, 200);
      }

      setState(() {
        _moreData = (_uniqueCode != null) ? _uniqueCode.toString() : '';
        _selectedChannelCode = _selectedChannelCodeLC;
        _metodePembayaranTerpilihMap = detailMetodePembayaran;
      });
    }
  }

  int _random(int min, int max) {
    var _randomObject = new Random();
    return min + _randomObject.nextInt(max - min);
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    bool _dataOK = (_channelCodeNeedsMoreData.contains(_selectedChannelCode))
        ? (_selectedChannelCode != null && _moreData != null)
            ? _moreData!.length >= 1 && _metodePembayaranTerpilihMap != null
            : false
        : _selectedChannelCode != null;

    TextStyle _channelPaymentNameStyle = TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.50,
        fontWeight: FontWeight.w500);

    int _totalTagihan = widget.totalTagihan;

    return Padding(
      padding: MediaQuery.of(context).viewInsets,
      child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15, vertical: 20),
          child: (_switchCollectData)
              ? _collectDataUI
              : SingleChildScrollView(
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Row(children: [
                          Expanded(
                            child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('Metode Pembayaran',
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 4,
                                          fontWeight: FontWeight.w700)),
                                  Text(
                                      'Silahkan pilih metode pembayaran yang anda inginkan : ',
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3))
                                ]),
                          ),
                          (_dataOK)
                              ? CustomButton.button(context, 'Buat Orderan',
                                  () async {
                                  setState(() {
                                    _isButtonBuatOrderBusy = true;
                                  });

                                  String _channelCodeTerpilihLC =
                                      _selectedChannelCode!.toLowerCase();

                                  Map<String, dynamic> _checkoutData =
                                      widget.checkoutData;
                                  Map<String, dynamic> _returnedData =
                                      await PembayaranModel.buatOrder(
                                          metodePembayaranTerpilihMap:
                                              _metodePembayaranTerpilihMap!,
                                          checkoutData: _checkoutData,
                                          moreData: _moreData!);

                                  setState(() {
                                    _isButtonBuatOrderBusy = false;
                                  });

                                  await PembayaranModel.buatOrderNextStep(
                                      context: context,
                                      channelCodeTerpilihLC:
                                          _channelCodeTerpilihLC,
                                      returnedData: _returnedData,
                                      checkoutData: _checkoutData,
                                      metodePembayaranTerpilihMap:
                                          _metodePembayaranTerpilihMap!,
                                      moreData: _moreData!,
                                      checkoutCallback:
                                          widget.checkoutCallback);

                                  //sebelumnya ini gak ada, karena ini gak ada, buat bugs. Sehingga ADJD yang sblmnya sudah dicoppy akan
                                  // tetap dipakai kalau user menutup atau tidak menyelesaikan pertanayaan ADJD sEHINGGA jadwal
                                  //delivery yang sudah dibatasi akan tetap bisa terpakai walau sudah memenuhi limit.
                                  //OKI kita hapus coppiedADJD agar memaksa user tentukan ADJD kalau sudah checkout (checout berhasil, kalau gagal tidak akan terhapus coppoed adjd tadi)
                                },
                                  color: CustomColors.green,
                                  isPill: true,
                                  isBusy: _isButtonBuatOrderBusy)
                              : SizedBox()
                        ]),
                        SizedBox(height: 10),
                        FutureBuilder<List>(
                            future: PaymentChannelModel.getPaymentChannels(),
                            builder: (futureBuilderContext, snapshot) {
                              if (snapshot.hasData) {
                                if (snapshot.data != null) {
                                  List listMetodePembayaran = snapshot.data!;
                                  List<Widget> listMetodePembayaranWidget =
                                      listMetodePembayaran
                                          .map((metodePembayaran) {
                                    String _channelCode =
                                        metodePembayaran['channel_code'];
                                    String _channelCodeLC =
                                        _channelCode.toLowerCase();
                                    String _channelCodeUC =
                                        _channelCode.toUpperCase();

                                    String _minimalPembayaran =
                                        metodePembayaran['minimalPembayaran'];
                                    int _minimalPembayaranInt =
                                        int.parse(_minimalPembayaran);

                                    bool _minimalPembayaranTerpenuhi =
                                        (_totalTagihan >=
                                            _minimalPembayaranInt);

                                    return ListTile(
                                        enabled: _minimalPembayaranTerpenuhi,
                                        dense: true,
                                        onTap: () => _onChannelCodeSelected(
                                            context,
                                            futureBuilderContext,
                                            _channelCodeLC,
                                            metodePembayaran),
                                        hoverColor: CustomColors.aliceBlue,
                                        contentPadding: EdgeInsets.zero,
                                        leading: Radio(
                                            value: _channelCodeLC,
                                            groupValue: _selectedChannelCode,
                                            onChanged:
                                                (_minimalPembayaranTerpenuhi)
                                                    ? (selected) =>
                                                        _onChannelCodeSelected(
                                                            context,
                                                            futureBuilderContext,
                                                            selected.toString(),
                                                            metodePembayaran)
                                                    : null),
                                        title: Row(children: [
                                          Image.asset(
                                              ClientPath.assetsImg +
                                                  '/payment/' +
                                                  _channelCodeUC +
                                                  '.png',
                                              width: 60.0),
                                          SizedBox(width: 10.0),
                                          Text(metodePembayaran['name'],
                                              style: _channelPaymentNameStyle)
                                        ]),
                                        subtitle: (!_minimalPembayaranTerpenuhi)
                                            ? Container(
                                                margin:
                                                    EdgeInsets.only(top: 5.0),
                                                child: Text(
                                                    'Minimal pembayaran ' +
                                                        Currency.formatRupiah(
                                                            _minimalPembayaranInt,
                                                            0),
                                                    style: TextStyle(
                                                        color: CustomColors
                                                            .textDanger)),
                                              )
                                            : null);
                                  }).toList();
                                  return Column(
                                      children: listMetodePembayaranWidget);
                                }
                              }
                              return Padding(
                                padding: EdgeInsets.symmetric(vertical: 15),
                                child: Center(
                                    child: SizedBox(
                                        height: 25,
                                        width: 25,
                                        child: CircularProgressIndicator(
                                            strokeWidth: 1.5))),
                              );
                            })
                      ]),
                )),
    );
  }
}
