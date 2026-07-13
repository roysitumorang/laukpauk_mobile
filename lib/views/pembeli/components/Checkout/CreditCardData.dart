import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:laukpauk/global-components/CustomForm.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Environment.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/PembayaranModel.dart';

class CreditCardData extends StatefulWidget {
  final int totalTagihan;
  final Map<String, dynamic> metodePembayaranTerpilihMap, checkoutData;
  final String channelCodeTerpilihLC;
  final void Function(Map<String, dynamic> data) checkoutCallback;
  final void Function() backToChannelPayment;
  CreditCardData(
      {required this.channelCodeTerpilihLC,
      required this.totalTagihan,
      required this.metodePembayaranTerpilihMap,
      required this.checkoutData,
      required this.checkoutCallback,
      required this.backToChannelPayment});
  _CreditCardDataState createState() => _CreditCardDataState();
}

class _CreditCardDataState extends State<CreditCardData> {
  TextEditingController _nomorAkunController = TextEditingController();
  TextEditingController _bulanExpiredController = TextEditingController();
  TextEditingController _tahunExpiredController = TextEditingController();
  TextEditingController _cvnController = TextEditingController();

  void initState() {
    super.initState();
    if (Environment.environment == 'development') {
      _nomorAkunController.text = '4000000000000002';
      _bulanExpiredController.text = '03';
      _tahunExpiredController.text = '26';
      _cvnController.text = '266';
    }
  }

  void dispose() {
    super.dispose();

    _nomorAkunController.dispose();
    _bulanExpiredController.dispose();
    _tahunExpiredController.dispose();
    _cvnController.dispose();
  }

  bool _isProcessButtonBusy = false;

  bool _isProcessingPayment = false;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double _horizontalPadding = SizeConfig.horizontalBlock * 3.5;
    double _formTopPadding = SizeConfig.horizontalBlock * 5.0;

    String _accountNumber = _nomorAkunController.text;
    String _expiryMonth = _bulanExpiredController.text;
    String _expiryYear = _tahunExpiredController.text;
    String _cardCVN = _cvnController.text;

    bool _formOkay = (_accountNumber.length >= 16) &&
        (_expiryMonth.length >= 2) &&
        (_expiryYear.length >= 2) &&
        (_cardCVN.length >= 3);

    int _totalTagihan = widget.totalTagihan;

