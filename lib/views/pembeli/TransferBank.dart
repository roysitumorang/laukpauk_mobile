import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:laukpauk/models/TransaksiModel.dart';

import 'package:laukpauk/views/pembeli/DetailTransaksi.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/SplashScreen.dart';

class TransferBank extends StatefulWidget {
  final Map<String, dynamic> returnedDataFromCheckout;
  TransferBank(this.returnedDataFromCheckout);

  _TransferBankState createState() => _TransferBankState();
}

class _TransferBankState extends State<TransferBank> {
  void initState() {
    super.initState();
    _initialization();
  }

  Future<void> _initialization() async {
    await TransaksiModel.removeTransactionSession(context);
  }

  Widget build(BuildContext context) {
    Map<String, dynamic> responseCheckout =
        widget.returnedDataFromCheckout['responseCheckout'];
    Map<String, dynamic> paymentResponse = responseCheckout['paymentResponse'];

    int totalBayar = paymentResponse['totalBayar'];
    int _kodeUnik = paymentResponse['kodeUnik'];
    String besarDana = Currency.formatRupiah(totalBayar + _kodeUnik, 0);
    String nomorRekening = paymentResponse['nomorRekening'];
    String pemilikNomorRekening = paymentResponse['pemilikNomorRekening'];

    SizeConfig().initSize(context);

    TextStyle titleSize = TextStyle(
        fontSize: SizeConfig.horizontalBlock * 4.25,
        fontWeight: FontWeight.w700);
    TextStyle subTitleSize =
        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25);

    TextStyle _xsTextStyle = TextStyle(
        color: CustomColors.textMuted,
        fontSize: SizeConfig.horizontalBlock * 3);

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: Center(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 15, vertical: 25),
            child:
                Column(mainAxisAlignment: MainAxisAlignment.center, children: [
              Image.asset(ClientPath.assetsImg + '/bca.png',
                  width: SizeConfig.horizontalBlock * 35),
              SizedBox(height: 20.0),
              Text('Orderan Berhasil Dibuat', style: titleSize),
              Padding(
                padding: EdgeInsets.only(
                    top: 5,
                    left: SizeConfig.horizontalBlock * 7.5,
                    right: SizeConfig.horizontalBlock * 7.5),
                child: Column(
                  children: [
                    Text(
                        'Silahkan lakukan transfer ke nomor rekening di bawah ini :',
                        style: subTitleSize,
                        textAlign: TextAlign.center),
                    SizedBox(height: 15.0),
                    Builder(
                        builder: (context) => InkWell(
                            onTap: () {
                              Clipboard.setData(
                                  ClipboardData(text: nomorRekening));
                              SnackBar snackBar = SnackBar(
                                  duration: Duration(seconds: 3),
                                  content: Text('Tersalin !',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.25)));
                              ScaffoldMessenger.of(context)
                                  .showSnackBar(snackBar);
                            },
                            child: Text(nomorRekening, style: titleSize))),
                    SizedBox(height: 2.5),
                    Text('A.N. ' + pemilikNomorRekening, style: subTitleSize),
                    SizedBox(height: 15.0),
                    Text(besarDana,
                        style: TextStyle(
                            color: CustomColors.green,
                            fontSize: SizeConfig.horizontalBlock * 5,
                            fontWeight: FontWeight.w700)),
                    SizedBox(height: 2.5),
                    Text('Klik nomor rekening untuk salin',
                        style: _xsTextStyle),
                    Container(
                      margin: EdgeInsets.symmetric(vertical: 15.0),
                      padding: EdgeInsets.all(10.0),
                      decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(10.0),
                          color: CustomColors.textWarning),
                      child: Text(
                          'Silahkan transfer sesuai dengan nominal.\nJika tidak sesuai, transaksi Anda tidak akan ter-proses',
                          textAlign: TextAlign.center,
                          style: _xsTextStyle.copyWith(
                              color: CustomColors.nearlyBlack)),
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        CustomButton.button(context, 'Belanja Lagi', () {
                          Navigator.pushAndRemoveUntil(
                              context,
                              MaterialPageRoute(builder: (_) => SplashScreen()),
                              (route) => false);
                        }, color: CustomButton.themeColor, isPill: true),
                        SizedBox(width: 7.5),
                        CustomButton.button(context, 'Detail Transaksi', () {
                          String idTransaksi = widget
                              .returnedDataFromCheckout['responseCheckout']
                                  ['idTransaksi']
                              .toString();

                          Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (_) =>
                                      DetailTransaksi(idTransaksi)));
                        },
                            color: CustomButton.themeColor,
                            isPill: true,
                            outline: true)
                      ],
                    )
                  ],
                ),
              )
            ]),
          ),
        ));
  }
}
