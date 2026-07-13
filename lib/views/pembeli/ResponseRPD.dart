import 'package:flutter/material.dart';
import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/views/penjual/TokoSaya.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class ResponseRPD extends StatelessWidget {
  final Map<String, dynamic> dataPenjual, dataRPD;
  ResponseRPD(this.dataPenjual, this.dataRPD);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String namaToko = dataPenjual['namaToko'];
    String statusRPD = dataRPD['status'];
    String statusRPDLC = statusRPD.toLowerCase();

    String imgURL = (statusRPDLC == 'pending')
        ? ClientPath.assetsImg + '/RPD_Pending.png'
        : ClientPath.assetsImg + '/RPD_Rejected.png';
    bool isApproved = (statusRPDLC == 'approved');

    String statusReason =
        (statusRPDLC == 'rejected') ? dataRPD['rejectionReason'] : '';
    String nomorRekeningRPD = dataRPD['nomorRekening'];
    int nominalRPD = int.parse(dataRPD['nominal']);
    String namaPenerimaRPD = dataRPD['namaPenerima'];

    return Scaffold(
        appBar: AppBar(
            title: Text('Response Pencairan Dana'),
            backgroundColor: CustomColors.green),
        body: Padding(
          padding: EdgeInsets.symmetric(vertical: 15, horizontal: 15),
          child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
            Image.asset(imgURL, width: SizeConfig.horizontalBlock * 30),
            SizedBox(height: 25),
            Text('Hai, Penjual ' + namaToko,
                style: TextStyle(
                    color: CustomColors.nearlyBlack,
                    fontSize: SizeConfig.horizontalBlock * 5,
                    fontWeight: FontWeight.w700)),
            SizedBox(height: 10),
            Text('Request Pencairan Dana',
                style: TextStyle(
                    color: CustomColors.textMuted,
                    fontSize: SizeConfig.horizontalBlock * 4.25,
                    fontWeight: FontWeight.w500),
                textAlign: TextAlign.center),
            Text(Currency.formatRupiah(nominalRPD, 0),
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 3.50,
                    color: CustomColors.green,
                    fontWeight: FontWeight.w500),
                textAlign: TextAlign.center),
            SizedBox(height: 7.5),
            (isApproved)
                ? Text(
                    'LaukPauk sudah melakukan penyetujuan terhadap proses pencairan dana anda, silahkan cek rekening anda.',
                    textAlign: TextAlign.center)
                : Text(
                    'Mohon maaf, permintaan anda untuk pencairan dana ditolak. Berikut alasan yang LaukPauk berikan :',
                    textAlign: TextAlign.center),
            (!isApproved)
                ? Container(
                    width: SizeConfig.horizontalBlock * 100,
                    margin: EdgeInsets.only(top: 10.0),
                    padding: EdgeInsets.symmetric(vertical: 10.0),
                    decoration: BoxDecoration(
                        color: Color(0x44dc3545),
                        borderRadius: BorderRadius.circular(10.0)),
                    child: Text(statusReason,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3,
                            color: CustomColors.textMuted)))
                : Padding(
                    padding: EdgeInsets.only(top: 7.5),
                    child: Column(children: [
                      Text(
                          'LaukPauk telah mentransfer dana sebesar ' +
                              Currency.formatRupiah(nominalRPD, 0) +
                              ' ke nomor rekening $nomorRekeningRPD AN Sdr. $namaPenerimaRPD',
                          textAlign: TextAlign.center),
                    ]),
                  ),
            SizedBox(height: 20.0),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                CustomButton.button(context, 'Halaman Awal', () {
                  Navigator.pushAndRemoveUntil(
                      context,
                      MaterialPageRoute(builder: (_) => SplashScreen()),
                      (route) => false);
                }, isPill: true, color: CustomButton.themeColor, outline: true),
                SizedBox(width: 7.5),
                CustomButton.button(context, 'Toko Saya', () {
                  Navigator.pushAndRemoveUntil(
                      context,
                      MaterialPageRoute(builder: (_) => TokoSaya()),
                      (route) => false);
                }, isPill: true, color: CustomButton.themeColor),
              ],
            )
          ]),
        ),
        backgroundColor: CustomColors.aliceBlue);
  }
}