    return SingleChildScrollView(
      child: Stack(
        children: [
          Center(
            child: Padding(
              padding: EdgeInsets.symmetric(horizontal: _horizontalPadding),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    SizedBox(height: 10),
                    Text('Kartu Kredit',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.5,
                            fontWeight: FontWeight.w600)),
                    SizedBox(height: 10),
                    Text(
                        'Silahkan input data kartu kredit yang anda gunakan untuk pembayaran',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25)),
                    SizedBox(height: _formTopPadding),
                    _inputLabel('Nomor Kartu Kredit'),
                    _inputForm('5100XXXXXXXXXXXX', _nomorAkunController,
                        maxLength: 16),
                    Image.asset(
                        ClientPath.assetsImg + '/payment/jcb-mastercard.png',
                        width: 175.0),
                    SizedBox(height: _formTopPadding),
                    _inputLabel('Masa Berlaku'),
                    Row(children: [
                      Expanded(
                          flex: 2,
                          child: _inputForm('Bulan', _bulanExpiredController,
                              maxLength: 2)),
                      Padding(
                          padding: EdgeInsets.only(
                              left: SizeConfig.horizontalBlock * 2.5,
                              right: SizeConfig.horizontalBlock * 2.5),
                          child: Text('/', style: TextStyle(fontSize: 30.0))),
                      Expanded(
                        flex: 2,
                        child: _inputForm('Tahun', _tahunExpiredController,
                            maxLength: 2),
                      ),
                      Expanded(child: SizedBox(), flex: 5)
                    ]),
                    SizedBox(height: _formTopPadding),
                    _inputLabel('CVV/CCV'),
                    Row(
                      children: [
                        Expanded(
                            flex: 1,
                            child: _inputForm(
                                '3 Digit Angka Rahasia Kartu Kredit',
                                _cvnController,
                                maxLength: 3)),
                        SizedBox(width: 7.5),
                        Image.asset(ClientPath.assetsImg + '/payment/CVV.png',
                            width: 50.0),
                        Expanded(child: SizedBox(), flex: 4),
                      ],
                    ),
                    SizedBox(height: _formTopPadding),
                    (_formOkay)
                        ? CustomButton.button(
                            context, 'Verifikasi Kartu Kredit', () async {
                            _expiryYear = '20' + _expiryYear;

                            setState(() {
                              _isProcessButtonBusy = true;
                            });

                            MethodChannel _creditCartChannel =
                                MethodChannel('laukpauk.xendit');

                            String _totalTagihanString =
                                _totalTagihan.toString();

                            String _dataTransaksi =
                                '{"totalTagihan":$_totalTagihanString, "cardNumber":"$_accountNumber", "expiryMonth":"$_expiryMonth", "expiryYear":"$_expiryYear", "cardCVN":"$_cardCVN"}';

                            String? _encodedTokenIDAndAuthenticationID =
                                await _creditCartChannel.invokeMethod(
                                    'createCreditCartToken', _dataTransaksi);

                            setState(() {
                              _isProcessButtonBusy = false;
                            });

                            if (_encodedTokenIDAndAuthenticationID != null &&
                                _encodedTokenIDAndAuthenticationID
                                        .toLowerCase() !=
                                    'null') {
                              Map<String, dynamic>
                                  _decodedTokenIDAndAuthenticationID =
                                  jsonDecode(
                                      _encodedTokenIDAndAuthenticationID);

                              String _creditCardDataString = jsonEncode(
                                  _decodedTokenIDAndAuthenticationID);

                              Map<String, dynamic>
                                  _metodePembayaranTerpilihMap =
                                  widget.metodePembayaranTerpilihMap;
                              Map<String, dynamic> _checkoutData =
                                  widget.checkoutData;

                              setState(() {
                                _isProcessingPayment = true;
                              });

                              Map<String, dynamic> _returnedData =
                                  await PembayaranModel.buatOrder(
                                      metodePembayaranTerpilihMap:
                                          _metodePembayaranTerpilihMap,
                                      checkoutData: _checkoutData,
                                      moreData: _creditCardDataString);

                              setState(() {
                                _isProcessingPayment = false;
                              });

                              await PembayaranModel.buatOrderNextStep(
                                  context: context,
                                  channelCodeTerpilihLC:
                                      widget.channelCodeTerpilihLC,
                                  returnedData: _returnedData,
                                  checkoutData: _checkoutData,
                                  metodePembayaranTerpilihMap:
                                      _metodePembayaranTerpilihMap,
                                  moreData: _creditCardDataString,
                                  checkoutCallback: widget.checkoutCallback);
                            } else {
                              print('gagal membuat token!');
                            }
                          },
                            isBlock: true,
                            color: CustomButton.themeColor,
                            isPill: true,
                            isBusy: _isProcessButtonBusy)
                        : SizedBox(),
                  ]),
            ),
          ),
          (_isProcessingPayment)
              ? Container(
                  padding: EdgeInsets.symmetric(
                      horizontal: SizeConfig.horizontalBlock * 15.0),
                  decoration: BoxDecoration(color: Color(0x9FFFFFFF)),
                  child: Center(
                      child: Column(mainAxisSize: MainAxisSize.min, children: [
                    Image.asset(ClientPath.assetsImg + '/loading.png',
                        width: SizeConfig.horizontalBlock * 50.0),
                    SizedBox(height: SizeConfig.horizontalBlock * 2.5),
                    Text('Sedang Memproses',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 4.0,
                            fontWeight: FontWeight.bold)),
                    SizedBox(height: 5.0),
                    Text('Tunggu sebentar ya, pembayaranmu sedang diproses',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25,
                            color: CustomColors.textMuted)),
                    SizedBox(height: 35.0),
                    SizedBox(
                        height: SizeConfig.horizontalBlock * 7.5,
                        width: SizeConfig.horizontalBlock * 7.5,
                        child: CircularProgressIndicator(
                            strokeWidth: 2.5,
                            backgroundColor: CustomColors.green,
                            valueColor: AlwaysStoppedAnimation(
                                CustomColors.nearlyWhite)))
                  ])))
              : SizedBox()
        ],
      ),
    );
  }

  Widget _inputLabel(String label) {
    return CustomForm.label(context, label: label);
  }

  Widget _inputForm(String hintText, TextEditingController controller,
      {required int maxLength}) {
    return CustomForm.textField(context,
        hintText: hintText,
        controller: controller,
        borderColor: CustomColors.green,
        borderWidth: 1.0);
  }
}
