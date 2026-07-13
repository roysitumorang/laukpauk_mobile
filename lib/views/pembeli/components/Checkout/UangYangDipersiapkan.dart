import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/PembayaranModel.dart';

class UangYangDipersiapkan extends StatefulWidget {
  final Map<String, dynamic> metodePembayaranTerpilihMap, checkoutData;
  final int totalBelanja;
  final String channelCodeTerpilihLC;
  final void Function(Map<String, dynamic> data) checkoutCallback;
  final void Function() backToChannelPayment;

  UangYangDipersiapkan(
      {required this.totalBelanja,
      required this.metodePembayaranTerpilihMap,
      required this.checkoutData,
      required this.channelCodeTerpilihLC,
      required this.checkoutCallback,
      required this.backToChannelPayment});
  _UangYangDipersiapkanState createState() => _UangYangDipersiapkanState();
}

class _UangYangDipersiapkanState extends State<UangYangDipersiapkan> {
  int? _uang;

  bool _isButtonBuatOrderBusy = false;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    double _horizontalPadding = SizeConfig.horizontalBlock * 3.5;

    Map<String, dynamic> _metodePembayaranTerpilihMap =
        widget.metodePembayaranTerpilihMap;
    Map<String, dynamic> _checkoutData = widget.checkoutData;
    int _totalBelanja = widget.totalBelanja;

    String _formattedTotalBelanja = Currency.formatRupiah(_totalBelanja, 0);

    return SingleChildScrollView(
      child: Column(
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
                Text('Bayar Tunai',
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
                    padding:
                        EdgeInsets.symmetric(horizontal: _horizontalPadding),
                    child: Text('Rp.', textAlign: TextAlign.center)),
                Expanded(
                  child: TextField(
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        color: CustomColors.green),
                    keyboardType: TextInputType.number,
                    onChanged: (value) {
                      if (value.length >= 1) {
                        int _intValue = int.parse(value);

                        setState(() {
                          _uang = _intValue;
                        });
                      } else {
                        setState(() {
                          _uang = null;
                        });
                      }
                    },
                    decoration: InputDecoration(
                        border: InputBorder.none,
                        hintText: 'Uang yang dipersiapkan untuk membayar',
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
                'Masukkan uang yang dipersiapkan untuk membayar agar memudahkan kurir mempersiapkan kembalian.',
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
            SizedBox(height: 10),
            Text(
                'Note : Uang yang dipersiapkan tidak boleh kurang dari ' +
                    _formattedTotalBelanja,
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 2.75)),
            SizedBox(height: 15),
            (_uang != null)
                ? (_uang.toString().length >= 1 && _uang! >= _totalBelanja)
                    ? CustomButton.button(context, 'Buat Orderan', () async {
                        setState(() {
                          _isButtonBuatOrderBusy = true;
                        });

                        String _moreData = _uang.toString();

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
                            metodePembayaranTerpilihMap:
                                _metodePembayaranTerpilihMap,
                            moreData: _moreData,
                            checkoutData: _checkoutData,
                            context: context,
                            returnedData: _returnedData,
                            channelCodeTerpilihLC: widget.channelCodeTerpilihLC,
                            checkoutCallback: widget.checkoutCallback);
                      },
                        isBlock: true,
                        color: CustomButton.themeColor,
                        isPill: true,
                        isBusy: _isButtonBuatOrderBusy)
                    : SizedBox()
                : SizedBox(),
            SizedBox(height: 10)
          ]),
    );
  }
}
