import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/pembeli/components/DetailTransaksi/StatusTransaksi.dart';
import 'package:laukpauk/views/pembeli/DetailTransaksi.dart';

import 'package:laukpauk/models/PesananSayaModel.dart';

import 'package:laukpauk/models/SettingModel.dart';

import 'package:laukpauk/views/pembeli/RepeatOrder.dart';

class PesananSayaItem extends StatefulWidget {
  final Map<String, dynamic> riwayatTransaksi;
  final Function(bool returnedData) pesananDiterimaCallback;

  PesananSayaItem(this.riwayatTransaksi,
      {required this.pesananDiterimaCallback});
  _PesananSayaItemState createState() => _PesananSayaItemState();
}

class _PesananSayaItemState extends State<PesananSayaItem> {
  Widget build(BuildContext context) {
    Map<String, dynamic> riwayat = widget.riwayatTransaksi;

    SizeConfig().initSize(context);

    String idTransaksi = riwayat['id'];

    String dayDelivery = riwayat['dayDelivery'];
    String status = riwayat['status'];
    String jadwalDelivery = riwayat['jadwalDelivery'];
    String waktuCheckoutFormatted = riwayat['waktuCheckout'];
    String namaMetodePembayaran = riwayat['namaMetodePembayaran'];
    String metodePembayaran = riwayat['metodePembayaran'];
    String namaMetodePembayaranLC = namaMetodePembayaran.toLowerCase();
    String nomorTransaksi = riwayat['nomorTransaksi'];

    int totalBelanja = int.parse(riwayat['totalBelanja']);
    int ongkir = int.parse(riwayat['ongkir']);

    int potonganWallet = int.parse(riwayat['potonganWallet']);

    int _nilaiVoucher = int.parse(riwayat['nilaiVoucher']);
    int _nilaiPointOut = int.parse(riwayat['pointOut']);
    int _nilaiDiskonKategori = int.parse(riwayat['diskonKategori']);
    int _diskonKodePromo  = int.parse(riwayat['diskonPromo']);

    int totalBelanjaBersih = (totalBelanja + ongkir) -
        (_nilaiVoucher +
            potonganWallet +
            _nilaiPointOut +
            _nilaiDiskonKategori + _diskonKodePromo);

    // bool _isBCA = false;
    if (metodePembayaran.toLowerCase() == 'tf_bca') {
      int _kodeUnik = int.parse(riwayat['moreDataMetodePembayaran']);
      totalBelanjaBersih += _kodeUnik;
      // _isBCA = true;
    }

    String statusReadable = PesananSayaModel.readAbleStatusForUser(status);

    return InkWell(
      onTap: () {
        Navigator.push(context,
            MaterialPageRoute(builder: (_) => DetailTransaksi(idTransaksi)));
      },
      child: Container(
          child: Card(
              elevation: 5,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
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
                                    Text('No. Invoice',
                                        style: TextStyle(
                                            color: CustomColors.textMuted,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    2.5)),
                                    SizedBox(height: 2.5),
                                    Text(nomorTransaksi,
                                        style: TextStyle(
                                            color: CustomColors.textMuted,
                                            fontSize:
                                                SizeConfig.horizontalBlock * 3,
                                            fontWeight: FontWeight.w600)),
                                    SizedBox(height: 5.0),
                                    Text(namaMetodePembayaran.toUpperCase(),
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
                            Expanded(
                              child: Text('Status Pesanan : ' + statusReadable,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.0,
                                      fontWeight: FontWeight.w600)),
                            ),
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
                                      iconWidth:
                                          SizeConfig.horizontalBlock * 10,
                                      iconSize:
                                          SizeConfig.horizontalBlock * 4.5,
                                      showIconLabel: true),
                                  SizedBox(width: 10.0),
                                  (status.toLowerCase() == 'received')
                                      ? Expanded(
                                          child: CustomButton.button(
                                              context, 'Pesan Lagi', () async {
                                            Navigator.push(
                                                context,
                                                MaterialPageRoute(
                                                    builder: (_) => RepeatOrder(
                                                        idTransaksi)));
                                          },
                                              size: CustomButton.smSize,
                                              color: CustomButton.themeColor,
                                              isPill: true),
                                        )
                                      : SizedBox(),
                                  (status.toLowerCase() == 'otw')
                                      ? Expanded(
                                          child: CustomButton.button(
                                              context, 'Diterima', () async {
                                            String _title = 'Pesanan Diterima';
                                            String _subTitle =
                                                'Lakukan konfirmasi jika pesanan sudah diterima ya mak ...';
                                            Widget _actions = Row(
                                                mainAxisAlignment:
                                                    MainAxisAlignment.center,
                                                children: [
                                                  TextButton(
                                                      onPressed: () =>
                                                          Navigator.pop(
                                                              context, false),
                                                      child: Text(
                                                          'Belum Diterima')),
                                                  TextButton(
                                                      onPressed: () =>
                                                          Navigator.pop(
                                                              context, true),
                                                      child: Text('OK')),
                                                ]);

                                            bool? konfirmasi =
                                                await CustomAlert.textComponent(
                                                    context,
                                                    title: _title,
                                                    subTitle: _subTitle,
                                                    actions: _actions);
                                            if (konfirmasi != null &&
                                                konfirmasi == true) {
                                              bool statusPesananDiterima =
                                                  await PesananSayaModel
                                                      .pesananDiterima(
                                                          idTransaksi);
                                              widget.pesananDiterimaCallback(
                                                  statusPesananDiterima);
                                            }
                                          },
                                              size: CustomButton.smSize,
                                              color: CustomButton.themeColor,
                                              isPill: true),
                                        )
                                      : SizedBox(),
                                  (status.toLowerCase() == 'pending' &&
                                          namaMetodePembayaranLC == 'tf_bca')
                                      ? Expanded(
                                          child: CustomButton.button(
                                              context, 'Konfirmasi Bayar',
                                              () async {
                                            dynamic contactUsWhatsapp =
                                                await SettingModel
                                                    .getContactUsWhatsappConfiguration();
                                            if (contactUsWhatsapp != false) {
                                              String text =
                                                  'Hai LaukPauk, saya ingin konfirmasi bahwa transaksi saya $nomorTransaksi sudah dibayar melalui $namaMetodePembayaran. Mohon untuk segera diproses. Terimakasih :)';
                                              String config = contactUsWhatsapp[
                                                  'contactUsWhatsapp'];
                                              Uri url = Uri.parse(
                                                  'https://wa.me/' +
                                                      config +
                                                      '?text=$text');
                                              if (await canLaunchUrl(url)) {
                                                await launchUrl(url);
                                              }
                                            } else {
                                              print('url gak valid');
                                            }
                                          },
                                              size: CustomButton.smSize,
                                              color: CustomButton.infoColor,
                                              isPill: true),
                                        )
                                      : SizedBox()
                                ])
                          : SizedBox(),
                    ]),
              ))),
    );
  }
}
