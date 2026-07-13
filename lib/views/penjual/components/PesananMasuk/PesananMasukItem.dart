import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/views/penjual/ProsesPesanan.dart';
import 'package:laukpauk/models/PesananSayaModel.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/views/pembeli/components/DetailTransaksi/StatusTransaksi.dart';

class PesananMasukItem extends StatefulWidget {
  final Map<String, dynamic> riwayatTransaksi;

  PesananMasukItem(this.riwayatTransaksi);
  _PesananMasukItemState createState() => _PesananMasukItemState();
}

class _PesananMasukItemState extends State<PesananMasukItem> {
  Future<dynamic> getContactUsWhatsappConfiguration() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Pengaturan/getContactUsWhatsappConfiguration?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      return dataObject['contactUsWhatsappConfiguration'];
    }
    return false;
  }

  Widget build(BuildContext context) {
    Map<String, dynamic> riwayat = widget.riwayatTransaksi;

    SizeConfig().initSize(context);

    String idTransaksi = riwayat['id'];

    String dayDelivery = riwayat['dayDelivery'];
    String status = riwayat['status'];
    String jadwalDelivery = riwayat['jadwalDelivery'];
    String waktuCheckoutFormatted = riwayat['waktuCheckout'];
    String namaMetodePembayaran = riwayat['namaMetodePembayaran'];
    String nomorTransaksi = riwayat['nomorTransaksi'];

    String jenisVoucher = riwayat['jenisVoucher'];
    int nilaiVoucher = int.parse(riwayat['nilaiVoucher']);

    int totalBelanja = int.parse(riwayat['totalBelanja']);
    int ongkir = int.parse(riwayat['ongkir']);

    if (jenisVoucher.toLowerCase() == 'belanja') {
      totalBelanja = totalBelanja - nilaiVoucher;
    }
    if (jenisVoucher.toLowerCase() == 'ongkir') {
      ongkir = ongkir - nilaiVoucher;
    }

    int potonganWallet = int.parse(riwayat['potonganWallet']);
    // int cashback  = int.parse(riwayat['cashback']);

    int totalBelanjaBersih = (totalBelanja + ongkir) - potonganWallet;
    String statusReadable = PesananSayaModel.readAbleStatusForUser(status);

    return Container(
        child: Card(
            elevation: 5,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            child: Container(
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  color: (status.toLowerCase() == 'rejected')
                      ? Color(0x25dc3545)
                      : CustomColors.nearlyWhite,
                  border: Border.all(
                      width: 1.5,
                      color: (status.toLowerCase() == 'rejected')
                          ? CustomColors.textMuted
                          : CustomColors.nearlyWhite)),
              padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                            child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                mainAxisAlignment: MainAxisAlignment.start,
                                children: [
                                  Text(nomorTransaksi,
                                      style: TextStyle(
                                          color: CustomColors.textMuted,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3,
                                          fontWeight: FontWeight.w600)),
                                  SizedBox(height: 2.5),
                                  Text(
                                      'Payment ' +
                                          namaMetodePembayaran.toUpperCase(),
                                      style: TextStyle(
                                          color: CustomColors.textMuted,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3,
                                          fontWeight: FontWeight.w600))
                                ]),
                          ),
                          Column(
                              crossAxisAlignment: CrossAxisAlignment.end,
                              children: [
                                Text(
                                    dayDelivery.toUpperCase() +
                                        ', ETA jam ' +
                                        jadwalDelivery,
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3,
                                        fontWeight: FontWeight.w600)),
                                SizedBox(height: 2.5),
                                Text(waktuCheckoutFormatted,
                                    style: TextStyle(
                                        color: CustomColors.textMuted,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 2.75,
                                        fontWeight: FontWeight.w600))
                              ])
                        ]),
                    SizedBox(height: 20),
                    Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text('Status Pesanan : ' + statusReadable,
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3.5,
                                  fontWeight: FontWeight.w600)),
                          Text(Currency.formatRupiah(totalBelanjaBersih, 0),
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 5,
                                  color: (status.toLowerCase() == 'rejected')
                                      ? CustomColors.textMuted
                                      : CustomColors.green,
                                  fontWeight: FontWeight.w800))
                        ]),
                    (status.toLowerCase() != 'rejected')
                        ? SizedBox(height: 5.0)
                        : SizedBox(),
                    (status.toLowerCase() != 'rejected')
                        ? Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                                StatusTransaksi(status,
                                    iconWidth: SizeConfig.horizontalBlock * 10,
                                    iconSize: SizeConfig.horizontalBlock * 4.5,
                                    showIconLabel: true),
                                SizedBox(width: 10.0),
                                (status.toLowerCase() == 'processing')
                                    ? Expanded(
                                        child: CustomButton.button(
                                            context, 'Proses', () async {
                                          Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                  builder: (_) => ProsesPesanan(
                                                      idTransaksi)));
                                        },
                                            size: CustomButton.smSize,
                                            color: CustomButton.themeColor,
                                            isPill: true),
                                      )
                                    : SizedBox()
                              ])
                        : SizedBox(),
                  ]),
            )));
  }
}
