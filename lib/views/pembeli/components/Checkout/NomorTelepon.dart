import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/PembayaranModel.dart';

class NomorTelepon extends StatefulWidget {
  final Map<String, dynamic> metodePembayaranTerpilihMap;
  final Map<String, dynamic> checkoutData;
  final String channelCodeTerpilihLC;
  final void Function(Map<String, dynamic> data) checkoutCallback;
  final void Function() backToChannelPayment;

  NomorTelepon(
      {required this.metodePembayaranTerpilihMap,
      required this.checkoutData,
      required this.channelCodeTerpilihLC,
      required this.checkoutCallback,
      required this.backToChannelPayment});
  _NomorTeleponState createState() => _NomorTeleponState();
}

class _NomorTeleponState extends State<NomorTelepon> {
  String? _nomorTelepon;

  bool _isButtonBuatOrderBusy = false;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double _horizontalPadding = SizeConfig.horizontalBlock * 3.5;
    String _paymentName = widget.metodePembayaranTerpilihMap['name'];
    _paymentName = _paymentName.toUpperCase();

    return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(height: 10),
          Row(
            children: [
              GestureDetector(
                  onTap: () => widget.backToChannelPayment(),
                  child: Icon(Icons.arrow_back_outlined)),
              SizedBox(width: 15.0),
              Text(_paymentName,
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 3.5,
                      fontWeight: FontWeight.w600)),
            ],
          ),
          SizedBox(height: 10),
          Container(
            decoration: BoxDecoration(
                color: Color(0x3366CC99),
                borderRadius: BorderRadius.circular(10)),
            child: Row(children: [
              Container(
                  padding: EdgeInsets.symmetric(horizontal: _horizontalPadding),
                  child: Text('Nomor Telepon', textAlign: TextAlign.center)),
              Expanded(
                child: TextField(
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 3.5,
                      color: CustomColors.green),
                  keyboardType: TextInputType.number,
                  onChanged: (value) {
                    if (value.length >= 1) {
                      setState(() {
                        _nomorTelepon = value;
                      });
                    } else {
                      setState(() {
                        _nomorTelepon = null;
                      });
                    }
                  },
                  decoration: InputDecoration(
                      border: InputBorder.none,
                      hintText: 'Nomor Yang Terhubung ke EWallet',
                      hintStyle: TextStyle(color: CustomColors.green),
                      isDense: true,
                      contentPadding: EdgeInsets.symmetric(
                          horizontal: 15,
                          vertical: SizeConfig.horizontalBlock * 5)),
                ),
              )
            ]),
          ),
          SizedBox(height: 10),
          Text(
              'Pastikan anda mengikuti format berikut ini 08XXXXXXXXXX tanpa menggunakan kode negara +62',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          SizedBox(height: 15),
          (_nomorTelepon != null)
              ? (_nomorTelepon!.length >= 10)
                  ? CustomButton.button(context, 'Buat Orderan', () async {
                      setState(() {
                        _isButtonBuatOrderBusy = true;
                      });

                      // await Future.delayed(Duration(seconds: 3));
                      String _moreData = _nomorTelepon!;

                      Map<String, dynamic> _metodePembayaranTerpilihMap =
                          widget.metodePembayaranTerpilihMap;
                      Map<String, dynamic> _checkoutData = widget.checkoutData;

                      Map<String, dynamic> _returnedData =
                          await PembayaranModel.buatOrder(
                              metodePembayaranTerpilihMap:
                                  _metodePembayaranTerpilihMap,
                              checkoutData: _checkoutData,
                              moreData: _moreData);

                      setState(() {
                        _isButtonBuatOrderBusy = false;
                      });

                      await PembayaranModel.buatOrderNextStep(
                          context: context,
                          channelCodeTerpilihLC: widget.channelCodeTerpilihLC,
                          returnedData: _returnedData,
                          checkoutData: _checkoutData,
                          metodePembayaranTerpilihMap:
                              _metodePembayaranTerpilihMap,
                          moreData: _moreData,
                          checkoutCallback: widget.checkoutCallback);
                    },
                      isBlock: true,
                      color: CustomButton.themeColor,
                      isPill: true,
                      isBusy: _isButtonBuatOrderBusy)
                  : SizedBox()
              : SizedBox(),
          SizedBox(height: 10)
        ]);
  }
}
